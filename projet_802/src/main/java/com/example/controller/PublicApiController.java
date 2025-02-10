package com.example.controller;

import com.example.service.BorneRechargeService;
import com.example.service.CartographieService;
import com.example.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final CartographieService cartographieService;
    private final BorneRechargeService borneRechargeService;
    private final VehicleService vehicleService;

    public PublicApiController(CartographieService cartographieService, BorneRechargeService borneRechargeService,
                               VehicleService vehicleService) {
        this.cartographieService = cartographieService;
        this.borneRechargeService = borneRechargeService;
        this.vehicleService = vehicleService;
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
    
        if (departVille == null || arriveeVille == null || vehicleId == null) {
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
        Map<String, Object> itineraire = cartographieService.getItineraireOnly(departVille, arriveeVille);
        if (itineraire.containsKey("error")) {
            return ResponseEntity.badRequest().body(itineraire);
        }
    
        // 🔹 Trouver les bornes de recharge nécessaires
        List<Map<String, Object>> bornes = borneRechargeService.getBornesPourRecharge(itineraire, worstRange);
        itineraire.put("bornes_recharge", bornes);
    
        return ResponseEntity.ok(itineraire);
    }
    
}
