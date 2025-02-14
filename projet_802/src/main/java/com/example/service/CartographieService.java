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


    public Map<String, Object> calculerItineraireSansRecharges(String depart, String arrivee) {
        // Récupérer les informations d'itinéraire
        Map<String, Object> itineraireData = getItineraireOnly(depart, arrivee);
    
        // Vérifier si la géométrie existe et a un format correct
        if (!itineraireData.containsKey("geometry") || !(itineraireData.get("geometry") instanceof Map)) {
            throw new RuntimeException("Format d'itinéraire invalide !");
        }
    
        // Récupérer la géométrie et les coordonnées
        Map<String, Object> geometry = (Map<String, Object>) itineraireData.get("geometry");
        if (!geometry.containsKey("coordinates") || !(geometry.get("coordinates") instanceof List<?>)) {
            throw new RuntimeException("Format des coordonnées invalide !");
        }
    
        // Extraire les coordonnées de l'itinéraire
        List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
        List<Map<String, Double>> itineraire = new ArrayList<>();
    
        for (List<Double> coord : coordinates) {
            if (coord.size() < 2) continue;
            itineraire.add(Map.of("lat", coord.get(1), "lon", coord.get(0)));
        }
    
        // Récupérer les points de départ et d'arrivée
        Map<String, Double> departPoint = itineraire.get(0);
        Map<String, Double> destinationFinale = itineraire.get(itineraire.size() - 1);
    
        // Créer le premier segment
        Map<String, Object> segment = new HashMap<>();
        segment.put("depart", departPoint);
        segment.put("arrivee", destinationFinale);
        segment.put("distance_km", itineraireData.get("distance_km"));
        segment.put("itineraire", Map.of(
            "distance_km", itineraireData.get("distance_km"),
            "geometry", geometry
        ));
    
        // Renvoyer les résultats
        return Map.of(
            "segments", List.of(segment), 
            "distance_km", itineraireData.get("distance_km")
        );
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
    
        double autonomieRestante = autonomieVehicule * 0.8; 
        double distanceTotale = 0;
        double distanceRestante = borneRechargeService.calculerDistance(
            itineraire.get(0).get("lat"), itineraire.get(0).get("lon"),
            itineraire.get(itineraire.size() - 1).get("lat"), itineraire.get(itineraire.size() - 1).get("lon")
        );
    
        Map<String, Double> departPoint = itineraire.get(0);
        Map<String, Double> destinationFinale = itineraire.get(itineraire.size() - 1);
    
        while (distanceRestante > autonomieRestante) {
            Map<String, Double> pointRecharge = trouverPointRecharge(itineraire, departPoint, autonomieRestante);
            Map<String, Double> borneRecharge = chercherBorneProche(pointRecharge, autonomieRestante);
    
            if (borneRecharge == null) {
                throw new RuntimeException("Impossible de trouver une borne de recharge même après élargissement du rayon de recherche !");
            }
    
            segments.add(creerSegment(departPoint, borneRecharge, autonomieRestante));
            bornesUtilisees.add(borneRecharge);
    
            // Mettre à jour la distance restante
            double distanceParcourue = borneRechargeService.calculerDistance(departPoint.get("lat"), departPoint.get("lon"), borneRecharge.get("lat"), borneRecharge.get("lon"));
            distanceTotale += distanceParcourue;
            distanceRestante -= distanceParcourue; 
            System.out.println("🔋 distance restante: " + distanceRestante + " km");

            departPoint = borneRecharge;
        }
    
        // Ajouter le dernier segment
        segments.add(creerSegment(departPoint, destinationFinale, distanceRestante));
        distanceTotale += distanceRestante;
    
        return Map.of(
            "segments", segments,
            "distance_km", distanceTotale,
            "bornes", bornesUtilisees
        );
    }
    
    
    private Map<String, Object> creerSegment(Map<String, Double> depart, Map<String, Double> arrivee, double distance) {
        return Map.of(
            "depart", depart,
            "arrivee", arrivee,
            "itineraire", getItineraireCoordonee(depart, arrivee),
            "distance_km", distance
        );
    }
    
    private Map<String, Double> trouverPointRecharge(List<Map<String, Double>> itineraire, Map<String, Double> depart, double autonomieRestante) {
        for (int i = 1; i < itineraire.size(); i++) {
            Map<String, Double> point = itineraire.get(i);
            double distance = borneRechargeService.calculerDistance(depart.get("lat"), depart.get("lon"), point.get("lat"), point.get("lon"));
            if (distance > autonomieRestante) {
                return itineraire.get(i - 1);
            }
        }
        return itineraire.get(itineraire.size() - 1);
    }
    
    private Map<String, Double> chercherBorneProche(Map<String, Double> point, double autonomieRestante) {
        double rayonRecherche = autonomieRestante * 150; // Commencer avec un rayon réduit
        Map<String, Double> borne = null;
        
        for (int tentative = 0; tentative < 5; tentative++) {
            if(borne != null) {
                break;
            }
            System.out.println("🔍 Recherche de borne à proximité de " + point + " avec un rayon de " + rayonRecherche + " mètres");
            borne = borneRechargeService.trouverBorneProche(point, rayonRecherche);
            rayonRecherche *= 1.5; // Augmenter le rayon de manière exponentielle
        }

        
        return borne;
    }
    
    
    
    

    
    
    
    
}
