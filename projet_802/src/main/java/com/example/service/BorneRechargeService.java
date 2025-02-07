package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BorneRechargeService {

    private static final String API_URL = "https://opendata.reseaux-energies.fr/api/records/1.0/search/";

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
}
