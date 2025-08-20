package ru.adrzdv;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar app.jar <path-to-tickets.json> <origin code> <destination code>");
            System.exit(1);
        }

        String filePath = args[0];
        String origin = args[1];
        String destination = args[2];

        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();

            JsonObject root = gson.fromJson(reader, JsonObject.class);
            Type listType = new TypeToken<List<Ticket>>() {
            }.getType();
            List<Ticket> tickets = gson.fromJson(root.get("tickets"), listType);

            List<Ticket> filterdList = tickets.stream()
                    .filter(t -> origin.equalsIgnoreCase(t.origin()) && destination.equalsIgnoreCase(t.destination()))
                    .toList();

            if (filterdList.isEmpty()) {
                System.out.println("Нет билетов по маршруту " + origin + " -> " + destination);
                return;
            }

            List<Integer> prices = getPrices(filterdList);

            double average = prices.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            double diff = Math.abs(average - getMedian(prices));

            System.out.printf("%.2f %.2f %.2f%n", average, getMedian(prices), diff);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            System.exit(2);
        } catch (JsonParseException e) {
            System.err.println("Некорректный JSON: " + e.getMessage());
            System.exit(3);
        } catch (DateTimeParseException e) {
            System.err.println("Ошибка парсинга даты/времени: " + e.getParsedString());
            System.exit(4);
        }
    }

    private static long getFlightMinutes(Ticket ticket) {
        LocalDateTime departure = LocalDateTime.parse(ticket.departureDate() + " " + ticket.departureTime(), FORMATTER);
        LocalDateTime arrival = LocalDateTime.parse(ticket.arrivalDate() + " " + ticket.arrivalTime(), FORMATTER);
        return ChronoUnit.MINUTES.between(departure, arrival);
    }

    private static double getMedian(List<Integer> tickets) {
        List<Integer> sorted = tickets.stream().sorted().toList();

        if (sorted.size() % 2 == 0) {
            return (sorted.get(sorted.size() / 2 - 1) + sorted.get(sorted.size() / 2)) / 2.0;
        } else {
            return sorted.get(sorted.size() / 2);
        }
    }

    private static List<Integer> getPrices(List<Ticket> tickets) {
        Map<String, Long> minByCarrier = new TreeMap<>();

        List<Integer> prices = new ArrayList<>();

        for (Ticket t : tickets) {
            long minutes = getFlightMinutes(t);
            minByCarrier.merge(t.carrier(), minutes, Math::min);
            prices.add(t.price());
        }

        for (Map.Entry<String, Long> e : minByCarrier.entrySet()) {
            long h = e.getValue() / 60;
            long m = e.getValue() % 60;
            System.out.printf("%s: минимальное время полёта %dч %dм%n", e.getKey(), h, m);
        }
        return prices;
    }
}