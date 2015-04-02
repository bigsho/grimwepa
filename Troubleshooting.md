# Trouble-Shooting #

  * Run as Root! I hate running executables as root, but I tried running GrimWepa as an unprivileged user and was met with errors.

  * Are you booting from a DVD, USB, or is it installed on your system? GrimWepa writes some files to the disk [temporarily](temporarily.md) and a DVD/non-persistent USB boot may cause the program to fail. You could try running GrimWepa from the directory /ramdisk/,
> where it should be able to save files -- Note: /ramdisk/ lets you write and save data to the RAM, which will be wiped upon reboot.

  * Make sure you choose a Wifi card that can go into monitor mode. Some wifi cards create a 'mon0' interface for a wifi card that is in montior mode; You may want to try both the wlan# and mon# interfaces until you find the right one.

  * Last possible check: To make sure airodump works with your wifi card, try typing (in console):
```
airodump-ng -w targets --output-format csv mon0
```
  * where _mon0_ is your wifi card's interface. This is the command that GrimWepa uses, and if targets-01.csv isn't created in the folder you run it in, this could mean there is a write-permissions problem.


---


If you are _still_ having problems, shoot me an email -- My email can be found in the README file, located inside of the grimwepa .jar file.  In your e-mail, please let me know your Operating system, if your operating system is run off of a DVD or USB or installed, your Wifi card type and chipset, your computer specs, and any other information you may think is useful.