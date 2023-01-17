import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

public class DatabaseNode {
    public static void main(String[] args) {
        NodeData databaseNode = null;

        try {
            databaseNode = setupNode(args);
            databaseNode.startNode();
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally { // Dispose node and threads
            if (databaseNode != null) {
                databaseNode.stopNode();
            }
        }
    }

    private static NodeData setupNode(String[] args) {
        var tcpPort = -1;
        SimpleEntry<Integer, Integer> record = null;
        var connections = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-tcpport")) {
                tcpPort = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-record")) {
                String[] split = args[++i].split(":");
                record = new SimpleEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } else if (args[i].equals("-connect")) {
                String[] split = args[++i].split(":");
                connections.add(split[0] + ":" + Integer.parseInt(split[1]));
            }
        }

        if(tcpPort == -1){
            throw new MissingFormatArgumentException("TCP port not set");
        } else if(record == null){
            throw new MissingFormatArgumentException("Record not set");
        }

        return new NodeData(tcpPort, record, connections);
    }
}
