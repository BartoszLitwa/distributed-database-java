package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.AbstractMap;

public class TCPClient {
    // Key - address, value - port
    private AbstractMap.SimpleEntry<String, Integer> connection;
    private boolean alive = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader input;

    public TCPClient(AbstractMap.SimpleEntry<String, Integer> connection) {
        this.connection = connection;
    }

    public boolean sendMessage(String message) {
        try {
            out.println(message);
            String clientInput = input.readLine();
            System.out.println(clientInput);
            return true;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + connection.getKey());
            return false;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + connection.getKey());
            return false;
        }
    }

    public String readLine() {
        try {
            return input.readLine();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + connection.getKey());
            return null;
        }
    }

    public void startClient(){
        try {
            InetAddress serverAddress = InetAddress.getByName(connection.getKey());
            System.out.println("server ip address: " + serverAddress.getHostAddress());

            socket = new Socket(serverAddress, connection.getValue());
            out = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            alive = true;
            System.out.println("Client started");
        } catch(UnknownHostException e1) {
            System.out.println("Unknown host exception " + e1.toString());
        } catch(IOException e2) {
            System.out.println("IOException " + e2.toString());
        } catch(IllegalArgumentException e3) {
            System.out.println("Illegal Argument Exception " + e3.toString());
        } catch(Exception e4) {
            System.out.println("Other exceptions " + e4.toString());
        }
    }

    public void stopClient(){
        try {
            input.close();
            out.close();
            socket.close();
            alive = false;
            System.out.println("Client stopped");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlive() {
        return alive;
    }
}
