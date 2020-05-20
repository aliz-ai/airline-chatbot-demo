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
import com.example.service.DatastoreService;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.client.util.Lists;
import com.google.api.services.actions_fulfillment.v2.model.RichResponse;
import com.google.api.services.actions_fulfillment.v2.model.Suggestion;

import java.util.List;
import java.util.Objects;

import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessage;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageBasicCardButton;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageCard;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageCardButton;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageImage;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageQuickReplies;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageTableCard;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageText;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.Gson;
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
        String customer = "";

        WebhookResponse webhookResponse = new WebhookResponse();
        List<IntentMessage> fulfillmentMessages = Lists.newArrayList();
        IntentMessage im = new IntentMessage();
        IntentMessageQuickReplies qr = new IntentMessageQuickReplies();
        List<String> l = Lists.newArrayList();
        l.add("Cancel my flight");
        l.add("Change the flight");
        qr.setQuickReplies(l);

        Suggestion suggestion = new Suggestion();
        Suggestion suggestion2 = new Suggestion();
        suggestion.setTitle("Cancel my flight");
        suggestion2.setTitle("Change the flight");


        try {
            customer = datastoreService.checkBookingNumber(bookingNumber);
            prompt = String.format("Thanks. Hello %s! How can I help you?", customer);
            datastoreService.saveCustomerAndConversationId(request, bookingNumber);
            responseBuilder.add(prompt).add(suggestion).add(suggestion2);
            qr.setTitle(prompt);
            im.setQuickReplies(qr);
            fulfillmentMessages.add(im);
            webhookResponse.setFulfillmentMessages(fulfillmentMessages);
            responseBuilder.setWebhookResponse$actions_on_google(webhookResponse);
        } catch (Exception e) {
            prompt = String.format("There is no booking number matching: %s! Please try again.", bookingNumber);
            responseBuilder.add(prompt);
        }

        return responseBuilder.build();
    }

    @ForIntent("give_booking_number - modify")
    public ActionResponse getFlightWithSameDirection(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        List<Flight> flights = datastoreService.getFlights(request);

        String prompt = "Ok. I can reschedule your flight for a 100 euro fee. Here are the available date options. Please choose one of them.";
        responseBuilder.add(prompt);

        WebhookResponse webhookResponse = new WebhookResponse();
        List<IntentMessage> fulfillmentMessages = Lists.newArrayList();
        List<IntentMessageCardButton> buttons = Lists.newArrayList();

        IntentMessage intentMessage = new IntentMessage();
        IntentMessageCard tableCard = new IntentMessageCard();

        tableCard.setImageUri("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Wizz_Air_logo.svg/1280px-Wizz_Air_logo.svg.png");
        tableCard.setSubtitle("team");
        tableCard.setTitle("Wizz air");

        for (Flight flight : flights) {
            Suggestion suggestion = new Suggestion();
            suggestion.setTitle(flight.getFlight_number() + " " + flight.getDate());
            responseBuilder.add(suggestion);

            IntentMessageCardButton button = new IntentMessageCardButton();
            button.setText(flight.getFlight_number() + " " + flight.getDate());
            button.setPostback(flight.getFlight_number() + " " + flight.getDate());
            buttons.add(button);
        }
        
        tableCard.setButtons(buttons);
        intentMessage.setCard(tableCard);
        fulfillmentMessages.add(intentMessage);
        webhookResponse.setFulfillmentMessages(fulfillmentMessages);
        responseBuilder.setWebhookResponse$actions_on_google(webhookResponse);

        Suggestion suggestion = new Suggestion();
        suggestion.setTitle("Cancel my flight anyway.");


        return responseBuilder.add(suggestion).build();
    }

    @ForIntent("give_booking_number - cancel - modification")
    public ActionResponse getFlightWithSameDirectionWithDiscount(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        List<Flight> flights = datastoreService.getFlights(request);

        String prompt = "Ok. In that case I can offer you the modification of your flight for 70 euros instead of 100 euros. Here you can choose from the available dates.";
        responseBuilder.add(prompt);

        WebhookResponse webhookResponse = new WebhookResponse();
        List<IntentMessage> fulfillmentMessages = Lists.newArrayList();
        List<IntentMessageCardButton> buttons = Lists.newArrayList();

        IntentMessage intentMessage = new IntentMessage();
        IntentMessageCard tableCard = new IntentMessageCard();

        tableCard.setImageUri("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Wizz_Air_logo.svg/1280px-Wizz_Air_logo.svg.png");
        tableCard.setSubtitle("team");
        tableCard.setTitle("Wizz air");

        for (Flight flight : flights) {
            Suggestion suggestion = new Suggestion();
            suggestion.setTitle(flight.getFlight_number() + " " + flight.getDate());
            responseBuilder.add(suggestion);

            IntentMessageCardButton button = new IntentMessageCardButton();
            button.setText(flight.getFlight_number() + " " + flight.getDate());
            button.setPostback(flight.getFlight_number() + " " + flight.getDate());
            buttons.add(button);
        }
        tableCard.setButtons(buttons);

        intentMessage.setCard(tableCard);
        fulfillmentMessages.add(intentMessage);
        webhookResponse.setFulfillmentMessages(fulfillmentMessages);
        responseBuilder.setWebhookResponse$actions_on_google(webhookResponse);

        return responseBuilder.build();
    }

    @ForIntent("give_booking_number - modify - confirmation")
    public ActionResponse modifyFlightDate(ActionRequest request) {
        String prompt = "";
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        String fightNumber = ((String) request.getParameter("flight_number")).toUpperCase().replaceAll("\\s+", "");

        try {
            datastoreService.modifyFlight(request, fightNumber);
            prompt = "Ok. I modified your flight for the chosen date. I sent you a confirmation in email. Thanks for contacting the Airline Chatbot. Have a nice day, bye!";
        } catch (Exception e) {
            prompt = "Something went wrong with your flight modification. Please try again later, or call our customer service.";
        }

        return responseBuilder.add(prompt).endConversation().build();
    }

    @ForIntent("give_booking_number - cancel - modification - confirmation")
    public ActionResponse modifyFlightDateWithDiscount(ActionRequest request) {
        String prompt = "";
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        String fightNumber = ((String) request.getParameter("flight_number")).toUpperCase().replaceAll("\\s+", "");

        try {
            datastoreService.modifyFlight(request, fightNumber);
            prompt = "Ok. I modified your flight for the chosen date. I sent you a confirmation in email. Thanks for contacting the Airline Chatbot. Have a nice day, bye!";
        } catch (Exception e) {
            prompt = "Something went wrong with your flight modification. Please try again later, or call our customer service.";
        }

        return responseBuilder.add(prompt).endConversation().build();
    }

    @ForIntent("give_booking_number - cancel - no modification")
    public ActionResponse cancelFlightAnyway(ActionRequest request) {
        String prompt = "";
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        try {
            String bookingNumber = datastoreService.cancelFlight(request);
            prompt = String.format("I understand. In that case I cancel your booking under %s booking number. I send you a confirmation in email. Thanks for contacting the Airline Chatbot. Have a nice day, bye!", bookingNumber);
        } catch (Exception e) {
            prompt = "Something went wrong. Please try again later.";
        }

        return responseBuilder.add(prompt).endConversation().build();
    }
}
