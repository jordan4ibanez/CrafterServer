# CrafterServer
The server for Crafter


# Building with ANT:

You must install ANT (Another Neat Tool) for the build to work.

Once you have ANT installed, you can simply CD to the Crafter directory.

Check build.properties to make sure that ``jdk.home.11=`` is pointing to your openJDK 16 install.

Run ``ant -keep-going``

Once it says build successful, you must create a folder for the game, preferably on your desktop.

Drop the jar from the build directory (WHEREVER/CrafterServer/out/artifacts/Crafter_jar/CrafterServer.jar) to your new folder directory.

You should now be able to run the server using java -jar CrafterServer.jar in the folder you created.


# Heads up:

This is under heavy prototyping

Things might change rapidly until beta
