import tcp.TCPClient;
import tcp.TCPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class NodeData {
    private int tcpPort;
    private String address;
    private SimpleEntry<Integer, Integer> record;
    private List<SimpleEntry<String, Integer>> connections;

    private Map<SimpleEntry<String, Integer>, SimpleEntry<Thread, TCPClient>> clientsThreads;
    private TCPServer tcpServer;

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
        stopNodeClients();
    }

    private void handleRequestMessages(BufferedReader reader, PrintWriter writer, String message){
        var split = message.split(" ");
        var command = split[0];
        switch (command){
            case "set-value":
                setValueNode(reader, writer, split[1]);
                break;
            case "get-value":
                getValueNode(reader, writer, split[1]);
                break;
            case "find-key":
                findKeyNode(reader, writer, split[1]);
                break;
            case "get-max":
                getMaxNode(reader, writer, split[1]);
                break;
            case "get-min":
                getMinNode(reader, writer, split[1]);
                break;
            case "new-record":
                newRecordNode(reader, writer, split[1]);
                break;
            case "terminate":
                terminateNode(reader, writer);
                break;
        }
    }

    private void setValueNode(BufferedReader reader, PrintWriter writer, String parameter){
        var split = parameter.split(":");
        var newRecord = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        if(record.getValue() == newRecord.getValue()){
            writer.println("OK");
            writer.flush();
        } else {
            var responses = sendRequestMessage("set-value " + parameter);
            for (var res : responses) {
                if(res.equals("OK")){
                    writer.println("OK");
                    writer.flush();
                    return;
                }
            }
        }
    }

    private void getValueNode(BufferedReader reader, PrintWriter writer, String parameter){
        var result = record.getValue() == Integer.parseInt(parameter) ? tcpServer.getHostAddress() + ":" + tcpServer.getPort() : "ERROR";
        if(result.equals("ERROR")){
            var responses = sendRequestMessage("get-value " + parameter);
            for (var response : responses) {
                if(!response.equals("ERROR")){
                    result = response;
                }
            }
        }

        System.out.println("Get Value - " + parameter + ": " + result);
        writer.println(result);
        writer.flush();
    }

    private void findKeyNode(BufferedReader reader, PrintWriter writer, String parameter){
        var responses = sendRequestMessage("find-key " + parameter);
        var result = record.getKey() == Integer.parseInt(parameter) ? tcpServer.getHostAddress() + ":" + tcpServer.getPort() : "ERROR";
        for (var response : responses) {
            System.out.println(response);
            if(!response.equals("ERROR")){
                result = response;
            }
        }
        System.out.println("Find Key - " + parameter + ": " + result);
        writer.println(result);
        writer.flush();
    }

    private void getMaxNode(BufferedReader reader, PrintWriter writer, String parameter){
        var responses = sendRequestMessage("get-max " + parameter);
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

    private void getMinNode(BufferedReader reader, PrintWriter writer, String parameter){
        var responses = sendRequestMessage("get-min " + parameter);
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

    private void newRecordNode(BufferedReader reader, PrintWriter writer, String parameter){
        var split = parameter.split(":");
        record = new SimpleEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        writer.println("OK");
        writer.flush();
    }

    private void terminateNode(BufferedReader reader, PrintWriter writer){
        writer.println("OK");
        writer.flush();
        System.out.println("Terminating node");
        stopNode();
    }

    private void startNodeServer() {
        tcpServer = new TCPServer(tcpPort);
        tcpServer.startServer();
        System.out.println("Server started: " + tcpServer.getHostAddress() + ":" + tcpServer.getPort());
        // Start new node server waiting for another client
        // startNodeServer(false);

        // Until it is alive keep reading
        tcpServer.listen((reader, writer) -> {
            try {
                String message = reader.readLine();
                System.out.println("Received message: " + message);
                handleRequestMessages(reader, writer, message);
            } catch (IOException e) {
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
