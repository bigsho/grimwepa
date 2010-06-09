/** load targets from airodump-ng's CSV dump file into the Gui's tabTargets list
*/

import java.io.IOException;

public class threadTargetScan implements Runnable {
	/** target scanning thread*/
	Thread t;
	
	/** creates new thread*/
	public threadTargetScan() {
		t = new Thread(this, "threadTargetScan");
	}
	
	/** deletes all airodump-ng log files,
		runs airodump-ng,
		watches airodump-ng's '.csv' output file,
		adds items from the file to the Gui.tabTargets list,
		waits until user clicks 'stop' aka flag = false
	*/
	public void run() {
		String[] tempArr = new String[20];
		
		Methods.stat("initializing scan");
		
		// remove the airodump-ng dump file(s)
		Methods.removeFile("targets-01.ivs");
		Methods.removeFile("targets-01.cap");
		Methods.removeFile("targets-01.csv");
		Methods.removeFile("targets-01.kismet.csv");
		Methods.removeFile("targets-01.kismet.netxml");
		
		// build the command, step-by-step:
		String command = ""; // start out blank
		
		// add xterm info to the front if user wants feedback...
		if (Gui.chkHideWin.isSelected() == false) {
			String col = (String)Gui.cboColors.getSelectedItem();
			command = 	"xterm" +
						" -fg " + col + 
						" -bg black" +
						" -bd " + col + 
						" -geom 100x15+0+0" +
						" -T gw-targetscan" +
						" -iconic" +
						" -e ";
		}
		
		// obviously going to be using this
		command += "airodump-ng -a ";
		
		// add channel (if selected)
		if (Gui.chkChannel.isSelected())
			command += "";
		else
			command += "-c " + Gui.sldChannel.getValue() + " ";
		
		// add output path
		command += "-w !PATH!targets ";
		
		// add wifi driver
		command += (String)Gui.cboDrivers.getItemAt(Gui.cboDrivers.getSelectedIndex());
		
		// run command
		Process pro1 = null;
		try {
			if (Methods.verbose)
				System.out.println("exec:\t" + command.replaceAll("!PATH!", Methods.grimwepaPath));
			pro1 = Runtime.getRuntime().exec(Methods.fixArgumentsPath(command));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// loop, waiting and adding targets from .csv file to list
		do {
			// time out is, by default, 3
			int tout = 3;
			try {
				// try to read timeout from textbox
				tout = Integer.parseInt(Gui.txtTargetTimeout.getText());
			} catch (NumberFormatException e) {
				// if we get an error, user didn't enter an umber correctly!
				tout = 3;
				Gui.txtTargetTimeout.setText("3");
			}
			
			// default exitval is negative
			int exitVal = -32767;
			
			for (int i = 0; i < tout; i++) {
				if (tout - i != 1)
					Methods.stat("waiting for " + (tout - i) + " seconds...");
				else
					Methods.stat("refreshing...");
				
				Methods.pause(1);
				try {
					exitVal = pro1.exitValue();
				} catch (IllegalThreadStateException e) {
					exitVal = -32767;
				}
				
				if (exitVal != -32767 || Gui.btnTargets.getLabel().equals("refresh targets"))
					break;
			}
			
			int selected = -1;
			String last_bssid = "";
			// get index of selected target
			try {
				selected = Gui.tabTargets.getSelectedRow();
				// if target is actually selected...
				if (selected >= 0) {
					// remember the bssid of who was selected
					last_bssid = (String)Gui.tabTargets.getValueAt(selected, 4);
				}
			} catch (NullPointerException npe) {}
			
			// clear targets list in preparation for new ones
			Gui.dtmTargets.setRowCount(0);
			
			// reset clients array
			Methods.clients = new String[]{""};
			
			String lines[] = Methods.readFile(Methods.grimwepaPath + "targets-01.csv");
			
			boolean hitClients = false;
			String aps = "";
			
			Gui.cboWepClients.removeAllItems();
			Gui.cboWpaClients.removeAllItems();
			
			// read every line of the targets-01.csv airodump file
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i]; // current line
				
				if (hitClients == true) {
					// we're now into the clients...
					
					tempArr = line.split(","); // it's a CSV, split by comma's
					
					if (tempArr.length >= 4) {
						tempArr[5] = tempArr[5].trim();
						// look at the 6th comma-separated item, if it contains ":" then ti's a CLIENT!
						if (tempArr[5].indexOf(":") >= 0) {
							// use addClient method from Methods.java to remember this client
							Methods.addClient(tempArr[5], tempArr[0]);
							
							if (Methods.currentBSSID != null && Methods.currentBSSID.equals(tempArr[5])) {
								Gui.cboWepClients.addItem(tempArr[0]);
								Gui.cboWpaClients.addItem(tempArr[0]);
							}
						}
					}
					
				} else if ((line.indexOf("WEP") >= 0) || (line.indexOf("WPA")) >= 0) {
					tempArr = line.split(",");
					if (tempArr.length > 6) {
						aps += "," + tempArr[0];
						Gui.dtmTargets.addRow(new 
							String[]{tempArr[8],tempArr[13],tempArr[3],tempArr[5],tempArr[0]});
					}
				} else if (line.indexOf("Station MAC") >= 0) {
					hitClients = true;
					if (aps.equals("") == false) {
						aps = aps.substring(1);
						Methods.clients = aps.split(",");
					}
				}
			}
			
			// re-select the last-selected item, based on BSSID
			if (!last_bssid.equals("")) {
				// go through every item in the list looking for that bssid
				for (int i = 0; i < Gui.tabTargets.getRowCount(); i++) {
					String cur_bssid = (String)Gui.tabTargets.getValueAt(i, 4);
					if (cur_bssid.equals(last_bssid)) {
						// if we found it, select it!
						Gui.tabTargets.setRowSelectionInterval(i, i);
						break;
					}
				}
			}
			
		} while (Gui.btnTargets.getLabel().equals("refresh targets") == false);
		
		pro1.destroy();
		
		String found = "" + Methods.clients.length;
		if (found.equals("1") && Methods.clients[0].length() == 0)
			found = "0";
		Methods.stat("scan complete, " + found + " access point" + 
										(found.equals("1") ? "" : "s") + " found.");
		
		if (Gui.cboWepClients.getItemCount() == 0) {
			Gui.cboWepClients.addItem("[no clients found]");
		}
		if (Gui.cboWpaClients.getItemCount() == 0) {
			Gui.cboWpaClients.addItem("[no clients found]");
		}
		
		// remove the airodump-ng dump file(s)
		Methods.removeFile("targets-01.cap");
		Methods.removeFile("targets-01.csv");
		Methods.removeFile("targets-01.kismet.csv");
		Methods.removeFile("targets-01.kismet.netxml");
		
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/