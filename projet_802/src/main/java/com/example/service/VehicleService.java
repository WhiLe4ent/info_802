package com.example.service;

import com.example.model.Vehicle;
import com.example.response.VehicleResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final String CHARGETRIP_API_URL = "https://api.chargetrip.io/graphql";

    private static final String CLIENT_ID = "678a18d96f014f34da84461e";
    private static final String APP_ID = "678a18d96f014f34da844620";

    private final WebClient webClient;

    public VehicleService() {
        this.webClient = WebClient.builder()
                .baseUrl(CHARGETRIP_API_URL)
                .defaultHeader("x-client-id", CLIENT_ID)
                .defaultHeader("x-app-id", APP_ID)
                .defaultHeader("Content-Type", "application/json") // Assure que le type est bien défini
                .build();
    }

    public List<Vehicle> getAllVehicles() {
        // Requête GraphQL sous forme JSON bien formatée
        String query = """
            {
                carList {
                    id
                    naming { make model }
                    battery { usable_kwh }
                    range { chargetrip_range { best worst } }
                }
            }
        """;

        // Conversion de la requête en JSON brut
        String requestBody = String.format("{\"query\": \"%s\"}", query.replace("\n", " ").replace("\"", "\\\""));
        System.out.println("📡 Requête envoyée à Chargetrip : " + requestBody); // Log pour debug

        try {
            // Récupération des données de Chargetrip
            VehicleResponse response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("❌ Erreur API Chargetrip : " + clientResponse.statusCode());
                                return Mono.error(new RuntimeException("Erreur API Chargetrip : " + clientResponse.statusCode()));
                            })
                    .bodyToMono(VehicleResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return response.getData().getCarList().stream()
                        .map(car -> new Vehicle(
                                car.getId(),
                                car.getNaming().getMake(),
                                car.getNaming().getModel(),
                                car.getBattery().getUsableKwh(),
                                car.getRange().getChargetripRange().getBest(),
                                car.getRange().getChargetripRange().getWorst()
                        ))
                        .collect(Collectors.toList());
            } else {
                System.err.println("⚠️ Aucune donnée reçue de Chargetrip.");
            }

        } catch (WebClientResponseException e) {
            System.err.println("❌ Erreur WebClient lors de la récupération des véhicules : " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue : " + e.getMessage());
        }

        return List.of(); // Retourne une liste vide si l’API échoue
    }
}
