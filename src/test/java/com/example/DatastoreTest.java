package com.example;

import com.example.model.Direction;
import org.junit.Test;

import java.util.List;

public class DatastoreTest {

    @Test
    public void testDatastore() {
        DatastoreService datastoreService = new DatastoreService();

        String customer = datastoreService.checkBookingNumber("AA123456");
    }

    @Test
    public void testGetFlightDates() {
        DatastoreService datastoreService = new DatastoreService();

        List<String> dates = datastoreService.getFlightDatesByDirection(new Direction("BUD", "PAR"));
    }
}
