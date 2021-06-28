package projkurose.core;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.mail.*;

import java.io.IOException;

public class CommonsMail {
	private String emitenteEmail;
	private String emitenteNome;
	private String emitentePasswd;
	private String destinatarioEmail;
	private String destinatarioNome;
	private String smtpServer;
	private int smtpPort;
	private boolean ssl;
	private boolean tls;

	private String title = "";
	private String message = "";

	public CommonsMail() {
		try {
			emitenteEmail = Config.getConfiguracao().getString("email_emitente_email");
			emitenteNome = Config.getConfiguracao().getString("email_emitente_nome");
			emitentePasswd = Config.getConfiguracao().getString("email_emitente_passwd");
			smtpServer = Config.getConfiguracao().getString("email_server_smtp");
			smtpPort = Config.getConfiguracao().getInt("email_server_smtp_porta");
			ssl = Config.getConfiguracao().getBoolean("email_server_smtp_ssl");
			tls = Config.getConfiguracao().getBoolean("email_server_smtp_tls");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendSimpleMail(String destinatarioNome, String destinatarioEmail, String title, String message) throws EmailException {

		SimpleEmail email = new SimpleEmail();
		email.setSmtpPort(smtpPort);
		email.setSSLOnConnect(ssl);
		email.setHostName(smtpServer); // o servidor SMTP para envio do e-mail
		email.setAuthentication(emitenteEmail, emitentePasswd);

		email.addTo(destinatarioEmail, destinatarioNome); // destinatçrio
		email.setFrom(emitenteEmail, emitenteNome); // remetente

		email.setSubject(title); // assunto do e-mail
		email.setMsg(message); // conteudo do e-mail
		email.send();
	}
}
