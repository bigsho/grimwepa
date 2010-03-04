/**
 * target refresher class.
 * opens airodump-ng, dumps to file (targets-01.csv)
 * does a loop through the file every such-and-such seconds looking for SSIDS and adding them to the list.
 * loops until user clicks "Stop Refreshing" on the main form.
 * Copyright 2010 Derv Merkler
 */

import java.io.*;
import java.util.*;

// load targets from airodump-ng's CSV dump file into the test2's GUI list

public class targetClass implements Runnable {
	Thread t;
	public targetClass() {
		t = new Thread(this, "Target Scanner");
	}
	public void run() {
		String[] tempArr = new String[20];
		Scanner res1 = new Scanner(System.in);
		String line;
		boolean clis = false;
		
		test2.stat("Initializing scan");
		
		// remove the kismet file
		try {
			Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!targets-01.csv"));
			test2.pause(0.3);
		} catch(IOException e) {
			e.printStackTrace();
		}
		String chan = "", xterm = "", blah[] = {""};
		
		String drv = (String)test2.drivers.getItemAt(test2.drivers.getSelectedIndex());
		Process pro1 = null;
		try {
			chan = "";
			if (test2.chkchan.isSelected() == false)
				chan = "-c " + test2.chslider.getValue() + " ";
			
			if (test2.hideWin.isSelected() == true)
				xterm = "";
			else
				xterm = "xterm -fg green -bg black -bd green -geom 100x15+0+0 " + 
					"-T GrimWepa-TargetScan -iconic -e ";
			// xterm += "airodump-ng " + chan + "-w " + test2.grimwepaPath + "targets --output-format csv " + drv;
			xterm = xterm + "airodump-ng " + chan + "-w !PATH!targets --output-format csv " + drv;
			blah = test2.fixArgumentsPath(xterm);
			pro1 = Runtime.getRuntime().exec(blah);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		do {
			int tout = 5;
			try {
				tout = Integer.parseInt(test2.timeout.getText());
			} catch (NumberFormatException e) {
				tout = 5;
				test2.timeout.setText("5");
			}
			
			int exitVal = -32767;
			for (int i = 0; i < tout; i++) {
				if (tout - i != 1)
					test2.stat("Waiting for " + (tout - i) + " seconds...");
				else
					test2.stat("Refreshing...");
				
				test2.pause(1);
				try {
					exitVal = pro1.exitValue();
				} catch (IllegalThreadStateException e) {
					exitVal = -32767;
				}
				if (exitVal != -32767)
					break;
				if (test2.buttonTargets.getLabel().equals("Refresh Targets") == true)
					break;
			}
			
			test2.targetsm.setRowCount(0);
			
			try {
				res1 = new Scanner(new FileReader(test2.grimwepaPath + "targets-01.csv"));
			} catch (FileNotFoundException e) {
				System.out.println("Error: FileNotFoundException: File: " + test2.grimwepaPath + "targets-01.csv");
				System.out.println("airodump-ng csv file was not found. The command GrimWepa used was:");
				System.out.println(blah);
				for (int i = 0; i < blah.length; i++) {
					System.out.print(blah[i] + " ");
				} System.out.print("\n");
				//System.out.println(xterm + "airodump-ng " + chan + "-w " + test2.grimwepaPath + "targets --output-format csv " + drv);
				System.out.println("If this command does not create a file 'targets.csv' when YOU run it, then there must be a problem with your airodump-ng, your user permissions in linux, or something else out of my control!");
				
				test2.buttonTargets.setLabel("Refresh Targets");
				res1 = null;
				continue;
			}
			
			clis = false;
			
			while (test2.clients.getItemCount() > 0) {
				test2.clients.removeItemAt(0);
				test2.pause(0.001);
			}
			while (test2.wpaclients.getItemCount() > 0) {
				test2.wpaclients.removeItemAt(0);
				test2.pause(0.001);
			}
			
			try {
				while ((res1.hasNext() == true) && (res1 != null)) {
					line = res1.nextLine();
					if (clis == true) {
						// we're into the clients...
						tempArr = line.split(",");
						if (tempArr.length >= 4) {
							if (tempArr[5].indexOf(":") >= 0) {
								test2.clients.addItem(tempArr[0]);
								test2.wpaclients.addItem(tempArr[0]);
							}
						}
					} else if ((line.indexOf("WEP") >= 0) || (line.indexOf("WPA")) >= 0) {
						tempArr = line.split(",");
						if (tempArr.length > 6) {
							test2.targetsm.addRow(new 
								String[]{tempArr[8],tempArr[13],tempArr[3],tempArr[5],tempArr[0]});
						}
					} else if (line.indexOf("Station MAC") >= 0) {
						clis = true;
					}
				}
			} catch (NullPointerException npe) {
				System.out.println("Null pointer exception! FUCK!");
				test2.buttonTargets.setLabel("Refresh Targets");
			}
		} while (test2.buttonTargets.getLabel().equals("Refresh Targets") == false);
		
		test2.stat("");
		
		test2.pause(0.1);
		pro1.destroy();
		test2.pause(0.1);
		
		if (test2.clients.getItemCount() == 0) {
			test2.clients.addItem("[no clients found]");
		}
		if (test2.wpaclients.getItemCount() == 0) {
			test2.wpaclients.addItem("[no clients found]");
		}
		
		// remove the capture file (clean up)
		try {
			Runtime.getRuntime().exec(test2.fixArgumentsPath("rm -rf !PATH!targets-01.csv"));
			test2.pause(0.3);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}