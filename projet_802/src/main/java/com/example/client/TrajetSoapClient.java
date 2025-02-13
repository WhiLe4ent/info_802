package com.example.client;

import com.example.model.CalculTrajetRequest;
import com.example.model.CalculTrajetResponse;

import jakarta.annotation.PostConstruct;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

@Service
public class TrajetSoapClient extends WebServiceGatewaySupport {

    @Value("${service.url}")
    private String serviceUrl;

    private String soapUrl; 

    private static final String SOAP_ACTION = "http://tp.vehicule.com/ws/CalculTrajetRequest";

    public TrajetSoapClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.example.model");
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    @PostConstruct
    public void init() {
        this.soapUrl = serviceUrl + "/ws"; // Construit l'URL après injection de serviceUrl
    }

    public CalculTrajetResponse calculerTrajet(double distance, double autonomie, double tempsRecharge) {
        CalculTrajetRequest request = new CalculTrajetRequest(distance, autonomie, tempsRecharge);

        return (CalculTrajetResponse) getWebServiceTemplate()
                .marshalSendAndReceive(soapUrl, request, new SoapActionCallback(SOAP_ACTION));
    }
}

