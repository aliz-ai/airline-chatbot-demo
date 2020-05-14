package com.example.model;

import com.google.cloud.Timestamp;

public class Flight {

    private String flight_number;

    private String from;

    private String to;

    private Timestamp date;

    private Long free_seat;

    public Flight() {
    }

    public Flight(String flight_number, Timestamp date) {
        this.flight_number = flight_number;
        this.date = date;
    }

    public Flight(String flight_number, String from, String to, Timestamp date, Long free_seat) {
        this.flight_number = flight_number;
        this.from = from;
        this.to = to;
        this.date = date;
        this.free_seat = free_seat;
    }

    public String getFlight_number() {
        return flight_number;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Long getFree_seat() {
        return free_seat;
    }

    public void setFree_seat(Long free_seat) {
        this.free_seat = free_seat;
    }
}
