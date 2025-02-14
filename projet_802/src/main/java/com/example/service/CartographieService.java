package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
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
    private final BorneRechargeService borneRechargeService;

    public CartographieService(BorneRechargeService borneRechargeService) {
        this.borneRechargeService = borneRechargeService;
    }

    /**
     * Récupère un itinéraire entre deux points donnés (villes ou coordonnées GPS).
     */
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
                    resultat.put("distance_km", distance / 1000);
                    resultat.put("geometry", geometry); 

                    return resultat;
                }
            }
        }
        return Map.of("error", "Itinéraire non trouvé");
    }

    public Map<String, Object> getItineraireCoordonee(Map<String, Double> departCoords, Map<String, Double> arriveeCoords) {
        if (departCoords == null || arriveeCoords == null || !departCoords.containsKey("lat") || !departCoords.containsKey("lon") ||
            !arriveeCoords.containsKey("lat") || !arriveeCoords.containsKey("lon")) {
            return Map.of("error", "Coordonnées invalides.");
        }
        System.out.println("🔍 Recherche d'itinéraire entre " + departCoords + " et " + arriveeCoords);
    
        String url = API_URL + "?api_key=" + apiKey
                     + "&start=" + departCoords.get("lon") + "," + departCoords.get("lat")
                     + "&end=" + arriveeCoords.get("lon") + "," + arriveeCoords.get("lat");
    
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

    private double distanceEntrePoints(Map<String, Double> pointA, Map<String, Double> pointB) {
        double latA = pointA.get("lat");
        double lonA = pointA.get("lon");
        double latB = pointB.get("lat");
        double lonB = pointB.get("lon");
    
        return borneRechargeService.calculerDistance(latA, lonA, latB, lonB);
    }
    

    public Map<String, Object> calculerItineraireAvecRecharges(String depart, String arrivee, double autonomieVehicule) {
        Map<String, Object> itineraireData = getItineraireOnly(depart, arrivee);
    
        if (!itineraireData.containsKey("geometry") || !(itineraireData.get("geometry") instanceof Map)) {
            throw new RuntimeException("Format d'itinéraire invalide !");
        }
    
        Map<String, Object> geometry = (Map<String, Object>) itineraireData.get("geometry");
        if (!geometry.containsKey("coordinates") || !(geometry.get("coordinates") instanceof List<?>)) {
            throw new RuntimeException("Format des coordonnées invalide !");
        }
    
        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
        List<Map<String, Double>> itineraire = new ArrayList<>();
    
        for (List<Double> coord : coordinates) {
            if (coord.size() < 2) continue;
            itineraire.add(Map.of("lat", coord.get(1), "lon", coord.get(0)));
        }
    
        List<Map<String, Object>> segments = new ArrayList<>();
        List<Map<String, Double>> bornesUtilisees = new ArrayList<>();
    
        double autonomieRestante = autonomieVehicule * 0.9; // 90% de l'autonomie initiale
        double distanceParcourue = 0;
        double distanceTotale = 0;
        
        Map<String, Double> dernierPoint = itineraire.get(0); // point de départ
        Map<String, Double> destinationFinale = itineraire.get(itineraire.size() - 1);
    
        while (true) {
            // Vérifier si l'autonomie restante permet d'atteindre l'arrivée
            double distanceRestante = borneRechargeService.calculerDistance(
                dernierPoint.get("lat"), dernierPoint.get("lon"),
                destinationFinale.get("lat"), destinationFinale.get("lon")
            );
    
            if (distanceRestante <= autonomieRestante) {
                // Si on peut atteindre l'arrivée, on ajoute le dernier segment et on termine
                Map<String, Object> itineraireFinal = getItineraireCoordonee(dernierPoint, destinationFinale);
    
                Map<String, Object> dernierSegment = new HashMap<>();
                dernierSegment.put("depart", dernierPoint);
                dernierSegment.put("arrivee", destinationFinale);
                dernierSegment.put("itineraire", itineraireFinal);
                dernierSegment.put("distance_km", distanceRestante);
    
                segments.add(dernierSegment);
                distanceTotale += distanceRestante;
                break;
            }
    
            // Trouver une borne proche lorsque l'autonomie est épuisée
            Map<String, Double> borneRecharge = null;
            double rayonRecherche = autonomieRestante;
    
            while (borneRecharge == null && rayonRecherche <= autonomieVehicule * 1.5) {
                borneRecharge = borneRechargeService.trouverBorneProche(dernierPoint, rayonRecherche);
                rayonRecherche += 10;
            }
    
            if (borneRecharge == null) {
                throw new RuntimeException("Impossible de trouver une borne même après élargissement !");
            }
    
            // Ajouter le segment jusqu'à la borne
            Map<String, Object> itineraireVersBorne = getItineraireCoordonee(dernierPoint, borneRecharge);
    
            Map<String, Object> segment = new HashMap<>();
            segment.put("depart", dernierPoint);
            segment.put("borneRecharge", borneRecharge);
            segment.put("itineraire", itineraireVersBorne);
            segment.put("distance_km", autonomieRestante);
    
            segments.add(segment);
            bornesUtilisees.add(borneRecharge);
    
            // Mettre à jour les variables pour continuer
            dernierPoint = borneRecharge;
            distanceTotale += autonomieRestante;
            autonomieRestante = autonomieVehicule * 0.9; // Recharger à 90%
        }
    
        // Construire la réponse finale
        Map<String, Object> result = new HashMap<>();
        result.put("segments", segments);
        result.put("distance_totale_km", distanceTotale);
        result.put("bornes", bornesUtilisees);
    
        return result;
    }
    
    
    

    
    
    
    
}
