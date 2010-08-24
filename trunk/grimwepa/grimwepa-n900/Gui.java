
/** Gui class; 
	The graphical side of GrimWepa<p>
	Everything visual is handled by this object; 
	all events are detected here, but [almost] no functional code is in this class 
	for functionality, look at Methods.java
*/

// basic gui libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

// For listening to events
import java.util.EventListener;

public class Gui extends Frame implements WindowListener, ActionListener, EventListener, ListSelectionListener {
	
	//////////// COMPONENTS //////////////
	
	// installer
	/** installer button*/
	public static Button	 btnInstall;
	
	// drivers
	/** label that shows Wifi Device:*/
	public static JLabel     lblIface;
	/** combobox holding wifi drivers in monitor mode*/
	public static JComboBox  cboDrivers;
	/** Refresh Drivers button*/
	public static Button     btnDrivers;
	
	// targets list, refresh button, and timeout
	/** scrollpane to keep list inside of*/
	public JScrollPane       tscroll;
	/** list of targets*/
	public static JTable     tabTargets;
	/** DTM for list (much easier to add/remove)*/
	public static DefaultTableModel dtmTargets;
	
	// channel slider
	/** hide xterms checkbox<p>
		this checkbox is no longer shown due to space requirements;
		many methods still check the isSelected property,
		and I don't want to remove this functionality just yet
	*/
	public static JCheckBox  chkHideWin;
	/** button to open key tank*/
	public static Button	 btnKeyTank;
	/** space between tank and targets button*/
	public static JLabel	 lblFiller;
	/** label that says 'channel:'*/
	public static JLabel     lblChannel;
	/** checkbox to use 'all channels'*/
	public static JCheckBox  chkChannel;
	/** channel slider (1-14)*/
	public static JSlider    sldChannel;
	
	// refresh & timeout components
	/** 'refresh targets' button*/
	public static Button     btnTargets;
	/** label showing refresh timeout*/
	public static JLabel     lblTargetTimeout;
	/** textfield holding timeout*/
	public static JTextField txtTargetTimeout;
	
	// wep panel
	/**  panel holding all WEP-related attack components*/
	public static JPanel     panWep;
	/** choose WEP clients checkbox*/
	public static JCheckBox  chkWepClient;
	/** WEP clients combobox*/
	public static JComboBox  cboWepClients;
	/** label for "Attack method"*/
	public static JLabel     lblWepAttack;
	/** WEP attacks combobox (frag, chopchop, etc)*/
	public static JComboBox  cboWepAttack;
	/** Start Attack button*/
	public static Button     btnWepAttack;
	/** # of IVs captured label*/
	public static JLabel     lblWepIvs;
	/** Start Cracking button*/
	public static Button     btnWepCrack;
	/** Test injection button*/
	public static Button     btnWepTestinj;
	/** Change MAC button*/
	public static Button     btnWepDeauth;
	/** injection rate slider*/
	public static JSlider    sldWepInjection;
	/** label for injection rate*/
	public static JLabel     lblWepInjection;
	/** signon after crack checkbox for wep*/
	public static JCheckBox chkWepSignon;
	
	// WPA panel
	/** panel holding all WPA-related attack components*/
	public static JPanel     panWpa;
	/** Start Cracking button*/
	public static Button     btnWpaCrack;
	/** path to wordlist textbox*/
	public static JTextField txtWpaWordlist;
	/** choose client" checkbox*/
	public static JCheckBox  chkWpaClients;
	/** list of clients combobox*/
	public static JComboBox  cboWpaClients;
	/** start deauth & handshake capture button*/
	public static Button     btnWpaDeauth;
	/** label that shows the timeout*/
	public static JLabel     lblWpaTimeout;
	/** text holding timeout*/
	public static JTextField txtWpaTimeout;
	/** label to fill up space (and to let user know)*/
	public static JLabel     lblWpaWarning;
	/** button to choose wpa word list*/
	public static Button     btnWpaWordlist;
	/** combobox holding methods for cracking wpa*/
	public static JComboBox  cboWpaCrackMethod;
	/** signon after crack checkbox for wpa*/
	public static JCheckBox chkWpaSignon;
	
	// Status bar row
	/** status bar (label)*/
	public static JLabel     lblStatus;
	/** color chooser combobox*/
	public static JComboBox cboColors;
	
	
	/** default constructor, builds the form, starts the ball rolling
		@see Main#main
		@param title the title of the window
	*/
	public Gui(String title) {
		super(title); // set the title
		
		// set window properties
		//this.setSize(450,432);
		this.setSize(800,480);
		this.setFont(new Font("Default", Font.BOLD, 11));
		
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension w = this.getSize();
		//this.setLocation((ss.width - w.width ) / 2, (ss.height - w.height ) / 2);
		
		setResizable(false);
		
		this.setForeground(Color.red);
		this.setBackground(Color.black);
		setLayout(new FlowLayout());
		addWindowListener(this);
		
		// add all of the buttons, textboxes, lists, etc to the form
		buildControls();
		
		// arrange them nicely
		doLayout();
		
		// get path to the .jar file, store it for the rest of the program to use
		Methods.setPath();
		
		// load settings from file; textbox input, sliders, colors, etc
		Methods.loadSettings();
	}
	
	/** method full of commands that should happen when the form is loaded,
		but while the form is visible,<p>
		these commands are ran after the splash screen is finished and gone
		@see Main#main
	*/
	public void afterLoad() {
		// show yourself!
		this.setVisible(true);
		
		// load previously-cracked accounts into String array Methods.cracked
		Methods.loadCracked();
		
		// set everything disabled while we load drivers
		setEnable(false);
		Methods.refreshDrivers();
		setEnable(true);
	}
	
	/** build the controls (textfields, buttons, labels, etc) to go on the form
	*/
	public void buildControls() {
		// gonna use thise font a lot
		Font f = new Font("Default", Font.BOLD, 11);
		
		// ROW #1: FOR INTERFACE
		// install button
		btnInstall = new Button("install");
		btnInstall.setFont(f);
		btnInstall.setBackground(Color.red);
		btnInstall.setForeground(Color.black);
		add(btnInstall);
		btnInstall.addActionListener(this);
		// iface label
		lblIface = new JLabel("        wifi interface:");
		// lblIface.setPreferredSize(new Dimension(80, 18));
		lblIface.setForeground(Color.red);
		lblIface.setHorizontalAlignment(JTextField.RIGHT);
		lblIface.setFont(f);
		add(lblIface);
		// drivers combobox
		cboDrivers  = new JComboBox();
		cboDrivers.setBackground(Color.black);
		cboDrivers.setForeground(Color.red);
		cboDrivers.setPreferredSize(new Dimension(125, 20));
		cboDrivers.setFont(f);
		cboDrivers.addItem("select one:");
		add(cboDrivers);
		cboDrivers.addActionListener(this);
		// 'refresh drivers' button
		btnDrivers = new Button("refresh drivers");
		btnDrivers.setFont(f);
		btnDrivers.setBackground(Color.red);
		btnDrivers.setForeground(Color.black);
		add(btnDrivers);
		btnDrivers.addActionListener(this);
		
		// ROW #2: FOR TARGETS LIST
		dtmTargets = new DefaultTableModel(null, new String[] 
							{"pwr","network name","channel","enc","bssid"});
		tabTargets = new JTable(dtmTargets) { 
					public TableCellRenderer getCellRenderer( int row, int col ) {
						TableCellRenderer renderer = super.getCellRenderer(row,col);
						((JLabel)renderer).setHorizontalAlignment( SwingConstants.CENTER );
						return renderer;
						
					}
					public boolean isCellEditable(int rowIndex, int vColIndex) { 
						return false; 
					} };
		tabTargets.setAutoCreateColumnsFromModel(false); 
		tabTargets.setAutoCreateRowSorter(true); // turn sorting on
		tabTargets.setBackground(Color.black);
		tabTargets.setForeground(Color.red);
		tabTargets.setPreferredScrollableViewportSize(new Dimension(750,100));
		tabTargets.setFillsViewportHeight(true);
		tabTargets.getColumnModel().getColumn(0).setPreferredWidth(20);   // power
		tabTargets.getColumnModel().getColumn(1).setPreferredWidth(200);  // SSID
		tabTargets.getColumnModel().getColumn(2).setPreferredWidth(25);   // channel
		tabTargets.getColumnModel().getColumn(3).setPreferredWidth(30);   // encryption
		tabTargets.getColumnModel().getColumn(4).setPreferredWidth(85);   // BSSID
		tabTargets.setFont(f);
		tabTargets.getSelectionModel().addListSelectionListener(this); // listen for selection changes
		tscroll = new JScrollPane(tabTargets); // scroller
		add(tscroll);
		
		// ROW #3 FOR CHANNEL SLIDER & CHECKBOX
		JLabel lblSpacer1 = new JLabel("");
		lblSpacer1.setPreferredSize(new Dimension(150, 20));
		add(lblSpacer1);
		lblChannel = new JLabel("channel:");
		lblChannel.setForeground(Color.red);
		add(lblChannel);
		sldChannel = new JSlider(0, 1, 14, 6);
		sldChannel.setBackground(Color.black);
		sldChannel.setForeground(Color.red);
		sldChannel.setMajorTickSpacing(1);
		sldChannel.setMinorTickSpacing(1);
		sldChannel.setPaintTicks(true);
		sldChannel.setPaintLabels(true);
		sldChannel.setPaintTrack(false);
		sldChannel.setSnapToTicks(true);
		sldChannel.setFont(new Font("Default", Font.BOLD, 10));
		sldChannel.setPreferredSize(new Dimension(250, 35));
		add(sldChannel);
		chkChannel = new JCheckBox("all channels");
		chkChannel.setBackground(Color.black);
		chkChannel.setForeground(Color.red);
		add(chkChannel);
		chkChannel.addActionListener(this);
		JLabel lblSpacer2 = new JLabel("");
		lblSpacer2.setPreferredSize(new Dimension(150, 20));
		add(lblSpacer2);
		
		// ROW #4: FOR 'HIDE XTERMS', REFRESH TARGETS, INTERVAL
		btnKeyTank = new Button("key tank (0)");
		btnKeyTank.setBackground(Color.red);
		btnKeyTank.setForeground(Color.black);
		btnKeyTank.setPreferredSize(new Dimension(90,22));
		add(btnKeyTank);
		btnKeyTank.addActionListener(this);
		// filler label to put a space between key tank button and targets button
		lblFiller = new JLabel("        ");
		lblFiller.setBackground(Color.black);
		add(lblFiller);
		// don't really need chkHideWin anymore...
		chkHideWin = new JCheckBox("hide xterms");
		chkHideWin.setBackground(Color.black);
		chkHideWin.setForeground(Color.red);
		chkHideWin.setPreferredSize(new Dimension(135,18));
		//add(chkHideWin); // keep it hidden; 
		// we still have if statements to see if chkHideWin is checked or not
		// but we wanted to use this space for the 'key tank' button
		
		// Refresh button & timeout
		btnTargets = new Button("refresh targets");
		btnTargets.setFont(f);
		btnTargets.setForeground(Color.black);
		btnTargets.setBackground(Color.red);
		add(btnTargets);
		btnTargets.addActionListener(this);
		// target timeout
		lblTargetTimeout = new JLabel("timeout (sec):");
		// lblTargetTimeout.setPreferredSize(new Dimension(90, 18));
		lblTargetTimeout.setForeground(Color.red);
		add(lblTargetTimeout);
		// txtTargetTimeout
		txtTargetTimeout = new JTextField("3");
		txtTargetTimeout.setPreferredSize(new Dimension(20, 20));
		txtTargetTimeout.setHorizontalAlignment(JTextField.CENTER);
		txtTargetTimeout.setBackground(Color.black);
		txtTargetTimeout.setForeground(Color.red);
		add(txtTargetTimeout);
		
		// ROW #5: WEP PANEL
		panWep = new JPanel();
		panWep.setPreferredSize(new Dimension(750,155));
		
		JLabel lblSpacer3 = new JLabel("");
		lblSpacer3.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer3);
		// client checkbox
		chkWepClient = new JCheckBox("use client in attack:", false);
		// chkWepClient.setPreferredSize(new Dimension(200, 18));
		chkWepClient.setBackground(Color.black);
		chkWepClient.setForeground(Color.red);
		panWep.add(chkWepClient);
		chkWepClient.addActionListener(this);
		// cboWepClients combobox
		cboWepClients = new JComboBox();
		cboWepClients.setBackground(Color.black);
		cboWepClients.setForeground(Color.red);
		cboWepClients.addItem("[no clients found]");
		cboWepClients.setPreferredSize(new Dimension(150, 20));
		cboWepClients.setEnabled(false);
		panWep.add(cboWepClients);
		// deauth button
		btnWepDeauth = new Button("deauth");
		btnWepDeauth.setFont(f);
		btnWepDeauth.setBackground(Color.red);
		btnWepDeauth.setForeground(Color.black);
		panWep.add(btnWepDeauth);
		btnWepDeauth.addActionListener(this);
		JLabel lblSpacer4 = new JLabel("");
		lblSpacer4.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer4);
		
		JLabel lblSpacer5 = new JLabel("");
		lblSpacer5.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer5);
		// attack type label
		lblWepAttack = new JLabel("attack method:");
		lblWepAttack.setForeground(Color.red);
		panWep.add(lblWepAttack);
		// attack type combobox
		cboWepAttack = new JComboBox();
		cboWepAttack.addItem("select attack:");		// 0
		cboWepAttack.addItem("arp-replay");			// 1
		cboWepAttack.addItem("chop-chop");			// 2
		cboWepAttack.addItem("fragmentation");		// 3
		cboWepAttack.addItem("caffe-latte");		// 4
		cboWepAttack.addItem("p0841 attack");		// 5
		cboWepAttack.addItem("passive capture");	// 6
		// cboWepAttack.setPreferredSize(new Dimension(200, 20));
		cboWepAttack.setBackground(Color.black);
		cboWepAttack.setForeground(Color.red);
		panWep.add(cboWepAttack);
		cboWepAttack.addActionListener(this);
		// injection test button
		btnWepTestinj = new Button("test injection");
		btnWepTestinj.setFont(f);
		btnWepTestinj.setBackground(Color.red);
		btnWepTestinj.setForeground(Color.black);
		panWep.add(btnWepTestinj);
		btnWepTestinj.addActionListener(this);
		JLabel lblSpacer6 = new JLabel("");
		lblSpacer6.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer6);
		
		JLabel lblSpacer7 = new JLabel("");
		lblSpacer7.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer7);
		// injection rate label
		lblWepInjection = new JLabel("injection rate (pps):");
		lblWepInjection.setBackground(Color.black);
		lblWepInjection.setForeground(Color.red);
		panWep.add(lblWepInjection);
		// injection rate slider
		sldWepInjection = new JSlider(0, 100, 1000, 600);
		sldWepInjection.setBackground(Color.black);
		sldWepInjection.setForeground(Color.red);
		sldWepInjection.setMajorTickSpacing(100);
		sldWepInjection.setMinorTickSpacing(100);
		sldWepInjection.setPaintTicks(true);
		sldWepInjection.setPaintLabels(true);
		sldWepInjection.setPaintTrack(false);
		sldWepInjection.setSnapToTicks(true);
		sldWepInjection.setFont(new Font("Default", Font.BOLD, 10));
		sldWepInjection.setPreferredSize(new Dimension(250, 35));
		panWep.add(sldWepInjection);
		JLabel lblSpacer8 = new JLabel("");
		lblSpacer8.setPreferredSize(new Dimension(150, 20));
		panWep.add(lblSpacer8);
		
		// start attack button
		btnWepAttack = new Button("start attack");
		btnWepAttack.setFont(f);
		btnWepAttack.setBackground(Color.red);
		btnWepAttack.setForeground(Color.black);
		btnWepAttack.setPreferredSize(new Dimension(100, 25));
		panWep.add(btnWepAttack);
		btnWepAttack.addActionListener(this);
		// ivs label
		lblWepIvs = new JLabel("ivs captured: 0");
		lblWepIvs.setPreferredSize(new Dimension(130, 18));
		lblWepIvs.setForeground(Color.red);
		panWep.add(lblWepIvs);
		// start crack button
		btnWepCrack = new Button("start cracking");
		btnWepCrack.setBackground(Color.red);
		btnWepCrack.setForeground(Color.black);
		btnWepCrack.setFont(f);
		panWep.add(btnWepCrack);
		// signon checkbox
		chkWepSignon = new JCheckBox("auto signon");
		chkWepSignon.setFont(f);
		chkWepSignon.setBackground(Color.black);
		chkWepSignon.setForeground(Color.red);
		panWep.add(chkWepSignon);
		// wep panel
		panWep.setBackground(Color.black);
		panWep.setForeground(Color.red);
		btnWepCrack.addActionListener(this);
		panWep.setForeground(Color.red);
		panWep.setBorder(BorderFactory.createTitledBorder(null, "wep", 0, 0, null, Color.gray));
		add(panWep);
		
		// ROW #6 (technically, 5; same as level WEP): WPA PANEL
		panWpa = new JPanel();
		panWpa.setPreferredSize(new Dimension(750, 155));
		panWpa.setBackground(Color.black);
		panWpa.setForeground(Color.red);
		panWpa.setBorder(BorderFactory.createTitledBorder(null, "wpa", 0, 0, null, Color.red));
		
		JLabel lblSpacer9 = new JLabel("");
		lblSpacer9.setPreferredSize(new Dimension(180, 20));
		panWpa.add(lblSpacer9);
		// clients checkbox
		chkWpaClients = new JCheckBox("use client in attack:");
		chkWpaClients.setBackground(Color.black);
		chkWpaClients.setForeground(Color.red);
		panWpa.add(chkWpaClients);
		chkWpaClients.addActionListener(this);
		// clients combobox
		cboWpaClients = new JComboBox();
		cboWpaClients.addItem("[no clients found]");
		cboWpaClients.setPreferredSize(new Dimension(100, 20));
		cboWpaClients.setBackground(Color.black);
		cboWpaClients.setForeground(Color.red);
		cboWpaClients.setEnabled(false);
		panWpa.add(cboWpaClients, BorderLayout.CENTER);
		JLabel lblSpacer10 = new JLabel("");
		lblSpacer10.setPreferredSize(new Dimension(180, 20));
		panWpa.add(lblSpacer10);
		
		JLabel lblSpacer16 = new JLabel("");
		lblSpacer16.setPreferredSize(new Dimension(180, 20));
		panWpa.add(lblSpacer16);
		// start attack button
		btnWpaDeauth = new Button("start handshake capture");
		btnWpaDeauth.setBackground(Color.red);
		btnWpaDeauth.setForeground(Color.black);
		panWpa.add(btnWpaDeauth);
		btnWpaDeauth.addActionListener(this);
		// timeout label
		lblWpaTimeout = new JLabel("delay (sec):");
		lblWpaTimeout.setForeground(Color.red);
		panWpa.add(lblWpaTimeout);
		// timeout textbox
		txtWpaTimeout = new JTextField("10");
		txtWpaTimeout.setHorizontalAlignment(JTextField.CENTER);
		txtWpaTimeout.setPreferredSize(new Dimension(30, 20));
		txtWpaTimeout.setBackground(Color.black);
		txtWpaTimeout.setForeground(Color.red);
		txtWpaTimeout.setHorizontalAlignment(JLabel.CENTER);
		panWpa.add(txtWpaTimeout);
		JLabel lblSpacer11 = new JLabel("");
		lblSpacer11.setPreferredSize(new Dimension(150, 20));
		panWpa.add(lblSpacer11);
		
		JLabel lblSpacer12 = new JLabel("");
		lblSpacer12.setPreferredSize(new Dimension(150, 20));
		panWpa.add(lblSpacer12);
		// warning label
		lblWpaWarning = new JLabel("[cracking is only accessible AFTER a handshake is captured]");
		lblWpaWarning.setPreferredSize(new Dimension(390, 30));
		lblWpaWarning.setBackground(Color.black);
		lblWpaWarning.setForeground(Color.red);
		panWpa.add(lblWpaWarning);
		JLabel lblSpacer13 = new JLabel("");
		lblSpacer13.setPreferredSize(new Dimension(100, 20));
		panWpa.add(lblSpacer13);
		
		JLabel lblSpacer14 = new JLabel("");
		lblSpacer14.setPreferredSize(new Dimension(100, 20));
		panWpa.add(lblSpacer14);
		// crack button
		btnWpaCrack = new Button("crack wpa with...");
		btnWpaCrack.setBackground(Color.red);
		btnWpaCrack.setForeground(Color.black);
		panWpa.add(btnWpaCrack);
		btnWpaCrack.addActionListener(this);
		btnWpaCrack.setEnabled(false);
		txtWpaWordlist = new JTextField("[default wordlist]");
		txtWpaWordlist.setBackground(Color.black);
		txtWpaWordlist.setForeground(Color.red);
		txtWpaWordlist.setPreferredSize(new Dimension(180, 20));
		
		/* no longer need wpawordlist button or textbox...
		   keeping the txtbox so we can remember the last file used;
		   last file is stored using saveSettings() and loaded using loadSettings()
		panWpa.add(txtWpaWordlist);
		txtWpaWordlist.setEnabled(false);
		// browse button
		btnWpaWordlist = new Button("...");
		panWpa.add(btnWpaWordlist);
		btnWpaWordlist.addActionListener(this);
		btnWpaWordlist.setEnabled(false);*/
		// wpa crack combobox
		cboWpaCrackMethod = new JComboBox();
		cboWpaCrackMethod.addItem("select a method:");
		cboWpaCrackMethod.addItem("dictionary attack");
		cboWpaCrackMethod.addItem("crunch passthrough");
		cboWpaCrackMethod.addItem("dictionary + pyrit");
		cboWpaCrackMethod.addItem("wordlist generator");
		cboWpaCrackMethod.addItem("www wpa cracker $$");
		cboWpaCrackMethod.setBackground(Color.black);
		cboWpaCrackMethod.setForeground(Color.red);
		cboWpaCrackMethod.setEnabled(false);
		panWpa.add(cboWpaCrackMethod);
		// wpa signon checkbox
		chkWpaSignon = new JCheckBox("auto signon");
		chkWpaSignon.setFont(f);
		chkWpaSignon.setBackground(Color.black);
		chkWpaSignon.setForeground(Color.red);
		panWpa.add(chkWpaSignon);
		JLabel lblSpacer15 = new JLabel("");
		lblSpacer15.setPreferredSize(new Dimension(100, 20));
		panWpa.add(lblSpacer15);
		
		// add the panel
		add(panWpa);
		doLayout();
		
		//panWep.setVisible(false); // remove
		panWpa.setVisible(false);  // change to false
		
		// ROW #7: STATUS BAR & COLOR CHOOSER
		lblStatus = new JLabel(" inactive");
		lblStatus.setForeground(Color.red);
		lblStatus.setBorder(BorderFactory.createLineBorder(Color.red));
		lblStatus.setPreferredSize(new Dimension(650, 20));
		add(lblStatus);
		cboColors = new JComboBox();
		cboColors.setBackground(Color.black);
		cboColors.setForeground(Color.red);
		cboColors.setFont(f);
		cboColors.addItem("red");
		cboColors.addItem("orange");
		cboColors.addItem("yellow");
		cboColors.addItem("green");
		cboColors.addItem("blue");
		cboColors.addItem("purple");
		cboColors.addItem("pink");
		cboColors.addItem("white");
		cboColors.addItem("gray");
		cboColors.setPreferredSize(new Dimension(65, 20));
		cboColors.addActionListener(this);
		add(cboColors);
		
		Methods.setEnableWEP(false);
		// btnWpaCrack.setEnabled(true); // remove
		// cboWpaCrackMethod.setEnabled(true); // remove
	}
	
	/** event method; ran whenever a control [which added this form as an action listener] is clicked<p>
		buildControls is the method where all of the controls used 'addActionListener(this)'
		@see Gui#buildControls()
		@param event information about the click event
	*/
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cboColors) {
			// user clicked colors combobox
			Methods.changeColor(cboColors.getSelectedIndex());
		
		} else if (event.getSource() == btnDrivers) {
			// refresh drivers button
			setEnable(false);
			Methods.refreshDrivers();
			setEnable(true);
		
		} else if (event.getSource() == btnTargets) {
			// user clicked 'refresh targets' OR 'stop refreshing'
			if (btnTargets.getLabel().equals("refresh targets"))
				Methods.targetScanStart();
			else
				Methods.targetScanStop();
		
		} else if (event.getSource() == chkWepClient) {
			// user clicked 'use client in attack' [wep panel]
			cboWepClients.setEnabled(chkWepClient.isSelected());
		
		} else if (event.getSource() == chkWpaClients) {
			// user clicked 'user client in attack' [wpa panel]
			cboWpaClients.setEnabled(chkWpaClients.isSelected());
		
		} else if (event.getSource() == chkChannel) {
			// user clicked 'all channels' checkbox
			sldChannel.setEnabled(!chkChannel.isSelected());
		
		} else if (event.getSource() == btnWepAttack) {
			// user clicked 'start attack' OR 'stop attack'
			Methods.wepAttack();
		
		} else if (event.getSource() == btnWepCrack) {
			// user clicked 'start crack' OR 'stop cracking'
			Methods.wepCrack();
		
		} else if (event.getSource() == btnWepDeauth) {
			// user clicked 'deauth'
			Methods.wepDeauth();
		
		} else if (event.getSource() == btnWpaDeauth) {
			// user cilcked 'start handshake capture' OR 'stop handshake capture'
			Methods.wpaAttack();
		
		} else if (event.getSource() == btnWpaCrack) {
			// user clicked 'crack wpa using...' or 'stop cracking'
			switch(cboWpaCrackMethod.getSelectedIndex()) {
			case 1: // dictionary attack
				Methods.wpaCrackDictionary();
				break;
			case 2: // crunch passthrough
				Methods.wpaCrackPassthrough();
				break;
			case 3: // pyrit
				Methods.wpaCrackPyrit();
				break;
			case 4: // wordlist generator
				Methods.wpaCrackWordlist();
				break;
			case 5: // online wpa cracker
				Methods.wpaCrackOnline();
				break;
			}
			
		} else if (event.getSource() == btnWepTestinj) {
			// user clicked 'test injection'
			Methods.testInjection();
		
		} else if (event.getSource() == btnKeyTank) {
			// user clicked 'key tank' button
			GuiPWTank gpwt = new GuiPWTank("grim wepa | key tank");
			Main.guiWindow.setVisible(false);
		
		} else if (event.getSource() == btnInstall) {
			// user clicked 'install' button
			Main.guiWindow.setVisible(false);
			GuiInstall gi = new GuiInstall("grim wepa | install");
		} // else if (event.getSource() == btnMyButton) {
		  // JOptionPane.showMessageDialog(null, "You clicked me!");
	}
	
	/** event method; ran whenever user clicks an item in the 'tabTargets' list;
		aka the list of access points (bssid, ssid, enc, power, etc);<p>
		event is occurring because we set a hook on the control in buildControls()
		
		@see Gui#buildControls()
		@param e information on the event
	*/
	public void valueChanged(ListSelectionEvent e) {
		int row = tabTargets.getSelectedRow();
		// row is selected item index
		
		if (row < 0) {
			// if there is no selected item, disable wep/wpa options
			Methods.setEnableWEP(false);
			Methods.setEnableWPA(false);
			
			// paint the border around wpa/wep so it has no targets
			panWep.setBorder(BorderFactory.createTitledBorder(
					null, "wep",
					0, 0, null, Color.gray));
			panWpa.setBorder(BorderFactory.createTitledBorder(
					null, "wpa",
					0, 0, null, Color.gray));
			
			// set our global variables to blank
			Methods.currentBSSID = null;
			Methods.currentSSID = "";
			Methods.currentChannel = "";
			
			return;
		}
		
		// the code below this line runs if there IS a selected list item
		
		String temp; // temporary variable
		temp = (String)tabTargets.getValueAt(row, 3);
		if (temp.indexOf("WEP") >= 0) {
			// user clicked an AP that is WEP
			
			panWpa.setVisible(false);
			panWep.setVisible(true);
			Methods.setEnableWEP(true);
			
			// set global variables
			Methods.currentBSSID = (String)tabTargets.getValueAt(row, 4);
			Methods.currentSSID = (String)tabTargets.getValueAt(row, 1);
			Methods.currentSSID = Methods.currentSSID.trim();
			Methods.currentChannel = (String)tabTargets.getValueAt(row, 2);
			Methods.currentChannel = Methods.currentChannel.trim();
			
			// trim spaces
			while (Methods.currentChannel.substring(0, 1).equals(" ") == true)
				Methods.currentChannel = Methods.currentChannel.substring(1);
			
			// this loop checks through global String array 'Methods.cracked' for 
			// the current bssid and ssid combination. if we have already cracked this access point,
			// the key is displayed on the border of the panel.
			String key = "";
			for (int i = 0; i < Methods.cracked.length; i++) {
				if (Methods.cracked[i].indexOf(Methods.currentBSSID + "(" + Methods.currentSSID + ")") >= 0) {
					key = Methods.cracked[i].substring(Methods.currentBSSID.length() + Methods.currentSSID.length() + 2);
					if (!Methods.getAscii(key).equals("n/a"))
						key += " (" + Methods.getAscii(key) + ")";
					key = " | key: " + key;
					break;
				}
			}
			
			// this loop clears the 'clients' combobox, 
			//   then goes through the global String array 'Methods.clients' looking for the
			//   current access point, if it finds clients associated with the current AP, it adds them to the list.
			cboWepClients.removeAllItems();
			for (int i = 0; i < Methods.clients.length; i++) {
				if (Methods.clients[i].indexOf(Methods.currentBSSID) >= 0) {
					String[] tempClients = Methods.clients[i].split(",");
					for (int j = 1; j < tempClients.length; j++)
						cboWepClients.addItem(tempClients[j]);
					break;
				}
			}
			// if there are no clients, let the user know (and have at least one item in the combobox)
			if (cboWepClients.getItemCount() == 0)
				cboWepClients.addItem("[no clients found]");
			
			// build the border
			panWep.setBorder(BorderFactory.createTitledBorder(
						null, "wep | targeting '" + (String)tabTargets.getValueAt(row,4) + "'" + key, 
						0, 0, null, 
						Methods.getColor(cboColors.getSelectedIndex())));
			
		} else if (temp.indexOf("WPA") >= 0) {
			// user clicked an AP that is WPA
			// these options are basically the same as WEP, so i won't comment them.
			panWep.setVisible(false);
			panWpa.setVisible(true);
			Methods.setEnableWPA(true);
			
			Methods.currentBSSID = (String)tabTargets.getValueAt(row,4);
			Methods.currentSSID = (String)tabTargets.getValueAt(row, 1);
			Methods.currentSSID = Methods.currentSSID.trim();
			Methods.currentChannel = (String)tabTargets.getValueAt(row, 2);
			while (Methods.currentChannel.substring(0, 1).equals(" ") == true) {
				Methods.currentChannel = Methods.currentChannel.substring(1);
			}
			
			String key = "";
			if (Methods.cracked.length >= 1 && Methods.cracked[0].length() > 1) {
				for (int i = 0; i < Methods.cracked.length; i++) {
					if (Methods.cracked[i].startsWith(Methods.currentBSSID + "(" + Methods.currentSSID + ")") == true) {
						key = " | key: " + Methods.cracked[i].substring(Methods.currentBSSID.length() +
										Methods.currentSSID.length() + 2);
						break;
					}
				}
			}
			
			cboWpaClients.removeAllItems();
			for (int i = 0; i < Methods.clients.length; i++) {
				if (Methods.clients[i].indexOf(",") > 0 && 
					Methods.clients[i].substring(0, Methods.clients[i].indexOf(",")).equals(Methods.currentBSSID)) {
					String[] tempClients = Methods.clients[i].split(",");
					for (int j = 1; j < tempClients.length; j++)
						cboWpaClients.addItem(tempClients[j]);
					break;
				}
			}
			if (cboWpaClients.getItemCount() == 0)
				cboWpaClients.addItem("[no clients found]");
			
			panWpa.setBorder(BorderFactory.createTitledBorder(
				null, "wpa | targeting '" + (String)tabTargets.getValueAt(row,4) + "'" + key, 
				0, 0, null, Methods.getColor(cboColors.getSelectedIndex())));
			
		} else {
			// access point contains neither 'WPA' or 'WEP'... must be a dud
		}
		
		// make it pretty, lest 'panWpa' appears all jacked up (lower on the form)
		super.doLayout();
	}
	
	/** window closing event; code is ran when user attempts to close window;<p>
		we added a window listener in the contructor of this form
		addWindowListener() was used in the Gui() method
		@see Gui#Gui(String)
		@see Methods#saveSettings
		@param e info on the event
	*/
	public void windowClosing(WindowEvent e) {
		// if our variable 'putInMonitorMode' is not blank, that means we put a device into
		//    monitor mode earlier, and the user might want to put it back now that they are exiting
		if (!Methods.putInMonitorMode.equals("") && Methods.isValidDriver(Methods.putInMonitorMode)) {
			// device we put into monitor mode is still in monitor mode
			
			// ask user if they want to take the device out of monitor mode
			if (JOptionPane.showConfirmDialog(
				null,
				"The device '" + Methods.putInMonitorMode + "' is still in Monitor Mode.\n\n" +
				"Would you like to take this device out of monitor mode?",
				"grim wepa | exiting",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				
				// hide form before running a command (window disappears faster)
				this.setVisible(false);
				
				// take device out of monitor mode
				Methods.readExec("airmon-ng stop " + Methods.putInMonitorMode);
			}
		}
		
		// hide the window
		this.setVisible(false);
		// save settings
		Methods.saveSettings();
		
		// if program is still scanning for targets...
		if (!btnTargets.getLabel().equals("refresh targets") && Methods.targetScan.t.isAlive()) {
			btnTargets.setLabel("refresh targets");
			try {
				Methods.targetScan.t.join();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		// need to add code to check all global Process variables
		// attempt to close any processes that are still running
		
		// get rid of the form
		dispose();
		
		// completely exit this program
		System.exit(0);
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	
	/** enables or disables [almost] all components depending on given value
		this is so the user cannot click more than one option at a given time.
		@param en what to set every components 'getEnabled()' value to
	*/
	public static void setEnable(boolean en) {
		// install button
		btnInstall.setEnabled(en);
		
		btnWepDeauth.setEnabled(false);
		// Drivers list
		cboDrivers.setEnabled(en);
		btnDrivers.setEnabled(en);
		// Targets list
		tabTargets.setEnabled(en);
		// Refresh button & channels
		btnTargets.setEnabled(en);
		chkChannel.setEnabled(en);
		sldChannel.setEnabled(en);
		btnKeyTank.setEnabled(en);
		if (en == true && chkChannel.isSelected() == true)
			sldChannel.setEnabled(false);
		// WEP panel
		if (Methods.currentBSSID != null) {
			panWep.setEnabled(en);
			cboWepAttack.setEnabled(en); // combobox
			chkWepClient.setEnabled(en); // check
			cboWepClients.setEnabled(en);   // combo
			if (en == true && chkWepClient.isSelected() == false)
				cboWepClients.setEnabled(false);
			btnWepAttack.setEnabled(en);
			btnWepCrack.setEnabled(en);
			lblWepAttack.setEnabled(en);
			lblWepIvs.setEnabled(en);
			btnWepTestinj.setEnabled(en);
			sldWepInjection.setEnabled(en);
			lblWepInjection.setEnabled(en);
		}

		// WPA panel
		panWpa.setEnabled(en);
		cboWpaCrackMethod.setEnabled(en);
		btnWpaCrack.setEnabled(en);
		chkWpaClients.setEnabled(en); //check
		cboWpaClients.setEnabled(en);    //combo
		if (en == true && chkWpaClients.isSelected() == false)
			cboWpaClients.setEnabled(false);
		btnWpaDeauth.setEnabled(en);     //button
		txtWpaTimeout.setEnabled(en);    //text
		// labels for iface, chan, timeouts
		lblIface.setEnabled(en);
		lblChannel.setEnabled(en);
		lblTargetTimeout.setEnabled(en);
		lblWpaTimeout.setEnabled(en);
		txtTargetTimeout.setEnabled(en);
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/