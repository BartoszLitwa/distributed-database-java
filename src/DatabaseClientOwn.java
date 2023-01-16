import java.util.MissingFormatArgumentException;

public class DatabaseClientOwn {
    public static void main(String[] args) {
        var clientData = setupClient(args);
    }

    private static ClientData setupClient(String[] args) {
        String record = null;
        String operation = null;
        String parameter = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-gateway")) {
                record = args[i + 1];
            } else if (args[i].equals("-operation")) {
                parameter = i + 1 < args.length ? args[i + 1] : null;
            }
        }

        var clientData = new ClientData(record);
        clientData.startClient();

        switch (operation){
            case "set-value":
                var splitSetValue = parameter.split(":");
                clientData.setValue(splitSetValue[0], splitSetValue[1]);
                break;
            case "get-value":
                clientData.getValue(Integer.parseInt(parameter));
                break;
            case "find-key":
                clientData.findKey(Integer.parseInt(parameter));
                break;
            case "get-max":
                clientData.getMax();
                break;
            case "get-min":
                clientData.getMin();
                break;
            case "new-record":
                var splitNewRecord = parameter.split(":");
                clientData.newRecord(splitNewRecord[0], splitNewRecord[1]);
                break;
            case "terminate":
                clientData.terminate();
                break;
            default:
                throw new MissingFormatArgumentException("Operation not recognized");
        }

        return clientData;
    }
}
