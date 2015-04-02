# GRIMWEPA TUTORIAL #

### Start Up ###

To run GrimWepa, navigate to the JAR file in the console and type:
```
java -jar grimwepa_X.X.jar
```
(X.X being the version you have).
This will load the program and display the GUI within a few seconds.

When GrimWepa first loads, it will check if any devices are in Monitor Mode.
Monitor mode is a state that some wifi cards can be in so they can
collect data packets which are not destined for themselves.  This is
crucial for receiving data packets to crack WEP and WPA access points.

If there are no found devices in monitor mode, GrimWepa will enumerate all
possible wifi devices and ask you to select a device to put into
Monitor Mode.  Once you select a device and press "OK", GrimWepa will
put the device into monitor mode using airmon-ng, then add it to the list
of Wifi Devices, automatically selecting the device it just put into MM.

Once we have a selected wifi device, we can begin to search for targets.


---


### Search ###

Click the "Refresh Targets" button to begin the search for targets.  If you want
to only search a specific channel, uncheck the "All Channels" option and
drag the slider to the channel you want to stay on.

After a few seconds (usually around 5), access points should appear in the list.
Some people have reported that they get a FileNotFoundException printout
in the console.  This is usually due to an invalid Wifi Device selection.

Note: GrimWepa will display the airodump-ng window (minimized by default) in an
XTerm window.  You can view the airodump output by maximizing the window.
GrimWepa will ONLY ADD WEP AND WPA/WPA2 access points to the list!
If you are surrounded by OPEN networks and nothing else, the list will
remain empty!

After you see your target in the list, click "Stop Refreshing", select your target,
and depending on if your target is WPA or WEP, start the attack.


---


### Attacks ###

**WEP**

There are 5 kinds of WEP attacks:
  * Arp-replay,
  * Chop-chop,
  * Fragmentation,
  * Cafe-latte,
  * p0841.
Some attacks only work with certain access points I have had lots of luck with ARP-replay (on an access point with activity) and Fragmentation (on an access point with no clients/activity).  Your own wifi cards and access points will vary, so find a method that works for you.

It is not necessary to change your MAC address ("Change MAC" button) to crack WEP sucessfully, but some routers may require it.  Use Change MAC if other methods are unsucessful.

GrimWepa will automatically begin cracking the WEP key after it has collected 10,000 IVs (initialization vectors, or "Data" packets).  You can start the cracker prior to this point by clicking Start Cracking. This is not recommended, since the chances of cracking a WEP key prior to 10,000 IVs is low; also, if the key IS cracked right before 10,000 IVs are captured, then GrimWepa MAY ignore the first crack and start the cracking session over (this is a known bug).

When GrimWepa (aircrack-ng) has cracked your WEP password, it will display the password in the status bar and also save the key in a text file "wepcracked.txt" in the same directory as the Jar file.


---


**WPA**

We want to crack the WPA access point.

To crack it, we need to get the 4-way handshake.

To get the 4-way handshake, we need a client to connect to the access point.

The Deauthentication attack will try to deauthenticate a client (if one is selected) or the entire access point (if no client is selected).  To increase your chances of getting a handshake, get as close as possible to the access point and wait for someone to connect.  If there's already a client connected, select their MAC address so GrimWepa will deauthenticate them, forcing them to reconnect, and forcing a handshake to be broadcasted.

After a handshake is received, the "Start Cracking" will become enabled.

You can type in the location of a password list you would like to use (path) or type the path to a non-existent file ('use\_default\_damnit') to force GrimWepa to use the default password list.  The default password list contains about 250,000 passwords (all over 8 characters in length) that I compiled from many different wordlists.  I know that Backtrack 4 has a wordlists folder located at /pentest/passwords/wordlists/ at least
one file in there (darkc0de.lst).


---


Good luck!