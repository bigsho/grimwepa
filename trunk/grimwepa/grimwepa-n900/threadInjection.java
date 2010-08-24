/** executes code to test injection, outputs results
*/

public class threadInjection implements Runnable {
	/** injection test thread*/
	Thread t;
	
	/** flag tells other classes if we are done with the injection test or not,
		true = done; false = still testing*/
	public static boolean flag;
	
	/** creates new thread
	*/
	public threadInjection() {
		t = new Thread(this, "threadInjection");
		flag = false;
	}
	
	/** runs aireplay-ng injection test, parses output and 
		sets the main window's status bar accordingly
	*/
	public void run() {
		Methods.stat("testing injection...");
		boolean sucess = false;
		
		String command =    "aireplay-ng" + 
							" -9" + 
							" -a " + Methods.currentBSSID +
							" " + (String)Gui.cboDrivers.getSelectedItem();
		if (Methods.verbose)
			System.out.println(command);
		
		String output[] = Methods.readExec(command);
		
		for (int i = 0; i < output.length; i++) {
			
			if (Methods.verbose) 
				System.out.println(output[i]);
			
			if (output[i].indexOf("/30:") >= 0) {
				Methods.stat("injection test results: " + output[i]);
				flag = true;
				sucess = true;
				break;
			} else if (output[i].indexOf("Failure: ") >= 0) {
				Methods.stat("injection test failed: " + output[i].substring(9));
				sucess = true;
				break;
			}
		}
		
		if (!sucess) {
			Methods.stat("injection test failed.");
		}
		
		flag = true;
		Methods.proInjection.destroy();
		Gui.btnWepTestinj.setLabel("test injection");
	}
	
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/
