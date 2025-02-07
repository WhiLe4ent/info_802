package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartographieService {

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Cacheable(value = "itineraireCache", key = "#departLat + '-' + #departLon + '-' + #arriveeLat + '-' + #arriveeLon")
    public Map<String, Object> getItineraire(double departLat, double departLon, double arriveeLat, double arriveeLon) {
        String url = API_URL + "?api_key=" + apiKey 
                     + "&start=" + departLon + "," + departLat
                     + "&end=" + arriveeLon + "," + arriveeLat;
    
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
    
        if (response != null && response.containsKey("features")) {
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");
            if (!features.isEmpty()) {
                Map<String, Object> feature = features.get(0);
                Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
    
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                List<Map<String, Object>> segments = (List<Map<String, Object>>) properties.get("segments");
    
                if (!segments.isEmpty()) {
                    Map<String, Object> segment = segments.get(0);
                    double distance = (double) segment.get("distance");
                    double duration = (double) segment.get("duration");
    
                    Map<String, Object> resultat = new HashMap<>();
                    resultat.put("distance_km", distance / 1000);
                    resultat.put("duree_minutes", duration / 60);
                    resultat.put("geometry", geometry); // Ajout des coordonnées
    
                    System.out.println("📡 Réponse envoyée à React : " + resultat);
                    return resultat;
                }
            }
        }
        return Map.of("message", "Itinéraire non trouvé");
    }
}
