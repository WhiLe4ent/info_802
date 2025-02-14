package com.example.controller;

import com.example.client.TrajetSoapClient;
import com.example.service.CartographieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final CartographieService cartographieService;
    private final TrajetSoapClient trajetSoapClient;

    public PublicApiController(CartographieService cartographieService,
                               TrajetSoapClient trajetSoapClient) {
        this.cartographieService = cartographieService;
        this.trajetSoapClient = trajetSoapClient;
    }

    @PostMapping("/trajet-complet")
    public ResponseEntity<Map<String, Object>> getTrajetComplet(@RequestBody Map<String, Object> request) {
        System.out.println("🔍 Requête reçue: " + request);

        if (request == null || request.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le corps de requête est vide."));
        }

        String departVille = (String) request.get("departVille");
        String arriveeVille = (String) request.get("arriveeVille");
        String vehicleId = (String) request.get("vehicleId");

        if (departVille == null || arriveeVille == null ) {
            return ResponseEntity.badRequest().body(Map.of("error", "Les champs departVille, arriveeVille et vehicleId sont requis."));
        }

        Number bestRangeNum = (Number) request.getOrDefault("bestRange", 0);
        Number worstRangeNum = (Number) request.getOrDefault("worstRange", 0);

        double bestRange = bestRangeNum.doubleValue();
        double worstRange = worstRangeNum.doubleValue();

        System.out.println("✅ Données récupérées :");
        System.out.println("   ➡️ Depart: " + departVille);
        System.out.println("   ➡️ Arrivée: " + arriveeVille);
        System.out.println("   ➡️ Vehicle ID: " + vehicleId);
        System.out.println("   ➡️ Best Range: " + bestRange);
        System.out.println("   ➡️ Worst Range: " + worstRange);

        // 🔹 Récupérer l'itinéraire
        if (vehicleId == null) {
            Map<String, Object> itineraire = cartographieService.getItineraireOnly(departVille, arriveeVille);
            if (itineraire.containsKey("error")) {
                return ResponseEntity.badRequest().body(itineraire);
            }       
            return ResponseEntity.ok(itineraire);

        }else{

            Map<String, Object> itineraire = cartographieService.calculerItineraireAvecRecharges(departVille, arriveeVille, worstRange);
            if (itineraire.containsKey("error")) {
                return ResponseEntity.badRequest().body(itineraire);
            }
            return ResponseEntity.ok(itineraire);

        }

        // // 🔹 Trouver les bornes de recharge nécessaires
        // List<Map<String, Object>> bornes = borneRechargeService.getBornesPourRecharge(itineraire, worstRange);
        // itineraire.put("bornes_recharge", bornes);

        // // 🔹 Calculer le temps de trajet via SOAP
        // double distance = (double) itineraire.get("distance_km");
        // CalculTrajetResponse trajetResponse = trajetSoapClient.calculerTrajet(distance, worstRange, tempsRecharge);

        // // 🔹 Ajouter le temps total dans la réponse
        // itineraire.put("temps_total", trajetResponse.getTempsTotal());

    }
}
