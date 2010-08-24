/** wordlist generator window
	<p>
	generates wordlists, 'passes-through' passwords to aircrack/pyrit
*/

// libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import javax.swing.JOptionPane;
// io stuff
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
// for events
import java.util.EventListener;

public class GuiWordlistGen extends Frame implements WindowListener, ActionListener, EventListener, ListSelectionListener {
	/** label to show wordlists*/
	public JLabel lblWordlist;
	
	/** scrollpane to keep list inside of*/
	public JScrollPane			scrWord;
	/** list of wordlists*/
	public JTable				tabWord;
	/** DTM for list (much easier to add/remove)*/
	public DefaultTableModel	dtmWord;
	
	/** pane for buttons */
	public JPanel panButtons;
	/** add wordlist*/
	public Button btnAdd;
	/** download wordlists*/
	public Button btnDownload;
	/** remove wordlist*/
	public Button btnRemove;
	/** clear wordlists*/
	public Button btnClear;
	
	/** label to show output textbox*/
	public JLabel lblOutput;
	/** where to put generated wordlist*/
	public JTextField txtOutput;
	/** browse for output file*/
	public Button btnOutput;
	
	/** generate wordlist*/
	public static Button btnGenerate;
	/** generate wordlist, then crack*/
	public static Button btnCrack;
	
	/** option to use pyrit to crack*/
	public JCheckBox chkPyrit;
	
	/** status bar*/
	public static JLabel lblStatus;
	
	/** constructor, builds form, loads settings for wordlist,
		if crunch is not found, prompts user to choose directory or download it
		@param title title of wordlist frame
	*/
	public GuiWordlistGen(String title) {
		super(title);
		
		this.setSize(380, 255);
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
	
	/** builds controls for the form
		@see #GuiWordlistGen(String)
	*/
	public void buildControls() {
		Color col = Methods.getColor(Gui.cboColors.getSelectedIndex());
		
		Font f = new Font("Default", Font.BOLD, 11);
		
		dtmWord = new DefaultTableModel(null, new String[] 
							{"wordlists"});
		tabWord = new JTable(dtmWord) { 
			public TableCellRenderer getCellRenderer( int row, int col ) {
				TableCellRenderer renderer = super.getCellRenderer(row,col);
				((JLabel)renderer).setHorizontalAlignment( SwingConstants.LEFT );
				return renderer;
				
			}
			public boolean isCellEditable(int rowIndex, int vColIndex) { 
				return false; 
			}
		};
		
		tabWord.setAutoCreateColumnsFromModel(false); 
		tabWord.setAutoCreateRowSorter(true); // turn sorting on
		tabWord.setBackground(Color.black);
		tabWord.setForeground(col);
		tabWord.setPreferredScrollableViewportSize(new Dimension(250,100));
		tabWord.setFillsViewportHeight(true);
		// tabWord.getColumnModel().getColumn(0).setPreferredWidth(100);	// ssid
		tabWord.setFont(f);
		tabWord.getSelectionModel().addListSelectionListener(this); // listen for selection changes
		
		scrWord = new JScrollPane(tabWord); // scroller
		add(scrWord);
		
		panButtons = new JPanel();
		panButtons.setPreferredSize(new Dimension(90, 120));
		panButtons.setBackground(Color.black);
		// panButtons.setLayout(new BorderLayout());
		panButtons.setLayout(new BoxLayout(panButtons, BoxLayout.Y_AXIS));
		
		btnAdd = new Button("add wordlist");
		btnAdd.setFont(f);
		btnAdd.setBackground(col);
		btnAdd.setForeground(Color.black);
		btnAdd.addActionListener(this);
		panButtons.add(btnAdd); //, BorderLayout.NORTH);
		
		panButtons.add(Box.createRigidArea(new Dimension(0, 10)));
		
		btnDownload = new Button("download lists");
		btnDownload.setFont(f);
		btnDownload.setBackground(col);
		btnDownload.setForeground(Color.black);
		btnDownload.addActionListener(this);
		panButtons.add(btnDownload); //, BorderLayout.NORTH);
		
		panButtons.add(Box.createRigidArea(new Dimension(0, 10)));
		
		btnRemove = new Button("remove list");
		btnRemove.setFont(f);
		btnRemove.setBackground(col);
		btnRemove.setForeground(Color.black);
		btnRemove.addActionListener(this);
		panButtons.add(btnRemove); //, BorderLayout.CENTER);
		
		panButtons.add(Box.createRigidArea(new Dimension(0, 10)));
		
		btnClear = new Button("clear lists");
		btnClear.setFont(f);
		btnClear.setBackground(col);
		btnClear.setForeground(Color.black);
		btnClear.addActionListener(this);
		panButtons.add(btnClear); //, BorderLayout.SOUTH);
		
		add(panButtons);
		
		lblOutput = new JLabel("output file:");
		lblOutput.setFont(f);
		lblOutput.setBackground(Color.black);
		lblOutput.setForeground(col);
		add(lblOutput);
		
		txtOutput = new JTextField(Methods.grimwepaPath + "wordlist.txt");
		txtOutput.setFont(f);
		txtOutput.setBackground(Color.black);
		txtOutput.setForeground(col);
		txtOutput.setPreferredSize(new Dimension(250, 18));
		add(txtOutput);
		
		btnOutput = new Button("...");
		btnOutput.setFont(f);
		btnOutput.setBackground(col);
		btnOutput.setForeground(Color.black);
		btnOutput.addActionListener(this);
		add(btnOutput);
		
		btnGenerate = new Button("generate");
		btnGenerate.setFont(f);
		btnGenerate.setBackground(col);
		btnGenerate.setForeground(Color.black);
		btnGenerate.addActionListener(this);
		add(btnGenerate);
		
		btnCrack = new Button("crack wordlist");
		btnCrack.setFont(f);
		btnCrack.setBackground(col);
		btnCrack.setForeground(Color.black);
		btnCrack.addActionListener(this);
		btnCrack.setEnabled(false);
		add(btnCrack);
		
		chkPyrit = new JCheckBox("use pyrit 0.2.5 to crack (experimental/buggy)");
		chkPyrit.setFont(f);
		chkPyrit.setBackground(Color.black);
		chkPyrit.setForeground(col);
		//chkPyrit.setPreferredSize(new Dimension(250, 18));
		add(chkPyrit);
		
		lblStatus = new JLabel(" cracking is available after you generate a wordlist");
		lblStatus.setFont(f);
		lblStatus.setBackground(Color.black);
		lblStatus.setForeground(col);
		lblStatus.setPreferredSize(new Dimension(365, 18));
		lblStatus.setBorder(BorderFactory.createLineBorder(col));
		add(lblStatus);
	}
	
	/** event for when user clicks an item
		@param e info on the event
	*/
	public void valueChanged(ListSelectionEvent e) {
		int row = tabWord.getSelectedRow();
		if (row < 0)
			return;
		
		String list = (String)tabWord.getValueAt(row, 0);
		
	}
	
	/** event that is called when an item that we are listening to is acted upon
		@param event info on event
	*/
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnAdd) {
			wordlistAdd();
		} else if (event.getSource() == btnDownload) {
			wordlistDownload();
		} else if (event.getSource() == btnRemove) {
			wordlistRemove();
		} else if (event.getSource() == btnClear) {
			wordlistClear();
		} else if (event.getSource() == btnOutput) {
			outputBrowse();
		} else if (event.getSource() == btnGenerate) {
			generate();
		} else if (event.getSource() == btnCrack) {
			crack();
		} 
	}
	
	/** prompt user to select a file to add to the list*/
	public void wordlistAdd() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(new File("/pentest/passwords/wordlists/"));
		int retval = fc.showOpenDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File files[] = fc.getSelectedFiles();
			for (int i = 0; i < files.length; i++) {
				String s = files[i].getPath();
				if (Methods.fileExists(s)) {
					dtmWord.addRow(new String[]{s});
				}
			}
		}
	}
	
	/** prompt user to select files to download from google code page*/
	public void wordlistDownload() {
		JPanel pane = new JPanel();
		pane.setPreferredSize(new Dimension(250, 260));
		
		JLabel lblTop 			= new JLabel("select wordlists to download:");
		pane.add(lblTop);
		
		String files[] = new String[] {
				"cain", "john", "myspace", "phpbb", "rockyou-75",
				"default_pw", "combined-big", "combined-small"
			};
		
		JCheckBox chk[] = new JCheckBox[8];
		chk[0] 	= new JCheckBox("cain - 2.5mb");
		chk[1] 	= new JCheckBox("john - 4kb");
		chk[2]	= new JCheckBox("myspace - 227kb");
		chk[3] 	= new JCheckBox("phpbb - 15kb");
		chk[4] 	= new JCheckBox("rockyou (small) - 191kb");
		chk[5] 	= new JCheckBox("old gw wordlist - 2.8mb");
		chk[6] 	= new JCheckBox("combined (big) - 2.9mb");
		chk[7] 	= new JCheckBox("combined (small) - 276kb");
		
		for (int i = 0; i < chk.length; i++) {
			chk[i].setPreferredSize(new Dimension(200, 20));
			chk[i].setSelected(true);
			pane.add(chk[i]);
		}
		
		String dir = Methods.grimwepaPath + "wordlists/";
		
		JLabel lblBottom1 = new JLabel("all files downloaded to:");
		JLabel lblBottom2 = new JLabel(dir);
		pane.add(lblBottom1);
		pane.add(lblBottom2);
		
		if (JOptionPane.showConfirmDialog(
				null,
				pane,
				"grim wepa | download wordlists",
				JOptionPane.OK_CANCEL_OPTION
			) != JOptionPane.OK_OPTION) {
			return;
		}
		
		Methods.readExec("mkdir \"" + dir + "\"");
		
		// loop through every checkbox
		for (int i = 0; i < chk.length; i++) {
			
			// if this checkbox is unchecked (NOT selected), then move on
			if (!chk[i].isSelected())
				continue;
			
			// if we already have the file downloaded...
			if (Methods.fileExists(dir + files[i] + ".txt")) {
				// add it to the list
				dtmWord.addRow(
					new String[]{
						dir + files[i] + ".txt"
					}
				);
				// move on
				continue;
			}
			
			// try to wget the file (from the string array containing the filenames)
			String command = "" +
				"xterm" +
				" -fg " + (String)Gui.cboColors.getSelectedItem() + 
				" -bg black" +
				" -geom 100x15+0+0" +
				" -T gw-downloader" +
				" -e " +
				
				"wget" + 
				" -O " + dir.replaceAll(" ", "\\\\ ") + files[i] + ".txt" +
				" http://grimwepa.googlecode.com/files/" + files[i] + ".txt";
			
			if (Methods.verbose)
				System.out.println(command);
			
			String output[] = Methods.readExec(command);
			
			// go through wget output to see if it downloaded properly
			for (int j = 0; j < output.length; j++) {
				if (Methods.verbose)
					System.out.println(output[j]);
				
				if (output[j].indexOf("failed") >= 0) {
					// download failed!
					lblStatus.setText(" download failed; make sure you are online!");
					return;
				}
			}
			
			// wait for 10th of a second
			Methods.pause(0.1);
			
			// if download was successful
			if (Methods.fileExists(dir + files[i] + ".txt")) {
				// add file to list
				dtmWord.addRow(
					new String[]{
						dir + files[i] + ".txt"
					}
				);
			}
			
		} // end of for-loop through every checkbox
		
	} // end of wordlistDownload() method
	
	/** remove selected item from the list*/
	public void wordlistRemove() {
		int index = tabWord.getSelectedRow();
		if (index == -1) {
			// no items selected
			return;
		}
		
		dtmWord.removeRow(index);
	}
	
	/** clear list*/
	public void wordlistClear() {
		dtmWord.setRowCount(0);
	}
	
	/** prompt user to select output file*/
	public void outputBrowse() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("/pentest/passwords/wordlists/"));
		int retval = fc.showSaveDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			
			txtOutput.setText(f.getPath());
		}
	}
	
	/** start/stop generating passwords*/
	public void generate() {
		if (!btnGenerate.getLabel().equals("generate")) {
			btnGenerate.setLabel("generate");
			threadWordlistExec.flag = true;
			lblStatus.setText(" wordlist generation cancelled");
			return;
		}
		
		if (tabWord.getRowCount() == 0) {
			JOptionPane.showMessageDialog(
				null,
				"you need to have wordlists added to the list \n" +
				"before you can combine them (output) into a larger wordlist!\n\n" +
				"tl;dr click 'add wordlist'",
				"grim wepa | wordlist generator error",
				JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		if (!Methods.fileExists("/usr/bin/sort")) {
			// user doesn't even have 'sort!'
			JOptionPane.showMessageDialog(
				null,
				"the program 'sort' is required for wordlist-generation to work.\n" +
				"a link for 'sort' was not found in /usr/bin/sort, so grim wepa cannot continue.\n\n" +
				"sort is required, and other recommended apps include:\n" +
				"    uniq - removes duplicate entries\n" +
				"    pw-inspector - allows filtering based on length (more than 8 characters, etc)\n\n",
				"grim wepa | wordlist generator error",
				JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		String command = "cat ";
		for (int i = 0; i < tabWord.getRowCount(); i++) {
			String file = (String)tabWord.getValueAt(i, 0);
			file = file.replaceAll(" ", "\\\\ ");
			
			command += file + " ";
		}
		
		command = command.substring(0, command.length() - 1);
		
		command += 	" |" +
					" sort";
		
		// check if user has 'uniq'
		if (Methods.fileExists("/usr/bin/uniq")) {
			command +=	" |" +
						" uniq";
		} else
			command += 	" -u";
		
		// check if user has pw-inspector
		if (Methods.fileExists("/usr/bin/pw-inspector")) {
			command +=	" |" +
						" pw-inspector -m 8 -M 63";
		}
		
		String file = txtOutput.getText();
		file = file.replaceAll(" ", "\\\\ ");
		
		command +=	" >>" +
					" " + file;
		
		Methods.readExec("rm " + txtOutput.getText());
		
		lblStatus.setText(" waiting to generate list...");
		btnGenerate.setLabel("wait...");
		
		String commandArr[] = new String[] {
			"/bin/sh", 
			"-c",
			command
		};
		
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + commandArr[0] + " " + commandArr[1] + " " + commandArr[2]);
			Runtime.getRuntime().exec(commandArr).waitFor();
		} catch (IOException ioe) {
		} catch (InterruptedException ie) {}
		
		lblStatus.setText(" list generated!");
		
		btnGenerate.setLabel("generate");
		
		btnCrack.setEnabled(true);
	}
	
	/** executes String array (parameter) and returns the output of that command
		@param command String array, command to execute
		@return output of command, String array, each element is a new line
	*/
	public static String[] readExec(String command[]) {
		
		Process proc = null;
		BufferedReader res1;
		String all = "";
		
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command);
			proc = Runtime.getRuntime().exec(command);
			res1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			proc.waitFor();
			
			String line;
			while ( (line = res1.readLine()) != null) {
				if (Methods.verbose)
					System.out.println(line);
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
	
	/** cracks generated wordlist, only available until after a list has been generated*/
	public void crack() {
		if (!btnCrack.getLabel().equals("crack wordlist")) {
			btnCrack.setLabel("crack wordlist");
			Methods.proCrack.destroy();
			threadWpaCracker.flag = true;
			
			return;
		}
		
		if (!Methods.fileExists(txtOutput.getText())) {
			JOptionPane.showMessageDialog(
				null,
				"the generated output file does not exist!\n" +
				"try generating the list again.",
				"grim wepa | wordlist generator error",
				JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		btnCrack.setLabel("stop cracking");
		
		String gwPath = Methods.grimwepaPath;
		gwPath = gwPath.replaceAll(" ", "\\\\ ");
		
		String wlist = txtOutput.getText();
		wlist = wlist.replaceAll(" ", "\\\\ ");
		
		String command[] = new String[3];
		command[0] = "/bin/sh";
		command[1] = "-c";
		command[2] = "xterm" +
					" -fg "+ (String)Gui.cboColors.getSelectedItem() +
					" -bg black" +
					" -T gw-aircrack" +
					" -geom 100x20+0+0" +
					" -e ";
		
		if (!chkPyrit.isSelected()) {
			lblStatus.setText(" cracking with aircrack-ng, please wait...");
			command[2] += 	"\"aircrack-ng" + 
							" -a 2" +
							" -l " + gwPath + "wpacracked.txt" +
							" -w " + wlist + 
							" " + gwPath + "wpa-01.cap\"";
		} else {
			lblStatus.setText(" pyrit doesn't output progress; please be patient...");
			command[2] = 	"\"pyrit" +
							" -r " + gwPath + 
							"wpa-01.cap -i " + wlist + 
							" -b " + Methods.currentBSSID + 
							" attack_passthrough" +
							" >" +
							" " + gwPath + "wpacracked.txt\"";
		}
		
		Methods.removeFile("wpacracked.txt");
		
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command[0] + " " + command[1] + " " + command[2]);
			Methods.proCrack = Runtime.getRuntime().exec(command);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		threadWpaCracker twc = new threadWpaCracker();
		twc.t.start();
	}
	
	
	/** event called when user tries to close window
		@param e info on event
	*/
	public void windowClosing(WindowEvent e) {
		setVisible(false);
		Main.guiWindow.setVisible(true);
		
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