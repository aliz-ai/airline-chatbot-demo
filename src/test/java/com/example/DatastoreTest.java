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

    @Test
    public void testGetFlightDates() {
        List<Flight> dates = datastoreService.getFlightDatesByDirection(new Direction("BUD", "PAR"));
    }

    @Test
    public void testFlightModification() {
        datastoreService.modifyCustomerFlightNumber("AA123456", "WZ1947");
        datastoreService.modifyFlights("WZ2036", "WZ1947");
    }
}
