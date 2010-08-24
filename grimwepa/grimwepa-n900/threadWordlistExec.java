import java.io.IOException;

public class threadWordlistExec implements Runnable {
	/** wordlist cracking thread*/
	Thread t;
	
	/** cracking process, public so we don't have a zombie process later*/
	public static Process process;
	/** flag, tells us when to stop*/
	public static boolean flag;
	/** command to execute*/
	public static String command[];
	
	/** constructor, initializes variables*/
	public threadWordlistExec(String com[]) {
		t = new Thread(this, "threadWordlistExec");
		process = null;
		flag = false;
		command = com;
	}
	
	/** executes wordlist generator command, 
		waits for user to click 'stop gen.' OR for the command to finish executing
	*/
	public void run() {
		try {
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (IOException ioe) {
		} catch (InterruptedException ie) {}
		
		int exitCode = -1, time = 0;
		do {
			Methods.pause (1);
			
			time++;
			GuiWordlistGen.lblStatus.setText(" waiting for sort to complete... " + secToHMS(time));
			try {
				exitCode = process.exitValue();
			} catch (IllegalThreadStateException itse) {}
			
		} while (exitCode == -1 && !flag && GuiWordlistGen.btnGenerate.getLabel().equals("stop gen."));
		
		process.destroy();
		
		GuiWordlistGen.lblStatus.setText(" wordlist generation complete.");
		
		GuiWordlistGen.btnGenerate.setLabel("generate");
		
	}
	
	public String secToHMS(int sec) {
		int h = sec / 3600;
		String sh = sec + "";
		
		sec %= 3600;
		int m = sec / 60;
		String sm = sec + "";
		
		sec %= 60;
		String ss = sec + "";
		
		String result = "";
		if (!sh.equals("0"))
			result = sh + "h";
		if (!sm.equals("0") || !result.equals(""))
			result += (result.equals("") ? "" : " ") + sm + "m";
		
		if (result.equals("")) {
			result = ss;
		} else {
			result += (result.equals("") ? "" : " ") + ss + "s";
		}
		
		return result;
	}
	
}