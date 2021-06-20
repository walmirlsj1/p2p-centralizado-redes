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
			config.setProperty("srv-dir-database", "srv_dir");
			config.setProperty("srv-dir-local-port", "6986");
			/**
			 * config client
			 */
			config.setProperty("srv-client-database", "srv_dir");
			config.setProperty("srv-client-local-port", "6315");
			config.setProperty("srv-client-user-id", "null");

			layout.save(config, new FileWriter(file));
		} else {
			layout.load(config, new InputStreamReader(new FileInputStream(file)));
		}

		return config;
	}
}
