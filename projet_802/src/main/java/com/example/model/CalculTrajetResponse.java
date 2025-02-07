package com.example.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CalculTrajetResponse", namespace = "http://tp.vehicule.com/ws")
public class CalculTrajetResponse {

    private double tempsTotal;

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    public double getTempsTotal() {
        return tempsTotal;
    }

    public void setTempsTotal(double tempsTotal) {
        this.tempsTotal = tempsTotal;
    }
}
