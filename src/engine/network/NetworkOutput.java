package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.chunk.ChunkObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static engine.network.NetworkThread.getGameOutputPort;

public class NetworkOutput {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendOutHandshake(InetAddress ip, String playerHandshakeName) {
        Socket socket = null;
        {
            try {
                socket = new Socket(ip.getHostAddress(), getGameOutputPort());
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
        Socket socket = null;
        {
            try {
                socket = new Socket(ip.getHostAddress(), getGameOutputPort());
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
            dataOutputStream.writeByte(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String stringedChunk = objectMapper.writeValueAsString(thisChunk);
            dataOutputStream.writeUTF(stringedChunk);
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
}
