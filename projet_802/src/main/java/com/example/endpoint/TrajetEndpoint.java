package com.example.endpoint;

import com.example.model.CalculTrajetRequest;
import com.example.model.CalculTrajetResponse;
import com.example.service.TrajetService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class TrajetEndpoint {

    private static final String NAMESPACE_URI = "http://tp.vehicule.com/ws";

    private final TrajetService trajetService;

    public TrajetEndpoint(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    @PayloadRoot(namespace = "http://tp.vehicule.com/ws", localPart = "CalculTrajetRequest")
    @ResponsePayload
    public CalculTrajetResponse calculerTrajet(@RequestPayload CalculTrajetRequest request) {
        return trajetService.calculerTrajet(request);
    }
    
}
