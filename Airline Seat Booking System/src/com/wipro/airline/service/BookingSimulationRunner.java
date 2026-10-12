package com.wipro.airline.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wipro.airline.util.SeatAlreadyBookedException;

public class BookingSimulationRunner {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" AIRLINE CONCURRENT SEAT BOOKING SYSTEM");
        System.out.println("======================================");

        SeatBookingService bookingService = new SeatBookingService();

        int port = Integer.getInteger("server.port", 8080);
        System.out.println("\nStarting local UI server at http://127.0.0.1:" + port);
        System.out.println("Open this URL in your browser to book seats using the form.");
        System.out.println("The server will keep running until you stop the process (Ctrl+C).");

        try {
            HttpServer server = createServer(bookingService, port);
            server.start();
            Thread.sleep(Long.MAX_VALUE);
        } catch (IOException e) {
            System.out.println("Failed to start localhost web UI: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server interrupted and stopping.");
        }
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private static void sendHtml(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static HttpServer createServer(SeatBookingService bookingService, int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String message = "";
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> params = parseForm(body);
                    String passengerName = params.getOrDefault("passengerName", "").trim();
                    String fromLocation = params.getOrDefault("fromLocation", "").trim();
                    String toLocation = params.getOrDefault("toLocation", "").trim();
                    String travelDate = params.getOrDefault("travelDate", "").trim();
                    String seatNo = params.getOrDefault("seatNo", "").trim().toUpperCase();

                    if (passengerName.isEmpty() || fromLocation.isEmpty() || toLocation.isEmpty() || travelDate.isEmpty() || seatNo.isEmpty()) {
                        message = "Please fill in all fields.";
                    } else if (!travelDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        message = "Travel date must be in YYYY-MM-DD format.";
                    } else if (!seatNo.matches("[A-E][0-9]{3}")) {
                        message = "Seat number must be in format A001-E100.";
                    } else {
                        try {
                            bookingService.bookSeat(seatNo, passengerName, fromLocation, toLocation, travelDate);
                            message = "Seat " + seatNo + " Confirmed Successfully";
                        } catch (SeatAlreadyBookedException e) {
                            message = "Seat " + seatNo + " Already Booked for the selected date and route.";
                        } catch (IllegalArgumentException e) {
                            message = e.getMessage();
                        }
                    }
                }
                String html = buildHtml(bookingService, message);
                sendHtml(exchange, html);
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        return server;
    }

    private static String buildHtml(SeatBookingService bookingService, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><title>Airline Seat Booking UI</title>");
        builder.append("<style>body{font-family:Arial,sans-serif;margin:20px;}table{border-collapse:collapse;width:100%;}td,th{border:1px solid #ccc;padding:6px;text-align:left;}thead{background:#f0f0f0;}input,select{padding:8px;width:100%;box-sizing:border-box;margin:4px 0;}button{padding:10px 16px;margin-top:10px;} .message{font-weight:bold;margin-bottom:16px;} .success{color:green;} .error{color:red;}</style>");
        builder.append("</head><body>");
        builder.append("<h1>Airline Seat Booking</h1>");
        builder.append("<p>Enter passenger details and seat number then submit.</p>");

        if (message != null && !message.isEmpty()) {
            String cssClass = message.contains("Confirmed") ? "success" : "error";
            builder.append("<div class='message " + cssClass + "'>").append(message).append("</div>");
        }

        builder.append("<form method='post'>");
        builder.append("<label>Passenger name:<br><input type='text' name='passengerName' required></label><br>");
        builder.append("<label>From Location:<br><input type='text' name='fromLocation' required></label><br>");
        builder.append("<label>To Location:<br><input type='text' name='toLocation' required></label><br>");
        builder.append("<label>Travel Date:<br><input type='date' name='travelDate' required></label><br>");
        builder.append("<label>Enter Seat Number (Ex: A001):<br><input type='text' name='seatNo' pattern='[A-Ea-e][0-9]{3}' required></label><br>");
        builder.append("<p>[A - First Class, B - Second Class, C - General, D - General, E - General]</p>");
        builder.append("<button type='submit'>Book Seat</button>");
        builder.append("</form>");
        builder.append("</body></html>");
        return builder.toString();
    }
}
