package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BorneRechargeService {

    private static final String API_URL = "https://opendata.reseaux-energies.fr/api/records/1.0/search/";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getBornesProches(double latitude, double longitude, int rayon) {
        String url = API_URL + "?dataset=bornes-irve&q=&geofilter.distance=" 
                     + latitude + "," + longitude + "," + rayon;
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("records")) {
            return (List<Map<String, Object>>) response.get("records");
        }
        return List.of();
    }
    
    public List<Map<String, Object>> getBornesPourRecharge(Map<String, Object> itineraire, double worstRange) {
        List<Map<String, Object>> bornesUtiles = new ArrayList<>();
        List<List<Double>> coordinates = (List<List<Double>>) ((Map<String, Object>) itineraire.get("geometry")).get("coordinates");

        double distanceParcourue = 0;
        double previousLat = 0, previousLon = 0;

        for (List<Double> coord : coordinates) {
            double lon = coord.get(0);
            double lat = coord.get(1);

            if (previousLat != 0 && previousLon != 0) {
                double distance = calculerDistance(previousLat, previousLon, lat, lon);
                distanceParcourue += distance;
            }

            previousLat = lat;
            previousLon = lon;

            if (distanceParcourue >= worstRange) {
                String url = API_URL + "?dataset=bornes-irve&geofilter.distance=" + lat + "," + lon + ",5000";
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null && response.containsKey("records")) {
                    List<Map<String, Object>> bornesTrouvees = (List<Map<String, Object>>) response.get("records");

                    if (!bornesTrouvees.isEmpty()) {
                        bornesUtiles.add(bornesTrouvees.get(0)); // Prendre la première borne trouvée
                        distanceParcourue = 0; // Réinitialiser pour le prochain arrêt
                    }
                }
            }
        }
        return bornesUtiles;
    }

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Rayon de la Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance en km
    }
}
