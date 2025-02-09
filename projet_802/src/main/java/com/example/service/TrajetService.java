package com.example.service;

import com.example.model.CalculTrajetRequest;
import com.example.model.CalculTrajetResponse;
import org.springframework.stereotype.Service;

@Service
public class TrajetService {

    private static final double VITESSE_MOYENNE = 90.0; // en km/h

    public CalculTrajetResponse calculerTrajet(CalculTrajetRequest request) {
        double distance = request.getDistance();
        double autonomie = request.getAutonomie();
        double tempsRecharge = request.getTempsRecharge();

        // Nombre d'arrêts nécessaires pour recharger (seulement si l'autonomie est dépassée)
        int arrets = (int) Math.floor(distance / autonomie);
        double tempsRechargeTotal = arrets * tempsRecharge;

        // Temps de conduite en heures
        double tempsConduite = distance / VITESSE_MOYENNE;

        // Temps total (conduite + recharge)
        double tempsTotal = tempsConduite + (tempsRechargeTotal / 60.0); // Convertir minutes en heures

        // Réponse
        CalculTrajetResponse response = new CalculTrajetResponse();
        response.setTempsTotal(tempsTotal);
        return response;
    }
}
