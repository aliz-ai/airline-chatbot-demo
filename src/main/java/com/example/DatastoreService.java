package com.example;


import com.example.model.Customer;
import com.example.model.Direction;
import com.example.model.Flight;
import com.google.actions.api.ActionRequest;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DatastoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreService.class);
    public static final String CHATBOT_NAMESPACE = "chatbot";
    public static final String CUSTOMER_KIND = "customer";
    public static final String BOOKING_NUMBER = "booking_number";
    public static final String FLIGHT_KIND = "flight";
    public static final String CONVERSATION_KIND = "conversation";
    public static final String FLIGHT_NUMBER = "flight_number";

    private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public String checkBookingNumber(String bookingNumber) {
        return getCustomer(bookingNumber).getString("name");
    }

    public List<Flight> getFlights(ActionRequest request) {
        String bookingNumber = getBookingNumberByConversationId(request);
        String flightNumber = getCustomer(bookingNumber).getString("flight");
        return getFlightDatesByDirection(getFlightDirection(flightNumber));
    }

    public void modifyFlight(ActionRequest request, String newFlightNumber) {
        String bookingNumber = getBookingNumberByConversationId(request);
        String oldFlightNumber = modifyCustomerFlightNumber(bookingNumber, newFlightNumber);
        modifyFlights(oldFlightNumber, newFlightNumber);
    }

    public void saveCustomerAndConversationId(ActionRequest request, String bookingNumber) {
        datastore.add(createEntity(request, bookingNumber));
    }

    public List<Flight> getFlightDatesByDirection(Direction direction) {
        List<Flight> flights = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(FLIGHT_KIND)
                .setFilter(StructuredQuery.CompositeFilter.and(StructuredQuery.PropertyFilter.eq("from", direction.getFrom()),
                        StructuredQuery.PropertyFilter.eq("to", direction.getTo())))
                .build();

        datastore.run(query).forEachRemaining(flight -> flights.add(new Flight(flight.getString("flight_number"), flight.getString("date"))));
        return flights;
    }

    public void modifyFlights(String oldFlightNumber, String newFlightNumber) {
        Entity entityAddSeat = getFlight(newFlightNumber);
        Entity entityReduceSeat = getFlight(oldFlightNumber);
        Flight flightToReduce = new Flight(newFlightNumber, entityAddSeat.getString("from"), entityAddSeat.getString("to"),
                entityAddSeat.getString("date"), entityAddSeat.getLong("free_seat") - 1);
        Flight flightToAdd = new Flight(oldFlightNumber, entityReduceSeat.getString("from"), entityReduceSeat.getString("to"),
                entityReduceSeat.getString("date"), entityReduceSeat.getLong("free_seat") + 1);

        updateDatabase(createFlightEntity(flightToAdd));
        updateDatabase(createFlightEntity(flightToReduce));
    }

    public String modifyCustomerFlightNumber(String bookingNumber, String newFlightNumber) {
        Entity entity = getCustomer(bookingNumber);
        Customer modifiedCustomer = new Customer(bookingNumber, entity.getString("name"), newFlightNumber, entity.getString("email"));
        updateDatabase(createCustomerEntity(modifiedCustomer));
        return entity.getString("flight").toUpperCase().replaceAll("\\s+", "");
    }

    public String cancelFlight(ActionRequest request) {
        return getBookingNumberByConversationId(request);
    }

    private void updateDatabase(Entity entity) {
        datastore.update(entity);
    }

    private Entity createFlightEntity(Flight flight) {
        Key key = datastore.newKeyFactory().setKind(FLIGHT_KIND).setNamespace(CHATBOT_NAMESPACE).newKey(flight.getFlight_number());

        Entity responseFlight = Entity.newBuilder(key)
                .set("flight_number", flight.getFlight_number())
                .set("from", flight.getFrom())
                .set("to", flight.getTo())
                .set("date", flight.getDate())
                .set("free_seat", flight.getFree_seat())
                .build();

        return responseFlight;
    }

    private Entity createCustomerEntity(Customer user) {
        Key key = datastore.newKeyFactory().setKind(CUSTOMER_KIND).setNamespace(CHATBOT_NAMESPACE).newKey(user.getBooking_number());

        Entity responseFlight = Entity.newBuilder(key)
                .set("booking_number", user.getBooking_number())
                .set("name", user.getName())
                .set("flight", user.getFlight())
                .set("email", user.getEmail())
                .build();

        return responseFlight;
    }

    private Direction getFlightDirection(String flightNumber) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(FLIGHT_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(FLIGHT_NUMBER, flightNumber))
                .build();

        Entity results = datastore.run(query).next();
        return new Direction(results.getString("from"), results.getString("to"));
    }

    private Entity getCustomer(String bookingNumber) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(CUSTOMER_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(BOOKING_NUMBER, bookingNumber))
                .build();

        return datastore.run(query).next();
    }

    private Entity getFlight(String flightNumber) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(FLIGHT_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("flight_number", flightNumber))
                .build();

        return datastore.run(query).next();
    }

    private String getBookingNumberByConversationId(ActionRequest request) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(CONVERSATION_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("user_id", request.getSessionId()))
                .build();

        return datastore.run(query).next().getString(BOOKING_NUMBER);
    }

    private Entity createEntity(ActionRequest request, String bookingNumber) {
        Key key = datastore.newKeyFactory().setNamespace(CHATBOT_NAMESPACE).setKind(CONVERSATION_KIND).newKey(request.getSessionId());

        Entity conversation = Entity.newBuilder(key)
                .set("user_id", request.getSessionId())
                .set(BOOKING_NUMBER, bookingNumber)
                .build();

        return conversation;
    }
}
