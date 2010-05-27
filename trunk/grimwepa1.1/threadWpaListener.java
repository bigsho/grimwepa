/** constantly checks 'wpa-01.cap' for a handshake using aircrack-ng
*/
import java.io.IOException;

public class threadWpaListener implements Runnable {
	/** wpa listener thread*/
	Thread t;
	/** flag, tells us when to stop listening*/
	public static boolean flag;
	/** tells other classes if/when we have the handshake*/
	public static boolean handshake;
	
	/** creates new thread, intializes instance variables
	*/
	public threadWpaListener() {
		t = new Thread(this, "threadWpaListener");
		flag = false;
		handshake = false;
	}
	
	/** every [Methods.txtWpaTimeout.getText()] seconds :
		sends 3 deauthentication packets to the selected target (AP);
		also, specifies selected client as target (if selected);
		runs waitForIt() method which checks for handshake while waiting;
		loops until handshake is found or flag = false
	*/
	public void run() {
		String driver, client, command;
		
		// get wifi card (found in test2.java)
		driver = (String)Gui.cboDrivers.getSelectedItem();
		
		// get clients (if any)
		client = "";
		if (Gui.chkWpaClients.isSelected()) {
			client = (String)Gui.cboWpaClients.getSelectedItem();
			if (client.equals("[no clients found]"))
				client = "";
			else
				client = " -c " + client;
		}
		
		do {
			Methods.stat("sending 3 deauthentication requests to access point...");
			
			command = 	"xterm" +
						" -fg " + (String)Gui.cboColors.getSelectedItem() + 
						" -bg black" +
						" -bd " + (String)Gui.cboColors.getSelectedItem() + 
						" -T gw-deauth" +
						" -geom 100x15+0+0" +
						" -iconic -e" + 
						
						" aireplay-ng -0 3 -a " + Methods.currentBSSID + 
						"" + client + 
						" " + driver;
			
			Methods.readExec(command);
			
			// Methods.stat("sent 3 deauths; waiting...");
			
			waitForIt();
			
		} while (!flag);
		Methods.stat("cleaning up files...");
		
		Methods.proAttack.destroy();
		
		Methods.removeFile("badpw.txt");
		
		Gui.btnWpaDeauth.setLabel("start handshake capture");
		Gui.setEnable(true);
		
		if (handshake) {
			// yay! we captured a handshake
			Methods.stat("handshake was captured; saved as wpa-01.cap");
			
			Gui.btnWpaCrack.setEnabled(true);
			Gui.cboWpaCrackMethod.setEnabled(true);
			Gui.chkWpaSignon.setEnabled(true);
			// file is: wpa-01.cap
			
			// strip everything except the handshake from the capture file
			// pyrit and other programs get buggy if the capture file is too big!
			Methods.stat("extracting handshake using pyrit/tshark..");
			stripHandshake(Methods.grimwepaPath, "wpa-01.cap");
			Methods.stat("handshake extracted. saved as wpa-01.cap");
			
			// copy the current ssid, only copy the letters and digits (no non-printable chars)
			String tempSSID = Methods.currentSSID.trim(), result = "";
			for (int i = 0; i < tempSSID.length(); i++) {
				if (Character.isLetterOrDigit(tempSSID.charAt(i)) == true)
					result += tempSSID.charAt(i);
			}
			
			// if result of the ssid isn't blank [meaning there's actual letters/numbers]
			if (!result.equals("")) {
				Methods.stat("backing-up handshake file to 'hs' directory...");
				
				Process proCopy = null;
				
				// create hs folder where we want to store handshakes
				Methods.readExec("mkdir !PATH!hs");
				
				// then copy the cap file to a file containing the same name as the ssid
				Methods.readExec("cp !PATH!wpa-01.cap !PATH!hs/" + result + ".cap");
				
				Methods.stat("handshake backed-up: '" + Methods.grimwepaPath + "hs/" + result + ".cap'");
				
				Methods.removeFile("wpa-01.csv");
				Methods.removeFile("wpa-01.kismet.csv");
				Methods.removeFile("wpa-01.kismet.netxml");
			}
			
		} else {
			// no handshake was captured, user hit stop
			Methods.stat("inactive");
			
			Gui.btnWpaCrack.setEnabled(false);
			Gui.cboWpaCrackMethod.setEnabled(false);
			Gui.chkWpaSignon.setEnabled(false);
			
			// delete it! (aka cap file)
			Methods.removeFile("wpa-01.cap");
			Methods.removeFile("wpa-01.csv");
			Methods.removeFile("wpa-01.kismet.csv");
			Methods.removeFile("wpa-01.kismet.netxml");
			
		}
	}
	
	/** pauses for given amount of time (txtWpaTimeout.getText()); 
		checks for handshake and updates status bar every second fo the way
	*/
	public static void waitForIt() {
		int waitime;
		
		try {
			waitime = Integer.parseInt(Gui.txtWpaTimeout.getText());
		} catch (NumberFormatException e) {
			waitime = 5;
			Gui.txtWpaTimeout.setText("5");
		}
		
		if (waitime <= 0)
			waitime = 1;
		try { 
			for (int i = 0; i < waitime; i++) {
				Methods.stat("sent 3 deauths; waiting for " + (waitime - i) + " seconds...");
				
				// check for handshake
				if (checkForHandshake()) {
					// valid handshake
					flag = true;
					handshake = true;
					break;
				}
				
				if (flag)
					break;
				
				Thread.currentThread().sleep((int)(1000));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** returns true if the file in grimwepaPath/wpa-01.cap contains a handshake, false otherwise
		uses aircrack-ng to check for handshake:
		- creates a fake password list file (badpw.txt) with a single password in it
		- runs aircrack
		- if aircrack says "passphrase not in dictionary", then we have a handshake
		- otherwise, aircrack has no handshake to use
		@return if the file wpa-01.cap contains a handshake
	*/
	public static boolean checkForHandshake() {
		if (!Methods.fileExists(Methods.grimwepaPath + "badpw.txt"))
			Methods.writeFile(Methods.grimwepaPath + "badpw.txt", "purposelythewrongpassword");
		
		String output[] = Methods.readExec(
			"aircrack-ng" +
			" -a 2" +
			" -w !PATH!badpw.txt" +
			" !PATH!wpa-01.cap");
		
		for (int j = 0; j < output.length; j++) {
			if (output[j].indexOf("Passphrase not in dictionary") >= 0) {
				// valid handshake
				return true;
			}
		}
		return false;
	}
	
	/** strips handshake from cap file using pyrit;<p>
		uses pyrit to remove everything in the cap file except the handshake
		stores to new file, deletes old file, replaces old file;
		if user does not have pyrit, attempts stripping using tshark;
		@param path path to the .cap file, needed for temporary file purposes
		@param filename name of .cap file
	*/
	public static void stripHandshake(String path, String filename) {
		if (!Methods.fileExists(path + filename) && Methods.fileExists("/usr/bin/pyrit")) {
			stripHandshakeTshark(path, filename);
			return;
		}
		
		// we have a valid cap file and pyrit!
		// tell pyritto strip the handshake, save to 'capfile.cap.temp'
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + 
					"/bin/sh -c pyrit -r " + path + filename + " -o " + path + "capfile.cap.temp strip");
			Process proStrip = Runtime.getRuntime().exec(new String[]{
					
					"/bin/sh",
					
					"-c",
					
					"pyrit" +
					" -r " + path + filename +
					" -o " + path + "capfile.cap.temp" +
					" strip"
				}
			);
			proStrip.waitFor();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// if we properly stripped the file, remove the old one and replace it w/ the new one
		if (Methods.fileExists(path + "capfile.cap.temp")) {
			Methods.readExec("rm " + path + filename);
			
			// move sexy smaller capfile over old cap file's place!
			Methods.readExec("mv " + path + "capfile.cap.temp " + path + filename);
		}
	}
	
	/** strips handshake from cap file using tshark;<p>
		uses tshark to remove everything in the cap file except the handshake
		stores to new file, deletes old file, replaces old file;
		if user does not have tshark, does nothing;
		@param path path to the .cap file, needed for temporary file purposes
		@param filename name of .cap file
	*/
	public static void stripHandshakeTshark(String path, String filename) {
		if (!Methods.fileExists(path + filename) && Methods.fileExists("/usr/bin/tshark")) {
			return;
		}
		
		// we have a valid cap file and tshark!
		// tell tshark to strip the handshake, save to 'capfile.cap.temp'
		try {
			if (Methods.verbose)
				System.out.println("exec:\t/bin/sh -c tshark -r " + path + filename + 
					" -R \"eapol || wlan.fc.type_subtype == 0x08\" -w " + path + "capfile.cap.temp");
			Process proStrip = Runtime.getRuntime().exec(new String[]{
					
					"/bin/sh",
					
					"-c",
					
					"tshark" +
					" -r " + path + filename +
					" -R \"eapol || wlan.fc.type_subtype == 0x08\"" +
					" -w " + path + "capfile.cap.temp"});
			proStrip.waitFor();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// if we properly stripped the file, remove the old one and replace it w/ the new one
		if (Methods.fileExists(path + "capfile.cap.temp")) {
			Methods.readExec("rm " + path + filename);
			
			// move sexy small new capfile to old cap file's place!
			Methods.readExec("cp " + path + "capfile.cap.temp " + path + filename);
		}
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler*/
