package com.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.*;

@Service
public class BorneRechargeService {

    private static final String API_URL = "https://opendata.reseaux-energies.fr/api/records/1.0/search/";
    private RestTemplate restTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // Cache pour éviter les appels redondants
    private final Cache<String, List<Map<String, Object>>> cache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    @Autowired
    public BorneRechargeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getBornesProches(double latitude, double longitude, int rayon) {
        String cacheKey = latitude + "," + longitude + "," + rayon;
        List<Map<String, Object>> cachedResult = cache.getIfPresent(cacheKey);
        if (cachedResult != null) return cachedResult;

        String url = API_URL + "?dataset=bornes-irve&geofilter.distance=" + latitude + "," + longitude + "," + rayon + "&rows=1";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("records")) {
            List<Map<String, Object>> bornes = (List<Map<String, Object>>) response.get("records");
            cache.put(cacheKey, bornes); // Stocker dans le cache
            return bornes;
        }
        return List.of();
    }

    public List<Map<String, Object>> getBornesPourRecharge(Map<String, Object> itineraire, double worstRange) {
        List<Map<String, Object>> bornesUtiles = new ArrayList<>();
    
        // Vérifier que 'itineraire' contient bien "geometry"
        if (itineraire == null || !itineraire.containsKey("geometry")) {
            throw new IllegalArgumentException("L'itinéraire ne contient pas de clé 'geometry'.");
        }
    
        Map<String, Object> geometry = (Map<String, Object>) itineraire.get("geometry");
    
        // Vérifier que 'geometry' contient bien "coordinates"
        if (!geometry.containsKey("coordinates")) {
            throw new IllegalArgumentException("L'itinéraire ne contient pas de clé 'coordinates'.");
        }
    
        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
    
        double distanceParcourue = 0;
        double previousLat = 0, previousLon = 0;
    
        List<CompletableFuture<List<Map<String, Object>>>> futures = new ArrayList<>();
    
        for (List<Double> coord : coordinates) {
            if (coord.size() < 2) continue; // Vérifier que les coordonnées sont valides
    
            double lon = coord.get(0);
            double lat = coord.get(1);
    
            if (previousLat != 0 && previousLon != 0) {
                distanceParcourue += calculerDistance(previousLat, previousLon, lat, lon);
            }
    
            previousLat = lat;
            previousLon = lon;
    
            if (distanceParcourue >= worstRange) {
                double searchLat = lat, searchLon = lon;
                futures.add(CompletableFuture.supplyAsync(() -> getBornesProches(searchLat, searchLon, 50000), executor));
                distanceParcourue = 0; // Réinitialiser la distance
            }
        }
    
        // Attendre toutes les requêtes en parallèle
        List<Map<String, Object>> allBornes = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    
        // Ajouter seulement les premières bornes trouvées
        if (!allBornes.isEmpty()) {
            bornesUtiles.addAll(allBornes.subList(0, Math.min(allBornes.size(), coordinates.size())));
        }
    
        return bornesUtiles;
    }
    


    public double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }


    public Map<String, Double> trouverBorneProche(Map<String, Double> position, double rayonRecherche) {
        if (position == null || !position.containsKey("lat") || !position.containsKey("lon") ||
            position.get("lat") == null || position.get("lon") == null) {
            throw new RuntimeException("Coordonnées invalides pour la recherche de borne !");
        }
    
        String url = API_URL + "?dataset=bornes-irve&q=&geofilter.distance=" +
        position.get("lat") + "," + position.get("lon") + "," + "30000" + "&rows=1";

        System.out.println("Appel API URL : " + url);
    
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
    
        JsonNode records = response.getBody().path("records");
        if (records.isEmpty()) {
            return null; 
        }
    
        JsonNode fields = records.get(0).path("fields");
        double lat, lon;
        if (fields.has("geo_point_borne") && fields.path("geo_point_borne").isArray() && fields.path("geo_point_borne").size() >= 2) {
            lat = fields.path("geo_point_borne").get(0).asDouble(); 
            lon = fields.path("geo_point_borne").get(1).asDouble(); 
        } else {
            return null; 
        }
    
        System.out.println("Borne trouvée : lat=" + lat + ", lon=" + lon);
    
        return Map.of("lat", lat, "lon", lon);
    }
    
    

}
