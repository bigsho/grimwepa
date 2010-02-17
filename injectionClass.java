import java.io.*;
import java.util.*;

public class injectionClass implements Runnable {
	Thread t;
	public static boolean flag;
	
	public injectionClass() {
		t = new Thread(this, "Injection Test");
		flag = false;
	}
	
	public void run() {
		Process proinj = null;
		BufferedReader in = null;
		test2.stat("Testing injection...");
		
		try {
			String xterm = ""; //"xterm -fg green -bg black -geom 100x15+0+450 -e ";
			proinj = Runtime.getRuntime().exec(
				xterm + 
				"aireplay-ng -9 -a " + test2.currentBSSID + " " + 
				(String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex()));
			
			in = new BufferedReader(new InputStreamReader(proinj.getInputStream()));
			
			String line = null;
			while ( (line = in.readLine()) != null && flag == false) {
				// System.out.println(line);
				if (line.indexOf("30:") >= 0) {
					// this line contains the rate
					test2.stat("Injection rate: " + line);
					flag = true;
					break;
				}// else if (line.indexOf("Failure: ") >= 0) {
			}
			in.close();
		} catch (IOException e) {}
		test2.buttonTest.setLabel("Test Injection");
		test2.proinj.destroy();
	}
	
} 
