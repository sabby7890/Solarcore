package org.solarcore.app;

/*
 This file is part of the Solarcore project (https://github.com/sabby7890/Solarcore).

 Solarcore is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Solarcore is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Solarcore.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class NotificationEmail extends Notification {
    NotificationEmail(SolarcoreConfig pConfig, SolarcoreLog pLog) {
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