package engine.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Vector3dn;
import game.player.Player;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import static engine.network.NetworkOutput.sendOutHandshake;
import static game.CrafterServer.isGameShouldClose;
import static game.chunk.Chunk.genBiome;
import static game.player.Player.*;

public class NetworkThread {

    private static final int port = 30_150;

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
        Server server = new Server();
        server.start();
        try {
            server.bind(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("AKA: THERE'S ALREADY A SERVER ON THIS PORT BOI");
        }


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {

                if (object instanceof String) {
                    String request = (String)object;
                    System.out.println(request);

                    String response = "WOW THANK YOU!";
                    connection.sendTCP(response);
                }
            }
        });

        /*
        new Thread(() -> {

            //used for raw data conversion
            final ObjectMapper objectMapper = new ObjectMapper();

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Socket socket = null;

            try {
                while (!isGameShouldClose()) {

                    try {
                        assert serverSocket != null;
                        serverSocket.setSoTimeout(10);
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        //e.printStackTrace(); <-THIS WILL SPAM YOUR TERMINAL LIKE CRAZY
                        //System.out.println("SKIPPIN");
                        continue;
                    }

                    DataInputStream dataInputStream;

                    try {
                        assert socket != null;
                        dataInputStream = new DataInputStream(socket.getInputStream());
                    } catch (IOException e) {
                        continue;
                        //e.printStackTrace();
                    }

                    InetAddress inetAddress = socket.getInetAddress();

                    boolean readingData = true;
                    while (readingData) {
                        byte messageType;
                        try {
                            messageType = dataInputStream.readByte();
                        } catch (IOException e) {
                            //e.printStackTrace();
                            break;
                        }

                        switch (messageType) {

                            //handshake
                            case 1 -> {
                                try {
                                    String playerHandshakeName = dataInputStream.readUTF();
                                    if (!playerExists(playerHandshakeName)) {
                                        sendOutHandshake(inetAddress, playerHandshakeName);
                                        System.out.println(playerHandshakeName + " has connected");
                                        addPlayer(playerHandshakeName,inetAddress);
                                    } else {
                                        sendOutHandshake(inetAddress, "KILL");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //player position
                            case 2 -> {
                                try {
                                    String position = dataInputStream.readUTF(); //vector 3fn
                                    Vector3dn newPosition = objectMapper.readValue(position, Vector3dn.class);
                                    if (newPosition != null) {
                                        Player player = getPlayerByName(newPosition.name);
                                        if (player != null) {
                                            player.pos.x = newPosition.x;
                                            player.pos.y = newPosition.y;
                                            player.pos.z = newPosition.z;
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            case 3 -> {
                                try {
                                    String chunkRequest = dataInputStream.readUTF(); //String
                                    //Vector3dn newPosition = objectMapper.readValue(position, Vector3dn.class);
                                    System.out.println(Arrays.toString(inetAddress.getAddress()) + " has requested chunks: " + chunkRequest);
                                    Player thisPlayer = getPlayerByInet(inetAddress);
                                    if (thisPlayer != null) {
                                        thisPlayer.chunkLoadingQueue.add(chunkRequest);
                                        String xString = chunkRequest.split(" ")[0];
                                        int x = Integer.parseInt(xString);
                                        String zString = chunkRequest.split(" ")[1];
                                        int z = Integer.parseInt(zString);
                                        genBiome(x, z);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            default -> readingData = false;
                        }
                    }
                }
            } finally {
                try {
                    assert serverSocket != null;
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert socket != null;
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

         */
    }
}
