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
    public static final String USER_ID = "user_id";
    public static final String NAME = "name";
    public static final String FROM = "from";
    public static final String DATE = "date";
    public static final String TO = "to";
    public static final String FREE_SEAT = "free_seat";
    public static final String EMAIL = "email";

    private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public String checkBookingNumber(String bookingNumber) {
        return getCustomer(bookingNumber).getString(NAME);
    }

    public List<Flight> getFlights(ActionRequest request) {
        String bookingNumber = getBookingNumberByConversationId(request);
        String flightNumber = getCustomer(bookingNumber).getString(FLIGHT_KIND);
        Entity results = getFlight(flightNumber);
        return getFlightDatesByDirection( new Direction(results.getString(FROM), results.getString(TO)));
    }

    public void modifyFlight(ActionRequest request, String newFlightNumber) {
        String bookingNumber = getBookingNumberByConversationId(request);
        String oldFlightNumber = modifyCustomerFlightNumber(bookingNumber, newFlightNumber);
        modifyFlights(oldFlightNumber, newFlightNumber);
    }

    public void saveCustomerAndConversationId(ActionRequest request, String bookingNumber) {
        datastore.add(createConversationEntity(request, bookingNumber));
    }

    public String cancelFlight(ActionRequest request) {
        return getBookingNumberByConversationId(request);
    }

    private void modifyFlights(String oldFlightNumber, String newFlightNumber) {
        Entity entityAddSeat = getFlight(newFlightNumber);
        Entity entityReduceSeat = getFlight(oldFlightNumber);
        Flight flightToReduce = new Flight(newFlightNumber, entityAddSeat.getString(FROM), entityAddSeat.getString(TO),
                entityAddSeat.getString(DATE), entityAddSeat.getLong(FREE_SEAT) - 1);
        Flight flightToAdd = new Flight(oldFlightNumber, entityReduceSeat.getString(FROM), entityReduceSeat.getString(TO),
                entityReduceSeat.getString(DATE), entityReduceSeat.getLong(FREE_SEAT) + 1);

        updateDatabase(createFlightEntity(flightToAdd));
        updateDatabase(createFlightEntity(flightToReduce));
    }

    private String modifyCustomerFlightNumber(String bookingNumber, String newFlightNumber) {
        Entity entity = getCustomer(bookingNumber);
        Customer modifiedCustomer = new Customer(bookingNumber, entity.getString(NAME), newFlightNumber, entity.getString(EMAIL));
        updateDatabase(createCustomerEntity(modifiedCustomer));
        return entity.getString(FLIGHT_KIND).toUpperCase().replaceAll("\\s+", "");
    }

    private void updateDatabase(Entity entity) {
        datastore.update(entity);
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
                .setFilter(StructuredQuery.PropertyFilter.eq(FLIGHT_NUMBER, flightNumber))
                .build();

        return datastore.run(query).next();
    }

    private String getBookingNumberByConversationId(ActionRequest request) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(CONVERSATION_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(USER_ID, request.getSessionId()))
                .build();

        return datastore.run(query).next().getString(BOOKING_NUMBER);
    }

    private List<Flight> getFlightDatesByDirection(Direction direction) {
        List<Flight> flights = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setNamespace(CHATBOT_NAMESPACE)
                .setKind(FLIGHT_KIND)
                .setFilter(StructuredQuery.CompositeFilter.and(StructuredQuery.PropertyFilter.eq(FROM, direction.getFrom()),
                        StructuredQuery.PropertyFilter.eq(TO, direction.getTo())))
                .build();

        datastore.run(query).forEachRemaining(flight -> flights.add(new Flight(flight.getString(FLIGHT_NUMBER), flight.getString(DATE))));
        return flights;
    }

    private Entity createConversationEntity(ActionRequest request, String bookingNumber) {
        Key key = datastore.newKeyFactory().setNamespace(CHATBOT_NAMESPACE).setKind(CONVERSATION_KIND).newKey(request.getSessionId());

        Entity conversation = Entity.newBuilder(key)
                .set(USER_ID, request.getSessionId())
                .set(BOOKING_NUMBER, bookingNumber)
                .build();

        return conversation;
    }

    private Entity createFlightEntity(Flight flight) {
        Key key = datastore.newKeyFactory().setKind(FLIGHT_KIND).setNamespace(CHATBOT_NAMESPACE).newKey(flight.getFlight_number());

        Entity responseFlight = Entity.newBuilder(key)
                .set(FLIGHT_NUMBER, flight.getFlight_number())
                .set(FROM, flight.getFrom())
                .set(TO, flight.getTo())
                .set(DATE, flight.getDate())
                .set(FREE_SEAT, flight.getFree_seat())
                .build();

        return responseFlight;
    }

    private Entity createCustomerEntity(Customer user) {
        Key key = datastore.newKeyFactory().setKind(CUSTOMER_KIND).setNamespace(CHATBOT_NAMESPACE).newKey(user.getBooking_number());

        Entity responseFlight = Entity.newBuilder(key)
                .set(BOOKING_NUMBER, user.getBooking_number())
                .set(NAME, user.getName())
                .set(FLIGHT_KIND, user.getFlight())
                .set(EMAIL, user.getEmail())
                .build();

        return responseFlight;
    }
}
