package com.example.client;

import com.example.model.CalculTrajetRequest;
import com.example.model.CalculTrajetResponse;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

@Service
public class TrajetSoapClient extends WebServiceGatewaySupport {

    private static final String SOAP_URL = "http://localhost:8080/ws";
    private static final String SOAP_ACTION = "http://tp.vehicule.com/ws/CalculTrajetRequest";

    public TrajetSoapClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.example.model"); 
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    public CalculTrajetResponse calculerTrajet(double distance, double autonomie, double tempsRecharge) {
        CalculTrajetRequest request = new CalculTrajetRequest(distance, autonomie, tempsRecharge);

        return (CalculTrajetResponse) getWebServiceTemplate()
                .marshalSendAndReceive(SOAP_URL, request, new SoapActionCallback(SOAP_ACTION));
    }
}
