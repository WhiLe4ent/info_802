package com.example.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@XmlRootElement(name = "CalculTrajetResponse", namespace = "http://tp.vehicule.com/ws")
@XmlAccessorType(XmlAccessType.FIELD) 
public class CalculTrajetResponse {

    @XmlElement(namespace = "http://tp.vehicule.com/ws")
    private double tempsTotal;

    public double getTempsTotal() { return tempsTotal; }
    public void setTempsTotal(double tempsTotal) { this.tempsTotal = tempsTotal; }
}
