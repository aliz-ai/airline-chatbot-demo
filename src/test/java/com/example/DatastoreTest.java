package com.example;

import com.example.service.DatastoreService;
import com.example.service.EmailService;
import org.junit.Test;

import javax.mail.MessagingException;

public class DatastoreTest {

    DatastoreService datastoreService = new DatastoreService();

    EmailService emailService = new EmailService();

    @Test
    public void testDatastore() {


        String customer = datastoreService.checkBookingNumber("AA123456");
    }
}
