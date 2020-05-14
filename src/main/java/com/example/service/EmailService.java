package com.example.service;

import com.example.model.Customer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public void sendMail(String recepient, Customer user, String startMessage) throws MessagingException {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myAccountEmail = "alizairlinechatbot@gmail.com";
        String password = "alizchatbot";

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recepient, user, startMessage);
        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipient, Customer user, String startMessage) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Confirmation");
            message.setText(String.format("Dear %s, \n %s: booking number: %s, flight number: %s. \n Check your flight details on our website --> https://www.mememaker.net/api/bucket?path=static/img/memes/full/2019/Oct/9/21/confirm-2585.png",
                    user.getName(), startMessage, user.getBooking_number(), user.getFlight()));
            return message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
