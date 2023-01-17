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
    }

    public void stopNode() {
        stopNodeClients();
        stopNodeServer();
    }

    private void handleRequestMessages(PrintWriter writer, SimpleEntry<String, String> data){
        var split = data.getKey().split(" ");
        var clientAddress = data.getValue();

        if(split[0].contains("-NODE-")){ // Node connection
            split[0] = split[0].substring(split[0].indexOf("-NODE-") + 6);
        } else { // Only when received from client
            clientAddress = "Received From Client";
        }

        var command = split[0];
        switch (command.trim()){
            case "set-value":
                setValueNode(writer, split[1], clientAddress);
                break;
            case "get-value":
                getValueNode(writer, split[1], clientAddress);
                break;
            case "find-key":
                findKeyNode(writer, split[1], clientAddress);
                break;
            case "get-max":
                getMaxNode(writer, clientAddress);
                break;
            case "get-min":
                getMinNode(writer, clientAddress);
                break;
            case "new-record":
                newRecordNode(writer, split[1], clientAddress);
                break;
            case "terminate": // Received from client
                terminateNode(writer, "", clientAddress);
                break;
            case "terminateNode": // Received from other node
                terminateNode(writer, split[1], clientAddress);
                break;
            case "connectedNewNode":
                connectedNewNode(writer, clientAddress);
                break;
        }
    }

    private void setValueNode(PrintWriter writer, String parameter, String clientAddress){
        var split = parameter.split(":");
        var newRecord = new SimpleEntry<Integer, Integer>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        boolean success = false;
        if(record.getKey() == newRecord.getKey()){
            record = newRecord;
            success = true;
        }

        var responses = sendRequestMessage("set-value " + parameter, clientAddress);
        for (var res : responses) {
            if(res.equals("OK")){
                success = true;
            }
        }
        writer.println(success ? "OK" : "ERROR");
        writer.flush();
    }

    private void getValueNode(PrintWriter writer, String parameter, String clientAddress){
        var result = record.getValue() == Integer.parseInt(parameter) ? record.getKey() + ":" + record.getValue() : "ERROR";
        if(result.equals("ERROR")){
            var responses = sendRequestMessage("get-value " + parameter, clientAddress);
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

    private void findKeyNode(PrintWriter writer, String parameter, String clientAddress){
        var result = record.getKey() == Integer.parseInt(parameter) ? tcpServer.getAddressPort() : "ERROR";
        if(result.equals("ERROR")){
            var responses = sendRequestMessage("find-key " + parameter, clientAddress);
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

    private void getMaxNode(PrintWriter writer, String clientAddress){
        var responses = sendRequestMessage("get-max", clientAddress);
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

    private void getMinNode(PrintWriter writer, String clientAddress){
        var responses = sendRequestMessage("get-min", clientAddress);
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

    private void newRecordNode(PrintWriter writer, String parameter, String clientAdress){
        var split = parameter.split(":");
        record = new SimpleEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        System.out.println("New record: " + parameter);
        writer.println("OK");
        writer.flush();
    }

    private void terminateNode(PrintWriter writer, String parameter, String clientAddress){
        if(parameter.equals("")) { // We want to terminate this node
            System.out.println("Sending terminate message to all neighbour nodes");
            var responses = sendRequestMessage("terminateNode " + tcpServer.getAddressPort(), clientAddress);
            for (var response : responses) {
                if (!response.equals("OK")) {
                    writer.println("ERROR");
                    writer.flush();
                    return;
                }
            }

            writer.println("OK");
            writer.flush();

            System.out.println("---Terminated node: " + tcpServer.getAddressPort() + "---");
            stopNode(); // Calls System.exit(0)
        } else { // We have been informed that another node is going to terminate
            System.out.println("Received terminate message from " + parameter);
            var client = clientsThreads.get(parameter);
            if(client != null){
                // Cleanup the client
                client.getValue().stopClient();
                client.getKey().interrupt();
                clientsThreads.remove(parameter);
            }
            // Remove the connection from the list
            if(connections.contains(parameter)){
                connections.remove(parameter);
            } else {
                tcpServer.removeConnection(parameter);
            }

            writer.println("OK");
            writer.flush();
        }
    }

    private void connectedNewNode(PrintWriter writer, String clientAddress) {
        System.out.println("Received connectedNewNode message from " + clientAddress);
        tcpServer.addConnection(clientAddress);
    }

    private void startNodeServer() {
        tcpServer = new TCPServer(tcpPort);
        tcpServer.startServer();
        System.out.println("Server started: " + tcpServer.getHostAddress() + ":" + tcpServer.getPort());

        startNodeClients();

        // Until it is alive keep listening for new connections
        tcpServer.listen((data, writer) -> {
            try {
                System.out.println("Received message: " + data.getKey());
                handleRequestMessages(writer, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void stopNodeServer(){
        tcpServer.stopServer();
    }

    private void startNodeClients() {
        System.out.println("Informing neighbours about new node");
        sendRequestMessage("connectedNewNode", tcpServer.getAddressPort());
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

    private List<String> sendRequestMessage(String message, String clientAddress){
        var responses = new ArrayList<String>();
        var merged = new HashSet<String>(connections);
        merged.addAll(tcpServer.getNodesConnected());
        merged.add(tcpServer.getAddressPort());

        var clientsVisited = clientAddress.split("\\|");
        for (var client : clientsVisited) {
            if(merged.contains(client)){
                merged.remove(client);
            }
            merged.add(client);
        }
        merged.remove("Received From Client");

        var stringBuilder = new StringBuilder();
        for (var connection : merged) {
            stringBuilder.append(connection + "|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1); // Remove last |

        if(message.contains("connectedNewNode")){
            message = tcpServer.getAddressPort() + "-NODE-" + message;
        } else {
            message = stringBuilder.toString() + "-NODE-" + message;
        }

        for(var sentToAlready : clientsVisited){
            merged.remove(sentToAlready);
        }

        merged.remove(tcpServer.getAddressPort());
        System.out.println("Sending message: " + message + " to " + merged);
        for (var connection : merged) {
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
