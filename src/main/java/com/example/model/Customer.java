package com.example.model;

public class Customer {

    private String booking_number;

    private String name;

    private String flight;

    private String email;

    public Customer() {
    }

    public Customer(String booking_number, String name, String flight, String email) {
        this.booking_number = booking_number;
        this.name = name;
        this.flight = flight;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBooking_number() {
        return booking_number;
    }

    public void setBooking_number(String booking_number) {
        this.booking_number = booking_number;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
