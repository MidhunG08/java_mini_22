package com.wipro.airline.util;

public class SeatAlreadyBookedException extends Exception {

    private String seatNo;

    public SeatAlreadyBookedException(String seatNo) {
        this.seatNo = seatNo;
    }

    @Override
    public String toString() {
        return "SeatAlreadyBookedException: Seat " + seatNo
                + " is already booked by another passenger.";
    }
}