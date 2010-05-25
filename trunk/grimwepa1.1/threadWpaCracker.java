/** thread that listens for 'wpacracked.txt';
	checks if password is stored in wpacracked.txt or not;
	validates aircrack-ng cracks and pyrit cracks
*/
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class threadWpaCracker implements Runnable {
	/** wpa cracker thread*/
	Thread t;
	/** flag, tells us when to stop*/
	public static boolean flag;
	/** flag, tells other classes if we cracked it or not*/
	public static boolean cracked;
	
	/** creates new thread, initializes instance variables
	*/
	public threadWpaCracker() {
		t = new Thread(this, "threadWpaCracker");
		flag = false;
		cracked = false;
	}
	
	/** scans for wpacracked.txt; waits for password to appear inside;
		also checks Methods.proCrack process, stops if the process is terminated;
		can handle pyrit output as well;
	*/
	public void run() {
		String input[], result = "";
		int xval = -1;
		
		// one big loop
		do {
			
			Methods.pause(1.0);
			
			try {
				xval = Methods.proCrack.exitValue();
			} catch (IllegalThreadStateException itse) {
				// thread is still running
			}
			
			result = "";
			input = Methods.readFile(Methods.grimwepaPath + "wpacracked.txt");
			result = input[0];
			
			// if we're using aircrack-ng and the first line of 'wpacracked.txt' is NOT blank
			if (!result.equals("") && result.indexOf("Pyrit ") < 0) {
				// we cracked it!
				flag = true;
				cracked = true;
				break;
			
			// if we're using pyrit...
			} else if (result.indexOf("Pyrit ") >= 0) {
				// go through every line of the output, looking for 'the passwrod is'
				for (int i = input.length - 1; i >= 0; i--) {
					if (input[i].indexOf("The password is '") >= 0) {
						// cracked it!
						result = input[i].substring(17, input[i].length() - 2);
						flag = true;
						cracked = true;
						break;
					} else if (input[i].indexOf("Tried ") >= 0) {
						// at least give them an output!
						Methods.stat(
				"cracking wpa via pyrit: '" + input[i].toLowerCase().substring(0, input[i].indexOf(";")) + "'");
						i = 0;
						continue;
					}
				}
			}
			
			if (xval != -1) {
				// process is completed, and there's no file containing the WPA...
				flag = true;
				break;
			}
			
		} while (!flag);
		
		if (cracked) {
			// cracked it!
			Methods.writeWpaToFile(result);
			Methods.stat("wpa cracked! | key: '" + result + "' | saved");
		} else {
			// didn't crack it.
			Methods.stat("wpa key was not found; try a new wordlist.");
			Methods.proCrack.destroy();
		}
		
		// remove old wpacracked.txt file
		Methods.removeFile("wpacracked.txt");
		
		// don't remove default password list
		// Methods.removeFile("default_pw.txt"); 
		
		// remove all aircrack-ng processes
		Methods.readExec("killall aircrack-ng");
		Methods.readExec("killall pyrit");
		
		if (Main.guiWindow.isVisible()) {
			// this thread is cracking using a main window call
			Gui.btnWpaCrack.setLabel("crack wpa with...");
			
		} else if (Methods.gwlgWindow != null) {
			// gui wordlist generator window was cracking
			// thread is cracking for the GuiwordlistGen
			Methods.gwlgWindow.setVisible(false);
			Main.guiWindow.setVisible(true);
			Methods.gwlgWindow.dispose();
		} else if (Methods.gcpWindow != null) {
			// gui crunch passthrough window was cracking
			// if we cracked it, load the main window so they can see the key
			Methods.gcpWindow.setVisible(false);
			Main.guiWindow.setVisible(true);
			Methods.gcpWindow.dispose();
		}
		
		Gui.setEnable(true);
		
		if (cracked && Gui.chkWpaSignon.isSelected())
			Methods.signonWpa(Methods.currentSSID, result);
	}
	
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/