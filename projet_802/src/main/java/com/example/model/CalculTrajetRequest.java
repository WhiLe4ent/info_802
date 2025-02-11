package com.example.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor 
@XmlRootElement(name = "CalculTrajetRequest", namespace = "http://tp.vehicule.com/ws")
@XmlAccessorType(XmlAccessType.FIELD) 
public class CalculTrajetRequest {

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    private double distance;

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    private double autonomie;

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    private double tempsRecharge;

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getAutonomie() { return autonomie; }
    public void setAutonomie(double autonomie) { this.autonomie = autonomie; }

    public double getTempsRecharge() { return tempsRecharge; }
    public void setTempsRecharge(double tempsRecharge) { this.tempsRecharge = tempsRecharge; }
}
