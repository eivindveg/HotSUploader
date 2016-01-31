package ninja.eivind.hotsreplayuploader.concurrent.tasks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.versions.VersionHandshakeToken;

public class SocketListenerTask extends Task<Void>
{
    @Override
    protected Void call() throws Exception
    {
        InetAddress loopback = InetAddress.getLoopbackAddress();

        try(ServerSocket ss = new ServerSocket(27000, 5, loopback)) {
            while(!Thread.currentThread().isInterrupted())
                if(readSocket(ss))
                    return null;
        }

        //let task fail, if interrupted
        throw new RuntimeException(); //TODO add message
    }

    private boolean readSocket(ServerSocket ss) throws IOException {
        VersionHandshakeToken tokenA = new VersionHandshakeToken();
        ObjectMapper mapper = new ObjectMapper();

        try(Socket socket = ss.accept();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            String read = dis.readUTF();
            VersionHandshakeToken tokenB = mapper.
                    readValue(read, VersionHandshakeToken.class);

            //same version, verify and unhide stage
            if(tokenA.equals(tokenB)) {
                dos.writeUTF(mapper.writeValueAsString(tokenA));
                return false;
            }
        }

        return true;
    }

}
