package com.example.service;

import com.example.model.Vehicle;
import com.example.model.Vehicle.Battery;
import com.example.model.Vehicle.Image;
import com.example.model.Vehicle.Media;
import com.example.model.Vehicle.Naming;
import com.example.model.Vehicle.Range;
import com.example.response.VehicleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final String CHARGETRIP_API_URL = "https://api.chargetrip.io/graphql";

    private final WebClient webClient;

    @Autowired
    public VehicleService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(CHARGETRIP_API_URL)
                .defaultHeader("x-client-id", "67ab59ed4802aaa070546d8c")
                .defaultHeader("x-app-id", "67ab59ed4802aaa070546d8e")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<Vehicle> getAllVehicles(int page, int size, String search) {
        // Requête GraphQL avec des variables
        String query = """
            query vehicleList($page: Int, $size: Int, $search: String) {
                vehicleList(page: $page, size: $size, search: $search) {
                    id
                    naming {
                        make
                        model
                        chargetrip_version
                    }
                    media {
                        image {
                            thumbnail_url
                        }
                    }
                    battery {
                        usable_kwh
                    }
                    range {
                        chargetrip_range {
                            best
                            worst
                        }
                    }
                }
            }
        """;
    
        // Définir les variables GraphQL
        String requestBody = """
            {
                "query": "%s",
                "variables": {
                    "page": %d,
                    "size": %d,
                    "search": "%s"
                }
            }
        """.formatted(query.replace("\n", " "), page, size, search != null ? search : "");
    
        System.out.println("📡 Requête envoyée à Chargetrip : " + requestBody);
    
        try {

            // System.out.println("📡 Réponse brute de Chargetrip : " + jsonResponse);
    
            // Désérialisation de la réponse
            VehicleResponse response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(VehicleResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return response.getData().getVehicleList().stream()
                        .map(car -> {
                            Naming naming = new Naming(car.getNaming().getMake(), car.getNaming().getModel(), null);
                            Battery battery = new Battery(car.getBattery().getUsable_kwh());
                            Range range = new Range(new Range.ChargetripRange(car.getRange().getChargetrip_range().getBest(),
                                    car.getRange().getChargetrip_range().getWorst()));
            
                            Image image = new Image();
                            image.setThumbnail_url("default-image.jpg");
                            Media mediaDefault = new Media();
                            mediaDefault.setImage(image);

                            Media media = (car.getMedia() != null && car.getMedia().getImage() != null && car.getMedia().getImage().getThumbnail_url() != null) 
                            ? car.getMedia() 
                            : mediaDefault;                         
                                    
                            return new Vehicle(car.getId(), naming, media, battery, range);
                        })
                        .collect(Collectors.toList());
            }
        } catch (WebClientResponseException e) {
            System.err.println("❌ Erreur WebClient lors de la récupération des véhicules : " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue : " + e.getMessage());
        }
    
        return List.of(); // Retourne une liste vide en cas d’erreur
    }
    
}
