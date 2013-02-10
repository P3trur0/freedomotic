/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.events.MessageEvent;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author http://www.javapractices.com/topic/TopicAction.do?Id=144
 */
public final class Mailer extends Protocol {

    private int sentMails;

    public Mailer() {
        super("Mailer", "/com.freedomotic.mailer/mailer.xml");
        addEventListener("app.event.sensor.messages.mail");
    }

    @Override
    protected void onRun() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String from = c.getProperty("from");
        if (from == null) {
            from = "your.home@freedomotic.com";
        }
        String to = c.getProperty("to");
        String subject = c.getProperty("subject");
        final String text = c.getProperty("message");
        send(from, to, subject, text);

    }

    private void send(
            String from,
            String to,
            String subject,
            String text) {
        final String username = configuration.getProperty("username");
        final String password = configuration.getProperty("password");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", configuration.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", configuration.getProperty("mail.smtp.port"));

        if (to == null || to.isEmpty()) {
            to = username;
        }
        if (subject == null || subject.isEmpty()) {
            subject = "A notification from freedomotic";
        }

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("you@home.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            sentMails++;
            setDescription(sentMails + " emails sent");
        } catch (javax.mail.AuthenticationFailedException auth) {
            setDescription("Authentication failed: set username and password");
            stop();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        if (event instanceof MessageEvent){
            MessageEvent mail = (MessageEvent)event;
            send(mail.getFrom(), mail.getTo(), null, mail.getText());
        }
    }
}