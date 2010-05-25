/**
 * Looks for the file "wepcracked.txt" to appear.
 * Waits until the file is there OR the user clicks "stop cracking"
 * Copyright 2010 Derv Merkler
 */

// ioexception is a bitch
import java.io.*;

// same method as wpacracker.java, listens to 'wepcracked.txt' for key to be written.

public class wepcracker implements Runnable {
	Thread t;
	public static boolean flag;
	public static boolean cracked;
	
	public wepcracker() {
		t = new Thread(this, "WEP Cracker");
		flag = false;
		cracked = false;
	}
	
	public void run() {
		BufferedReader input = null;
		String line = null, result = "";
		int xval = -1;
		
		do {
			pause(1);
			try {
				xval = test2.procrack.exitValue();
				
				input =  new BufferedReader(new FileReader(test2.grimwepaPath + "wepcracked.txt"));
				while (( line = input.readLine()) != null ) {
					result += line + "";
				}
				if (result.equals("") == false) {
					// we cracked it!
					flag = true;
					cracked = true;
					break;
				}
			} catch (FileNotFoundException fnfe) {
				// still haven't cracked it.
				if (xval == 0) {
					// process is completed, and there's no file containing the WPA...
					flag = true;
					break;
				}
			} catch (IOException ioe) {
				
			} catch (IllegalThreadStateException itse) {
				// thread is still running
			}
		} while (flag == false);
		
		if (cracked == true) {
			// cracked it!
			appendToFile(test2.grimwepaPath + "pass.txt", "WEP\tB(SSID): " + 
				test2.currentBSSID + "(" + test2.currentSSID + ")\tKEY: " + result + "");
			test2.stat("WEP Key: " + result + " | saved: '" + test2.grimwepaPath + "pass.txt'");
			wepattack1.flag = true;
			try {
				wepattack1.profrag.destroy();
			} catch (NullPointerException npe) {}
			
			// remove old wepcracked.txt file
			try {
				test2.procrack = Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!wepcracked.txt"));
				test2.procrack .waitFor();
				test2.procrack .destroy();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			// didn't crack it.
			test2.stat("WEP Cracking Stopped; Try again!");
		}
		test2.procrack.destroy();
		test2.buttonCrack.setLabel("Start Cracking");
	}
	public static void appendToFile(String file, String text) {
		Writer bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(text + "\n");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	public static void pause(double dtime) {
		// pauses for selected period of time (IN SECONDS)
		try {
			Thread.currentThread().sleep((int)(dtime * 1000));
		}
			catch (InterruptedException e) {}
	}
}