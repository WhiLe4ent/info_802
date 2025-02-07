package com.example.controller;

import com.example.model.Vehicle;
import com.example.service.BorneRechargeService;
import com.example.service.CartographieService;
import com.example.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final BorneRechargeService borneRechargeService;
    private final CartographieService cartographieService;
    private final VehicleService vehicleService;

    public PublicApiController(BorneRechargeService borneRechargeService, CartographieService cartographieService, VehicleService vehicleService) {
        this.borneRechargeService = borneRechargeService;
        this.cartographieService = cartographieService;
        this.vehicleService = vehicleService;
        System.out.println("✅ PublicApiController chargé avec succès !");
    }

    @GetMapping("/trajet")
    public Map<String, Object> getTrajetData(@RequestParam(defaultValue = "0") double departLat,
                                             @RequestParam(defaultValue = "0") double departLon,
                                             @RequestParam(defaultValue = "0") double arriveeLat,
                                             @RequestParam(defaultValue = "0") double arriveeLon) {
    

        // Récupérer l'itinéraire
        Map<String, Object> itineraire = cartographieService.getItineraire(departLat, departLon, arriveeLat, arriveeLon);

        // Récupérer les bornes de recharge autour du point de départ
        List<Map<String, Object>> bornes = borneRechargeService.getBornesProches(departLat, departLon, 5000);
        
        for (Map<String, Object> borne : bornes) {
            Map<String, Object> fields = (Map<String, Object>) borne.get("fields");
            
            if (fields != null) {
                // Si les coordonnées existent déjà dans geo_point_borne, on les met dans coordonnees
                if (fields.containsKey("geo_point_borne")) {
                    fields.put("coordonnees", fields.get("geo_point_borne"));
                } 
                // Si elles existent seulement dans geometry.coordinates, on les transforme
                else if (borne.containsKey("geometry")) {
                    Map<String, Object> geometry = (Map<String, Object>) borne.get("geometry");
                    if (geometry.containsKey("coordinates")) {
                        List<Double> coords = (List<Double>) geometry.get("coordinates");
                        if (coords.size() == 2) {
                            // Inverser l'ordre pour correspondre au format attendu [lat, lon]
                            fields.put("coordonnees", List.of(coords.get(1), coords.get(0)));
                        }
                    }
                }
            }
        }
        

        // Récupérer la liste des véhicules
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        System.out.println("📡 Données des bornes reçues de l'API : " + bornes);

        // Regrouper les données en un seul objet JSON
        return Map.of(
                "itineraire", itineraire,
                "bornes_recharge", bornes,
                "vehicles", vehicles
        );
    }
}
