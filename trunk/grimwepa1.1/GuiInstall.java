/** Install screen;
	lets user select directory and install options,
	also has 'uninstall' option
*/

// Gui libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

import java.util.Scanner;

// IO libraries
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.OutputStream;
import java.io.IOException;

// jar/zip libraries for extracting from .JAR
import java.util.jar.*;
import java.util.zip.*;

// for getting filenames
import java.net.URISyntaxException;

// For listening to events
import java.util.EventListener;

public class GuiInstall extends Frame implements WindowListener, ActionListener, EventListener {
	/** shows 'install path:'*/
	public static JLabel 		lblPath;
	/** holds installation path*/
	public static JTextField 	txtPath;
	/** '...' browse for install path button*/
	public static Button		btnPath;
	
	/** 'create /usr/bin link' checkbox*/
	public static JCheckBox		chkUsrBin;
	/** create menu checkbox*/
	public static JCheckBox		chkMenu;
	/** create desktop link checkbox*/
	public static JCheckBox		chkDesktop;
	
	/** install button*/
	public static Button		btnInstall;
	/** uninstall button*/
	public static Button		btnUninstall;
	
	/** update button*/
	public static Button		btnUpdate;
	
	/** creates frame, sets properties, builds controls, sets frame visible
	*/
	public GuiInstall(String title) {
		super(title);
		
		this.setSize(250, 205);
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
		
		this.setVisible(true);
	}
	
	/** builds controls onto the form
	*/
	public void buildControls() {
		Font f = new Font("Default", Font.BOLD, 11);
		Color col = Methods.getColor(Gui.cboColors.getSelectedIndex());
		
		lblPath = new JLabel("install directory:");
		lblPath.setForeground(col);
		lblPath.setBackground(Color.black);
		add(lblPath);
		
		txtPath = new JTextField("/pentest/wireless/grimwepa/");
		txtPath.setForeground(col);
		txtPath.setBackground(Color.black);
		txtPath.setEditable(false);
		txtPath.setText(getInstallPath());
		add(txtPath);
		
		btnPath = new Button("...");
		btnPath.setBackground(col);
		btnPath.setForeground(Color.black);
		add(btnPath);
		btnPath.addActionListener(this);
		
		chkUsrBin = new JCheckBox("add to /usr/bin/");
		chkUsrBin.setForeground(col);
		chkUsrBin.setBackground(Color.black);
		chkUsrBin.setSelected(true);
		chkUsrBin.setEnabled(false);
		chkUsrBin.setPreferredSize(new Dimension(200, 18));
		add(chkUsrBin);
		
		chkMenu = new JCheckBox("add to system menu");
		chkMenu.setForeground(col);
		chkMenu.setBackground(Color.black);
		chkMenu.setSelected(true);
		chkMenu.setPreferredSize(new Dimension(200, 18));
		add(chkMenu);
		
		chkDesktop = new JCheckBox("add desktop shortcut");
		chkDesktop.setForeground(col);
		chkDesktop.setBackground(Color.black);
		chkDesktop.setSelected(true);
		chkDesktop.setPreferredSize(new Dimension(200, 18));
		add(chkDesktop);
		
		btnInstall = new Button("install");
		btnInstall.setForeground(Color.black);
		btnInstall.setBackground(col);
		add(btnInstall);
		btnInstall.addActionListener(this);
		
		btnUninstall = new Button("uninstall");
		btnUninstall.setForeground(Color.black);
		btnUninstall.setBackground(col);
		add(btnUninstall);
		btnUninstall.addActionListener(this);
		
		btnUpdate = new Button("check for updates");
		btnUpdate.setForeground(Color.black);
		btnUpdate.setBackground(col);
		add(btnUpdate);
		btnUpdate.addActionListener(this);
	}
	
	/** event that is called when user clicks a button
		@param event info on the event
	*/
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnPath) {
			choosePath();
		} else if (event.getSource() == btnInstall) {
			install();
		} else if (event.getSource() == btnUninstall) {
			uninstall();
		} else if (event.getSource() == btnUpdate) {
			checkUpdates();
		}
	}
	
	/** cancels everything, closes form, loads main form
	*/
	public void cancel() {
		Main.guiWindow.setVisible(true);
		setVisible(false);
		dispose();
	}
	
	/** installs program to specified directory with given options
	*/
	public void install() {
		if (JOptionPane.showConfirmDialog(
			null,
			"all of the contents of the directory: \n" +
			"    " + txtPath.getText() + "\n" +
			"will be erased and replaced with the \n" +
			"grim wepa program files.\n\n" +
			"note: stored passwords and handshakes \n" +
			"will not be lost\n\n" +
			"do you want to continue?",
			"grim wepa | overwrite confirmation",
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				cancel();
				return;
		}
		
		// install directory
		String dir = txtPath.getText();
		
		// true if directory has space in it, false if not
		boolean space = (dir.indexOf(" ") >= 0);
		
		String temp = dir;
		
		exec(new String[] {"rm", "-rf", "/tmp/grimwepa/"});
		
		exec(new String[] {"mkdir", "/tmp/grimwepa/"});
		
		// back up old pass.txt
		if (Methods.fileExists(dir + "pass.txt")) {
			exec(new String[] {"mv", temp + "pass.txt", "/tmp/grimwepa/"});
		}
		
		// backup handshakes
		exec(new String[] {"mv", temp + "hs/", "/tmp/grimwepa/"});
		
		// delete the directory
		exec(new String[] {"rm", "-rf", temp + "*"});
		exec(new String[] {"rm", "-rf", dir});
		
		// create the directory
		exec(new String[] {"mkdir", temp});
		
		// copy back pass.txt
		exec(new String[] {"mv", "/tmp/grimwepa/pass.txt", temp});
		
		// copy back handshakes
		exec(new String[] {"mv", "/tmp/grimwepa/hs/ " + temp});
		
		// delete temp folder
		exec(new String[] {"rm", "-rf", "/tmp/grimwepa/"});
		
		// get jar file location
		File file = null;
		try {
			file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException use) {}
		
		// copy jar file to install dir
		String temp2 = file.getPath();
		exec(new String[] {"cp", "-f", temp2, temp});
		
		// extract readme to dir
		extractFile("README", dir);
		// extract default passwordlist to dir
		extractFile("default_pw.txt", dir);
		// extract walkthrough guide to dir
		extractFile("GUIDE", dir);
		
		// create link in /usr/bin/
		Methods.writeFile("/usr/bin/grimwepa", "java -jar \"" + txtPath.getText() + file.getName() + "\" $1 &");
		// change permissions on linked file
		exec(new String[] {"chmod", "755", "/usr/bin/grimwepa"});
		
		// generate text for desktop shortcut
		String s = "";
		s += "[Desktop Entry]\n";
		s += "Comment=\n";
		s += "Exec=grimwepa\n";
		s += "GenericName=\n";
		s += "Icon=grimwepa\n";
		s += "Name=GrimWepa\n";
		s += "Path=\n";
		s += "StartupNotify=true\n";
		s += "Terminal=0\n";
		s += "TerminalOptions=\n";
		s += "Type=Application\n";
		s += "X-KDE-SubstituteUID=false\n";
		s += "X-KDE-Username=\n";
		
		// create desktop icon
		if (chkDesktop.isSelected()) {
			Methods.writeFile("/root/grimwepa.desktop", s);
		}
		
		// create menu item
		if (chkMenu.isSelected()) {
			// just need to add 'categories' to the end of the desktop shortcut text
			s += "Categories=BT-Radio-Network-Analysis-80211-Cracking\n";
			
			Methods.writeFile("/usr/share/applications/grimwepa.desktop", s);
		}
		
		// if we're installing to desktop or menu... make an icon and update the menus
		if (chkDesktop.isSelected() || chkMenu.isSelected()) {
			extractFile("grimwepa.xpm", "/usr/share/pixmaps/");
			exec(new String[] {"update-menus"});
		}
		
		JOptionPane.showMessageDialog(
			null,
			"grim wepa has been successfully installed.",
			"grim wepa | installation",
			JOptionPane.INFORMATION_MESSAGE
		);
		
		// hide the window, show the main window
		cancel();
	}
	
	/** executes a string array
		similar to Methods.readExec(), but made for the install() method
		@param command commands to execute
		@return output of command
		@see #install
	*/
	public String[] exec(String[] command) {
		if (Methods.verbose)
			System.out.println(command);
		
		String all = "";
		
		Process pro = null;
		BufferedReader res1;
		try {
			pro = Runtime.getRuntime().exec(command);
			res1 = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			pro.waitFor();
			
			String line;
			while ( (line = res1.readLine()) != null) {
				if (Methods.verbose && !line.trim().equals(""))
					System.out.println("\t" + line);
				all = all + line + "|||||";
			}
			if (all.length() >= 5) {
				if (all.substring(all.length() - 5).equals("|||||"))
					all = all.substring(0, all.length() - 5);
			}
			
			pro.destroy();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		pro.destroy();
		
		return all.split("\\|\\|\\|\\|\\|");
	}
	
	/** uninstalls program (deletes working directory, desktop/menu items)
	*/
	public void uninstall() {
		// read install path from /usb/bin link
		String dir = getInstallPath();
		txtPath.setText(dir);
		
		// get current jar file
		File file = null;
		try {
			file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException use) {}
		
		String msg = ".";
		// if the current program's .jar file is in the directory we are uninstalling, let user know
		if (file.getPath().equals(dir + file.getName())) {
			msg = " and the program will \nbe shut down after uninstallation.";
		}
		
		if (JOptionPane.showConfirmDialog(
			null,
			"all of the contents of the directory:\n" + 
			"    " + dir + "\n" +
			"will be erased" + msg + 
			"\n\nare you sure you want to continue?",
			"grim wepa | uninstall",
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				cancel();
				return;
		}
		
		// delete all files ... just in case
		Methods.readExec("rm /usr/bin/grimwepa");
		Methods.readExec("rm /usr/share/pixmaps/grimwepa.xpm");
		Methods.readExec("rm /usr/share/applications/grimwepa.desktop");
		Methods.readExec("rm /root/grimwepa.desktop");
		
		exec(new String[] {"rm", "-rf", dir + "*"});
		exec(new String[] {"rm", "-rf", dir});
		
		JOptionPane.showMessageDialog(
			null,
			"grim wepa has been successfully removed from your computer.",
			"grim wepa | uninstall",
			JOptionPane.INFORMATION_MESSAGE
		);
		
		if (file.getPath().equals(dir + file.getName())) {
			// if we deleted the file we are running... we need to gtfo
			System.exit(0);
		}
		
		// cancel();
	}
	
	/** reads from /usr/bin/grimwepa link in an attempt to find installation location
		@return folder location of grimwepa
	*/
	public String getInstallPath() {
		String dir = Methods.readFile("/usr/bin/grimwepa")[0];
		
		if (!dir.equals("")) {
			// get rid of extraneous characters
			dir = dir.replaceAll("java -jar ", "");
			dir = dir.replaceAll("\\\"", "");
			dir = dir.replaceAll(" &", "");
			
			// get everything to the left of the last slash
			int i = dir.indexOf("/");
			while (dir.indexOf("/", i + 1) >= 0) {
				i = dir.indexOf("/", i + 1);
			}
			dir = dir.substring(0, i);
			
			// add slash to the end if we haven't already
			if (!dir.substring(dir.length() - 1).equals("/"))
				dir += "/";
		
		// if there is no link, use the default
		} else
			dir = txtPath.getText();
		
		return dir;
	}
	
	/** prompts user to select a path to un/install grimwepa to/from
		stores the user's input into the textbox txtPath
		@see #actionPerformed(ActionEvent)
	*/
	public void choosePath() {
		JFileChooser chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File(txtPath.getText()));
		chooser.setDialogTitle("grim wepa | choose install path");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			txtPath.setText(chooser.getSelectedFile().toString());
			if (!txtPath.getText().endsWith("/"))
				txtPath.setText(txtPath.getText() + "/");
		}
	}
	
	/** extracts a file from the JAR to a destination
		careful, this thing throws an exception;	
		@param filename name of file to extract as it appears in the JAR file
		@param destination path to where the file will go (keeps same filename)
		@see #install()
	*/
	public static void extractFile(String filename, String destination) throws NullPointerException {
		// extracts file from the program's JAR file
		try {
			String home = Main.class.getProtectionDomain().
                   				getCodeSource().getLocation().
                      			getPath().replaceAll("%20", " ");
			JarFile jar = new JarFile(home);
			ZipEntry entry = jar.getEntry(filename);
			File efile = new File(destination + filename);
			
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
	
	/** checks if grimwepa, aircrack-ng, or pyrit need updating
	*/
	public void checkUpdates() {
		Color col = Methods.getColor(Gui.cboColors.getSelectedIndex());
		JPanel		panUpgrade = new JPanel();
		panUpgrade.setPreferredSize(new Dimension(300, 100));
		
		JLabel		lblTitle = new JLabel("the following updates are available:");
		panUpgrade.add(lblTitle);
		
		JCheckBox 	chkGW = new JCheckBox("upgrade to ...");
		chkGW.setPreferredSize(new Dimension(250, 20));
		// panUpgrade.add(chkGW);
		
		JCheckBox	chkAC = new JCheckBox("upgrade to aircrack-ng 1.1");
		chkAC.setPreferredSize(new Dimension(250, 20));
		// panUpgrade.add(chkAC);
		
		JCheckBox	chkPy = new JCheckBox("upgrade to pyrit 0.3.0");
		chkPy.setPreferredSize(new Dimension(250, 20));
		// panUpgrade.add(chkPy);
		
		JLabel		lblEnd = new JLabel("do you want to upgrade the selected items?");
		
		boolean atLeastOneUpdate = false;
		
		// check if grimwepa needs to be updated to the latest version
		String strGW = checkGrimwepa();
		if (!strGW.equals("") && !strGW.equals("n/a")) {
			// grimwepa update!
			atLeastOneUpdate = true;
			chkGW.setText("upgrade to '" + strGW + "'");
			panUpgrade.add(chkGW);
			chkGW.setSelected(true);
		}
		
		// check if aircrack-ng needs to be updated 1.1
		if (checkAircrack()) {
			atLeastOneUpdate = true;
			chkAC.setSelected(true);
			panUpgrade.add(chkAC);
		}
		
		// check if pyrit needs to be updated to 0.3.0
		if (checkPyrit()) {
			atLeastOneUpdate = true;
			chkPy.setSelected(true);
			panUpgrade.add(chkPy);
		}
		
		// if there are no new updates...	
		if (!atLeastOneUpdate) {
			// let 'em know, gtfo
			JOptionPane.showMessageDialog(
				null,
				"no updates are available at this time.",
				"grim wepa | updates",
				JOptionPane.INFORMATION_MESSAGE
			);
			return;
		}
		
		panUpgrade.add(lblEnd);
		
		if (JOptionPane.showConfirmDialog(
				null,
				panUpgrade,
				"grim wepa | updates",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) 
		{
			
			if (chkAC.isSelected())
				upgradeAircrack();
			
			if (chkPy.isSelected())
				upgradePyrit();
			
			if (chkGW.isSelected())
				upgradeGrimwepa(strGW);
			
		}
		
	}
	
	/** checks grimwepa's google-code page for the latest version
		on the site: http://code.google.com/p/griwmepa/wiki/Latest
		@return latest version, if there is one, otherwise 'n/a' if we're already up-to-date
	*/
	public String checkGrimwepa() {
		Methods.readExec("rm /tmp/gw-latest");
		
		String output[] = Methods.readExec("wget -O /tmp/gw-latest http://grimwepa.googlecode.com/files/latest.txt");
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("failed") >= 0) {
				/*JOptionPane.showMessageDialog(
					null,
					"unable to access the internet.",
					"grim wepa | updates",
					JOptionPane.ERROR_MESSAGE
				);*/
				return "n/a";
			}
		}
		
		if (!Methods.fileExists("/tmp/gw-latest")) {
			/*JOptionPane.showMessageDialog(
				null,
				"error occurred. unable to retrieve update.",
				"grim wepa | updates",
				JOptionPane.ERROR_MESSAGE
			);*/
			return "n/a";
		}
		
		String ver = "", url = "";
		
		output = Methods.readFile("/tmp/gw-latest");
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("latest version is -") >= 0 && output[i].indexOf(".jar- ") >= 0)
				ver = output[i].substring(output[i].indexOf(" is -") + 5, output[i].indexOf(".jar- ") + 4);
			if (output[i].indexOf("download from here <a href=\"") >= 0 && output[i].indexOf(".jar\"") >= 0)
				url = output[i].substring(output[i].indexOf(" from here <a href=\"") + 20, output[i].indexOf(".jar\"") + 4);
			if (!ver.equals("") && !url.equals(""))
				break;
		}
		
		Methods.readExec("rm /tmp/gw-latest");
		
		if (ver.compareTo(Main.VERSION) > 0) {
			// newest version > this version, need to update
			return ver;
		} else {
			return "";
		}
	}
	
	public void upgradeGrimwepa(String ver) {
		Methods.readExec("rm " + ver);
		Methods.readExec(	"wget" +
							" -O " + Methods.grimwepaPath.replaceAll(" ", "\\\\ ") + ver + 
							" http://grimwepa.googlecode.com/files/" + ver);
		
		try {
				Runtime.getRuntime().exec("java -jar " + ver);
		} catch (IOException ioe) {}
		System.exit(0);
		
	}
	
	/** checks current version of aircrack-ng
		@return true if user needs to update, false if they already have 1.1
	*/
	public boolean checkAircrack() {
		String output[] = Methods.readExec("aircrack-ng --help");
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("Aircrack-ng 1.1") >= 0) {
				return false;
			}
		}
		
		return true;
	}
	
	/** downloads and installs aircrack-ng v1.1*/
	public void upgradeAircrack() {
		// remove any running instances of aircrack
		Methods.readExec("killall aircrack-ng");
		// remove previously downloaded files
		Methods.readExec("rm /tmp/aircrack-ng-1.1.tar.gz");
		Methods.readExec("rm -rf /tmp/aircrack-ng-1.1");
		// download and install aircrack using a shell script (in nice popup window)
		String f = "wget -O /tmp/aircrack-ng-1.1.tar.gz " + 
			"http://download.aircrack-ng.org/aircrack-ng-1.1.tar.gz\n" +
			"tar -C /tmp/ -zxvf /tmp/aircrack-ng-1.1.tar.gz\n" +
			"cd /tmp/aircrack-ng-1.1\n" +
			"make\n" +
			"make install\n";
		
		Methods.writeFile("/tmp/install-aircrack.sh", f);
		
		Methods.readExec("chmod 755 /tmp/install-aircrack.sh");
		
		Methods.readExec("xterm" +
				" -fg " + (String)Gui.cboColors.getSelectedItem() + 
				" -bg black" +
				" -geom 100x15+0+0" + 
				" -T aircrack-installer" +
				" -e /tmp/install-aircrack.sh");
		Methods.readExec("rm /tmp/install-aircrack.sh");
		Methods.readExec("rm -rf /tmp/aircrack-ng-1.1");
		Methods.readExec("rm /tmp/aircrack-ng-1.1.tar.gz");
	}
	
	/** checks current version of pyrit
		@return true if user needs to update, false if they already have 0.3.0
	*/
	public boolean checkPyrit() {
		String output[] = Methods.readExec("pyrit help");
		for (int i = 0; i < output.length; i++) {
			if (output[i].indexOf("Pyrit 0.3.0") >= 0) {
				return false;
			}
		}
		return true;
	}
	
	public void upgradePyrit() {
		// remove any running instances of pyrit
		Methods.readExec("killall pyrit");
		Methods.readExec("rm /tmp/pyrit-0.3.0.tar.gz");
		Methods.readExec("rm -rf /tmp/pyrit-0.3.0");
		// download and install pyrit using a shell script (in nice popup window)
		String f = "wget -O /tmp/pyrit-0.3.0.tar.gz" +
			" http://pyrit.googlecode.com/files/pyrit-0.3.0.tar.gz\n" +
			"tar -C /tmp/ -zxvf /tmp/pyrit-0.3.0.tar.gz\n" +
			"cd /tmp/pyrit-0.3.0\n" +
			"python setup.py build\n" +
			"python setup.py install\n";
		
		Methods.writeFile("/tmp/install-pyrit.sh", f);
		
		Methods.readExec("chmod 755 /tmp/install-pyrit.sh");
		
		Methods.readExec("xterm" +
				" -fg " + (String)Gui.cboColors.getSelectedItem() + 
				" -bg black" +
				" -geom 100x15+0+0" + 
				" -T pyrit-installer" +
				" -e /tmp/install-pyrit.sh");
		Methods.readExec("rm /tmp/install-pyrit.sh");
		Methods.readExec("rm -rf /tmp/pyrit-0.3.0");
		Methods.readExec("rm /tmp/pyrit-0.3.0.tar.gz");
	}
	
	/** event is called when user attempts to close the window
		@param e info on the event
	*/
	public void windowClosing(WindowEvent e) {
		cancel();
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