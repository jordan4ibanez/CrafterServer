package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Vector3dn;
import game.player.Player;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static engine.network.NetworkOutput.sendOutHandshake;
import static game.CrafterServer.isGameShouldClose;
import static game.player.Player.getPlayer;
import static game.player.Player.playerExists;

public class NetworkThread {

    private static final int port = 30_150; //minetest, why not

    public static int getGamePort(){
        return port;
    }

    //if players send garbage data, break connection, destroy player object

    /*
    data chart: (base 1 like LUA - 0 reserved for null data)
    1 - handshake, check username
    2 - position for players object (JACKSON CONVERSION)
    3 - requested chunk data
     */

    public static void startNetworkThread() {
        new Thread(() -> {

            //used for raw data conversion
            final ObjectMapper objectMapper = new ObjectMapper();

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


                InetAddress inetAddress = socket.getInetAddress();

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
                                        String playerHandshakeName = dataInputStream.readUTF();
                                        if (!playerExists(playerHandshakeName)){
                                            sendOutHandshake(inetAddress, playerHandshakeName);
                                            System.out.println("ADD PLAYER TO INET ADDRESSES");
                                        } else {
                                            sendOutHandshake(inetAddress, "KILL");
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        //player position
                        case 2 ->
                                {
                                    try {
                                        String position =  dataInputStream.readUTF(); //vector 3fn
                                        Vector3dn newPosition = objectMapper.readValue(position, Vector3dn.class);
                                        if (newPosition != null){
                                            Player player = getPlayer(newPosition.name);
                                            if (player != null){
                                                player.pos.x = newPosition.x;
                                                player.pos.y = newPosition.y;
                                                player.pos.z = newPosition.z;
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        default -> readingData = false;
                    }
                }

                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
