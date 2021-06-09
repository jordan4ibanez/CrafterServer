package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

import static engine.network.NetworkThread.getGamePort;

public class NetworkOutput {


    /*
    data byte list:
    0: reserved for null
    1: handshake
    2: TODO
    3: chunks
    4: player data objects (multiplayer other players)
     */

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendOutHandshake(InetAddress ip, String playerHandshakeName) {
        /*
        Socket socket = null;
        {
            try {
                socket = new Socket(ip.getHostAddress(), getGamePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        OutputStream outputStream = null;

        {
            try {
                assert socket != null;
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        try {
            dataOutputStream.writeByte(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(playerHandshakeName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.flush(); // Send off the data
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPlayerChunkData(InetAddress ip, ChunkObject thisChunk) {
        Socket socket;
        {
            try {
                socket = new Socket(ip.getHostAddress(), getGamePort());
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }

        OutputStream outputStream;

        {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        try {
            dataOutputStream.writeByte(3);
        } catch (IOException e) {
            //e.printStackTrace();
            return;
        }
        try {

            ChunkSavingObject savingObject = new ChunkSavingObject();

            savingObject.I = thisChunk.ID;
            savingObject.x = thisChunk.x;
            savingObject.z = thisChunk.z;
            savingObject.b = thisChunk.block;
            savingObject.r = thisChunk.rotation;
            savingObject.l = thisChunk.light;
            savingObject.h = thisChunk.heightMap;
            savingObject.e = thisChunk.lightLevel;

            String stringedChunk = objectMapper.writeValueAsString(savingObject);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(stringedChunk);
            objectOut.close();
            byte[] bytes = baos.toByteArray();
            dataOutputStream.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.flush(); // Send off the data
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPlayerPosition(InetAddress ip, PlayerPosObject playerPosObject) {
        Socket socket;
        {
            try {
                socket = new Socket(ip.getHostAddress(), getGamePort());
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }

        OutputStream outputStream;

        {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        try {
            dataOutputStream.writeByte(4);
        } catch (IOException e) {
            //e.printStackTrace();
            return;
        }
        try {

            String stringedPlayer = objectMapper.writeValueAsString(playerPosObject);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(stringedPlayer);
            objectOut.close();
            byte[] bytes = baos.toByteArray();
            dataOutputStream.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.flush(); // Send off the data
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
    }
}
