package com.example.controller;

import com.example.service.CartographieService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CartographieController {

    private final CartographieService cartographieService;

    public CartographieController(CartographieService cartographieService) {
        this.cartographieService = cartographieService;
    }

    @PostMapping("/itineraire")
    public Map<String, Object> getItineraire(@RequestBody Map<String, String> request) {
        String depart = request.get("departVille");
        String arrivee = request.get("arriveeVille");

        if (depart == null || arrivee == null) {
            return Map.of("error", "Les champs 'depart' et 'arrivee' sont requis.");
        }

        return cartographieService.calculerItineraireSansRecharges(depart, arrivee);
    }
}
