/** GrimWepa v1.10
	Derv82[at]gmail.com
	
    Copyright (C) 2010 Derv Merkler
	
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.-
	
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.-
	
    For more info, see http://www.gnu.org/licenses/.
*/

public class threadURAPyrit implements Runnable {
	/** pyrit cracking thread*/
	Thread t;
	
	public static boolean flag;
	public static boolean cracked;
	public static String wordlist;
	
	public threadURAPyrit(String wlist) {
		t = new Thread(this, "threadURAPyrit");
		flag = false;
		cracked = false;
		wordlist = wlist;
	}
	
	public void run() {
		String input[], command;
		boolean moveon;
		
		command = 	"pyrit" +
					" -e \"" + Methods.currentSSID + "\"" +
					" -f " + wordlist + " passthrough" +
					" | " +
					" cowpatty" +
					" -d - -r !PATH!wpa-01.cap" +
					" -s \"" + Methods.currentSSID + "\"";
		
		input = Methods.readExec(command);
		
		moveon = false;
		for (int i = 0; i < input.length; i++) {
			if (input[i].indexOf("something") >= 0) {
				// good we can move on..
				moveon = true;
			}
		}
		
		if (!moveon)
			flag = false;
			return;
		
	}
	
}

/** GrimWepa v1.10
	Copyright (C) 2010 Derv Merkler
*/