package ninja.eivind.hotsreplayuploader.concurrent.tasks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.versions.VersionHandshakeToken;

/**
 * A {@link Task}, which constantly listens on a local port and indicates an already running
 * instance of the program.<br>
 * Propagates the command to bring the application to the front via
 * changing the workDoneProperty and the shutdown command by succeeding.
 */
public class SocketListenerTask extends Task<Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(SocketListenerTask.class);

    private long count = 0;

    @Override
    protected Void call() throws Exception
    {
        InetAddress loopback = InetAddress.getLoopbackAddress();

        try(ServerSocket ss = new ServerSocket(27000, 5, loopback)) {
            while(!Thread.currentThread().isInterrupted())
                if(readSocket(ss))
                    return null;
        }

        throw new RuntimeException("Socket listener thread was interrupted.");
    }

    private boolean readSocket(ServerSocket ss) throws IOException {
        VersionHandshakeToken tokenA = new VersionHandshakeToken();
        ObjectMapper mapper = new ObjectMapper();

        try(Socket socket = ss.accept();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            String read = dis.readUTF();
            dos.writeUTF(mapper.writeValueAsString(tokenA));

            VersionHandshakeToken tokenB = mapper.
                    readValue(read, VersionHandshakeToken.class);

            //reject wrong applications
            if(!tokenA.getApplicationName().equals(tokenB.getApplicationName()))
                return false;

            if(tokenA.compareTo(tokenB) >= 0)  //same or newer version
                updateProgress(count++, count);
            else  //we're running an old version, terminate
                return true;
        }
        catch (Exception e) {
            LOG.error("Handshake failed", e);
        }

        return false;
    }

}
