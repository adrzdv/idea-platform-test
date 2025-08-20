package ru.adrzdv;

import com.google.gson.annotations.SerializedName;

public record Ticket(
        String origin,
        @SerializedName("origin_name")
        String originName,
        String destination,
        @SerializedName("destination_name")
        String destinationName,
        String carrier,
        int price,
        @SerializedName("departure_date")
        String departureDate,
        @SerializedName("departure_time")
        String departureTime,
        @SerializedName("arrival_date")
        String arrivalDate,
        @SerializedName("arrival_time")
        String arrivalTime,
        int stops
) {
}
