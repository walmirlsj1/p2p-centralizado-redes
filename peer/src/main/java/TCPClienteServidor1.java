
import org.apache.commons.configuration2.PropertiesConfiguration;
import projkurose.core.Config;

public class TCPClienteServidor1 {
    public static void main(String[] args) throws Exception {
        Long clientId = 0L;
        int clientSrvPort = 6500;
        int serverPortDir;
        String serverIpDir;

        try {
            PropertiesConfiguration config = Config.getConfiguracao();
//            clientSrvPort = config.getInt("client_port"); //6789
//            clientId = config.getLong("client_id");

            serverIpDir = config.getString("server_ip");
            serverPortDir = config.getInt("server_port");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Config.deleteConfig();
            throw new RuntimeException("Falha na configuração, tente novamente!");
        }
        Peer client = new Peer(clientId, clientSrvPort, serverIpDir, serverPortDir);
        client.run();
        client.disconnect();
    }
}
