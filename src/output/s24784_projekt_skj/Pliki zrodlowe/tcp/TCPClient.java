package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    // Key - address, value - port
    private String connection;
    private boolean alive = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader input;

    public TCPClient(String connection) {
        this.connection = connection;
    }

    public boolean sendMessage(String message) {
        try {
            if(socket == null || socket.isClosed() || out == null) return false;

            out.println(message);
            out.println("\n");
            out.flush();
            return true;
        } catch (Exception e) {
            System.out.println("Couldn't get I/O for the connection to " + connection);
            stopClient();
            return false;
        }
    }

    public String readLine() {
        try {
            if(socket == null || socket.isClosed() || input == null) return "";

            return input.readLine();
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + connection);
            stopClient();
            return null;
        }
    }

    public boolean startClient(){
        try {
            InetAddress serverAddress = InetAddress.getByName(connection.split(":")[0]);
            System.out.println("Starting Client - server ip address: " + connection);

            socket = new Socket(serverAddress, Integer.parseInt(connection.split(":")[1]));
            out = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            alive = true;
            return true;
        } catch(Exception e1) {
            System.out.println("Exception " + e1.toString());
            stopClient();
            return false;
        }
    }

    public void stopClient(){
        try {
            if(out != null) out.close();
            if(input != null) input.close();
            if(socket != null && !socket.isClosed()) socket.close();
            alive = false;
        } catch (IOException e) {
            System.out.println("Couldn't close I/O for the connection to " + connection);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public String getHostAddress(){
        return socket.getInetAddress().getHostAddress();
    }

    public int getPort(){
        return socket.getPort();
    }
}
