package com.example.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@Configuration
public class WebServiceConfig {

    /**
     * Expose /ws/* for the web service, using the application context to configure the servlet and the dispatcher servlet to handle the requests and responses SOAP
     * @param context the application context
     * @return
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /**
     * Expose /ws/trajet.xsd for the web service, using the schema to define the structure of the messages
     * @param trajetSchema the schema
     * @return
     */
    @Bean(name = "trajet")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema trajetSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("TrajetPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://tp.vehicule.com/ws");
        definition.setSchema(trajetSchema);
        return definition;
    }

    @Bean
    public XsdSchema trajetSchema() {
        return new SimpleXsdSchema(new org.springframework.core.io.ClassPathResource("wsdl/trajet.xsd", this.getClass().getClassLoader()));
    }
    
}
