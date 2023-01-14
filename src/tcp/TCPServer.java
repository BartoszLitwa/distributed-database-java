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

    private ServerSocket serverSocket;
    private List<Thread> threadList;

    public TCPServer(int port) {
        this.port = port;
        threadList = new ArrayList<>();
    }

    public void startServer() {
        try {
            if(port == -1){
                throw new Exception("Port not set");
            }
            serverSocket = new ServerSocket(port);
            System.out.println("wainting for clients...");

            alive = true;
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void listen(BiConsumer<BufferedReader, PrintWriter> operation) {
        int index = 0;
        while(alive){ // Wait for new clients
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client(" + index++ + ") connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                var thread = new Thread(() -> {
                    try {
                        var input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        var out = new PrintWriter(socket.getOutputStream(), true);

                        operation.accept(input, out);

                        System.out.println("Client stopped: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                        input.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            } catch (SocketException se){
                System.out.println("Server stopped");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer(){
        try {
            alive = false;
            serverSocket.close();

            System.out.println("Stopping threads...");
            for (Thread thread : threadList) {
                thread.interrupt();
            }

            System.out.println("Client stopped");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean isAlive(){
        return alive;
    }

    public String getHostAddress(){
        return serverSocket.getInetAddress().getHostAddress();
    }

    public int getPort(){
        return port;
    }
}
