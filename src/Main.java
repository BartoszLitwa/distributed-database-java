import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

public class Main {
    public static void main(String[] args) {
        DatabaseNode databaseNode = null;
        try {
            databaseNode = setupNode(args);
            databaseNode.startNode();
        } catch (Exception e) {
            e.printStackTrace();
        } finally { // Dispose node and threads
            if (databaseNode != null) {
                databaseNode.stopNode();
            }
        }
    }

    private static DatabaseNode setupNode(String[] args) {
        var tcpPort = -1;
        AbstractMap.SimpleEntry<Integer, Integer> record = null;
        var connections = new ArrayList<AbstractMap.SimpleEntry<String, Integer>>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-tcpport")) {
                tcpPort = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("-record")) {
                String[] split = args[i + 1].split(":");
                record = new AbstractMap.SimpleEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } else if (args[i].equals("-connect")) {
                String[] split = args[i + 1].split(":");
                connections.add(new AbstractMap.SimpleEntry<>(split[0], Integer.parseInt(split[1])));
            }
        }

        if(tcpPort == -1){
            throw new MissingFormatArgumentException("TCP port not set");
        } else if(record == null){
            throw new MissingFormatArgumentException("Record not set");
        } else if(connections.size() == 0){
            throw new MissingFormatArgumentException("Connections not set");
        }

        return new DatabaseNode(tcpPort, record, connections);
    }
}
