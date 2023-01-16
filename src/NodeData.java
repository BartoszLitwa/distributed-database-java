import tcp.TCPClient;
import tcp.TCPServer;

import java.io.PrintWriter;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class NodeData {
    private int tcpPort;
    private SimpleEntry<Integer, Integer> record; // Key, Value
    private List<String> connections;

    private Map<String, SimpleEntry<Thread, TCPClient>> clientsThreads;
    private TCPServer tcpServer;

    public NodeData(int tcpPort, SimpleEntry<Integer, Integer> record, List<String> connections) {
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
        stopNodeClients();
    }

    private void handleRequestMessages(PrintWriter writer, String message){
        var split = message.split(" ");
        if(split[0].startsWith("NODE-")){
            split[0] = split[0].substring(5);
        }

        var command = split[0];
        switch (command){
            case "set-value":
                setValueNode(writer, split[1]);
                break;
            case "get-value":
                getValueNode(writer, split[1]);
                break;
            case "find-key":
                findKeyNode(writer, split[1]);
                break;
            case "get-max":
                getMaxNode(writer);
                break;
            case "get-min":
                getMinNode(writer);
                break;
            case "new-record":
                newRecordNode(writer, split[1]);
                break;
            case "terminate":
                terminateNode(writer, "");
                break;
            case "terminateNode":
                terminateNode(writer, split[1]);
                break;
        }
    }

    private void setValueNode(PrintWriter writer, String parameter){
        var split = parameter.split(":");
        var newRecord = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        boolean success = false;
        if(record.getKey() == newRecord.getKey()){
            record = newRecord;
            success = true;
        }

        var responses = sendRequestMessage("NODE-set-value " + parameter);
        for (var res : responses) {
            if(res.equals("OK")){
                writer.println("OK");
                writer.flush();
                success = true;
                break;
            }
        }
        writer.println(success ? "OK" : "ERROR");
        writer.flush();
    }

    private void getValueNode(PrintWriter writer, String parameter){
        var result = record.getKey() == Integer.parseInt(parameter) ? record.getKey() + ":" + record.getValue() : "ERROR";
        if(result.equals("ERROR")){
            var responses = sendRequestMessage("NODE-get-value " + parameter);
            for (var response : responses) {
                if(!response.equals("ERROR")){
                    result = response;
                    break;
                }
            }
        }

        System.out.println("Get Value - " + parameter + ": " + result);
        writer.println(result);
        writer.flush();
    }

    private void findKeyNode(PrintWriter writer, String parameter){
        var result = record.getKey() == Integer.parseInt(parameter) ? tcpServer.getHostAddress() + ":" + tcpServer.getPort() : "ERROR";
        if(result.equals("ERROR")){
            var responses = sendRequestMessage("NODE-find-key " + parameter);
            for (var response : responses) {
                if(!response.equals("ERROR")){
                    result = response;
                    break;
                }
            }
        }
        System.out.println("Find Key - " + parameter + ": " + result);
        writer.println(result);
        writer.flush();
    }

    private void getMaxNode(PrintWriter writer){
        var responses = sendRequestMessage("NODE-get-max");
        var max = record.getValue();
        var recordMax = record;
        for (var response : responses) {
            var split = response.split(":");
            var entry = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            if(entry.getValue() > max){
                max = entry.getValue();
                recordMax = entry;
            }
        }
        System.out.println("The max value is " + max + " and the record is " + recordMax.getKey());
        writer.println(recordMax.getKey() + ":" + recordMax.getValue());
        writer.flush();
    }

    private void getMinNode(PrintWriter writer){
        var responses = sendRequestMessage("NODE-get-min");
        var min = record.getValue();
        var recordMin = record;
        for (var response : responses) {
            var split = response.split(":");
            var entry = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            if(entry.getValue() < min){
                min = entry.getValue();
                recordMin = entry;
            }
        }
        System.out.println("The min value is " + min + " and the record is " + recordMin.getKey());
        writer.println(recordMin.getKey() + ":" + recordMin.getValue());
        writer.flush();
    }

    private void newRecordNode(PrintWriter writer, String parameter){
        var split = parameter.split(":");
        record = new SimpleEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        writer.println("OK");
        writer.flush();
    }

    private void terminateNode(PrintWriter writer, String parameter){
        if(parameter.equals("")) { // We want to terminate this node
            System.out.println("Sending terminate message to all neighbour nodes");
            var responses = sendRequestMessage("NODE-terminateNode " + tcpServer.getHostAddress() + ":" + tcpServer.getPort());
            for (var response : responses) {
                if (!response.equals("OK")) {
                    writer.println("ERROR");
                    writer.flush();
                    return;
                }
            }
            stopNode();
            System.out.println("---Terminated node: " + tcpServer.getHostAddress() + ":" + tcpServer.getPort() + "---");

        } else { // We have been informed that another node is going to terminate
            System.out.println("Received terminate message from " + parameter);
            var client = clientsThreads.get(parameter);
            if(client != null){
                // Cleanup the client
                client.getValue().stopClient();
                client.getKey().interrupt();
                clientsThreads.remove(parameter);
                if(connections.contains(parameter)){
                    connections.remove(parameter);
                } else {
                    tcpServer.removeConnection(parameter);
                }
            }
        }
        writer.println("OK");
        writer.flush();
    }

    private void startNodeServer() {
        tcpServer = new TCPServer(tcpPort);
        tcpServer.startServer();
        System.out.println("Server started: " + tcpServer.getHostAddress() + ":" + tcpServer.getPort());

        // Until it is alive keep listening for new connections
        tcpServer.listen((message, writer) -> {
            try {
                System.out.println("Received message: " + message);
                handleRequestMessages(writer, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void stopNodeServer(){
        tcpServer.stopServer();
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
            var client = clientsThreads.get(connection);
            if(client == null)
                continue;
            // Stop the client
            client.getValue().stopClient();
            // Stop the client's thread
            client.getKey().interrupt();
        }
    }

    private List<String> sendRequestMessage(String message){
        var responses = new ArrayList<String>();
        var merged = new HashSet<String>(connections);
        merged.addAll(tcpServer.getNodesConnected());
        for (var connection : merged) {
            System.out.println("Sending message to " + connection);
            var TcpClient = new TCPClient(connection);
            if(!TcpClient.startClient()){ // Could not connect to the client
                continue;
            }
            TcpClient.sendMessage(message);
            responses.add(TcpClient.readLine());
            TcpClient.stopClient();
        }

        return responses;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public SimpleEntry<Integer, Integer> getRecord() {
        return record;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void setRecord(SimpleEntry<Integer, Integer> record) {
        this.record = record;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }
}
