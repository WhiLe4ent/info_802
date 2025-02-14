package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartographieService {

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search?format=json&q=";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Récupère un itinéraire entre deux points donnés (villes ou coordonnées GPS).
     */
    @Cacheable(value = "itineraireCache", key = "#depart + '-' + #arrivee")
    public Map<String, Object> getItineraire(String depart, String arrivee) {
        double[] departCoords = getCoordinatesFromCity(depart);
        double[] arriveeCoords = getCoordinatesFromCity(arrivee);

        if (departCoords == null || arriveeCoords == null) {
            return Map.of("error", "Impossible d'obtenir les coordonnées des villes.");
        }

        String url = API_URL + "?api_key=" + apiKey
                     + "&start=" + departCoords[1] + "," + departCoords[0]
                     + "&end=" + arriveeCoords[1] + "," + arriveeCoords[0];

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        System.out.println("🔍 Response openrouteservice : " + response);
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

                    Map<String, Object> resultat = new HashMap<>();
                    resultat.put("distance_km", distance / 1000);
                    resultat.put("geometry", geometry); 
                    // resultat.put("temps_total", time); 

                    return resultat;
                }
            }
        }
        return Map.of("error", "Itinéraire non trouvé");
    }


    @Cacheable(value = "itineraireCache", key = "#depart + '-' + #arrivee")
    public Map<String, Object> getItineraireOnly(String depart, String arrivee) {
        double[] departCoords = getCoordinatesFromCity(depart);
        double[] arriveeCoords = getCoordinatesFromCity(arrivee);

        if (departCoords == null || arriveeCoords == null) {
            return Map.of("error", "Impossible d'obtenir les coordonnées des villes.");
        }

        String url = API_URL + "?api_key=" + apiKey
                     + "&start=" + departCoords[1] + "," + departCoords[0]
                     + "&end=" + arriveeCoords[1] + "," + arriveeCoords[0];

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

                    Map<String, Object> resultat = new HashMap<>();
                    System.out.println("🔍 Response openrouteservice : " + response);
                    resultat.put("distance_km", distance / 1000);
                    resultat.put("geometry", geometry); 

                    return resultat;
                }
            }
        }
        return Map.of("error", "Itinéraire non trouvé");
    }

    /**
     * Convertit un nom de ville en coordonnées GPS via Nominatim.
     */
    private double[] getCoordinatesFromCity(String cityName) {
        String url = NOMINATIM_API + cityName;
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
