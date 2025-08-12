package ru.adrzdv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -jar app.jar <path-to-tickets.json>");
            System.exit(1);
        }

        String filePath = args[0];
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        JSONObject root = new JSONObject(content);
        JSONArray tickets = root.getJSONArray("tickets");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
        Map<String, Long> minFlightTimeByCarrier = new TreeMap<>();
        List<Integer> prices = new ArrayList<>();

        for (int i = 0; i < tickets.length(); i++) {
            JSONObject ticket = tickets.getJSONObject(i);

            if (!"VVO".equals(ticket.getString("origin")) || !"TLV".equals(ticket.getString("destination"))) {
                continue;
            }

            LocalDateTime departure = LocalDateTime.parse(
                    ticket.getString("departure_date") + " " + ticket.getString("departure_time"),
                    dtf
            );
            LocalDateTime arrival = LocalDateTime.parse(
                    ticket.getString("arrival_date") + " " + ticket.getString("arrival_time"),
                    dtf
            );

            long durationMinutes = Duration.between(departure, arrival).toMinutes();

            String carrier = ticket.getString("carrier");
            int price = ticket.getInt("price");

            minFlightTimeByCarrier.merge(carrier, durationMinutes, Math::min);

            prices.add(price);
        }

        for (Map.Entry<String, Long> entry : minFlightTimeByCarrier.entrySet()) {
            long hours = entry.getValue() / 60;
            long minutes = entry.getValue() % 60;
            System.out.printf("%s: минимальное время полёта %dч %dм%n", entry.getKey(), hours, minutes);
        }
        double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Integer> sortedPrices = prices.stream().sorted().collect(Collectors.toList());
        double medianPrice;
        int size = sortedPrices.size();

        if (size % 2 == 0) {
            medianPrice = (sortedPrices.get(size / 2 - 1) + sortedPrices.get(size / 2)) / 2.0;
        } else {
            medianPrice = sortedPrices.get(size / 2);
        }
        double difference = Math.abs(averagePrice - medianPrice);
        System.out.printf("Средняя цена: %.2f%nМедиана цены: %.2f%nРазница: %.2f%n", averagePrice, medianPrice, difference);
    }
}