package com.wipro.airline.service;

import com.wipro.airline.util.SeatAlreadyBookedException;

public class PassengerBookingTask implements Runnable {

    private final String passengerName;
    private final String seatNo;
    private final String fromLocation;
    private final String toLocation;
    private final String travelDate;
    private final SeatBookingService bookingService;

    public PassengerBookingTask(
            String passengerName,
            String seatNo,
            String fromLocation,
            String toLocation,
            String travelDate,
            SeatBookingService bookingService) {

        this.passengerName = passengerName;
        this.seatNo = seatNo;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.travelDate = travelDate;
        this.bookingService = bookingService;
    }

    @Override
    public void run() {

        try {

            System.out.println(
                    Thread.currentThread().getName()
                    + " - "
                    + passengerName
                    + " attempting to book seat "
                    + seatNo
                    + " from "
                    + fromLocation
                    + " to "
                    + toLocation);

            String result = bookingService.bookSeat(
                    seatNo,
                    passengerName,
                    fromLocation,
                    toLocation,
                    travelDate);

            System.out.println(
                    Thread.currentThread().getName()
                    + " - "
                    + result);

        } catch (SeatAlreadyBookedException e) {

            System.out.println(
                    Thread.currentThread().getName()
                    + " - "
                    + passengerName
                    + " failed: "
                    + e);

        } catch (IllegalArgumentException e) {

            System.out.println(
                    Thread.currentThread().getName()
                    + " - "
                    + e.getMessage());
        }
    }
}