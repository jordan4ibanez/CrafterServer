# CrafterServer
The server for Crafter

A blocky game written in java with LWJGL 3

Based off of what I've learned from Minetest's engine and lua api

Discord: https://discord.gg/fEjssvEYMH

Required Java version (JRE): 16

You can get this version at: https://jdk.java.net/16/

Or you can install openJDK on most linux distros.

Ubuntu: ``sudo apt install openjdk-16-jdk``

Fedora: ``sudo dnf install java-latest-openjdk-devel.x86_64``

To update default JRE: sudo update-alternatives --config java

# Building with ANT:

You must install ANT (Another Neat Tool) for the build to work.

Once you have ANT installed, you can simply CD to the Crafter directory.

Check build.properties to make sure that ``jdk.home.16=`` is pointing to your openJDK 16 install.

You can find this with: ``readlink -f $(which java)`` (Don't copy the bin/java part)

Run ``ant -keep-going``

Once it says build successful, you must create a folder for the game, preferably on your desktop.

Drop the jar from the build directory (WHEREVER/CrafterServer/out/artifacts/Crafter_jar/CrafterServer.jar) to your new folder directory.

You should now be able to run the server using java -jar CrafterServer.jar in the folder you created.


# Heads up:

This is under heavy prototyping

Things might change rapidly until beta
