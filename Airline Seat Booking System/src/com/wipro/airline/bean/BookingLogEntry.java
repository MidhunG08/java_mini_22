package com.wipro.airline.bean;

public class BookingLogEntry {

    private String passengerName;
    private String seatNo;
    private String fromLocation;
    private String toLocation;
    private String travelDate;
    private String status;
    private long timestamp;

    public BookingLogEntry(String passengerName,
            String seatNo,
            String fromLocation,
            String toLocation,
            String travelDate,
            String status) {
        this.passengerName = passengerName;
        this.seatNo = seatNo;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.travelDate = travelDate;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Passenger: " + passengerName
                + ", Seat: " + seatNo
                + ", Route: " + fromLocation + "->" + toLocation
                + ", Date: " + travelDate
                + ", Status: " + status
                + ", Timestamp: " + timestamp;
    }
}