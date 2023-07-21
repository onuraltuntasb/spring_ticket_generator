package com.springticketgenerator.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    public void sendmail(String mailTo, String mailSubject, String mailContent)
            throws AddressException, MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ticket.generator.system@gmail.com", "ehbhpleygeadtzio");
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("ticket.generator.system@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        msg.setSubject(mailSubject);
        msg.setContent(mailContent, "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(mailContent, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart attachPart = new MimeBodyPart();

//        attachPart.attachFile("/var/tmp/image19.png");
//        multipart.addBodyPart(attachPart);
//        msg.setContent(multipart);
        Transport.send(msg);
    }

}
