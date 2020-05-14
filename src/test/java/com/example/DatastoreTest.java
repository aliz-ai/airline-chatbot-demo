package com.example;

import com.example.model.Direction;
import com.example.model.Flight;
import org.junit.Test;

import java.util.List;

public class DatastoreTest {

    DatastoreService datastoreService = new DatastoreService();

    @Test
    public void testDatastore() {


        String customer = datastoreService.checkBookingNumber("AA123456");
    }
}
