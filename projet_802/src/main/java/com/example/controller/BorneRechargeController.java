package com.example.controller;

import com.example.service.BorneRechargeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BorneRechargeController {

    private final BorneRechargeService borneRechargeService;

    public BorneRechargeController(BorneRechargeService borneRechargeService) {
        this.borneRechargeService = borneRechargeService;
    }

    @GetMapping("/bornes")
    public List<Map<String, Object>> getBornes(@RequestParam double latitude, 
                                               @RequestParam double longitude, 
                                               @RequestParam(defaultValue = "5000") int rayon) {
        return borneRechargeService.getBornesProches(latitude, longitude, rayon);
    }
}

