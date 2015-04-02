# GRIM WEPA INSTALLATION GUIDE #

### STEP -1 : SETUP ###

Grim Wepa requires certain applications in order to function properly.

**You need Java**

Java comes standard on Backtrack 4, but there is a chance that your distribution doesn't have Java installed.

If you don't have java (typing 'java' into a console gives you a 'command not found' error), you can install the Java Runtime Environment (JRE) using the command:
```
apt-get install sun-java6-jre
```

In other distributions, you may need to add a repository if 'sun-java6-jre' is not found.
You need to add the line
```
deb http://archive.canonical.com/lucid partner
```
to the sources list located at /etc/apt/sources.list

_Then_ run the apt-get install command from before.

**You need aircrack-ng**

aircrack-ng comes standard on Backtrack4, but if you need to install it (or want the newest version 1.1), run these these commands in a console (run as root!):
```
wget http://download.aircrack-ng.org/aircrack-ng-1.1.tar.gz
tar -zxvf aircrack-ng-1.1.tar.gz
cd aircrack-ng-1.1
make
make install
```

This will install aircrack-ng and all of the tools that comes with it (aireplay-ng, airodump-ng, airmon-ng, packetforge-ng, and aircrack-ng, as well as other applications).

### STEP 0 : Before we start ###

**Run these commands as root!**
To login as root, type:
```
su
```
then enter the password for your root account.

If you can not get internet access on your system, this install guide can't help you : that's beyond the scope of these instructions. Also, if you are booting from a DVD, then the "installation" will not be permanent. If you don't fall into these two categories, installation of GrimWepa should be a breeze.  The first step is simply downloading the grimstall.sh file, so if you already have it, skip to STEP 2!


### STEP 1 : DOWNLOAD the GrimWepa install script (grimstall.sh) ###

Open a console (Terminal) window (it's the black-box icon near the menu button on the lower-left).
You should see a black background with text that says something like:
```
root@bt:~#
```

To download the grimwepa install script, type the following into the console:
```
wget http://grimwepa.googlecode.com/files/grimstall.sh
```
Alternatively, you could copy that code and paste it into console using Shift+Insert. The wget command will download the install script to the current directory that console is in (usually the default is root). You should now see the file "grimstall.sh" appear (next to other files/directories) when you type:
```
ls
```
Note: the first letter is a lower-case L, not a capital i.

After you have the GrimWepa install script on your computer (or in RAM via the /ramdisk/ directory), you can move on.


### STEP 2 : CHANGE PERMISSIONS of the install script ###

We can't just run the shell script file : we need to change the permissions before we can run it. To change the permissions, type this into the same console window:
```
chmod 755 grimstall.sh
```
You won't see any output or confirmation, so just assume that the permissions were changed accordingly.


### STEP 3 : INSTALL!!! ###

_The grimstall.sh script **will download (if needed) and install grimwepa v1.0 from this project homepage to your computer!**_

Now that we have grimstall.sh with the correct permissions, we can begin the install. In the same console that we've been typing in, type:
```
./grimstall.sh install /pentest/wireless/grimwepa/
```
GrimWepa will default to install to the directory '/pentest/wireless/grimwepa/' if no other directory is given. This directory common in Backtrack 3 and Backtrack 4 linux distributions, but is not very popular in others.  If you want to install to your own directory, type:
```
./grimstall.sh install /home/whatever/directories/you/want/
```
**Don't forget the trailing / at the end!**
You should see an output showing GrimWepa being installed. After the install script is done running, you can test if it worked by typing (into console):
```
grimwepa
```
GrimWepa should open up (after a few seconds), proving the installation was successful.
If you chose to install to '/pentest/wireless/grimwepa/', then GrimWepa should also be in your menu under "BackTrack > Radio Network Analysis > 80211 > Cracking". You can also run GrimWepa by typing "grimwepa" into any console anywhere!

You can now remove the downloaded grimwepa\_1.0.jar file (you shan't be needing it now that the jar is saved in whatever directory you chose).

If this installation guide doesn't work, or if I should add/change something (experts), let me know via e-mail so I can correct the problems. Hopefully there are no errors; if you have any, copy/paste the errors to me in an e-mail! I <3 bug reports

..........

### STEP 99 : UNINSTALL GrimWepa ###

So you enjoyed GrimWepa, but now you're grown up, tired of shoddily-automated scripts, and want to banish GrimWepa to the great /dev/null in the sky, eh?

To undo everything that the install script did (basically, to Uninstall GrimWepa), type:
```
./grimstall.sh remove /pentest/wireless/grimwepa/
```
GrimWepa defaults to uninstall grimwepa from /pentest/wireless/grimwepa/ if no other directory is given ("./grimstall.sh remove").  If you installed GrimWepa to a different directory, type the directory after the 'remove'.

You'll still have to delete the .jar file you downloaded manually:
```
rm grimwepa_1.0.jar
```

But you don't want to uninstall it... right?


Thanks for using Grim Wepa!!!