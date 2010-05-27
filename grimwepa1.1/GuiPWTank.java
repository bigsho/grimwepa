/** key tank
	displays previously-cracked keys for the user to access and/or signon
*/

// gui libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

// for file info
import java.io.File;

// for process errors
import java.io.IOException;

// For listening to events
import java.util.EventListener;

public class GuiPWTank extends Frame implements WindowListener, ActionListener, EventListener, ListSelectionListener {
	/** scrollpane to keep list inside of*/
	public JScrollPane			scrTank;
	/** list of cracked accts*/
	public JTable				tabTank;
	/** DTM for list (much easier to add/remove)*/
	public DefaultTableModel	dtmTank;
	
	/** signon button*/
	public Button				btnSignon;
	/** to display hex version of selected item's password*/
	public JTextField			txtPWHex;
	/** to display ascii version of selected item's password*/
	public JTextField			txtPWAscii;
	/** remove item from cracked pwlist*/
	public Button				btnRemove;
	
	/** status bar*/
	public JLabel				lblStatus;
	
	/** constructor, builds key tank form, loads passwords into the list
		@param title title of this window
	*/
	public GuiPWTank(String title) {
		super(title);
		
		this.setSize(500, 245);
		this.setFont(new Font("Default", Font.BOLD, 11));
		
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension w = this.getSize();
		this.setLocation((ss.width - w.width ) / 2, (ss.height - w.height ) / 2);
		
		setResizable(false);
		
		this.setForeground(Color.red);
		this.setBackground(Color.black);
		
		setLayout(new FlowLayout());
		
		addWindowListener(this);
		
		buildControls();
		
		doLayout();
		
		loadPasswords();
		
		this.setVisible(true);
	}
	
	/** creates controls, adds them to the form
	*/
	public void buildControls() {
		Font f = new Font("Default", Font.BOLD, 11);
		Color col = Methods.getColor(Gui.cboColors.getSelectedIndex());
		
		dtmTank = new DefaultTableModel(null, new String[] 
							{"network name", "bssid", "enc", "password", "date cracked", "more info"});
		tabTank = new JTable(dtmTank) { 
			public TableCellRenderer getCellRenderer( int row, int col ) {
				TableCellRenderer renderer = super.getCellRenderer(row,col);
				((JLabel)renderer).setHorizontalAlignment( SwingConstants.CENTER );
				return renderer;
				
			}
			/*public boolean isCellEditable(int rowIndex, int vColIndex) { 
				return false; 
			}*/
		};
		
		tabTank.setAutoCreateColumnsFromModel(false); 
		tabTank.setAutoCreateRowSorter(true); // turn sorting on
		tabTank.setBackground(Color.black);
		tabTank.setForeground(col);
		tabTank.setPreferredScrollableViewportSize(new Dimension(475,140));
		tabTank.setFillsViewportHeight(true);
		tabTank.getColumnModel().getColumn(0).setPreferredWidth(100);	// ssid
		tabTank.getColumnModel().getColumn(1).setPreferredWidth(0);		// bssid
		tabTank.getColumnModel().getColumn(2).setPreferredWidth(20);	// encryption
		tabTank.getColumnModel().getColumn(3).setPreferredWidth(75);	// password
		tabTank.getColumnModel().getColumn(4).setPreferredWidth(75);	// date
		tabTank.getColumnModel().getColumn(5).setPreferredWidth(75);	// more info
		tabTank.setFont(f);
		tabTank.getSelectionModel().addListSelectionListener(this); // listen for selection changes
		
		tabTank.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);
		tabTank.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);
		tabTank.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(0);
		tabTank.getTableHeader().getColumnModel().getColumn(1).setResizable(false);
		
		scrTank = new JScrollPane(tabTank); // scroller
		add(scrTank);
		
		// BOTTOM ROW
		
		btnSignon = new Button("sign on");
		btnSignon.setFont(f);
		btnSignon.setBackground(col);
		btnSignon.setForeground(Color.black);
		add(btnSignon);
		btnSignon.addActionListener(this);
		
		f = new Font("Default", Font.BOLD, 12);
		txtPWHex = new JTextField("[hex password]");
		txtPWHex.setFont(f);
		txtPWHex.setBackground(Color.black);
		txtPWHex.setForeground(col);
		txtPWHex.setPreferredSize(new Dimension(150, 20));
		// txtPWHex.setEditable(false);
		txtPWHex.setHorizontalAlignment(JTextField.CENTER);
		add(txtPWHex);
		
		txtPWAscii = new JTextField("[ascii password]");
		txtPWAscii.setFont(f);
		txtPWAscii.setBackground(Color.black);
		txtPWAscii.setForeground(col);
		txtPWAscii.setPreferredSize(new Dimension(150, 20));
		// txtPWAscii.setEditable(false);
		txtPWAscii.setHorizontalAlignment(JTextField.CENTER);
		add(txtPWAscii);
		
		btnRemove = new Button("remove");
		btnRemove.setFont(f);
		btnRemove.setBackground(col);
		btnRemove.setForeground(Color.black);
		add(btnRemove);
		btnRemove.addActionListener(this);
		
		lblStatus = new JLabel(" select an account to view more info");
		lblStatus.setFont(f);
		lblStatus.setBackground(Color.black);
		lblStatus.setForeground(col);
		lblStatus.setPreferredSize(new Dimension(475, 18));
		lblStatus.setBorder(BorderFactory.createLineBorder(col));
		add(lblStatus);
	}
	
	/** displays text in this window's status bar
		@param txt current status to be displayed
	*/
	public void stat(String txt) {
		lblStatus.setText(" " + txt);
	}
	
	/** loads passwords from 'pass.txt' into the list
	*/
	public void loadPasswords() {
		// load passwords to list using 'pass.txt'
		String line[] = Methods.readFile(Methods.grimwepaPath + "pass.txt");
		for (int i = 0; i < line.length; i++) {
			if (line[i].equals(""))
				continue;
			
			String enc = line[i].substring(0, 3);
			line[i] = line[i].substring(line[i].indexOf(": ") + 2);
			
			if (line[i].indexOf("(") < 0)
				continue;
			String bssid = line[i].substring(0, line[i].indexOf("("));
			line[i] = line[i].substring(bssid.length() + 1);
			
			String ssid = line[i].substring(0, line[i].indexOf("\t") - 1);
			line[i] = line[i].substring(ssid.length() + 7);
			
			String pw;
			if (line[i].indexOf("|||") >= 0) {
				pw = line[i].substring(0, line[i].indexOf("|||"));
				line[i] = line[i].substring(pw.length() + 3);
			} else {
				pw = line[i];
				line[i] = "";
			}
			
			String date = "n/a", info = "n/a";
			if (line[i].indexOf("|||") >= 0) {
				// if we have more info...
				date = line[i].substring(0, line[i].indexOf("|||"));
				line[i] = line[i].substring(date.length() + 3);
				
				info = line[i];
			} else {
				date = line[i];
				if (date.equals(""))
					date = "n/a";
				
				info = "n/a";
			}
			
			// System.out.println(enc + "/" + bssid + "/" + ssid + "/" + pw + "/" + date + "/" + info);
			dtmTank.addRow(
				new String[]{
						ssid,
						bssid,
						enc,
						pw,
						date,
						info
				}
			);
		}
	}
	
	/** event that occurs whenever an action happens
		@param event information about what caused the event
	*/
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnRemove) {
			// remove the selected item
			int index = tabTank.getSelectedRow();
			if (index == -1) // no items selected
				return;
			
			dtmTank.removeRow(index);
			
			if (index < tabTank.getRowCount())
				tabTank.setRowSelectionInterval(index, index);
			else {
				if (index > 0) {
					index--;
					tabTank.setRowSelectionInterval(index, index);
				}
			}
			saveTank();
		} else if (event.getSource() == btnSignon) {
			// signon to selected item
			int index = tabTank.getSelectedRow();
			if (index == -1) // no items selected
				return;
			
			String driver = (String)Gui.cboDrivers.getSelectedItem();
			String enc = (String)tabTank.getValueAt(index, 2);
			String ssid = (String)tabTank.getValueAt(index, 0);
			String key = (String)tabTank.getValueAt(index, 3);
			
			// check the encryption
			if (enc.equals("WEP")) {
				// signon to wep
				signonWep(ssid, key);
			} else if (enc.equals("WPA")) {
				// signon to wpa
				signonWpa(ssid, key);
			}
			
		}
	}
	
	/** executes a command directly, waits for process to finish, then returns
		@param command command to execute
		@see #signonWep(String, String)
		@see #signonWpa(String, String)
	*/
	public void exec(String command) {
		Process proExec = null;
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command);
			proExec = Runtime.getRuntime().exec(command);
			proExec.waitFor();
		} catch (IOException ioe) {
		} catch (InterruptedException ie) {}
	}
	
	/** executes a command directly, doesn't wait for process to finish, returns
		@param command command to execute
		@see #signonWpa(String, String)
	*/
	public void execNoWait(String command) {
		Process proExec = null;
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command);
			proExec = Runtime.getRuntime().exec(command);
			// proExec.waitFor();
		} catch (IOException ioe) {}
	}
	
	/** signs onto WEP-encrypted access point
		@param ssid name of access point (case sensitive)
		@param key	required password (key) for access point
		@see #actionPerformed(ActionEvent)
	*/
	public void signonWep(String ssid, String key) {
		String driver = getDriver(ssid);
		if (driver.equals("null"))
			return;
		Process proSignon = null;
		
		stat("putting device down...");
		exec("ifconfig " + driver + " down");
		
		stat("entering wireless settings...");
		exec("iwconfig " + driver + " mode Managed");
		exec("iwconfig " + driver + " essid \"" + ssid + "\"");
		exec("iwconfig " + driver + " key " + key + "");
		
		stat("putting device up...");
		exec("ifconfig " + driver + " up");
		
		stat("waiting for dhclient to connect");
		
		// display the dhclient window to the user so they can see if it gets hung up
		exec(	"xterm" + 
				" -fg " + (String)Gui.cboColors.getSelectedItem() + 
				" -bg black" +
				" -geom 100x15+0+0" + 
				" -T gw-dhclient" +
				" -e" + 
				" dhclient " + driver + ""
		);
		
		// assume the worst
		stat("unable to connect; out of range / invalid password?");
		
		// loop through 'ifconfig's output to see if they are connected
		String output[] = Methods.readExec("ifconfig " + driver);
		for (int i = 0; i < output.length; i++) {
			
			// if they are connected...
			if (output[i].indexOf("Bcast:") >= 0) {
				stat("connected!");
				break;
			}
		}
		
	}
	
	/** finds type of WPA (1, 2, 1+2) using iwlist
		@param driver name of driver (iface) to connect using
		@param ssid name of access point
		@return "WPA1", "WPA2", "WPA1+2", or "n/a", depending on what iwlist finds
	*/
	public String wpaType(String driver, String ssid) {
		String output[] = Methods.readExec("iwlist " + driver + " scan");
		
		boolean found = false;
		int count = 0;
		
		for (int i = 0; i < output.length; i++) {
			if (Methods.verbose)
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
		@see #actionPerformed(ActionEvent)
	*/
	public void signonWpa(String ssid, String key) {
		String driver = getDriver(ssid);
		if (driver.equals("null"))
			return;
		
		String type = wpaType(driver, ssid);
		if (Methods.verbose)
			System.out.println("Type: " + type);
		
		// get rid of any wpa_supplicant processes
		exec("killall wpa_supplicant");
		
		String conf = 	"ctrl_interface=/var/run/wpa_supplicant\n" +
						"\n" +
						"network={\n" +
						"\tssid=\"" + ssid + "\"\n" +
						"\tscan_ssid=0\n";
		
		if (type.equals("WPA1")) {
			// WPA1
			if (Methods.verbose)
				System.out.println("Signing on to WPA1");
			conf += 		"\tproto=WPA\n" +
						"\tkey_mgmt=WPA-PSK\n" +
						"\tpsk=\"" + key + "\"\n" +
						"\tpairwise=TKIP\n" +
						"\tgroup=TKIP\n" +
						"}\n";
		} else {
			// WPA2
			if (Methods.verbose)
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
		execNoWait("wpa_supplicant -Dwext -i" + driver + " -c/etc/wpa_supplicant.conf -B");
		
		// display the dhclient window to the user so they can see if it gets hung up
		exec(	"xterm" + 
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
	
	/** asks user for device to use to connect to the access point with; 
		returns the user's selection, or "null" if cancelled
		@param ssid default device name (used for the attack)
		@see #signonWep(String, String)
		@see #signonWpa(String, String)
	*/
	public String getDriver(String ssid) {
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
	
	/** saves current password list to the file
		only runs when the user has removed an item
	*/
	public void saveTank() {
		String output = "", cracked = "";
		for (int row = 0; row < tabTank.getRowCount(); row++) {
			// for Methods.cracked array that holds the currently cracked items
			cracked += 	tabTank.getValueAt(row, 1) + 
						"(" + tabTank.getValueAt(row, 0) + ")" + 
						tabTank.getValueAt(row, 3) + "|||";
			
			// for the file 'pass.txt'.
			output += (String)tabTank.getValueAt(row, 2) + "\t";		// encryption
			output += "B(SSID): " + (String)tabTank.getValueAt(row, 1);	// bssid
			output += "(" + (String)tabTank.getValueAt(row, 0);			// ssid
			output += ")\tKEY: " + (String)tabTank.getValueAt(row, 3);	// key
			output += "|||" + (String)tabTank.getValueAt(row, 4);		// date
			output += "|||" + (String)tabTank.getValueAt(row, 5);			// more info
			output += "\n";
			
		}
		
		// get rid of trailing |||
		if (cracked.endsWith("|||"))
			cracked = cracked.substring(0, cracked.length() - 3);
		Methods.cracked = cracked.split("|||");
		
		// write passes to file
		Methods.writeFile(Methods.grimwepaPath + "pass.txt", output);
	}
	
	/** event for when user clicks an item
		@param e info on the event
	*/
	public void valueChanged(ListSelectionEvent e) {
		int row = tabTank.getSelectedRow();
		if (row < 0)
			return;
		
		String pw = (String)tabTank.getValueAt(row, 3);
		txtPWHex.setText(pw);
		if ( ((String)tabTank.getValueAt(row, 2)).equals("WEP") )
			txtPWAscii.setText(Methods.getAscii(pw));
		else
			txtPWAscii.setText("[no ascii for wpa]");
	}
	
	/** event for when window is closing
		@param e info on the event
	*/
	public void windowClosing(WindowEvent e) {
		setVisible(false);
		Main.guiWindow.setVisible(true);
		Methods.loadCracked();
		dispose();
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/