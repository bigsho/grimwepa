/** main class<p>
	displays splash screen while loading main window;
	checks if user is on a Linux machine 
	and if user is logged in as root, exits if eitehr are false
	
	@author derv
	@version 1.10a6 05/29/10
*/

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
	/** program version, for updating purposes */
	public static final String VERSION = "grimwepa-n900.jar";
	
	/** main program window*/
	public static Gui guiWindow;
	
	/** main method; where the magic happens
		@param args arguments passed via the command line (unused by this program)
	*/
	public static void main(String[] args) {
		// check arguments
		Methods.verbose = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-v") || args[i].equals("--verbose"))
				Methods.verbose = true;
		}
		
		// load and display splash screen
		GuiSplash gs = new GuiSplash();
		
		// check for operating system / user is root
		if (!Methods.startupCheck())
			System.exit(0);
		
		// load gui window (program's main window) while splash is displayed
		guiWindow = new Gui("GRIM WEPA v1.10 rN900");
		
		// wait for a second (show off the awesome artwork)
		Methods.pause(1);
		
		// hide the slash screen
		gs.setVisible(false);
		
		// continue loading the rest of main window
		guiWindow.afterLoad();
		
		// get rid of splash screen
		gs.exit();
		gs.dispose();
	}
	
} 

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/