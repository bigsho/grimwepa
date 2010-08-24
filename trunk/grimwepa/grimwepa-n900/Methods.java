/** holds all of the methods used by Gui.java and other classes<p>
	this is where the bulk of the program's code is stored; <p>
	functionality abounds, yo!
*/

// for file/output writing
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
// for file/input reading
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
// exceptions
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

// for colors
import java.awt.Color;

// for borders
import javax.swing.BorderFactory;

// file access
import java.io.File;

// reading text
import java.util.Scanner;

// for URI exception crap (finding path to jar file)
import java.net.URISyntaxException ;

// extracting default_pw.txt from jar file...
import java.util.jar.*;
import java.util.zip.*;

// getting proper date for writing cracked keys to file
import java.util.Calendar;
import java.text.SimpleDateFormat;

// for file choosing
import javax.swing.JFileChooser;

// for online wpa cracker method
import javax.swing.JTextArea;

public class Methods {
	// there are a lot of public variables here
	// other classes, at one time or another, need to access some of the information stored here
	
	/** crucch passthrough window ... for wpa cracking*/
	public static GuiCrunchPass gcpWindow;
	
	/** wordlist generator window ... for wpa cracking*/
	public static GuiWordlistGen gwlgWindow;
	
	/** keep track of scanner thread (so we can close it from the Gui class on exit)*/
	public static threadTargetScan targetScan = null;
	
	/** so we know if we put a device into monitor mode or not*/
	public static String putInMonitorMode = "";
	
	/** aircrack-ng process, for cracking, used by other classes*/
	public static Process proCrack = null;
	/** airodump-ng process, for capturing, used by other classes */
	public static Process proAttack = null;
	/** aireplay-ng -9 ; injection test process*/
	public static Process proInjection = null;
	
	/** bssid of current targeted access point*/
	public static String currentBSSID;   // holds our current target's bssid
	/** ssid of current targeted access point*/
	public static String currentSSID;    // holds current target's ssid (AP name)
	/** channel of current targeted access point*/
	public static String currentChannel; // channel the current target is on
	
	/** path to grimwepa working directory (where the jar file is)*/
	public static String grimwepaPath;
	
	/** list of previously-cracked access points based on BSSID*/
	public static String[] cracked = new String[]{""};
	
	/** list of clients, starting w/ BSSID (XX:XX:XX:XX:XX:XX)*/
	public static String[] clients = new String[]{""};
	
	/** flag, outputs more command-line info to user when true
		set using argument '-v' on the main class*/
	public static boolean verbose;
	
	/** looks up path to Main.java (the jar file) and 
		sets the global variable 'Methods.grimwepaPath' to this path
		basically, finds the working directory for the program<p>
		this is done in the first stages of GW loading
		@see Gui#Gui(String)
	*/
	public static void setPath() {
		grimwepaPath = "";
		try {
			File f = new File (Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			grimwepaPath = f.getPath();
			f = null;
		} catch (URISyntaxException use) {
			use.printStackTrace();
		}
		grimwepaPath = grimwepaPath.substring(0, grimwepaPath.lastIndexOf("/"));
		if (grimwepaPath.equals("") == false) {
			if (grimwepaPath.substring(grimwepaPath.length() - 1, grimwepaPath.length()).equals("/") == false)
				grimwepaPath = grimwepaPath + "/";
		}
	}
	
	/** loads previously-cracked keys into an public String array called 'cracked';
		makes for quick searching when user clicks on an access point;
		items in the array are stored in the format:
			<code>BSSID(SSID)KEY<code>
		@see Gui#afterLoad()
	*/
	public static void loadCracked() {
		String input[] = readFile(grimwepaPath + "pass.txt");
		String result = "";
		
		// if we have no previously-cracked items, gtfo
		if (input[0].equals("") && input.length == 0)
			return;
		
		// loop through every line of the saved passwords
		for (int i = 0; i < input.length; i++) {
			String line = input[i];
			
			// if current line isn't long enough, go to next line
			if (line.length() < 14)
				continue;
			
			// backwards compatibility check, newer versions separate information with "|||"
			if (line.indexOf("|||") >= 0) {
				line = line.substring(13, line.indexOf("|||"));
			
			} else {
				// old version did not separate using "|||"
				line = line.substring(13);
			}
			
			// strip excess data from the line
			line = line.replaceAll("KEY: ", "");
			line = line.replaceAll("\t", "");
			
			// store info from the line in a String, separated by new line characters
			result += "" + line.trim() + "\n";
		}
		
		// if we have no stored passwords, leave the method
		if (result.equals(""))
			return;
		
		// get rid of trailing new line character at the end of result
		if (result.substring(result.length() - 1).equals("\n"))
			result = result.substring(0, result.length() - 1);
		
		// split the result String by new line characters, creating an array of previously-cracked passwords
		cracked = result.split("\n");
		
		// update the number on the 'key tank' button
		Gui.btnKeyTank.setLabel("key tank (" + cracked.length+  ")");
	}
	
	/** enables or disables all of the WEP components on the Gui object
		@param en whether to enable or disable (true/false)
		@see Gui#buildControls()
		@see Gui#valueChanged(ListSelectionEvent)
	*/
	public static void setEnableWEP(boolean en) {
		Gui.panWep.setEnabled(en);
		Gui.lblWepAttack.setEnabled(en);
		Gui.cboWepAttack.setEnabled(en);
		Gui.chkWepClient.setEnabled(en);
		if (Gui.chkWepClient.isSelected() && en)
			Gui.cboWepClients.setEnabled(true);
		else
			Gui.cboWepClients.setEnabled(false);
		Gui.btnWepAttack.setEnabled(en);
		Gui.lblWepIvs.setEnabled(en);
		Gui.btnWepCrack.setEnabled(en);
		Gui.btnWepTestinj.setEnabled(en);
		Gui.btnWepDeauth.setEnabled(en);
		Gui.lblWepInjection.setEnabled(en);
		Gui.sldWepInjection.setEnabled(en);
		Gui.chkWepSignon.setEnabled(en);
	}
	
	/** enables/disables all WPA components on the Gui object;
		useful when doing specific commands
		@param en enables/disables based on this value
		@see Gui#buildControls()
		@see Gui#valueChanged(ListSelectionEvent)
	*/
	public static void setEnableWPA(boolean en) {
		Gui.panWpa.setEnabled(en);
		Gui.btnWpaCrack.setEnabled(en);
		Gui.cboWpaCrackMethod.setEnabled(en);
		Gui.btnWpaCrack.setEnabled(false);
		Gui.cboWpaCrackMethod.setEnabled(false);
		Gui.chkWpaSignon.setEnabled(false);
		Gui.chkWpaClients.setEnabled(en);
		if (Gui.chkWpaClients.isSelected() && en)
			Gui.cboWpaClients.setEnabled(true);
		else
			Gui.cboWpaClients.setEnabled(false);
		Gui.btnWpaDeauth.setEnabled(en);
		Gui.lblWpaTimeout.setEnabled(en);
		Gui.txtWpaTimeout.setEnabled(en);
		Gui.lblWpaWarning.setEnabled(en);
	}
	
	/** returns the color (as a Color object) depending on the index given
		for finding the user's current selection of color on the Gui object
		@param colorindex index of the 'colors' combobox on the Gui
		@see #changeColor(int)
	*/
	public static Color getColor(int colorindex) {
		// repetitive code, but i want to be able to add new colors easily
		Color color = null;
		switch (colorindex) {
		case 0:
			color = Color.red;
			break;
		case 1:
			color = Color.orange;
			break;
		case 2:
			color = Color.yellow;
			break;
		case 3:
			color = Color.green;
			break;
		case 4:
			color = Color.blue;
			break;
		case 5:
			color = new Color(255, 0, 255);
			break;
		case 6:
			color = Color.pink;
			break;
		case 7:
			color = Color.white;
			break;
		case 8:
			color = Color.gray;
			break;
		}
		
		return color;
	}
	
	/** changes all Gui component colors based on index given
		@param colorindex color, based on Gui.cboColors combobox indexes, to change everything to
		@see #getColor(int)
	*/
	public static void changeColor(int colorindex) {
		Color color = getColor(colorindex);
		
		Gui.cboColors.setForeground(color);			// colors combobox
		Gui.btnInstall.setBackground(color);		// install button
		Gui.chkHideWin.setForeground(color);        // hide xterms checkbox
		Gui.lblIface.setForeground(color);          // label that shows Wifi Device:
		Gui.cboDrivers.setForeground(color);        // combobox holding wifi drivers in monitor mode
		Gui.btnDrivers.setBackground(color); 		// Refresh Drivers button
		Gui.tabTargets.setForeground(color);		// list of targets
		Gui.lblChannel.setForeground(color);  		// label that says "channel"
		Gui.chkChannel.setForeground(color);  		// checkbox to use "all channels"
		Gui.sldChannel.setForeground(color); 		// channel slider (1-14)
		Gui.btnKeyTank.setBackground(color); 		// key tank
		Gui.btnTargets.setBackground(color); 		// "refresh targets" button
		Gui.lblTargetTimeout.setForeground(color);  // label showing refresh timeout
		Gui.txtTargetTimeout.setForeground(color);  // textfield holding timeout
		Gui.panWep.setForeground(color);     		// panel holding all WEP-related attack components
		Gui.panWep.setBorder(BorderFactory.createTitledBorder(null, "wep", 0, 0, null, color));
		Gui.lblWepAttack.setForeground(color);  	// label for "Attack method"
		Gui.cboWepAttack.setForeground(color);   	// WEP attacks combobox (frag, chopchop, etc)
		Gui.chkWepClient.setForeground(color);   	// WEP choose client checkbox
		Gui.cboWepClients.setForeground(color);     // WEP clients combobox
		Gui.btnWepAttack.setBackground(color); 		// Start Attack button
		Gui.lblWepIvs.setForeground(color);      	// # of IVs captured label
		Gui.btnWepCrack.setBackground(color); 		// Start Cracking button
		Gui.btnWepTestinj.setBackground(color);		// Test injection button
		Gui.btnWepDeauth.setBackground(color);		// Change MAC button
		Gui.sldWepInjection.setForeground(color);	// injection rate slider
		Gui.lblWepInjection.setForeground(color);	// label for injection rate
		Gui.chkWepSignon.setForeground(color);		// checkbox for signon on WEP
		Gui.panWpa.setForeground(color);    		// panel holding all WPA-related attack components
		Gui.panWpa.setBorder(BorderFactory.createTitledBorder(null, "wpa", 0, 0, null, color));
		Gui.btnWpaCrack.setBackground(color);     	// Start Cracking button
		Gui.chkWpaClients.setForeground(color);		// "choose client" checkbox
		Gui.cboWpaClients.setForeground(color);  	// list of clients combobox
		Gui.btnWpaDeauth.setBackground(color);		// start deauth & handshake capture button
		Gui.lblWpaTimeout.setForeground(color);		// label that shows the timeout
		Gui.txtWpaTimeout.setForeground(color);		// text holding timeout
		Gui.lblWpaWarning.setForeground(color);     // another label... to fill up space!
		Gui.cboWpaCrackMethod.setForeground(color); // combobox for cracking methods
		Gui.chkWpaSignon.setForeground(color);		// signon checkbox for wpa
		// Gui.txtWpaWordlist.setForeground(color); // path to wordlist textbox
		// Gui.btnWpaWordlist.setForeground(color); // button to choose wpa word list
		Gui.lblStatus.setForeground(color);         // status bar (label)
		Gui.lblStatus.setBorder(BorderFactory.createLineBorder(color));
	}
	
	/** reads every line from a file, returns as a String array
		each element of the array is a line from the file<p>
		this method is used by many classes and methods
		@param file path to file you want to read
		@return contents of the file in  String array, split by new lines
	*/
	public static String[] readFile(String file) {
		Scanner input = null;
		try {
			input = new Scanner(new FileReader(file));
		} catch (FileNotFoundException fnfe) {
			// fnfe.printStackTrace();
			// don't display the error everytime
			return new String[]{""};
		}
		
		String s = "";
		while (input.hasNext()) {
			s = s + input.nextLine();
			while (s.length() > 0 && s.substring(s.length() - 1).equals("|"))
				s = s.substring(0, s.length() - 1);
			s += "|||||";
		}
		
		// removing trailing space
		if (s.length() >= 5) {
			if (s.substring(s.length() - 5).equals("|||||"))
				s = s.substring(0, s.length() - 5);
		}
		return s.split("\\|\\|\\|\\|\\|");
	}
	
	/** writes data to a file, doesn't append or anything, just completely overwrites it
		@param file file to write everything to
		@param text what to put in the file
	*/
	public static void writeFile(String file, String text) {
		Writer w = null;
		try {
			w = new PrintWriter(new FileWriter(file));
			w.write(text);
			w.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				w.close();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
	}
	
	/** loads settings from /etc/grimwepa.conf file
		loading certain settings can be complicated... this sub takes care of most of it
	*/
	public static void loadSettings() {
		boolean showreadme = true;
		boolean changeColor = false;
		
		String s[] = readFile("/etc/grimwepa.conf");
		for (int i = 0; i < s.length; i++) {
			String line = s[i];
			// lines for settings must be of length 5 or greater
			if (line.length() < 5) {
				continue;
			
			// only one setting can be of length 5...
			} else if (line.length() == 5) {
				if (line.equals("shown"))
					showreadme = false;
			}
			
			// 2nd-half of settings should be the wordlist settings... exit if we hit those
			if (line.equals("[crunch]"))
				break;
			
			if (line.substring(0, 5).equals("iface")) {
				// iface [text]
				for (int j = 0; j < Gui.cboDrivers.getItemCount(); j++) {
					if (Gui.cboDrivers.getItemAt(j).equals(line.substring(6))) {
						Gui.cboDrivers.setSelectedIndex(j);
						break;
					}
				}
			} else if(line.substring(0, 5).equals("chann")) {
				// channel #
				try {
					Gui.sldChannel.setValue(Integer.parseInt(line.substring(8)));
				} catch (NumberFormatException nfe) {}
			} else if (line.substring(0, 5).equals("allch") == true) {
				// allchan [true/false]
				if (line.substring(8).equals("true") == true) {
					Gui.chkChannel.setSelected(true);
					Gui.sldChannel.setEnabled(false);
				}
			} else if (line.substring(0, 5).equals("targe")) {
				// targettimeout #
				Gui.txtTargetTimeout.setText(line.substring(14));
			} else if (line.substring(0, 5).equals("wpati")) {
				// wpatimeout #
				Gui.txtWpaTimeout.setText(line.substring(11));
			} else if (line.substring(0, 5).equals("wepat")) {
				// wepattack #
				int wepind = 0;
				try {
					wepind = Integer.parseInt(line.substring(10));
				} catch (NumberFormatException nfe) {}
				Gui.cboWepAttack.setSelectedIndex(wepind);
			} else if (line.substring(0, 5).equals("wpawo")) {
				// wpaword [file]
				Gui.txtWpaWordlist.setText(line.substring(8)); 
			} else if (line.substring(0, 5).equals("shown")) {
				// shown
				showreadme = false;
			} else if (line.substring(0, 5).equals("xterm")) {
				// xterm [true/false]
				if (line.substring(6).equals("true") == true)
					Gui.chkHideWin.setSelected(true);
			} else if (line.substring(0, 5).equals("irate")) {
				// irate #
				try {
					Gui.sldWepInjection.setValue(Integer.parseInt(line.substring(6)));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (line.substring(0, 5).equals("color")) {
				// color #
				int index = 0;
				changeColor = true;
				try {
					index = Integer.parseInt(line.substring(6));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				Gui.cboColors.setSelectedIndex(index);
				Methods.changeColor(index);
			} else if (line.substring(0, 5).equals("wpame")) {
				// wpamethod #
				try {
					Gui.cboWpaCrackMethod.setSelectedIndex(Integer.parseInt(line.substring(10)));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
			
		} // end of for-loop through every configuration line
		
		// if this is the first time loading the configuration...
		if (showreadme == true) {
			// put the README and grimstall.sh in the working directory
			extractFile("README");
			extractFile("GUIDE");
			
			checkCompatibility();
		}
		
		// if settings didn't include a saved color...
		if (!changeColor) {
			Gui.cboColors.setSelectedIndex(2);
			Methods.changeColor(2);
		}
	}
	
	/** checks current operating system,
		and checks if user is root;
		if not, tells user they need to be on Backtrack or logged in as root,
		@return true if OS and user are valid, false otherwise
	*/
	public static boolean startupCheck() {
		// get OS
		String os = System.getProperty("os.name");
		
		// msg is error log
		String msg = "";
		
		// if os is not Linux
		if (!os.equals("Linux"))
			msg = "you are not using a linux-based operating system.\n\n";
		
		// get user
		String user = readExec("whoami")[0];
		// if user is not root
		if (!user.equals("root"))
			msg += "you are not logged in as root.\n\n";
		
		// if we have errors, display them, then return false
		if (!msg.equals("")) {
			JOptionPane.showMessageDialog(
				null,
				"some errors occurred during load:\n\n" +
				msg +
				"please correct these errors before loading grim wepa again!",
				"grim wepa | system errors",
				JOptionPane.ERROR_MESSAGE
			);
			extractFile("README");
			extractFile("GUIDE");
			return false;
		}
		
		// return true if no errors
		return true;
	}
	
	/** saves program's settings to /etc/grimwepa.conf
		copies 'wordlist' settings which are saved to the same file
	*/
	public static void saveSettings() {
		// bring up old preferences for the wordlist file
		String input[] = readFile("/etc/grimwepa.conf");
		String toStore = "";
		boolean flag = false;
		for (int i = 0; i < input.length; i++) {
			if (input[i].equals("[crunch]"))
				flag = true;
			if (flag) {
				toStore += "\n" + input[i];
			}
		}
		
		String output = "";
		output += "iface " +(String)Gui.cboDrivers.getSelectedItem();
		output += "\nchannel " + 	Gui.sldChannel.getValue();
		output += "\nallchan " + 	Gui.chkChannel.isSelected();
		output += "\ntargettimeout " + Gui.txtTargetTimeout.getText();
		output += "\nwpatimeout " + Gui.txtWpaTimeout.getText();
		output += "\nwpaword " + 	Gui.txtWpaWordlist.getText();
		output += "\nwepattack " + 	Gui.cboWepAttack.getSelectedIndex();
		output += "\nxterm " + 		Gui.chkHideWin.isSelected();
		output += "\nirate " + 		Gui.sldWepInjection.getValue();
		output += "\ncolor " + 		Gui.cboColors.getSelectedIndex();
		output += "\nwpamethod " +  Gui.cboWpaCrackMethod.getSelectedIndex();
		output += "\nshown"; // only display the README once!
		output += toStore; // add wordlist settings to the end
		
		writeFile("/etc/grimwepa.conf", output);
	}
	
	/** checks user's /usr/bin/ folder for required/recommended apps
		lets user know what, if anything, they are missing or required to have.*/
	public static void checkCompatibility() {
		String msg = "";
		
		// required apps:
		if (!fileExists("/usr/sbin/airmon-ng") && !fileExists("/usr/bin/airmon-ng"))
			msg += "  airmon-ng : for putting devices into monitor mode\n";
		if (!fileExists("/usr/sbin/aircrack-ng") && !fileExists("/usr/bin/aircrack-ng"))
			msg += "  aircrack-ng : for cracking wep and wpa\n";
		if (!fileExists("/usr/bin/airodump-ng") && !fileExists("/usr/sbin/airodump-ng"))
			msg += "  airodump-ng : for scanning wireless networks\n";
		if (!fileExists("/usr/sbin/aireplay-ng") && !fileExists("/usr/bin/aireplay-ng"))
			msg += "  aireplay-ng : for injecting packets\n";
		if (!fileExists("/usr/sbin/packetforge-ng") && !fileExists("/usr/bin/packetforge-ng"))
			msg += "  packetforge-ng : for creating arp packets to inject\n";
		if (!fileExists("/usr/bin/macchanger"))
			msg += "  macchanger : for client-based wep attacks\n";
		if (!fileExists("/sbin/iwconfig") && !fileExists("/usr/sbin/iwconfig"))
			msg += "  iwconfig: for verifying devices in monitor mode, signing on\n";
		if (!fileExists("/sbin/ifconfig") && !fileExists("/usr/sbin/ifconfig"))
			msg += "  ifconfig : for putting devices up or down\n";
		if (!fileExists("/sbin/wpa_supplicant") && !fileExists("/usr/sbin/wpa_supplicant") &&
				!fileExists("/usr/bin/wpa_supplicant"))
			msg += "  wpa_supplicant : for signing onto wpa and intel4965 chipsets\n";
		if (!fileExists("/usr/bin/sort") && !fileExists("/usr/sbin/sort"))
			msg += "  sort : for password list generation\n";
		if (!fileExists("/sbin/iwlist") && !fileExists("/usr/bin/iwlist"))
			msg += "  iwlist : for discovering wpa encryption types\n";
		
		if (!msg.equals("")) {
			// we have required apps missing!
			msg = 	"one or more required apps are missing!\n" +
					"please install the files below before running grim wepa again!\n\n" + 
					msg + "\n";
		}
		
		String msg2 = "";
		// recommended apps
		if (!fileExists("/usr/bin/pyrit") && !fileExists("/usr/sbin/pyrit"))
			msg2 += "  pyrit : for speed gpu-based wpa cracking\n";
		if (!fileExists("/usr/bin/crunch") && !fileExists("/pentest/passwords/crunch/crunch")) {
			msg2 += "  crunch : for password generating (passthrough attacks)\n";
			msg2 += "     note: you can select crunch's directory before the attack.\n";
		}
		if (!fileExists("/usr/bin/uniq") && !fileExists("/usr/sbin/uniq"))
			msg2 += "  uniq : for removing duplicates from wordlists\n";
		if (!fileExists("/usr/bin/pw-inspector") && !fileExists("/usr/sbin/pw-inspector"))
			msg2 += "  pw-inspector : for filtering passwords by size\n";
		
		if (!msg2.equals("")) {
			// we have recommended apps missing!
			msg2 = 	"one or more recommended apps are missing!\n" +
					"while these apps are not required for grim wepa to function,\n" +
					"they do add a lot of functionality to the program.\n" +
					"please instsall the files below before running grim wepa again!\n\n" +
					msg2;
		}
		
		// if either required or recommended are missing, display it to the user.
		if (!msg.equals("") || !msg2.equals("")) {
			JOptionPane.showMessageDialog(
				null,
				msg + msg2 + "\n\n" + 
				"you will not receive this message again",
				"grim wepa | required applications",
				JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	/** extracts a file from the project's .JAR collection,
		stores the extracted file in the project's working directory (grimwepaPath)
		file stored as the same name it was stored in the .jar
		@param filename name of the file in the .JAR to extract
	*/
	public static void extractFile(String filename) {
		try {
			String home = grimwepaPath + Main.VERSION;
			
			JarFile jar = new JarFile(home);
			ZipEntry entry = jar.getEntry(filename);
			File efile = new File(grimwepaPath, entry.getName());
			
			InputStream in = 
				new BufferedInputStream(jar.getInputStream(entry));
			OutputStream out = 
				new BufferedOutputStream(new FileOutputStream(efile));
			byte[] buffer = new byte[2048];
			for (;;)  {
				int nBytes = in.read(buffer);
				if (nBytes <= 0) break;
				out.write(buffer, 0, nBytes);
			}
			out.flush();
			out.close();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** executes a command ala command-line, returns the output of the command
		returns a String array; each element of the array is a line of the output
		@param command command to execute (ls, cd, java -jar asdf.jar, etc)
		@return String array of command's output, split by new lines
	*/
	public static String[] readExec(String command) {
		if (verbose)
			System.out.println("exec:\t" + command.replaceAll("!PATH!", grimwepaPath));
		
		Process proc = null;
		BufferedReader res1;
		String all = "";
		
		try {
			proc = Runtime.getRuntime().exec(fixArgumentsPath(command));
			res1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			proc.waitFor();
			
			String line;
			while ( (line = res1.readLine()) != null) {
				if (verbose && !line.trim().equals(""))
					System.out.println("\t" + line);
				all = all + line + "|||||";
			}
			if (all.length() >= 5) {
				if (all.substring(all.length() - 5).equals("|||||"))
					all = all.substring(0, all.length() - 5);
			}
			
			proc.destroy();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		proc.destroy();
		
		return all.split("\\|\\|\\|\\|\\|");
	}
	
	/** clears drivers list,
		executes airmon-ng and looks at available wifi devices,
		checks each device in iwconfig to see if it is in monitor mode,
		if no devices are found in monitor mode, 
		grimwepa prompts the user to put a device into monitor mode
	*/
	public static void refreshDrivers() {
		// adds valid drivers to 'drivers' combobox (monitor mode only!)
		// if no devices are found, prompts user to put device into monitor mode.
		
		String output[], ifaces = "";
		
		stat("scanning for wireless cards...");
		
		Gui.cboDrivers.removeAllItems();
		Gui.cboDrivers.addItem("select one:");
		
		output = readExec("airmon-ng");
		for (int i = 0; i < output.length; i++) {
			String line = output[i];
			if ((line.indexOf("Interface") < 0) && (line.equals("") == false)) {
				String driver = line.substring(0, line.indexOf("\t"));
				ifaces += "," + driver;
				if (isValidDriver(driver)) {
					Gui.cboDrivers.addItem(driver);
					Gui.cboDrivers.setSelectedIndex(Gui.cboDrivers.getItemCount() - 1);
				}
			}
		}
		
		if (Gui.cboDrivers.getItemCount() == 1) {
			// no devices in montior mode, prompt user!
			if (ifaces.equals("")) {
				// no wirless interfaces at all
				JOptionPane.showMessageDialog(
					null,
					"no wireless devices were found.\n\n" + 
					"a possible reason for this is that Backtrack does not detect your chipset.",
					"grim wepa | error",
					JOptionPane.ERROR_MESSAGE);
				stat("inactive");
				return;
			}
			
			ifaces = ifaces.substring(1);
			String driversa[] = ifaces.split(",");
			String choice = (String)JOptionPane.showInputDialog(null, 
						"select a wireless device to put into monitor mode:", 
						"grim wepa | select device for monitor mode",
						JOptionPane.INFORMATION_MESSAGE, null, driversa, driversa[0]);
			
			if (choice == null) {
				// user clicked 'cancel'
				stat("inactive");
				return;
			}
			
			stat("putting '" + choice + "' into monitor mode...");
			Gui.cboDrivers.removeAllItems();
			Gui.cboDrivers.addItem("select one:");
			
			// flag, waits until we hit 'Interface' in airmon-ng output
			// prevents us from interpretting errors in airmon-ng as interfaces
			boolean ifaceFlag = false;
			
			// put device into montior mode, read output
			output = readExec("airmon-ng start " + choice);
			for (int i = 0; i < output.length; i++) {
				String line = output[i];
				if (ifaceFlag) {
					if (line.indexOf("monitor mode enabled") >= 0) {
						line.replaceAll("\t", "");
						Methods.stat(choice + ": " + line);
						if (line.indexOf("enabled on") >= 0) {
							choice = line.substring(line.indexOf(
							"enabled on") + 11, line.length() - 1);
						}
						
					} else if ((line.indexOf("Interface") < 0) 
							&& (line.indexOf("\t") >= 0)) {
						// add driver to list
						String driver = line.substring(0, line.indexOf("\t"));
						if (isValidDriver(driver) == true) {
							Gui.cboDrivers.addItem(driver);
						}
					}
				}
				if (line.indexOf("Interface") >=0)
					ifaceFlag = true;
			}
			
			int tempi = -1;
			for (tempi = 0; tempi < Gui.cboDrivers.getItemCount(); tempi++) {
				if (choice.equals((String)Gui.cboDrivers.getItemAt(tempi)) == true) {
					tempi = -1;
					break;
				}
			}
			
			if (tempi != -1) 
				Gui.cboDrivers.addItem(choice);
			
			putInMonitorMode = choice;
			
			Gui.cboDrivers.setSelectedItem(choice);		
		} else {
			stat("inactive");
		}
	}
	
	/** checks if a driver is in monitor mode
		vague method name, i know
		@param driver name of device to check
	*/
	public static boolean isValidDriver(String driver) {
		// checks if certain driver is in monitor mode
		String[] output;
		
		output = readExec("iwconfig " + driver);
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("Mode:Monitor") >= 0)
				return true;
		}
		
		return false;
	}
	
	/** starts target scan
	*/
	public static void targetScanStart() {
		String driver = (String)Gui.cboDrivers.getSelectedItem();
		if (driver.equals("select one:")) {
			JOptionPane.showMessageDialog(
				null,
				"you need to select a wireless card to use for scanning.",
				"grim wepa | error",
				JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		Gui.dtmTargets.setRowCount(0);
		Gui.setEnable(false);
		Gui.tabTargets.setEnabled(true);
		
		Gui.btnTargets.setEnabled(true);
		Gui.btnTargets.setLabel("stop scanning");
		
		targetScan = new threadTargetScan();
		targetScan.t.start();
	}
	
	/** stops target scan
	*/
	public static void targetScanStop() {
		// stops target scanning thread
		Gui.btnTargets.setLabel("refresh targets");
		Gui.setEnable(true);
	}
	
	/** adds a client to our 'clients' array
		each element of the 'clients' array corresponds to a specific access point
		this way the user can only select clients which are connected to the current access point
		@param bssid mac address of access point client is connected to
		@param station mac address of the client connected
	*/
	public static void addClient(String bssid, String station) {
		// adds client to client array based on bssid
		String arr[] = new String[]{""};
		
		for (int i = 0; i < clients.length; i++) {
			
			arr = clients[i].split(",");
			
			if (arr[0].equals(bssid)) {
				
				if (arr[0].indexOf(station) < 0)
					clients[i] += "," + station;
				
				return;
			}
		}
		
		// bssid must be to an open network (not in the list)
	}
	
	/** deletes a file (or directory) from the working directory
		@param filename name of file to delete
	*/
	public static void removeFile(String filename) {
		// removes file, assumes we're in the working directory
		try {
			if (verbose)
				System.out.println("exec:\trm -rf " + grimwepaPath + filename);
			Runtime.getRuntime().exec(Methods.fixArgumentsPath("rm -rf !PATH!" + filename + ""));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/** sets statusbar text, nicely
		@param status text to change status to
	*/
	public static void stat(String status) {
		// update statusbar
		Gui.lblStatus.setText(" " + status);
	}
	
	/** pauses for a given amount of time
		@param duration time, in seconds, to 'pause' aka sleep
	*/
	public static void pause(double duration) {
		// pauses for selected period of time (IN SECONDS)
		try {
			Thread.currentThread().sleep((int)(duration * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** converts a string of arguments (separated by spaces) into an array
		it also replaces the string "!PATH!" that's in an argument with the program's working dir.
		this is so we can have paths that contain a space!! :D
		this took me a while to figure out, but I think i got it! :D
	*/
	public static String[] fixArgumentsPath(String args) {
		
		String[] blah = args.split(" ");
		for (int i = 0; i < blah.length; i++) {
			if (blah[i].startsWith("!PATH!") == true) {
				blah[i] = Methods.grimwepaPath + blah[i].substring(6);
			}
		}
		return blah;
	}
	
	/** checks if a file exists in the file system
		@param filename full path to the file we are checking
	*/
	public static boolean fileExists(String filename) {
		// returns true if a file exists, false if it does not.
		File f = new File(filename);
		return f.exists();
	}
	
	/** returns the mac address for a given driver/device
		@param driver name of device to get the mac of
	*/
	public static String getMac(String driver) {
		// get output from ifconfig on driver
		String output[] = readExec("ifconfig " + driver);
		for (int i = 0; i < output.length; i++) {
			String line = output[i], result;
			// look for line containing mac address...
			if (line.indexOf("HWaddr ") >= 0) {
				// get the MAC & return it
				result = line.substring(line.indexOf("HWaddr ") + 7, line.indexOf("HWaddr ") + 7 + 17);
				result = result.replace('-', ':');
				return result;
			}
		}
		// return blank if not found.
		return "";
	}
	
	/** changes a driver's mac address to a given address
		@param driver name of device to change the mac of (wlan0)
		@param newMac mac address to change to
	*/
	public static void changeMac(String driver, String newMac) {
		// changes mac to of wifi device 'driver' to 'newMac'
		boolean changed = false;
		if (newMac.equals(""))
			return;
		
		// put device down
		stat("macchanger: putting device down...");
		readExec("ifconfig " + driver + " down");
		
		stat("macchanger: changing mac to '" + newMac + "'...");
		String output[] = readExec("macchanger -m " + newMac + " " + driver);
		for (int i = 0; i < output.length; i++) {
			// look for output
			if (output[i].indexOf("Faked MAC") >= 0) {
				stat(output[i].toLowerCase());
				changed = true;
				break;
			} else if (output[i].indexOf("Can't change MAC") >= 0) {
				stat("ERROR: unable to change mac address!");
				break;
			}
		}
		
		// put device back up
		stat("macchanger: putting device back up...");
		readExec("ifconfig " + driver + " up");
		
		// output
		stat("mac address " + (changed ? "" : "un") + "successfully changed to '" + newMac + "'");
		pause (1);
	}
	
	/** converts hex to ascii
		if ascii value is invalid (non-printable characters), returns "n/a"
		@param hex hexidecimal value to convert to ascii characters
	*/
	public static String getAscii(String hex) {
		// converts hex to ascii ... if it can. otherwise, returns 'n/a'
		if (hex.length() % 2 != 0)
			return "n/a";
		
		int num = 0;
		String result = "";
		for (int i = 0; i < hex.length(); i += 2) {
			String s = hex.substring(i, i + 2);
			int dec = 0;
			try {
				dec = Integer.parseInt(s, 16);
			} catch (NumberFormatException nfe) {
				return "n/a";
			}
			
			if (dec <= 13 || dec >= 127)
				return "n/a";
			result += (char)(dec) + "";
		}
		return result;
	}
	
	/** starts/stops wep cracking accordingly
	*/
	public static void wepCrack() {
		if (Gui.btnWepCrack.getLabel().equals("start cracking")) {
			Gui.btnWepCrack.setLabel("stop cracking");
			removeFile("wepcracked.txt");
			
			String command = "";
			if (!Gui.chkHideWin.isSelected()) {
				command = 	"xterm" +
							" -fg " + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -geom 100x22+0+450" +
							" -T gw-aircrack" +
							" -iconic -e ";
			}
			
			command +=	"aircrack-ng" +
						" -a 1" +
						" -b " + currentBSSID +
						" -l !PATH!wepcracked.txt" +
						" !PATH!wep-01.ivs";
			
			if (verbose)
				System.out.println("exec:\t" + command.replaceAll("!PATH!", grimwepaPath));
			
			try {
				proCrack = Runtime.getRuntime().exec(fixArgumentsPath(command));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			threadWepCracker wepcrack = new threadWepCracker();
			wepcrack.t.start();
		} else {
			// Gui.btnWepCrack.setLabel("start cracking");
			threadWepCracker.flag = true;
		}
	}
	
	/** starts/stops wepattack accordingly
		when starting, it basically creates a new thread for threadWepAttack
		when stopping, it stops that thread
	*/
	public static void wepAttack() {
		// wep attack event, starts/stops accordingly
		if (Gui.btnWepAttack.getLabel().equals("start attack") == true) {
			// make sure they selected a valid attack
			if (Gui.cboWepAttack.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(
					null,
					"select an attack method!",
					"grim wepa | wep attack",
					JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			
			// start the attack
			Gui.setEnable(false);
			Gui.btnWepAttack.setEnabled(true);
			Gui.btnWepCrack.setEnabled(true);
			Gui.btnWepDeauth.setEnabled(true);
			threadWepAttack wa = new threadWepAttack();
			wa.t.start();
			
			// capture!
			Gui.btnWepAttack.setLabel("stop attack");
			
			// to turn off, set threadWepAttack.stopFlag = true;
			
		} else {
			Gui.setEnable(true);
			
			Gui.btnWepAttack.setLabel("start attack");
			Gui.btnWepDeauth.setEnabled(false);
			
			proAttack.destroy();
			threadWepAttack.stopFlag = true;
			try {
				// kill aireplay -- stuck on listening for packets!
				threadWepAttack.proAttack.destroy();
			} catch (IllegalStateException npe) {
			} catch (NullPointerException npe) {}
			
			String attack = "";
			switch(Gui.cboWepAttack.getSelectedIndex()) {
			case 1: attack = "arp-replay";
				break;
			case 2: attack = "chop-chop";
				break;
			case 3: attack = "fragmentation";
				break;
			case 4: attack = "cafe-latte";
				break;
			case 5: attack = "p0841";
				break;
			case 6: attack = "passive capture";
				break;
			}
			stat(attack + " attack stopped");
		}
	}
	
	/** creates a new deauth thread which attempts to deauthenticate
		NEEDCODE -> who are we deauthing? everyone? that's great, kinda.
	*/
	public static void wepDeauth() {
		threadDeauth td = new threadDeauth();
		td.t.start();
	}
	
	/** starts/stops wpa attack (deauth+handshake capture)
		when starting, starts airodump and creates a threadWpaListener object/thread
		when stopping, closes that thread
	*/
	public static void wpaAttack() {
		// this method is called when the Start Deauth + Handshake button is pressed
		if (Gui.btnWpaDeauth.getLabel().equals("start handshake capture")) {
			// if we are supposed to be deauthing...
			
			Gui.btnWpaDeauth.setLabel("stop handshake capture");
			Gui.setEnable(false);
			Gui.btnWpaDeauth.setEnabled(true);
			
			// remove old wpa capture file
			Methods.removeFile("wpa-01.cap");
			Methods.removeFile("wpa-01.csv");
			Methods.removeFile("wpa-01.kismet.csv");
			Methods.removeFile("wpa-01.kismet.netxml");
			
			String command = "";
			if (!Gui.chkHideWin.isSelected()) {
				command = 	"xterm" +
							" -fg " + (String)Gui.cboColors.getSelectedItem() + 
							" -bg black" +
							" -T gw-wpacapture" +
							" -geom 100x15+0+0 -e ";
			}
			
			command +=  "airodump-ng" +
						" -w !PATH!wpa" +
						" --bssid " + Methods.currentBSSID +
						" -c " + Methods.currentChannel +
						" " + (String)Gui.cboDrivers.getSelectedItem();
			
			// run airodump, saved to a pcap file, targetting the bssid/channel
			if (verbose)
				System.out.println("exec:\t" + command.replaceAll("!PATH!", grimwepaPath));
			
			try {
				proAttack = Runtime.getRuntime().exec(fixArgumentsPath(command));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// load new 'scripter' aka class that looks for a handshake in the pcap file
			threadWpaListener twl = new threadWpaListener();
			twl.t.start();
			// to turn off, set scripter.flag = true;
		} else {
			// user clicked STOP
			Gui.setEnable(true);
			Gui.btnWpaDeauth.setLabel("start handshake capture");
			threadWpaListener.flag = true; // tells class to stop
		}
	}
	
	/** starts/stops dictionary attack
		assumes handshake is in the cap file 'wpa-01.cap' in the working directory
	*/
	public static void wpaCrackDictionary() {
		if (Gui.btnWpaCrack.getLabel().equals("crack wpa with...")) {
			// user selected to begin cracking
			Gui.btnWpaCrack.setLabel("stop cracking");
			
			// remove old 'wpacracked.txt' file so we don't get a false-positive
			removeFile("wpacracked.txt");
				
			// the below code attempts to locate the user-selected path to the wordlist
			// if the path is invalid [file not found] it uses the default wordlist
			String wordlist;
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(Gui.txtWpaWordlist.getText()));
			int retval = fc.showOpenDialog(null);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				
				wordlist = f.getPath();
				
				Gui.txtWpaWordlist.setText(wordlist);
			} else {
				wordlist = "[default wordlist]";
			}
			
			// change gui so they can only click 'stop cracking'
			Gui.setEnable(false);
			Gui.btnWpaCrack.setEnabled(true);
			
			// get rid of \'s (for spaces)
			wordlist = wordlist.replaceAll("\\\\", "");
			
			if (fileExists(wordlist) == false) {
				// wordlist doesn't exist, use this default one
				wordlist = grimwepaPath + "default_pw.txt";
				
				if (fileExists(wordlist) == false)
					extractFile("default_pw.txt");
				Gui.txtWpaWordlist.setText(grimwepaPath + "default_pw.txt");
				stat("cracking wpa | dictionary attack | default wordlist");
			} else {
				Gui.txtWpaWordlist.setText(wordlist);
				stat("cracking wpa | dictionary attack | '" + wordlist + "'");
			}
			wordlist = wordlist.replaceAll(" ", "\\\\ ");
			String gwPath = Methods.grimwepaPath.replaceAll(" ", "\\\\ ");
			
			String commandArr[] = new String[]{"/bin/sh", "-c", ""};
			
			if (!Gui.chkHideWin.isSelected()) {
				commandArr[2] = "xterm" +
								" -fg " + (String)Gui.cboColors.getSelectedItem() + 
								" -bg black" +
								" -T gw-wpacrack" +
								" -geom 80x20+0+0 -e ";
			}
			
			commandArr[2] +=  "aircrack-ng" +
						" -a 2" +
						" -w " + wordlist + "" +
						" -l " + gwPath + "wpacracked.txt" +
						" " + gwPath + "wpa-01.cap";
			
			try {
				if (verbose)
					System.out.println("exec:\t" + commandArr[0] + " " + commandArr[1] + " " + commandArr[2]);
				proCrack = Runtime.getRuntime().exec(commandArr); //commandArr);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
			
			threadWpaCracker twc = new threadWpaCracker();
			twc.t.start();
		} else {
			Gui.btnWpaCrack.setLabel("crack wpa with...");
			threadWpaCracker.flag = true;
		}
	}
	
	/** starts/stops wpa cracking with pyrit
		assumes .cap file is wpa-01.cap in working directory
	*/
	public static void wpaCrackPyrit() {
		if (Gui.btnWpaCrack.getLabel().equals("crack wpa with...")) {
			// user selected to begin cracking
			Gui.btnWpaCrack.setLabel("stop cracking");
			
			// remove old 'wpacracked.txt' file so we don't get a false-positive
			removeFile("wpacracked.txt");
			
			// the below code attempts to locate the user-selected path to the wordlist
			// if the path is invalid: file-not-found or cancel is clicked, it uses the default wordlist
			String wordlist;
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(Gui.txtWpaWordlist.getText()));
			int retval = fc.showOpenDialog(null);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				
				wordlist = f.getPath();
				
				Gui.txtWpaWordlist.setText(wordlist);
			} else {
				wordlist = "[default wordlist]";
			}
			
			// get rid of \'s (for spaces)
			wordlist = wordlist.replaceAll("\\\\", "");
			
			if (fileExists(wordlist) == false) {
				// wordlist doesn't exist, use this default one
				wordlist = grimwepaPath + "default_pw.txt";
				
				if (fileExists(wordlist) == false)
					extractFile("default_pw.txt");
				Gui.txtWpaWordlist.setText(grimwepaPath + "default_pw.txt");
				stat("cracking wpa, using default wordlist");
			} else {
				Gui.txtWpaWordlist.setText(wordlist);
				stat("cracking wpa using '" + wordlist + "'");
			}
			//wordlist = wordlist.replaceAll(" ", "\\\\ ");
			
			// change gui so they can only click 'stop cracking'
			Gui.setEnable(false);
			Gui.btnWpaCrack.setEnabled(true);
			
			String command = "";
			
			command +=  "pyrit" +
						" -e \"" + currentSSID + "\"" +
						" -r \"" + grimwepaPath + "wpa-01.cap\"" +
						" -i \"" + wordlist + "\"" +
						" attack_passthrough" +
						" >" +
						" \"" + grimwepaPath + "wpacracked.txt\"";
			
			String[] commandArr = new String[]{
					"/bin/sh",
					"-c",
					command
			};
			
			try {
				if (verbose)
					System.out.println("exec:\t" + command);
				proCrack = Runtime.getRuntime().exec(commandArr);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
			
			threadWpaCracker twc = new threadWpaCracker();
			twc.t.start();
		} else {
			Gui.btnWpaCrack.setLabel("crack wpa with...");
			
			threadWpaCracker.flag = true;
		}
	}
	
	/** opens the GuiCrunchPass window
	*/
	public static void wpaCrackPassthrough() {
		Main.guiWindow.setVisible(false);
		gcpWindow = new GuiCrunchPass("grim wepa | crunch passthrough");
	}
	
	/** opens the GuiWordlist Gen window
	*/
	public static void wpaCrackWordlist() {
		Main.guiWindow.setVisible(false);
		gwlgWindow = new GuiWordlistGen("grim wepa | wordlist generator");
	}
	
	/** basically just redirects the user to one of two online wpa cracker webpages
	*/
	public static void wpaCrackOnline() {
		String s;
		s = "there are two known online wpa cracking websites,\n\n" +
			"question-defense.com:\n" +
			"\t-> $10 flat fee,\n" +
			"\t-> \"~2 hour average wait\",\n" +
			"\t-> 550+ million passwords tried,\n" +
			"\t-> \"25% success rate\",\n\n" +
			"wpacracker.com:\n" +
			"\t-> $17 flat fee,\n" +
			"\t-> \"20 minute average wait\",\n" +
			"\t-> 135 million passwords tried,\n" +
			"\t-> extended 284 mil password option,\n\n\n" +
			"which site would you like to visit in firefox?";
		String choice = (String)JOptionPane.showInputDialog(
				null,
				s,
				"grim wepa | online wpa cracker",
				JOptionPane.INFORMATION_MESSAGE,
				null,
				new String[]{"question-defense.com", "wpacracker.com"},
				"question-defense.com"
		);
		
		if (choice == null)
			return;
		
		if (choice.equals("question-defense.com"))
			choice = "http://tools.question-defense.com/wpa-password-cracker/";
		else if (choice.equals("wpacracker.com"))
			choice = "http://www.wpacracker.com/gate/select";
		
		try {
			if (verbose)
				System.out.println("exec:\tfirefox " + choice);
			Runtime.getRuntime().exec("firefox " + choice + "");
		} catch (IOException ioe) {}
		
		JTextArea jta = new JTextArea(
				"below is the information about the access point and handshake.\n" +
				(fileExists("/usr/bin/tshark") ? 
					"grim wepa has already compressed + stripped the .cap file using tshark\n" : "") +
				"feel free to keep this window open so you can copy/paste info to the online cracker.\n\n" +
				"path to cap file: \t" + grimwepaPath + "wpa-01.cap\n" +
				"ssid: \t\t" + currentSSID + "\n" +
				"bssid: \t\t" + currentBSSID
		);
		
		JOptionPane.showMessageDialog(
			null,
			jta,
			"grim wepa | online wpa cracker",
			JOptionPane.INFORMATION_MESSAGE
		);
	}
	
	/** signs in to wep access point using iwconfig
		@param ssid name of access point
		@param hexkey hex password for access point
	*/
	public static void signonWep(String ssid, String hexkey) {
		// ask user for driver to use for signon
		String driver = getDriver(ssid);
		
		if (driver.equals("null"))
			return;
		
		Process proSignon = null;
		
		stat("putting device " + driver + " down...");
		readExec("ifconfig " + driver + " down");
		
		stat("entering wireless settings...");
		readExec("iwconfig " + driver + " mode Managed");
		readExec("iwconfig " + driver + " essid \"" + ssid + "\"");
		readExec("iwconfig " + driver + " key " + hexkey + "");
		
		stat("bringing device " + driver + " up...");
		readExec("ifconfig " + driver + " up");
		
		stat("waiting for dhclient to finish (closes automatically)");
		
		// display the dhclient window to the user so they can see if it gets hung up
		readExec(	"xterm" + 
					" -fg " + (String)Gui.cboColors.getSelectedItem() + 
					" -bg black" +
					" -geom 100x15+0+0" + 
					" -T gw-dhclient" +
					" -e " + 
					"dhclient " + driver + ""
		);
		
		// assume the worst
		stat("unable to connect; out of range / invalid password?");
		
		// loop through 'ifconfig's output to see if they are connected
		String output[] = readExec("ifconfig " + driver);
		for (int i = 0; i < output.length; i++) {
			
			// if they are connected...
			if (output[i].indexOf("Bcast:") >= 0) {
				stat("successfully connected to '" + ssid + "'");
				break;
			}
		}
	}
	/** asks user for device to use to connect to the access point with; 
		returns the user's selection, or "null" if cancelled
		@param ssid default device name (used for the attack)
		@see #signonWep(String, String)
		@see #signonWpa(String, String)
		@return user-selected driver to use for signing on
	*/
	public static String getDriver(String ssid) {
		String output[] = Methods.readExec("iwconfig");
		String result = "";
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("IEEE 802.11") >= 0) {
				output[i] = output[i].substring(0, output[i].indexOf("IEEE"));
				output[i] = output[i].trim();
				
				result += output[i] + ",";
			}
		}
		
		if (result.endsWith(","))
			result = result.substring(0, result.length() - 1);
		String drivers[] = result.split(",");
		
		return (String)JOptionPane.showInputDialog(null, 
				"select which wireless device want to use to connect to '" + ssid + "':", 
				"grim wepa | select wireless device",
				JOptionPane.INFORMATION_MESSAGE, null, drivers, ssid);
		
	}
	
	/** finds type of WPA (1, 2, 1+2) using iwlist
		@param driver name of driver (iface) to connect using
		@param ssid name of access point
		@return "WPA1", "WPA2", "WPA1+2", or "n/a", depending on what iwlist finds
	*/
	public static String wpaType(String driver, String ssid) {
		String output[] = Methods.readExec("iwlist " + driver + " scan");
		
		// flag, only true after we've hit the ssid
		boolean found = false;
		int count = 0; // counter that keeps track of the encryption
		
		for (int i = 0; i < output.length; i++) {
			if (verbose)
				System.out.println(found + ": "+  output[i]);
			if (output[i].trim().startsWith("Cell ") && found) {
				break;
			} else if (output[i].indexOf("ESSID:\"" + ssid + "\"") >= 0) {
				found = true;
				count = 0;
			}
			
			if (found) {
				if (output[i].indexOf("IE: WPA Version 1") >= 0) {
					count++;
				} else if (output[i].indexOf("IE: IEEE 802.11i/WPA2 Version 1") >= 0) {
					count += 2;
				}
			}
		}
		
		switch (count) {
		case 1:
			return "WPA1";
		case 2:
			return "WPA2";
		case 3:
			return "WPA1+2";
		default:
			return "n/a";
		}
		
	}
	
	/** signs onto WPA-encrypted access point, uses wpa_supplicant
		@param ssid name of access point (case sensitive)
		@param key	required password (key) for access point
		@see threadWpaListener#run()
	*/
	public static void signonWpa(String ssid, String key) {
		String driver = getDriver(ssid);
		if (driver.equals("null"))
			return;
		
		String type = wpaType(driver, ssid);
		System.out.println(type);
		
		// get rid of any wpa_supplicant processes
		readExec("killall wpa_supplicant");
		
		String conf = 	"ctrl_interface=/var/run/wpa_supplicant\n" +
						"\n" +
						"network={\n" +
						"\tssid=\"" + ssid + "\"\n" +
						"\tscan_ssid=0\n";
		
		if (type.equals("WPA1")) {
			// WPA1
			if (verbose)
				System.out.println("Signing on to WPA1");
			conf += 		"\tproto=WPA\n" +
						"\tkey_mgmt=WPA-PSK\n" +
						"\tpsk=\"" + key + "\"\n" +
						"\tpairwise=TKIP\n" +
						"\tgroup=TKIP\n" +
						"}\n";
		} else {
			// WPA2
			if (verbose)
				System.out.println("Signing on to WPA2");
			conf += 		"\tproto=RSN\n" +
						"\tpairwise=CCMP TKIP\n" +
						"\tkey_mgmt=WPA-PSK\n" +
						"\tpsk=\"" + key + "\"\n" +
						"}\n";
		}
		
		// write this config to a file
		Methods.writeFile("/etc/wpa_supplicant.conf", conf);
		
		// run wpa_supplicant in the background
		try {
			Process newProc = Runtime.getRuntime().exec(
				"wpa_supplicant -Dwext -i" + driver + " -c/etc/wpa_supplicant.conf -B");
		} catch (IOException ioe) {}
		
		stat("waiting for dhclient to finish (closes automatically)");
		
		// display the dhclient window to the user so they can see if it gets hung up
		readExec(	"xterm" + 
					" -fg " + (String)Gui.cboColors.getSelectedItem() + 
					" -bg black" +
					" -geom 100x15+0+0" + 
					" -T gw-dhclient" +
					" -e " + 
					"dhclient " + driver + ""
		);
		
		// assume the worst
		stat("unable to connect; out of range / invalid password?");
		
		// loop through 'ifconfig's output to see if they are connected
		String output[] = Methods.readExec("ifconfig " + driver);
		for (int i = 0; i < output.length; i++) {
			
			// if they are connected...
			if (output[i].indexOf("Bcast:") >= 0) {
				stat("connected successfully to '" + ssid + "'!");
				break;
			}
		}
	}
	
	/** creates a new thread for testing injection (threadINjection.java)
	*/
	public static void testInjection() {
		// tests injection, stops if it's already testing
		
		if (Gui.btnWepTestinj.getLabel().equals("test injection") == true) {
			Gui.btnWepTestinj.setLabel("stop inj. test");
			try {
				// have to use airodump to set it on the right channel!
				String command = "";
				
				if (!Gui.chkHideWin.isSelected()) {
					command = 	"xterm" +
								" -fg " + (String)Gui.cboColors.getSelectedItem() + 
								" -bg black" +
								" -T gw-injtest" +
								" -geom 100x15+0+450" +
								" -iconic -e ";
				}
				
				// start airodump to listen on the specific channel
				// this way the wireless card doesn't go bouncing around to other channels!
				command += 	"airodump-ng" +
							" -c " + currentChannel + 
							" " + (String)Gui.cboDrivers.getSelectedItem();
				
				if (verbose)
					System.out.println("exec:\t" + command);
				
				proInjection = Runtime.getRuntime().exec(command);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			threadInjection ti = new threadInjection();
			ti.t.start();
		} else {
			Gui.btnWepTestinj.setLabel("test injection");
			threadInjection.flag = true;
			proInjection.destroy();
		}
	}
	
	/** stores a wpa password using the correct format : WPA\tBSSID(SSID)\tKEY: thekey|||date|||other_info
		also, stores the key at the top of the list (so we will get read it before other copies)
		@param key key to save, other info like bssid and ssid are grabbed while saving
		@see threadWpaCracker#run()
	*/
	public static void writeWpaToFile(String key) {
		// (hopefully) writes it to the beginning of the file.. so we choose the most-recently cracked one
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Writer bw = null;
		
		String text;
		text = 	"WPA\tB(SSID): " + currentBSSID + 
				"(" + currentSSID + ")\t" + 
				"KEY: " + key + 
				"|||" + sdf.format(cal.getTime()) + "|||" + (String)Gui.cboWpaCrackMethod.getSelectedItem();
		
		String file[] = readFile(grimwepaPath + "pass.txt");
		String output = text + "\n";
		for (int i = 0; i < file.length; i++) {
			output += file[i] + "\n";
		}
		writeFile(grimwepaPath + "pass.txt", output);
		
		// save this key into the array that we look for keys in!
		String[] newlist = new String[cracked.length + 1];
		newlist[0] = currentBSSID + "(" + currentSSID + ")\t" + key;
		for (int i = 1; i < Methods.cracked.length; i++) {
			newlist[i] = Methods.cracked[i - 1];
		}
		cracked = newlist;
		
		Gui.btnKeyTank.setLabel("key tank (" + cracked.length + ")");
	}
	
	/** convert seconds into hours, minutes, and seconds<p>
		output format is: #h #m #s
		@param sec number of seconds
		@return conversion of seconds into h:m:s format
		*/
	public static String secToHMS(int sec) {
		int h = sec / 3600;
		String sh = h + "";
		
		sec %= 3600;
		int m = sec / 60;
		String sm = m+ "";
		
		sec %= 60;
		String ss = sec + "";
		
		String result = "";
		if (!sh.equals("0"))
			result = sh + "h";
		if (!sm.equals("0") || !result.equals(""))
			result += (result.equals("") ? "" : " ") + sm + "m";
		
		if (result.equals("")) {
			result = ss + "s";
		} else {
			result += (result.equals("") ? "" : " ") + ss + "s";
		}
		
		return result;
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/