package com.example.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CalculTrajetRequest", namespace = "http://tp.vehicule.com/ws")
public class CalculTrajetRequest {

    private double distance;
    private double autonomie;
    private double tempsRecharge;

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    public double getAutonomie() {
        return autonomie;
    }

    public void setAutonomie(double autonomie) {
        this.autonomie = autonomie;
    }

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    public double getTempsRecharge() {
        return tempsRecharge;
    }

    public void setTempsRecharge(double tempsRecharge) {
        this.tempsRecharge = tempsRecharge;
    }
}
