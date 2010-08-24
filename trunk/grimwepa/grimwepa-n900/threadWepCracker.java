/** listens for 'wepcracked.txt'
	when the file has text in it, that means we've cracked it
*/

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class threadWepCracker implements Runnable {
	/** creates new wep cracker thread*/
	Thread t;
	/** flag, lets this and other classes know when we want to stop cracking*/
	public static boolean flag;
	/** flag, true only afte we have cracked the wep*/
	public static boolean cracked;
	
	/** create new thread so we don't lock up the GUI
	*/
	public threadWepCracker() {
		t = new Thread(this, "threadWepCracker");
		flag = false;
		cracked = false;
	}
	
	/** constantly reads wepcracked.txt looking for text; 
		loops until wepcracked.txt contains text or user clicks 'stop cracking'
	*/
	public void run() {
		String input[], result = "";
		int xval = -1;
		
		do {
			Methods.pause(1.0);
			try {
				xval = Methods.proCrack.exitValue();
			} catch (IllegalThreadStateException itse) {
				// thread is still running
			}
			
			input = Methods.readFile(Methods.grimwepaPath + "wepcracked.txt");
			for (int i = 0; i < input.length; i++)
				result += input[i] + "";
			
			if (!result.equals("")) {
				// we cracked it!
				flag = true;
				cracked = true;
				break;
			}
			
			if (xval != -1) {
				// process is completed, and there's no file containing the WPA...
				flag = true;
				break;
			}
			
		} while (!flag);
		
		if (cracked == true) {
			// cracked it!
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			appendToFile(
				"WEP\tB(SSID): " + Methods.currentBSSID +  	// bssid
				"(" + Methods.currentSSID + 				// ssid
				")\tKEY: " + result +						// key
				"|||" + sdf.format(cal.getTime()) +			// date
				"|||" + (String)Gui.cboWepAttack.getSelectedItem() + 	// more info
				"@" + Gui.sldWepInjection.getValue() + "pps");		// even more info
			String key = result;
			
			result = result + (Methods.getAscii(result).equals("n/a") ? "" : " (" + Methods.getAscii(result) + ")");
			
			Methods.stat("wep cracked! | key: " + result + " | saved");
			
			String[] newlist = new String[1];
			// save this key into the array that we look for keys in!
			if (Methods.cracked.length > 1 || Methods.cracked[0].length() > 0) {
				// if there's already items in the cracked array
				newlist = new String[Methods.cracked.length + 1];
				for (int i = 0; i < Methods.cracked.length; i++) {
					newlist[i] = Methods.cracked[i];
				}
			}
			
			// store it
			newlist[newlist.length - 1] = Methods.currentBSSID + "(" + Methods.currentSSID + ")\t" + result;
			Methods.cracked = newlist;
			
			Gui.btnKeyTank.setLabel("key tank (" + Methods.cracked.length + ")");
			
			threadWepAttack.stopFlag = true;
			try {
				threadWepAttack.proAttack.destroy();
			} catch (NullPointerException npe) {
			} catch (IllegalStateException ise) {}
			
			// remove old wepcracked.txt file
			Methods.removeFile("wepcracked.txt");
			
			if (Gui.chkWepSignon.isSelected())
				Methods.signonWep(Methods.currentSSID, key);
		} else {
			// didn't crack it.
			Methods.stat("wep cracking stopped; " + 
					(Gui.btnWepAttack.getLabel().equals("stop attack") ? 
						" wep attack continuing..." : 
						"try again!"
					)
			);
		}
		
		Methods.proCrack.destroy();
		Gui.btnWepCrack.setLabel("start cracking");
	}
	
	/** appends 'text' to the file 'pass.txt', but 'text' will be at the top of the file
		@param text whatever text to write to the file
	*/
	public static void appendToFile(String text) {
		String file[] = Methods.readFile(Methods.grimwepaPath + "pass.txt");
		String output = text + "\n";
		for (int i = 0; i < file.length; i++) {
			output += file[i] + "\n";
		}
		Methods.writeFile(Methods.grimwepaPath + "pass.txt", output);
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/