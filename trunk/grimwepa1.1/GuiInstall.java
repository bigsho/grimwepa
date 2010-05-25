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
	
	/** creates frame, sets properties, builds controls, sets frame visible
	*/
	public GuiInstall(String title) {
		super(title);
		
		this.setSize(250, 175);
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
		
		String temp = "";
		
		Methods.readExec("rm -rf /tmp/grimwepa/");
		
		Methods.readExec("mkdir /tmp/grimwepa/");
		
		// back up old pass.txt
		if (Methods.fileExists(dir + "pass.txt")) {
			temp = dir + "pass.txt";
			if (space)
				temp = "\"" + temp + "\"";
			
			Methods.readExec("mv " + temp + " /tmp/grimwepa/");
		}
		
		// backup handshakes
		temp = dir + "hs/";
		if (space)
			temp = "\"" + temp + "\"";
		Methods.readExec("mv " + temp + " /tmp/grimwepa/");
		
		// delete the directory
		temp = dir + "*";
		if (space)
			temp = "\"" + temp + "\"";
		Methods.readExec("rm -rf " + temp);
		
		temp = dir;
		if (space)
			temp = "\"" + temp + "\"";
		Methods.readExec("rm -rf " + dir);
		
		// create the directories
		Methods.readExec("mkdir " + temp);
		
		// copy back pass.txt
		Methods.readExec("mv /tmp/grimwepa/pass.txt " + temp);
		
		// copy back handshakes
		Methods.readExec("mv /tmp/grimwepa/hs/ " + temp);
		
		// delete temp folder
		Methods.readExec("rm -rf /tmp/grimwepa/");
		
		// get jar file location
		File file = null;
		try {
			file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException use) {}
		
		// copy jar file to install dir
		String temp2 = file.getPath();
		if (temp2.indexOf(" ") >= 0)
			temp2 = "\"" + temp2 + "\"";
		Methods.readExec("cp -f " + temp2 + " " + temp);
		
		// extract readme to dir
		extractFile("README", dir);
		// extract default passwordlist to dir
		extractFile("default_pw.txt", dir);
		
		// create link in /usr/bin/
		Methods.writeFile("/usr/bin/grimwepa", "java -jar \"" + txtPath.getText() + file.getName() + "\" $1 &");
		// change permissions on linked file
		Methods.readExec("chmod 755 /usr/bin/grimwepa");
		
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
			Methods.readExec("update-menus");
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
		
		Methods.readExec("rm -rf " + dir + "*");
		Methods.readExec("rm -rf " + dir);
		
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