package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TCPServer {
    private int port = -1;
    private boolean alive = false;
    private String address = null;

    private ServerSocket serverSocket;
    private List<Thread> threadList;
    private List<String> nodesConnected;

    public TCPServer(int port) {
        this.port = port;
        threadList = new ArrayList<>();
        nodesConnected = new ArrayList<>();
    }

    public void startServer() {
        try {
            if(port == -1){
                throw new Exception("Port not set");
            }
            serverSocket = new ServerSocket(port);
            System.out.println("wainting for clients...");
            address = serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort();

            alive = true;
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void listen(BiConsumer<String, PrintWriter> operation) {
        int index = 0;
        while(alive){ // Wait for new clients
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client(" + index++ + ") connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                var thread = new Thread(() -> {
                    BufferedReader input = null;
                    PrintWriter out = null;
                    try {
                        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(socket.getOutputStream(), true);

                        var message = input.readLine();
                        var address = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                        if(message.startsWith("NODE-") && !nodesConnected.contains(address)){
                            nodesConnected.add(address);
                            System.out.println("Added to Node connected: " + address);
                        }

                        operation.accept(message, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            if(input != null) input.close();
                            if(out != null) out.close();
                            if(socket != null) socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            } catch (SocketException se){
                System.out.println("Socked Exception - Server stopped");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer(){
        try {
            alive = false;
            serverSocket.close();

            for (Thread thread : threadList) {
                thread.interrupt();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean isAlive(){
        return alive;
    }

    public String getHostAddress(){
        return serverSocket.getInetAddress().getHostAddress().contains("0.0.0.0") ?
                "localhost" : serverSocket.getInetAddress().getHostAddress();
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

    public List<String> getNodesConnected(){
        return nodesConnected;
    }

    public void removeConnection(String address){
        nodesConnected.remove(address);
    }
}
