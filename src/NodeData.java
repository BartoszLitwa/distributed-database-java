import tcp.TCPClient;
import tcp.TCPServer;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class NodeData {
    private int tcpPort;
    private SimpleEntry<Integer, Integer> record;
    private List<SimpleEntry<String, Integer>> connections;

    private Map<SimpleEntry<String, Integer>, SimpleEntry<Thread, TCPClient>> clientsThreads;
    private SimpleEntry<Thread, TCPServer> serverThread;

    public NodeData(int tcpPort, SimpleEntry<Integer, Integer> record, List<SimpleEntry<String, Integer>> connections) {
        this.tcpPort = tcpPort;
        this.record = record;
        this.connections = connections;

        clientsThreads = new HashMap<>();
    }

    public void startNode() {
        startNodeServer();
        startNodeClients();
    }

    public void stopNode() {
        stopNodeServer();
    }

    private void startNodeServer() {
        var server = new TCPServer(tcpPort);
        var thread = new Thread(() -> {
            server.startServer();
        });
        serverThread = new SimpleEntry<>(thread, server);
        thread.start();
    }

    private void stopNodeServer(){
        // Stop the server
        serverThread.getValue().stopServer();
        // Stop the server's thread
        serverThread.getKey().interrupt();
    }

    private void startNodeClients() {
        for (var connection : connections) {
            var client = new TCPClient(connection);
            var thread = new Thread(() -> {
                client.startClient();
            });
            // Add it to our threads map
            clientsThreads.put(connection, new SimpleEntry<>(thread, client));
            thread.start();
        }
    }

    private void stopNodeClients(){
        for (var connection : connections) {
            // Stop the client
            clientsThreads.get(connection).getValue().stopClient();
            // Stop the client's thread
            clientsThreads.get(connection).getKey().interrupt();
        }
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public SimpleEntry<Integer, Integer> getRecord() {
        return record;
    }

    public List<SimpleEntry<String, Integer>> getConnections() {
        return connections;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void setRecord(SimpleEntry<Integer, Integer> record) {
        this.record = record;
    }

    public void setConnections(List<SimpleEntry<String, Integer>> connections) {
        this.connections = connections;
    }
}
