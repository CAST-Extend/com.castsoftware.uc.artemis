package com.castsoftware.artemis.mailer;

import com.castsoftware.artemis.config.Configuration;
import com.castsoftware.artemis.config.UserConfiguration;
import com.castsoftware.artemis.database.Neo4jAL;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mailer {

    static final String FROM = UserConfiguration.get("smtp.mail.username");
    static final String FROMNAME = UserConfiguration.get("smtp.mail.displayed_name");

    static final String RECIPIENT_LIST = UserConfiguration.get("smtp.mail.recipients");

    static final String SMTP_USERNAME = UserConfiguration.get("smtp.mail.username");
    static final String SMTP_PASSWORD = UserConfiguration.get("smtp.mail.password");


    static final String CONFIGSET = "ConfigSet";
    static final String HOST = UserConfiguration.get("smtp.server");

    // The port you will connect to on the Amazon SES SMTP endpoint.
    static final int PORT = Integer.parseInt(UserConfiguration.get("smtp.port"));


    static Session SESSION = buildSession();

    private static Session buildSession() {
        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        return Session.getDefaultInstance(props);
    }

    private static Message buildMessage(Session session, String to, String subject, String body) throws Exception {
        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setContent(body,"text/html");

        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
        msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

        return msg;
    }

    public static void sendMail(String to, String subject, String body) throws Exception {

        System.out.printf("Now mailing to %s : %s", to, subject);

        Session session = SESSION;
        Message msg = buildMessage(session, to, subject, body);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try
        {
            System.out.println(String.join("Sending a mail to %s ..."));

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        }
        catch (Exception ex) {
            System.out.println(String.format("The email was not sent to %s.", to));
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {
            // Close and terminate the connection.
            transport.close();
        }
    }

    public static void bulkMail() throws Exception {
        if(RECIPIENT_LIST == null) {
            return;
        }

        String subject = "Failed to analyze application : Wealthcare";
        String body = MailerTemplate.generateFailureMail("Wealthcare", new Exception("Test"));
        String[] recipientList = RECIPIENT_LIST.split(",");
        for(String recipient : recipientList) {
            sendMail(recipient, subject, body);
        }
    }

}