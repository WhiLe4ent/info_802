package com.example.service;

import com.example.model.CalculTrajetRequest;
import com.example.model.CalculTrajetResponse;
import org.springframework.stereotype.Service;

@Service
public class TrajetService {

    public CalculTrajetResponse calculerTrajet(CalculTrajetRequest request) {
        double distance = request.getDistance();
        double autonomie = request.getAutonomie();
        double tempsRecharge = request.getTempsRecharge();

        // Calcul du nombre d'arrêts nécessaires
        int arrets = (int) Math.ceil(distance / autonomie);

        // Temps de trajet total (temps de recharge + conduite)
        double tempsTotal = (distance / 100) + (arrets * tempsRecharge);

        // Création de la réponse
        CalculTrajetResponse response = new CalculTrajetResponse();
        response.setTempsTotal(tempsTotal);
        return response;
    }
}
