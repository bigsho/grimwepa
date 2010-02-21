# GrimWepa install script
# 
# to INSTALL, type:
#    ./install.sh install
# 
# to UNINSTALL (remove entirely from your system), type:
#    ./install.sh remove
# 
# 'install' argument will do the following:
#   1. creates folder /pentest/wireless/grimwepa/
#   2. copies .jar file to /pentest/wireless/grimwepa
#   3. creates link to /pentest/wireless/grimwepa in /usr/bin/ as 'grimwepa'
#   4. create /usr/share/applications/grimwepa.desktop , add text to it, update menus (for menu)

# version is subject to change
FILE="grimwepa_0.9.jar"

if [ $1 ]; then
	# user asked to install GrimWepa
	if [ $1 = "install" ]; then
		# check if jar file is in this directory...
		if [ ! -f ${FILE} ]; then
			# if the grimwepa jar isn't in the current directory...
			echo "[!] Error! In order to install grimwepa,"
			echo "    you must run this script in the directory"
			echo "    which has the file '${FILE}'"
			
			exit 0
		fi
		
		echo "[-] Removing old GrimWepa files..."
		# remove old files
		rm -rf /pentest/wireless/grimwepa/
		
		echo "[+] Creating new GrimWepa directory"
		# create new folder
		mkdir /pentest/wireless/grimwepa/
		
		echo "[+] Copying grimwepa files to directory..."
		# copy jar file to pentest
		cp ${FILE} /pentest/wireless/grimwepa/
		
		echo "[+] Creating /usr/bin/ execution link..."
		# create /usr/bin/grimwepa file with execution line inside (so it can be called from anywhere)
		echo "java -jar /pentest/wireless/grimwepa/${FILE} &" > /usr/bin/grimwepa
		
		echo "[+] Changing permissions of execution link..."
		# change permissions so it can be run
		chmod 755 /usr/bin/grimwepa
		
		echo "[-] Clearing GrimWepa's old desktop file..."
		# clear grimwepa.desktop file
		# rm -rf /usr/share/applications/grimwepa.desktop
		
		echo "[+] Creating new 'GrimWepa' desktop entry..."
		# create new desktop entry:
		echo "[Desktop Entry]"            > /usr/share/applications/grimwepa.desktop
		echo "Comment="                  >> /usr/share/applications/grimwepa.desktop
		echo "Exec=grimwepa"             >> /usr/share/applications/grimwepa.desktop
		echo "GenericName="              >> /usr/share/applications/grimwepa.desktop
		echo "Icon=cache"                >> /usr/share/applications/grimwepa.desktop
		echo "Name=GrimWepa"             >> /usr/share/applications/grimwepa.desktop
		echo "Path="                     >> /usr/share/applications/grimwepa.desktop
		echo "StartupNotify=true"        >> /usr/share/applications/grimwepa.desktop
		echo "Terminal=0"                >> /usr/share/applications/grimwepa.desktop
		echo "TerminalOptions="          >> /usr/share/applications/grimwepa.desktop
		echo "Type=Application"          >> /usr/share/applications/grimwepa.desktop
		echo "X-KDE-SubstituteUID=false" >> /usr/share/applications/grimwepa.desktop
		echo "X-KDE-Username="           >> /usr/share/applications/grimwepa.desktop
		echo "Categories=BT-Radio-Network-Analysis-80211-Cracking" >> /usr/share/applications/grimwepa.desktop
		
		echo "[+] Updating menus..."
		# update menus with new .desktop information
		update-menus
		
		echo "[!] Installation complete!"
		echo "[!] GrimWepa is now in the menu under BT > Radio & Network Analysis > 80211 > Cracking"
		echo "[!] You can now delete ${FILE} from this directory"
		echo "[!] Save this install.sh file if you wish to uninstall GrimWepa in the future."
		echo "[!] Type 'grimwepa' to run!"
		
		exit 0
	elif [ $1 = "remove" ]; then
		# user asked to remove/uninstall GrimWepa
		echo "[!] Removing GrimWepa from your system..."
		
		echo "[-] Removing files from /pentest/wireless/grimwepa/ ..."
		rm -rf /pentest/wireless/grimwepa/*
		
		echo "[-] Removing directory: /pentest/wireless/grimwepa/ ..."
		rm -rf /pentest/wireless/grimwepa/
		
		echo "[-] Removing /usr/bin/ execution link..."
		rm -rf /usr/bin/grimwepa
		
		echo "[-] Remove GrimWepa from menu..."
		rm -rf /usr/share/applications/grimwepa.desktop
		
		echo "[-] Updating menu..."
		update-menus
		
		echo "[!] GrimWepa has been completely removed from your system"
		
		exit 0
	fi
fi


echo "[?] GrimWepa installation shell script."
echo "[?] Usage: ./install.sh [install/remove]"
echo ""
echo "[?] To install GrimWepa to your system, "
echo "    run this install.sh file in the same "
echo "    directory as ${FILE} with this argument:"
echo "./install.sh install"
echo ""
echo "[?] To remove GrimWepa from your system, "
echo "    run this install.sh file with 'remove' as an argument:"
echo "./install.sh remove"
