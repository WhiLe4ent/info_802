package com.example.controller;

import com.example.service.CartographieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CartographieController {

    private final CartographieService cartographieService;

    public CartographieController(CartographieService cartographieService) {
        this.cartographieService = cartographieService;
    }

    @GetMapping("/itineraire")
    public Map<String, Object> getItineraire(@RequestParam double departLat, 
                                             @RequestParam double departLon,
                                             @RequestParam double arriveeLat,
                                             @RequestParam double arriveeLon) {
        return cartographieService.getItineraire(departLat, departLon, arriveeLat, arriveeLon);
    }
}
