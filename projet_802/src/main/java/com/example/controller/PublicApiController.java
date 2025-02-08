package com.example.controller;

import com.example.model.Vehicle;
import com.example.service.BorneRechargeService;
import com.example.service.CartographieService;
import com.example.service.VehicleService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/trajet")
    public ResponseEntity<Map<String, Object>> getTrajet(@RequestBody Map<String, String> request) {
        String departVille = request.get("departVille");
        String arriveeVille = request.get("arriveeVille");
        String vehicleId = request.get("vehicleId");
    
        if (departVille == null || arriveeVille == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Les champs departVille, arriveeVille et vehicleId sont requis."));
        }
    
        // 1. Convertir les villes en coordonnées GPS
        double[] departCoords = getCoordinatesFromCity(departVille);
        double[] arriveeCoords = getCoordinatesFromCity(arriveeVille);
    
        if (departCoords == null || arriveeCoords == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Impossible d'obtenir les coordonnées des villes."));
        }
    
        // 2. Récupérer l'itinéraire
        String url = "https://api.openrouteservice.org/v2/directions/driving-car?"
                   + "api_key=" + apiKey
                   + "&start=" + departCoords[1] + "," + departCoords[0]
                   + "&end=" + arriveeCoords[1] + "," + arriveeCoords[0];
    
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Impossible de récupérer l'itinéraire."));
        }
    
        return ResponseEntity.ok(response);
    }
    

    // 🔍 Fonction pour convertir une ville en coordonnées GPS
    private double[] getCoordinatesFromCity(String cityName) {
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + cityName;
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        if (response.getBody() == null || response.getBody().isEmpty()) {
            return null;
        }

        Map<String, Object> firstResult = (Map<String, Object>) response.getBody().get(0);
        double lat = Double.parseDouble((String) firstResult.get("lat"));
        double lon = Double.parseDouble((String) firstResult.get("lon"));
        return new double[]{lat, lon};
    }
}

