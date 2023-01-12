import tcp.TCPClient;
import tcp.TCPServer;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class NodeData {
    private int tcpPort;
    private SimpleEntry<Integer, Integer> record;
    private List<SimpleEntry<String, Integer>> connections;

    private Map<SimpleEntry<String, Integer>, SimpleEntry<Thread, TCPClient>> clientsThreads;
    private Map<SimpleEntry<Thread, TCPServer>, SimpleEntry<String, Integer>> serversThreads;

    public NodeData(int tcpPort, SimpleEntry<Integer, Integer> record, List<SimpleEntry<String, Integer>> connections) {
        this.tcpPort = tcpPort;
        this.record = record;
        this.connections = connections;

        clientsThreads = new HashMap<>();
        serversThreads = new HashMap<>();
    }

    public void startNode() {
        startNodeServer(true);
        startNodeClients();
    }

    public void stopNode() {
        stopNodeServer();
        stopNodeClients();
    }

    private void handleRequestMessages(String message){
        var split = message.split(" ");
        var command = split[0];
        switch (command){
            case "set-value":
                setValueNode(split[1]);
                break;
            case "get-value":
                getValueNode(split[1]);
                break;
            case "find-key":
                findKeyNode(split[1]);
                break;
            case "get-max":
                getMaxNode(split[1]);
                break;
            case "get-min":
                getMinNode(split[1]);
                break;
            case "new-record":
                newRecordNode(split[1]);
                break;
            case "terminate":
                terminateNode();
                break;
        }
    }

    private void setValueNode(String parameter){
        var split = parameter.split(":");
        var newRecord = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        if(record.getKey().equals(newRecord.getKey())){
            record = newRecord;
        } else { // Forward the message
            var responses = sendRequestMessage("set-value " + parameter);
        }
    }

    private void getValueNode(String parameter){

    }

    private void findKeyNode(String parameter){

    }

    private void getMaxNode(String parameter){

    }

    private void getMinNode(String parameter){

    }

    private void newRecordNode(String parameter){
    }

    private void terminateNode(){

        stopNode();
        System.exit(0);
    }

    private void startNodeServer(boolean join) {
        var server = new TCPServer(tcpPort);
        var thread = new Thread(() -> {
            server.startServer();
            // Start new node server waiting for another client
            startNodeServer(false);

            // Until it is alive keep reading
            while (server.isAlive()) {
                var message = server.readLine();
                if (message != null) {
                    System.out.println(message);
                    handleRequestMessages(message);
                }
            }
            // When it is not alive anymore, remove it from the list
            server.stopServer();
            serversThreads.remove(new SimpleEntry<>(Thread.currentThread(), server));
        });
        serversThreads.put(new SimpleEntry<>(thread, server), null);
        thread.start();
        if(join){
            try { // Run on the main thread
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopNodeServer(){
        for (var connection : serversThreads.keySet()) {
            // Stop the server
            connection.getValue().stopServer();
            // Stop the server's thread
            connection.getKey().interrupt();
        }
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

    private List<String> sendRequestMessage(String message){
        var responses = new ArrayList<String>();
        for (var connection : clientsThreads.keySet()) {
            var TcpClient = clientsThreads.get(connection).getValue();
            TcpClient.sendMessage(message);
            responses.add(TcpClient.readLine());
        }
        for(var connection : serversThreads.keySet()){
            var TcpServer = connection.getValue();
            TcpServer.sendMessage(message);
            responses.add(TcpServer.readLine());
        }
        return responses;
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
