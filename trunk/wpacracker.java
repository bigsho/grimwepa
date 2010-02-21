// i h8 ioexception
import java.io.*;

// looks for wpacracked.txt, waits for data to be written to it.  Goes crazy if it finds it!

public class wpacracker implements Runnable {
	Thread t;
	public static boolean flag;
	public static boolean cracked;
	
	public wpacracker() {
		t = new Thread(this, "WPA Cracker");
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
				
				input =  new BufferedReader(new FileReader(test2.grimwepaPath + "wpacracked.txt"));
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
			test2.stat("WPA Key: " + result + " | saved '" + test2.grimwepaPath + "wpacracked.txt'");
		} else {
			// didn't crack it.
			test2.stat("WPA Key Not Found; Try a new wordlist!");
			test2.procrack.destroy();
		}
		Process proremove = null;
		try {
			proremove = Runtime.getRuntime().exec("rm -rf defautl_pw.txt");
			proremove.waitFor();
			proremove.destroy();
		} catch (IOException ioe) {} catch(InterruptedException ie) {}
		test2.wpacrack.setLabel("Crack WPA (Dictionary Attack)");
	}
	public static void pause(double dtime) {
		// pauses for selected period of time (IN SECONDS)
		try {
			Thread.currentThread().sleep((int)(dtime * 1000));
		}
			catch (InterruptedException e) {}
	}
}