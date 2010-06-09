/** splash screen<p>
	displays for 1.5 seconds, then disappears
	started by Main.java's main method
*/

import java.awt.*;
import java.awt.event.*;

public class GuiSplash extends Frame implements ActionListener {
	/** splash screen*/
	public SplashScreen splash;
	
	/** i honestly have no idea what this does
		@param g no clue, 2d graphics object?
		@param frame the frame? maybe?
	*/
    static void renderSplashFrame(Graphics2D g, int frame) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.BLACK);
    }
	
	/** constructor for this GUI;
		builds a pointless frame and displays the splash screen
	*/
    public GuiSplash() {
        super("grim wepa | splash screen"); // no one will ever see this frame!
        
		// get the splash screen
        splash = SplashScreen.getSplashScreen();
        if (splash == null) {
        	// splash screen can't be displayed, show this
			System.out.println("*******************");
			System.out.println("******E*P*I*C******");
            System.out.println("***SPLASH SCREEN***");
            System.out.println("*******************");
            return;
        }
		
		// show the splash screen
        Graphics2D g = splash.createGraphics();
        if (g == null) {
			// splash screen can't be displayed, show this
            System.out.println("*******************");
			System.out.println("******E*P*I*C******");
            System.out.println("***SPLASH SCREEN***");
            System.out.println("*******************");
            return;
        }
    }
	
	/** called by the Main window to close the splash screen
		@see Main#main(String[])
	*/
	public void exit() {
		try {
			splash.close();
		} catch (IllegalStateException ise) {}
	}
	
	/** event is called when anyone tries to do anything
		@param ae info on the event
	*/
    public void actionPerformed(ActionEvent ae) {
        dispose();
    }
	
	/** event is called when user tries to close window
	*/
    private static WindowListener closeWindow = new WindowAdapter(){
        public void windowClosing(WindowEvent e){
            e.getWindow().dispose();
        }
    };
	
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/