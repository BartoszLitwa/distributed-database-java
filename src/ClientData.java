import tcp.TCPClient;

public class ClientData {
    private String record;
    private TCPClient client;

    public ClientData(String record){
        this.record = record;
    }

    public void startClient(){
        client = new TCPClient(record);
        client.startClient();
    }

    public void setValue(String key, String value){

    }

    public int getValue(int key){
        return 0;
    }

    public int findKey(int key){
        return 0;
    }

    public int getMax(){
        return 0;
    }

    public int getMin(){
        return 0;
    }

    public void newRecord(String key, String value){

    }

    public void terminate(){

    }
}
