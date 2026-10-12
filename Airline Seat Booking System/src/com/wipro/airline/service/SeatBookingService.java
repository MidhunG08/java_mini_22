package com.wipro.airline.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wipro.airline.bean.BookingLogEntry;
import com.wipro.airline.util.SeatAlreadyBookedException;

public class SeatBookingService {

    private final ConcurrentHashMap<String, String> bookings;
    private final List<BookingLogEntry> bookingLogs;

    public SeatBookingService() {
        bookings = new ConcurrentHashMap<>();
        bookingLogs = Collections.synchronizedList(new ArrayList<>());
    }

    public synchronized String bookSeat(
            String seatNo,
            String passengerName,
            String fromLocation,
            String toLocation,
            String travelDate)
            throws SeatAlreadyBookedException {

        String normalizedSeatNo = seatNo.trim().toUpperCase();
        String normalizedFrom = fromLocation.trim().toUpperCase();
        String normalizedTo = toLocation.trim().toUpperCase();
        String normalizedDate = travelDate.trim();

        validateSeatNumber(normalizedSeatNo);
        validateTravelDate(normalizedDate);

        String bookingKey = createBookingKey(normalizedSeatNo, normalizedFrom, normalizedTo, normalizedDate);

        if (bookings.containsKey(bookingKey)) {
            BookingLogEntry log = new BookingLogEntry(
                    passengerName,
                    normalizedSeatNo,
                    fromLocation,
                    toLocation,
                    normalizedDate,
                    "FAILED - SEAT ALREADY BOOKED");
            bookingLogs.add(log);
            throw new SeatAlreadyBookedException(normalizedSeatNo);
        }

        bookings.put(bookingKey, passengerName);

        BookingLogEntry log = new BookingLogEntry(
                passengerName,
                normalizedSeatNo,
                fromLocation,
                toLocation,
                normalizedDate,
                "SUCCESS");
        bookingLogs.add(log);

        return "Seat " + normalizedSeatNo + " successfully booked by " + passengerName;
    }

    public Map<String, String> getSeatMap() {
        return Collections.unmodifiableMap(bookings);
    }

    public List<BookingLogEntry> getBookingLogs() {
        return new ArrayList<>(bookingLogs);
    }

    public void printSeatMap() {
        System.out.println("\n========== FINAL BOOKINGS ==========");
        bookings.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> System.out.println(
                        "Booking " + entry.getKey()
                        + " -> " + entry.getValue()));
    }

    public void printBookingSummary() {
        System.out.println("\n========== BOOKING SUMMARY ==========");
        for (BookingLogEntry log : bookingLogs) {
            System.out.println(log);
        }
    }

    public void printSeatMapAsciiUI() {
        System.out.println("\n========== AIRLINE BOOKING STATUS ==========");
        System.out.printf("%-30s %-16s %-16s %s%n",
                "Booking Key", "Seat Class", "Passenger", "Route / Date");
        System.out.println("-------------------------------------------------------------------------------");
        bookings.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    String bookingKey = entry.getKey();
                    String[] parts = bookingKey.split("\\|", 4);
                    String seatNo = parts[0];
                    String seatClass = getSeatClass(seatNo);
                    String passenger = entry.getValue();
                    String route = parts.length == 4 ? parts[1] + " -> " + parts[2] + " on " + parts[3] : "";
                    System.out.printf("%-30s %-16s %-16s %s%n",
                            bookingKey,
                            seatClass,
                            passenger,
                            route);
                });
    }

    private void validateSeatNumber(String seatNo) {
        if (seatNo == null || !seatNo.matches("[A-E][0-9]{3}")) {
            throw new IllegalArgumentException("Seat " + seatNo + " does not exist.");
        }

        char prefix = seatNo.charAt(0);
        int number = Integer.parseInt(seatNo.substring(1));
        if (number < 1 || number > 100) {
            throw new IllegalArgumentException("Seat " + seatNo + " does not exist.");
        }

        if (prefix < 'A' || prefix > 'E') {
            throw new IllegalArgumentException("Seat " + seatNo + " does not exist.");
        }
    }

    private String createBookingKey(String seatNo, String fromLocation, String toLocation, String travelDate) {
        return seatNo + "|" + fromLocation + "|" + toLocation + "|" + travelDate;
    }

    private void validateTravelDate(String travelDate) {
        if (travelDate == null || !travelDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Travel date must be in YYYY-MM-DD format.");
        }
    }

    public String getSeatClass(String seatNo) {
        if (seatNo == null || seatNo.isEmpty()) {
            return "Unknown";
        }
        char seatPrefix = seatNo.charAt(0);
        switch (seatPrefix) {
            case 'A':
                return "First Class";
            case 'B':
                return "Second Class";
            default:
                return "General Class";
        }
    }
}