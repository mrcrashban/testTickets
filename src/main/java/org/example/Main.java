package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class Ticket {
    String origin;
    String origin_name;
    String destination;
    String destination_name;
    String departure_date;
    String departure_time;
    String arrival_date;
    String arrival_time;
    String carrier;
    int stops;
    int price;

    public long getFlightDuration() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        LocalDate departureDate = LocalDate.parse(departure_date, dateFormatter);
        LocalTime departureTime = LocalTime.parse(departure_time, timeFormatter);
        LocalDateTime departureDateTime = LocalDateTime.of(departureDate, departureTime);

        LocalDate arrivalDate = LocalDate.parse(arrival_date, dateFormatter);
        LocalTime arrivalTime = LocalTime.parse(arrival_time, timeFormatter);
        LocalDateTime arrivalDateTime = LocalDateTime.of(arrivalDate, arrivalTime);

        return Duration.between(departureDateTime, arrivalDateTime).toMinutes();
    }
}

class Tickets {
    List<Ticket> tickets;
}

public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader("src/main/resources/tickets.json")) {
            Type type = new TypeToken<Tickets>() {}.getType();
            Tickets tickets = gson.fromJson(reader, type);

            Map<String, List<Ticket>> ticketsByCarrier = tickets.tickets.stream()
                    .filter(ticket -> ticket.origin.equals("VVO") && ticket.destination.equals("TLV"))
                    .collect(Collectors.groupingBy(ticket -> ticket.carrier));

            System.out.println("Минимальное время полета для каждого авиаперевозчика:");
            for (Map.Entry<String, List<Ticket>> entry : ticketsByCarrier.entrySet()) {
                String carrier = entry.getKey();
                List<Ticket> carrierTickets = entry.getValue();

                Optional<Ticket> minDurationTicket = carrierTickets.stream()
                        .min(Comparator.comparingLong(Ticket::getFlightDuration));

                if (minDurationTicket.isPresent()) {
                    long minDuration = minDurationTicket.get().getFlightDuration();
                    System.out.println("Перевозчик: " + carrier + ", минимальное время полета: " + minDuration + " минут");
                }
            }

            List<Integer> prices = tickets.tickets.stream()
                    .filter(ticket -> ticket.origin.equals("VVO") && ticket.destination.equals("TLV"))
                    .map(ticket -> ticket.price)
                    .sorted().toList();

            double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
            double medianPrice = 0;

            if (prices.size() > 0) {
                if (prices.size() % 2 == 0) {
                    medianPrice = (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2.0;
                } else {
                    medianPrice = prices.get(prices.size() / 2);
                }
            }

            System.out.println("Средняя цена билета: " + averagePrice);
            System.out.println("Медиана цены билета: " + medianPrice);
            System.out.println("Разница между средней и медианной ценой: " + Math.abs(averagePrice - medianPrice));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
