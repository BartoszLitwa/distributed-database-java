package tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;

public class TCPServer {
    private int port = -1;
    private boolean alive = false;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter out;

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean sendMessage(String message) {
        try {
            out.println(message);
            out.println("\n");
            out.flush();
            return true;
        } catch (Exception e) {
            System.err.println("Couldn't get I/O for the connection to " + port);
            return false;
        }
    }

    public String readLine() {
        try {
            return input.readLine();
        } catch (Exception e) {
            System.err.println("Couldn't get I/O for the connection to " + port);
            return null;
        }
    }

    public void startServer() {
        try {
            if(port == -1){
                throw new Exception("Port not set");
            }

            serverSocket = new ServerSocket(port);
            System.out.println("wainting for clients...");

            clientSocket = serverSocket.accept();
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress()
                    + ":" + clientSocket.getPort());
            alive = true;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void stopServer(){
        try {
            input.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            alive = false;
            System.out.println("Client stopped");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean isAlive(){
        return alive;
    }
}
