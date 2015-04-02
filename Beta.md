# Alpha Testers Needed! #

If you want to help test and make Grim Wepa better (beta), then download the latest release from here:
[http://grimwepa.googlecode.com/files/grimwepa1.10a6.jar](http://grimwepa.googlecode.com/files/grimwepa1.10a6.jar)

You can copy/paste this code into a console window to download and run the latest release:

```
wget http://grimwepa.googlecode.com/files/grimwepa1.10a6.jar
java -jar grimwepa1.10a6.jar
```

This version (and future versions) will contain an installer within the program; so there is no longer a need for a 'grimstall.sh' script.

Please test as much as you can.  Post bugs, glitches, and the like to the [issues section](http://code.google.com/p/grimwepa/issues/list) OR email derv82 at gmail

All suggestions, comments, and bug reports are appreciated!!!

# Requirements #

### Required Applications ###
All of the required/recommended apps _should be_ checked when the program is first loaded.
If you are missing any of these apps, the program will alert you accordingly.  You can find information on downloading/installing these apps (if you don't have them) using the [Guide located here](http://code.google.com/p/grimwepa/source/browse/trunk/grimwepa1.1/GUIDE#265)
  * the aircrack-ng suite.  Tested with 1.0, should work with 1.1:
    * airmon-ng
    * aircrack-ng
    * airodump-ng
    * aireplay-ng
    * packetforge-ng

  * macchanger - for client-based wep attacks
  * ifconfig   - put device up/down
  * iwconfig   - check for monitor mode, signing on
  * sort      - for wordlist generation
  * wpa\_supplicant - sign on, intel4965 chipset workaround

### Recommended Applications ###

The below apps aren't required, but grimwepa will use them if
they are found -- Just make sure there's a link in /usr/bin/

  * tshark - strip the handshake from large capture files
  * pyrit  - cracking, super-fast, gpu-style.
  * crunch - generating passwords to passthrough to aircrack
  * uniq   - for removing duplicates in wordlist generation
  * pw-manager - for filtering passwords (wordlist generator)


# What's new in v1.10 #
  * splash screen
  * checks required/recommended apps at first load
  * checks OS and if user is signed in as root on every load
  * install (and uninstall) from inside the app
    * default install directory: /pentest/wireless/grimwepa/
    * creates link in /usr/bin/
    * optional: create desktop shortcut
    * optional: create menu shortcut
      * backtrack > radio network analysis > 80211 > cracking
    * update to newer versions of grim wepa when they are released
    * update to newer version of aircrack-ng and pyrit automatically
  * faster + more stable monitor-mode checks, asks to take device out of monitor mode on exit
  * clients of target AP's sorted based on BSSID > user can only choose clients of a select AP
  * corrected some of the WEP attacks
  * new WPA cracking methods:
    * dictionary attack with pyrit (buggy, no output, but works!)
    * crunch wordlist passthrough > passes generated passwords straight into aircrack-ng/pyrit
    * wordlist generator -> give it a lot of wordlist files and it'll combine/sort/filter/de-duplicate
    * "online wpa cracker" -> directs browser to 2 online wpa key resolution services
    * note: pyrit attacks are experimental right now (no output, really slow, etc)
  * handshake gets stripped by pyrit if user has pyrit in their /usr/bin/ folder
    * also tries to strip handshake using tshark if pyrit isn't found
  * all handshakes are backed up to ::grimwepaPath::/hs/
    * removes spaces/non-alpha-numeric chars from name, stores it as that name.cap
    * for example: ssid "Netgear! 5831" would be stored as "Netgear5831.cap"
  * stored passwords (key tank):
    * remembers crack method, date, time, ssid, bssid, encryption type, and (for wep) pps
    * displays most-recently-cracked password first for multiple entries
    * ability to view & remove previously-cracked AP's
    * backwards compatible with grimwepa v1.0
    * converts hex to ascii when applicable
    * signs onto WEP and WPA1 encrypted access points using iwconfig


### New in v1.10 alpha 6 ###
  * fixed '0 captured ivs' bug (for installed versions)
  * fixed crunch passthrough error: directories with spaces now work
  * fixed installer; works with spaces in install path
  * 'check for updates' (inside installer) also checks aircrack-ng and pyrit
    * installs aircrack-ng 1.1 or pyrit 0.3.0 if an older version is found on the user's computer.

### New in v1.10 alpha 5 ###
  * fixed wpa dictionary attack error :
    * attempted to open "xterm-fg", not just 'xterm'. whoops!
    * spaces in directory/file would cause attack to stop
  * fixed aireplay-ng zombie process for WEP attacks

### New in v1.10 alpha 4 ###
  * auto-signon checkbox for both WEP and WPA -- signs onto access point if/when key is compromised.
  * fixed signon for WPA (now handles WPA1, WPA2, and WPA1+2)
    * NOTE: uses iwlist to figure out encryption of access point
  * new WEP attack: 'passive capture'
    * does not attempt any fake-auth or any aireplay-ng attacks
    * listens to a network passively (non-intrusively).
    * useful when network is already generating lots of traffic (streaming, netflix, torrents, etc)
  * 'download wordlist' option for wordlist generator
    * user selects which files to download (8 total)
    * downloads from google-code page and stores in ::grimwepapath::/wordlists/
  * refreshing targets doesn't deselect access point (small fix)
  * included GUIDE (walk-through for beginners), it is extracted during installation
  * 'update' to latest version button, seen in the installer window.
    * NOTE: (won't work until there's an alpha 5 released)


### New in v1.10 alpha 3 ###
  * fixed injection test for WEP-based attacks.
  * added 'verbose' mode for debugging.
    * prints every command executed to the console.
    * also prints (some of) the system's response to commands.
    * useful, so testers can copy/paste commands and see what is going wrong.
    * to use, add -v argument when executing grimwepa, for example:
      * java -jar grimwepa1.10a3.jar -v
      * grimwepa -v


# Known Issues #


  * your wireless card may not work; this is not my fault
    * you can solve your own hardware issues using google
  * pyrit in this version is 'sketchy'
    * cannot run in an xterm window (no visual output)
    * the dump file gets updated every 10 min or so (infrequently)
    * (sometimes) waits for all input before it starts cracking
      * ...meaning crunch passthrough takes a LONG time
  * aircrack-ng is apparently 'unstable' with large wordlists (2GB +).
  * intel 4965 chipset fix works for some, but not all. unable to test
  * tested with aircrack-ng suite version 1.0 r.1661
    * uses '--output-format csv' arguments with airodump-ng
    * older versions (BT3's) do not have this option
  * directories with spaces in them have been buggy in the past
    * i tried fixing this bug in this version; I think it's fixed
  * some people can't run grimwepa using the openjre for java
    * please use the java6 from sun!
    * to get the Java Runtime Environment (JRE) type:
      * apt-get install sun-java6-jre