package org.solarcore.app;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NotificationEmail extends Notification {
    public NotificationEmail(SolarcoreConfig pConfig, SolarcoreLog pLog) {
        super(pConfig, pLog);

        log.info("Notification EMAIL");


    }

    public boolean send() {
        Properties props = new Properties();

        if (config.getEmailStartTLS())
            props.put("mail.smtp.starttls.enable", "true");
        else
            props.put("mail.smtp.starttls.enable", "false");

        if (config.getEmailAuthRequired()) {
            props.put("mail.smtp.auth", "true");

        }

        props.put("mail.smtp.host", config.getEmailServer());
        props.put("mail.smtp.port", config.getEmailPort());

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getEmailLoginUsername(), config.getEmailPassword());
                    }
                });

        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(config.getEmailSenderEmail(), config.getEmailSenderName()));
            msg.setRecipients( Message.RecipientType.TO,InternetAddress.parse(recipent) );
            msg.setSentDate( new Date());
            msg.setSubject(subject);

            msg.setText(message);

            Transport.send( msg );
            return true;
        } catch (MessagingException|UnsupportedEncodingException e) {
            log.error("Unable to send email notification! Reason: " + e.getMessage());
        }
        return false;
    }
}