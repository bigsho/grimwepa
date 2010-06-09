/** GUI for crunch passthrough window
	<p>
	generates wordlists via crunch and 'passes-through' passwords to aircrack/pyrit
*/

// libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
// io stuff
import java.io.File;
import java.io.IOException;
// for events
import java.util.EventListener;

public class GuiCrunchPass extends Frame implements WindowListener, ActionListener, EventListener {
	/** holds actual characters in the char sets*/
	public String[] charset;
	
	// first row, path to the program 'crunch'
	/** label showing 'path to crunch'*/
	public JLabel 		lblCrunch;
	/** holds path to crunch*/
	public JTextField 	txtCrunch;
	/** browse for path to crunch button '...' */
	public Button 		btnCrunch;
	
	// second row, length of password items
	/** starting length label*/
	public JLabel 		lblLenStart;
	/** starting length txtbox*/
	public JTextField 	txtLenStart;
	/** ending length label*/
	public JLabel		lblLenEnd;
	/** ending length txtbox*/
	public JTextField	txtLenEnd;
	
	// third row, charset
	/** label showing 'charset'*/
	public JLabel		lblChars;
	/** combobox holding names of charsets*/
	public JComboBox	cboChars;
	/** preview of what character set is*/
	public JTextField	txtCharPreview = null;
	
	// fourth row, cracking
	/** label showing 'path to cap file'*/
	public JLabel		lblCap;
	/** path to cap file txtbox*/
	public JTextField	txtCap;
	/** crack button*/
	public static Button btnCrack;
	
	// fifth row, pyrit option
	/** checkbox to use pyrit instead of aircrack*/
	public JCheckBox	chkPyrit;
	
	/** constructor, builds form, loads settings for crunch passthrough,
		if crunch is not found, prompts user to choose directory or download it
		@param title title of crunch passthrough window frame
	*/
	public GuiCrunchPass(String title) {
		super(title);
		
		Methods.proCrack = null;
		
		charset = null;
		
		this.setSize(350, 180);
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
		
		loadSettings();
		
		this.setVisible(true);
		
		// if crunch isn't found...
		if (!Methods.fileExists(txtCrunch.getText() + "/crunch")) {
			
			if (Methods.fileExists("/usr/bin/crunch")) {
				txtCrunch.setText("/usr/bin");
				return;
			}
			
			// set up options for user to select
			String options[] = new String[]{
					"locate crunch myself",
					"attempt to download crunch via apt-get",
					"do nothing, go back"
				};
			
			String choice = (String)JOptionPane.showInputDialog(
					null,
					"the wordlist-generating program 'crunch' was not found \n" +
					"in the default directory '/pentest/passwords/crunch/'.\n" +
					"crunch is *required* for passthrough-cracking to work!\n\n" +
					"please select what you would like to do:",
					"grim wepa | crunch passthrough error",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]
				);
			
			if (choice == null || choice.equals("do nothing, go back")) {
				// go back to main form
				Main.guiWindow.setVisible(true);
				this.dispose();
				
			} else if (choice.equals("locate crunch myself")) {
				// open file dialog chooser
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("/pentest/passwords/crunch/"));
				int retval = fc.showOpenDialog(null);
				if (retval == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					
					String s = f.getPath();
					
					if (s.endsWith("crunch"))
						s = s.substring(0, s.length() - 7);
					txtCrunch.setText(s);
				}
				
			} else {
				// download via apt-get
				try {
					Runtime.getRuntime().exec(
						"xterm -fg" +
						" " + (String)Gui.cboColors.getSelectedItem() +
						" -bg black" +
						" -geom 100x15+0+0" +
						" -T gw-installcrunch" +
						" -hold" +
						" -e" +
						" apt-get install crunch");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	/** builds controls for the form
		@see GuiCrunchPass
	*/
	public void buildControls() {
		Color col = Methods.getColor(Gui.cboColors.getSelectedIndex());
		
		Font f = new Font("Default", Font.BOLD, 11);
		
		lblCrunch = new JLabel("crunch path:");
		lblCrunch.setFont(f);
		lblCrunch.setBackground(Color.black);
		lblCrunch.setForeground(col);
		add(lblCrunch);
		
		txtCrunch = new JTextField("/pentest/passwords/crunch");
		txtCrunch.setFont(f);
		txtCrunch.setBackground(Color.black);
		txtCrunch.setForeground(col);
		txtCrunch.setPreferredSize(new Dimension(225, 18));
		add(txtCrunch);
		
		btnCrunch = new Button("...");
		btnCrunch.setFont(f);
		btnCrunch.setBackground(col);
		btnCrunch.setForeground(Color.black);
		btnCrunch.addActionListener(this);
		add(btnCrunch);
		
		lblLenStart = new JLabel("starting word length:");
		lblLenStart.setFont(f);
		lblLenStart.setBackground(Color.black);
		lblLenStart.setForeground(col);
		add(lblLenStart);
		
		txtLenStart = new JTextField("8");
		txtLenStart.setFont(f);
		txtLenStart.setBackground(Color.black);
		txtLenStart.setForeground(col);
		txtLenStart.setPreferredSize(new Dimension(30, 18));
		txtLenStart.setHorizontalAlignment(JTextField.CENTER);
		add(txtLenStart);
		
		lblLenEnd = new JLabel("    ending word length:");
		lblLenEnd.setFont(f);
		lblLenEnd.setBackground(Color.black);
		lblLenEnd.setForeground(col);
		add(lblLenEnd);
		
		txtLenEnd = new JTextField("8");
		txtLenEnd.setFont(f);
		txtLenEnd.setBackground(Color.black);
		txtLenEnd.setForeground(col);
		txtLenEnd.setPreferredSize(new Dimension(30, 18));
		txtLenEnd.setHorizontalAlignment(JTextField.CENTER);
		add(txtLenEnd);
		
		lblChars = new JLabel("charset:");
		lblChars.setFont(f);
		lblChars.setBackground(Color.black);
		lblChars.setForeground(col);
		add(lblChars);
		
		cboChars = new JComboBox();
		cboChars.setFont(f);
		cboChars.setBackground(Color.black);
		cboChars.setForeground(col);
		cboChars.setPreferredSize(new Dimension(150, 20));
		loadCharset();
		cboChars.addActionListener(this);
		add(cboChars);
		
		txtCharPreview = new JTextField(charset[0]);
		txtCharPreview.setPreferredSize(new Dimension(300, 18));
		txtCharPreview.setFont(f);
		txtCharPreview.setBackground(Color.black);
		txtCharPreview.setForeground(col);
		txtCharPreview.setEditable(false);
		add(txtCharPreview);
		
		lblCap = new JLabel("cap file:");
		lblCap.setFont(f);
		lblCap.setBackground(Color.black);
		lblCap.setForeground(col);
		add(lblCap);
		
		// Methods.grimwepaPath + "wpa-01.cap"
		txtCap = new JTextField(Methods.grimwepaPath + "wpa-01.cap");
		txtCap.setPreferredSize(new Dimension(150, 18));
		txtCap.setFont(f);
		txtCap.setBackground(Color.black);
		txtCap.setForeground(col);
		add(txtCap);
		
		btnCrack = new Button("start cracking");
		btnCrack.setFont(f);
		btnCrack.setBackground(col);
		btnCrack.setForeground(Color.black);
		btnCrack.addActionListener(this);
		add(btnCrack);
		
		chkPyrit = new JCheckBox("use pyrit 0.2.5 to crack (experimental/buggy)");
		chkPyrit.setFont(f);
		chkPyrit.setBackground(Color.black);
		chkPyrit.setForeground(col);
		//chkPyrit.setPreferredSize(new Dimension(250, 18));
		add(chkPyrit);
	}
	
	/** loads charset names into cboChars combobox,
		and the actual charsets into a String array called 'charset'
	*/
	public void loadCharset() {
		cboChars.addItem("numeric");
		cboChars.addItem("numeric-space");
		cboChars.addItem("ualpha");
		cboChars.addItem("ualpha-space");
		cboChars.addItem("ualpha-numeric");
		cboChars.addItem("ualpha-numeric-space");
		cboChars.addItem("ualpha-numeric-symbol14");
		cboChars.addItem("ualpha-numeric-symbol14-space");
		cboChars.addItem("ualpha-numeric-all");
		cboChars.addItem("ualpha-numeric-all-space");
		cboChars.addItem("lalpha");
		cboChars.addItem("lalpha-space");
		cboChars.addItem("lalpha-numeric");
		cboChars.addItem("lalpha-numeric-space");
		cboChars.addItem("lalpha-numeric-symbol14");
		cboChars.addItem("lalpha-numeric-symbol14-space");
		cboChars.addItem("lalpha-numeric-all");
		cboChars.addItem("lalpha-numeric-all-space");
		cboChars.addItem("mixalpha");
		cboChars.addItem("mixalpha-space");
		cboChars.addItem("mixalpha-numeric");
		cboChars.addItem("mixalpha-numeric-space");
		cboChars.addItem("mixalpha-numeric-symbol14");
		cboChars.addItem("mixalpha-numeric-symbol14-space");
		cboChars.addItem("mixalpha-numeric-all");
		cboChars.addItem("mixalpha-numeric-all-space");
		cboChars.addItem("custom");
		
		charset = new String[]{
			"0123456789",
			"0123456789 ",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ ",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+= ",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/ ",
			"abcdefghijklmnopqrstuvwxyz",
			"abcdefghijklmnopqrstuvwxyz ",
			"abcdefghijklmnopqrstuvwxyz0123456789",
			"abcdefghijklmnopqrstuvwxyz0123456789 ",
			"abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=",
			"abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+= ",
			"abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/",
			"abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/ ",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+= ",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/ ",
			"[type your own charset here]"
		};
		
		// load charset from file:
		/*if (!Methods.fileExists("charset.lst"))
			Methods.extractFile("charset.lst");
		charset = Methods.readFile("charset.lst");
		for (int i = 0; i < charset.length; i++) {
			if (!charset[i].equals("")) {
				String temp = charset[i].substring(0, charset[i].indexOf("="));
				temp = temp.trim();
				cboChars.addItem("'" + temp + "'");
				charset[i] = charset[i].substring(charset[i].indexOf("[") + 1, charset[i].length() - 1);
			}
		}*/
		
	}
	
	/** event that is called when an item that we are listening to is acted upon
		@param event info on event
	*/
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnCrunch) {
			// user clicked 'crunch' button; select location of crunch
		JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(txtCrunch.getText()));
			int retval = fc.showOpenDialog(null);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				
				String s = f.getPath();
				
				if (s.endsWith("crunch"))
					s = s.substring(0, s.length() - 7);
				txtCrunch.setText(s);
			}
			
		} else if(event.getSource() == cboChars) {
			// user clicked a charset, display it for them to see
			if (charset != null && txtCharPreview != null)
				txtCharPreview.setText(charset[cboChars.getSelectedIndex()]);
			
			if (cboChars.getSelectedIndex() == cboChars.getItemCount() - 1)
				txtCharPreview.setEditable(true);
			else
				txtCharPreview.setEditable(false);
			
		} else if(event.getSource() == btnCrack) {
			// user clicked crack
			if (btnCrack.getLabel().equals("start cracking")) {
				// start cracking!
				
				// make sure user entered valid data...
				if (!crackCheck())
					return;
				
				btnCrack.setLabel("stop cracking");
				
				// command is the crazy command-line string array we need to execute the command.
				// java is usually cool about executing command-line, but when you have a pipe,
				// we have to use an array where the first two items are /bin/sh and -c, the last
				// array item is the full command, no gaps!
				String command[] = new String[3];
				
				String crunchPath = txtCrunch.getText().replaceAll(" ", "\\\\ ");
				String gwPath = Methods.grimwepaPath.replaceAll(" ", "\\\\ ");
				
				Methods.extractFile("charset.lst");
				
				// using aircrack-ng
				if (!chkPyrit.isSelected()) {
					command[0] = "/bin/sh";
					command[1] = "-c";
					command[2] = "xterm" +
								" -fg "+ (String)Gui.cboColors.getSelectedItem() +
								" -bg black" +
								" -T gw-passthrough" +
								" -geom 100x25+0+0" +
								" -e " +
								
								// enclosed in quotes
								"\"" +
								"" + crunchPath + "/./crunch" +
								" " + txtLenStart.getText() +
								" " + txtLenEnd.getText();
					
					// check if they want a cutsom charset
					if (cboChars.getSelectedIndex() == cboChars.getItemCount() - 1) {
						txtCharPreview.setText(txtCharPreview.getText().replaceAll("\"", "\\\\\""));
						command[2] += " " + txtCharPreview.getText() + "";
					} else {
						command[2] += " -f " + gwPath+ "charset.lst " +
						" " + (String)cboChars.getSelectedItem();
					}
					
					// pipe
						command[2] += " | ";
					
					// command for aircrack
						command[2] += "aircrack-ng" +
									" -w-" +
									" -l " + gwPath + "wpacracked.txt" +
									" -b " + Methods.currentBSSID +
									" " + (String)(txtCap.getText()).replaceAll(" ", "\\\\ ") + "\"";
				
				// using pyrit
				} else {
					// if they want to use pyrit...
					// .. may god have mercy on their soul
					
					command[0] = "/bin/sh";
					command[1] = "-c";
					command[2] = "\"" + txtCrunch.getText() + "/./crunch\"" +
								 " " + txtLenStart.getText() +
								 " " + txtLenEnd.getText();
					
					// check if they want a cutsom charset
					if (cboChars.getSelectedIndex() == cboChars.getItemCount() - 1) {
						txtCharPreview.setText(txtCharPreview.getText().replaceAll("\"", "\\\\\""));
						command[2] += " " + txtCharPreview.getText() + "";
					} else {
						command[2] += " -f " + gwPath + "/charset.lst " +
						" " + (String)cboChars.getSelectedItem();
					}
					
					command[2] += " | ";
					
					// pyrit command
					command[2] += "pyrit" + 
									" -e \"" + Methods.currentSSID + "\"" +
									" -i -" +
									" -r \"" + txtCap.getText() + "\"" +
									" attack_passthrough " +
									"> " + 
									"\"" + Methods.grimwepaPath + "wpacracked.txt\"";
				}
				
				Methods.removeFile("wpacracked.txt");
				
				try {
					if (Methods.verbose)
						System.out.println("exec:\t" + command[2]);
					Methods.proCrack = Runtime.getRuntime().exec(command);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				
				threadWpaCracker twc = new threadWpaCracker();
				twc.t.start();
				
			// if we were already cracking...
			} else {
				// change label
				btnCrack.setLabel("start cracking");
				Methods.proCrack.destroy();
				threadWpaCracker.flag = true;
				
				Methods.readExec("killall aircrack-ng");
				Methods.readExec("rm \"" + Methods.grimwepaPath + "charset.lst\"");
			}
		}
	}
	
	/** makes sure all user-inputed values are correct; 
		returns false if any values are out of order, 
		and prompts the user to correct the mistakes
	*/
	public boolean crackCheck() {
		String msg = "";
		
		int x = 0;
		// txtLenStart
		try {
			x = Integer.parseInt(txtLenStart.getText());
		} catch (NumberFormatException nfe) {
			msg += "starting length must be numeric.\n";
			txtLenStart.setText("8");
			x = 8;
		}
		if (x < 8) {
			msg += "wpa passwords cannot be less than 8 characters long.\n";
			txtLenStart.setText("8");
		}
		
		int y = 0;
		// txtLenEnd
		try {
			y = Integer.parseInt(txtLenEnd.getText());
		} catch (NumberFormatException nfe) {
			msg += "ending length must be numeric.\n";
			txtLenStart.setText("8");
			y = 8;
		}
		if (y > 64) {
			msg += "wpa passwords cannot be more than 64 characters long.\n";
			txtLenStart.setText("64");
		} else if (y < 8) {
			msg += "wpa passwords cannot be less than 8 characters long.\n";
			txtLenEnd.setText("8");
		}
		// txtLenEnd must be bigger than txtLenStart
		if (x > y) {
			msg += "ending length must be greater than or equal to starting length.\n";
			txtLenEnd.setText(txtLenStart.getText());
		}
		
		// pyrit check
		if (!Methods.fileExists("/usr/bin/pyrit") && chkPyrit.isSelected()) {
			msg += "pyrit was not found. look for '/usr/bin/pyrit'. make a link there.\n";
			msg += "\tor install pyrit using 'apt-get install pyrit'\n";
		}
		
		// display errors
		if (!msg.equals("")) {
			JOptionPane.showMessageDialog(
				null,
				msg,
				"grim wepa | crunch passthrough error",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// if user doesn't have correct path to crunch...
		if (!Methods.fileExists(txtCrunch.getText() + "/crunch")) {
			msg += "the file 'crunch' was not found in directory '" + txtCrunch.getText() + "'.\n\n";
			msg += "if you don't have crunch, use the command 'apt-get install crunch'\n\n";
			msg += "would you like for grim wepa to run this command for you?";
			if (JOptionPane.showConfirmDialog(
				null,
				msg,
				"grim wepa | crunch passthrough error",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					try {
						Runtime.getRuntime().exec(
							"xterm -fg" +
							" " + (String)Gui.cboColors.getSelectedItem() +
							" -bg black" +
							" -geom 100x15+0+0" +
							" -T gw-crunchinstall" +
							" -hold" +
							" -e" +
							" apt-get install crunch");
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			}
			return false;
		}
		
		// made it this far, the information must be accurate & okay for crackin'
		return true;
	}
	
	/** loads this form's settings from the same .conf file as the main form
		<code>/etc/grimwepa.conf</code>
	*/
	public void loadSettings() {
		// get the text from the file, put into array
		String input[] = Methods.readFile("/etc/grimwepa.conf");
		boolean flag = false; // flag is true when we've passed all of the main form's settings
		for (int i = 0; i < input.length; i++) {
			// all of this form's settings are 6 or more characters long, substring would error otherwise
			if (flag && input[i].length() >= 6) {
				if (input[i].substring(0, 5).equals("lenst")) {
					txtLenStart.setText(input[i].substring(6));
				} else if(input[i].substring(0, 5).equals("lenen")) {
					txtLenEnd.setText(input[i].substring(6));
				} else if(input[i].substring(0, 5).equals("chars")) {
					int x = 0;
					try {
						Integer.parseInt(input[i].substring(6));
					} catch (NumberFormatException nfe) {}
					cboChars.setSelectedIndex(i);
				} else if(input[i].substring(0, 5).equals("cpath")) {
					txtCrunch.setText(input[i].substring(6));
				}
			}
			// if we hit our part of the settings file, set flag to true
			if (input[i].equals("[crunch]"))
				flag = true;
		}
	}
	
	/** save settings to same .conf file as main form.<p>
		takes original settings (for the main form) and re-writes them to the file,
		THEN writes this form's settings (so we don't lose the main form's settings)
	*/
	public void saveSettings() {
		String toStore = "";
		String input[] = Methods.readFile("/etc/grimwepa.conf");
		boolean flag = false;
		for (int i = 0; i < input.length; i++) {
			if (input[i].equals("[crunch]"))
				break;
			toStore += input[i] + "\n";
		}
		toStore += 	"[crunch]" +
					"\nlenst " + txtLenStart.getText() +
					"\nlenen " + txtLenEnd.getText() +
					"\nchars " + cboChars.getSelectedIndex() +
					"\ncpath " + txtCrunch.getText();
		Methods.writeFile("/etc/grimwepa.conf", toStore);
	}
	
	/** event called when user tries to close window
		@param e info on event
	*/
	
	public void windowClosing(WindowEvent e) {
		setVisible(false);
		Main.guiWindow.setVisible(true);
		
		saveSettings();
		
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