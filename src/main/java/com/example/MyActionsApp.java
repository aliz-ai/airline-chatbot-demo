/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.example.model.Flight;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.actions_fulfillment.v2.model.Suggestion;
import com.google.api.services.actions_fulfillment.v2.model.User;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.google.cloud.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements all intent handlers for this Action. Note that your App must extend from DialogflowApp
 * if using Dialogflow or ActionsSdkApp for ActionsSDK based Actions.
 */
public class MyActionsApp extends DialogflowApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyActionsApp.class);

    private DatastoreService datastoreService = new DatastoreService();

    @ForIntent("give_booking_number")
    public ActionResponse getCustomerByBookingNumber(ActionRequest request) {
        String prompt = "";

        ResponseBuilder responseBuilder = getResponseBuilder(request);
        String bookingNumber = ((String) Objects.requireNonNull(request.getParameter("bookingnumber"))).toUpperCase();

        Suggestion suggestion = new Suggestion();
        Suggestion suggestion2 = new Suggestion();
        suggestion.setTitle("Cancel my flight");
        suggestion2.setTitle("Change the flight");

        try {
            String customer = datastoreService.checkBookingNumber(bookingNumber);
            prompt = String.format("Thanks. Hello %s! How can I help you?", customer);
            datastoreService.saveCustomerAndConversationId(request, bookingNumber);
            responseBuilder.add(prompt).add(suggestion).add(suggestion2);
        } catch (Exception e) {
            prompt = String.format("There is no booking number matching: %s! Please try again", bookingNumber);
            responseBuilder.add(prompt);
        } finally {
            return responseBuilder.build();
        }
    }

    @ForIntent("give_booking_number - modify")
    public ActionResponse getFlightWithSameDirection(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        List<Flight> flights = datastoreService.getFlights(request);

        String prompt = "Ok. I can reschedule your flight for a 100 euro fee. Here are the available date options. Please choose one of them.";
        responseBuilder.add(prompt);

        for (Flight flight : flights) {
            Suggestion suggestion = new Suggestion();
            suggestion.setTitle(flight.getFlight_number() + " " + flight.getDate());
            responseBuilder.add(suggestion);
        }
        return responseBuilder.build();
    }

    @ForIntent("give_booking_number - modify - confirmation")
    public ActionResponse modifyFlightDate(ActionRequest request) {
        String prompt = "";
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        String fightNumber = ((String) request.getParameter("flight_number")).toUpperCase().replaceAll("\\s+","");

        try {
            datastoreService.modifyFlight(request, fightNumber);
            prompt = "Ok. I modified your flight for the chosen date. I sent you a confirmation in email. Thanks for contacting the Airline Chatbot. Have a nice day, bye!";
        } catch (Exception e) {
            prompt = "Something went wrong with your flight modification. Please try again later, or call our customer service.";
        }finally {
            return responseBuilder.add(prompt).endConversation().build();
        }
    }
}
