_GRIM WEPA_ was written in Java and is intended for use with the Linux Operating System (specifically the [Backtrack 4](http://www.backtrack-linux.org/) distribution).

GrimWepa 1.1 has been translated for Português-Brasil users.  It is available in the [downloads section](http://grimwepa.googlecode.com/files/grimwepa-por.jar).

# Update #

### GRIM WEPA is no longer being supported ###

GRIM WEPA is on an indefinite hiatus while I work on other projects.

Please use Wifite instead of GRIM WEPA.  Wifite is a newer wifi cracker with more functionality and stability than GRIM WEPA.
Wifite is available here: [http://code.google.com/p/wifite/](http://code.google.com/p/wifite/)

Please update your bookmarks and links accordingly.

This project will remain open so that I may eventually update GrimWepa.


# Overview #

![http://grimwepa.googlecode.com/files/awesome1.png](http://grimwepa.googlecode.com/files/awesome1.png)

GRIM WEPA is a password cracker for both WEP and WPA-encrypted access points (routers).  This program uses the following applications and suites:
  * **aircrack-ng suite:**
    * aircrack-ng, to crack WPA and WEP;
    * airodump-ng, to capture packets and find access points;
    * airmon-ng, to enumerate devices in monitor mode;
    * aireplay-ng, to forge and replay packets;
    * and packetforge-ng, to create replay packets.
  * iwconfig, to see if devices are in monitor mode;
  * xterm, to show output to user;
  * ifconfig, to get the MAC address of devices;
  * macchanger, to change MAC address of wifi cards.
These applications are **required** for GRIM WEPA to run properly.
All of these applications come standard with Backtrack4.

_note: the settings & configuration file for Grim Wepa is saved to /etc/grimwepa.conf_

# About #

GRIM WEPA's cracking methods are archaic and have been around for years. It simply uses the existing cracking methods in aireplay-ng (for WEP) and aircrack-ng (for WPA).  Grim Wepa is similar in style and functionality to shamanvirtuel's Spoon series (SpoonWEP, SpoonWPA, and SpoonDRV). The Spoon suite is still available, though it is not kept updated.

The Backtrack 4 Linux distribution has a default WEP/WPA cracker, but it does not work properly for me; also, the Spoon series does not run properly for me on BT4, so I created GRIM WEPA for myself and as an homage to shamanvirtuel.


# Options #

GRIM WEPA has only two options: Crack WEP-encrypted access points (routers) and crack WPA-encrypted access points.  The program can search for new targets, and auto-selects your cracking method.  The options for each method are as follows:

### Attacks for WEP-encrypted Access Points ###

  * ARP-Replay attack
  * Chop-chop attack
  * Fragmentation attack
  * p0841 attack
  * Cafe-Latte attack
  * Cracking options:
    * aircrack-ng is able to crack just about any WEP password after about 20,000 IV (Initialization Vector) data packets have been captured. The capture usually takes about 2 minutes, and the crack another 2-3 minutes.


### Attacks for WPA-encrypted Access Points ###

  * Basic deauthorization attack to get handshake.
  * Cracking:
    * GRIM WEPA includes a 2MB default password list containing approximately 250,000 commonly-used passwords.
    * Wordlist / Dictionary / Brute-force attack: Currently, there is only one consistent method of cracking WPA, and that is by brute force.  aircrack-ng can crack hundreds of passwords per second, so this method is not nearly as arbitrary as has been proposed.


# Execution #
To run GRIM WEPA, navigate to the file's location in Terminal and type:
```
java -jar grimwepa_X.X.jar
```
at the command line prompt, where **X.X** is your version of grimwepa.

**Run GRIM WEPA as _root!_**

I have posted a [Step-by-Step Tutorial](http://code.google.com/p/grimwepa/wiki/Tutorial), and also a [Troubleshooting Guide](http://code.google.com/p/grimwepa/wiki/Troubleshooting).


# Installation #

**Installation is not required for GRIM WEPA to run properly, but it is recommended if you use are going to GRIM WEPA frequently.**

GrimWepa can be downloaded and installed by running the "grimstall.sh" script.

**For Backtrack Users:**
To download the install script via wget, change permissions on it, and run the install script (which will download the latest version of grimwepa and install it), copy-and-paste the below code into console (as root!):
```
wget http://grimwepa.googlecode.com/files/grimstall.sh
chmod 755 grimstall.sh
./grimstall.sh install

```
_Note: Change the directory from /pentest/wireless/grimwepa/ to whichever directory you want to install to ; /pentest/wireless is commonly found in Backtrack distributions ; all files in the selected directory will be deleted (a prompt will confirm this); **don't forget the / at the end!**_

**A more-detailed installation guide can be found [here, in the wiki](http://code.google.com/p/grimwepa/wiki/Installation).**

# Sample Videos #
Thanks to [Weasek](http://www.youtube.com/user/weasel1617) from the [Backtrack-Linux forums](http://www.backtrack-linux.org/forums/) for providing these videos!

**An example of using the WEP Fragmentation attack:**

<a href='http://www.youtube.com/watch?feature=player_embedded&v=7RSZP0zAAJE' target='_blank'><img src='http://img.youtube.com/vi/7RSZP0zAAJE/0.jpg' width='425' height=344 /></a>


**And an example of the WPA attack (deauthentication, handshake, and brute-force dictionary attack):**

<a href='http://www.youtube.com/watch?feature=player_embedded&v=3GdorNVFv6U' target='_blank'><img src='http://img.youtube.com/vi/3GdorNVFv6U/0.jpg' width='425' height=344 /></a>