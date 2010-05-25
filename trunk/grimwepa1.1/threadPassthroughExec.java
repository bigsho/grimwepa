/** executes cracking commands from GuiCrunchPassthrough window;
	scans wpacracked.txt after executing the command, looking for password; 
	this threaded class is intended to keep the GUI from locking up*/

import java.io.IOException;

public class threadPassthroughExec implements Runnable {
	/** passthrough cracking thread*/
	Thread t;
	/** cracking process, public so we don't have a zombie process later*/
	public static Process process;
	/** flag, tells us when to stop*/
	public static boolean flag;
	/** flag, lets other classes know what we are doing*/
	public static boolean cracking;
	/** command to execute*/
	public static String command[];
	
	/** starts a new thread, initializes instances variable*/
	public threadPassthroughExec() {
		t = new Thread(this, "threadPassthroughExec");
		flag = false;
		cracking = true;
		command = new String[]{""};
	}
	
	/** executes the command,
		scans wpacracked.txt every second, looking for a cracked password*/
	public void run() {
		boolean cracked = false;
		String result = "";
		Methods.removeFile("wpacracked.txt");
		
		try {
			if (Methods.verbose) {
				for (int i = 0; i < command.length; i++) {
					System.out.println(( i == 0 ? "exec:\t" : "") + command[i]);
				}
			}
			process = Runtime.getRuntime().exec(command);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		do {
			Methods.pause(1.0);
			try {
				int xval = -1;
				xval = process.exitValue();
				if (xval == 0) {
					// process has ended
					flag = true;
					break;
				}
			} catch (IllegalThreadStateException itse) {
				// process is still going
			}
			
			if (Methods.fileExists(Methods.grimwepaPath + "wpacracked.txt")) {
				cracked = true;
				flag = true;
				result = Methods.readFile(Methods.grimwepaPath + "wpacracked.txt")[0];
				break;
			}
		} while (!flag);
		
		cracking = false;
		
		if (cracked) {
			// was cracked
			Methods.gcpWindow.dispose();
			Main.guiWindow.setVisible(true);
			Methods.stat("wpa cracked! | key: '" + result + "' | saved");
			Methods.writeWpaToFile(result);
			Methods.removeFile("wpacracked.txt");
			
		} else {
			// wasn't cracked
			Methods.gcpWindow.btnCrack.setLabel("start crack");
		}
	}
	
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/