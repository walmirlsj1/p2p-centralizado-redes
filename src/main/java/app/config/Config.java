package app.config;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;

public class Config {

    public static PropertiesConfiguration getConfiguracao() throws ConfigurationException, IOException {

        PropertiesConfiguration config = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);

        File file = new File("config.properties");

        if (!file.exists()) {

            /* Define configuration basic for APP */
            file.createNewFile();
            config.setHeader("Configuracoes");
            /**
             * config server
             */
            config.setProperty("srv_database", "databaseSrv.db");
            config.setProperty("srv_port", "6986");
            config.setProperty("srv_ip", "127.0.0.1");

            /**
             * config client
             */
            config.setProperty("client_database", "database.db");
            config.setProperty("client_port", "6315");
            config.setProperty("client_id", "0");
            config.setProperty("server_ip", "127.0.0.1");
            config.setProperty("server_port", "6986");

            layout.save(config, new FileWriter(file));
        } else {
            layout.load(config, new InputStreamReader(new FileInputStream(file)));
        }

        return config;
    }

    public static void deleteConfig() {
        File file = new File("config.properties");
        file.delete();
    }
}
