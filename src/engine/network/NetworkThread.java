package engine.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static game.Crafter.isGameShouldClose;

public class NetworkThread {

    private static final int port = 30_150; //minetest, why not

    //if players send garbage data, break connection, destroy player object

    /*
    data chart: (base 1 like LUA - 0 reserved for null data)
    1 - handshake, check username
    2 - position for players object (JACKSON CONVERSION)
     */

    public static void startNetworkThread() {
        new Thread(() -> {

            while (!isGameShouldClose()) {

                ServerSocket server = null;
                Socket socket = null;

                try {
                    server = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert server != null;
                    socket = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                DataInputStream dataInputStream = null;

                try {
                    assert socket != null;
                    dataInputStream = new DataInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }



                boolean readingData = true;
                while (readingData) {
                    byte messageType = 0;
                    try {
                        assert dataInputStream != null;
                        messageType = dataInputStream.readByte();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    switch (messageType) {

                        //handshake
                        case 1 ->
                                {
                                    try {
                                        System.out.println("Message A: " + dataInputStream.readUTF());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        //player position
                        case 2 ->
                                {
                                    try {
                                        System.out.println("Message B: " + dataInputStream.readUTF());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        default -> readingData = false;
                    }
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
