package com.example.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

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

        String url = API_URL + "?dataset=bornes-irve&geofilter.distance=" + latitude + "," + longitude + "," + rayon;
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
        List<List<Double>> coordinates = (List<List<Double>>) ((Map<String, Object>) itineraire.get("geometry")).get("coordinates");

        double distanceParcourue = 0;
        double previousLat = 0, previousLon = 0;

        List<CompletableFuture<List<Map<String, Object>>>> futures = new ArrayList<>();

        for (List<Double> coord : coordinates) {
            double lon = coord.get(0);
            double lat = coord.get(1);

            if (previousLat != 0 && previousLon != 0) {
                distanceParcourue += calculerDistance(previousLat, previousLon, lat, lon);
            }

            previousLat = lat;
            previousLon = lon;

            if (distanceParcourue >= worstRange) {
                double searchLat = lat, searchLon = lon;
                futures.add(CompletableFuture.supplyAsync(() -> getBornesProches(searchLat, searchLon, 5000), executor));
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

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
