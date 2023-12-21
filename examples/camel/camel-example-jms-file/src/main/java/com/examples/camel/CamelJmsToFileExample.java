package com.examples.camel;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * This example sends some text messages on to a JMS Queue, consumes them and
 * persists them to disk
 */
public final class CamelJmsToFileExample {

    private CamelJmsToFileExample() {        
    }
    
    public static void main(String args[]) throws Exception {
       
    	// Step 1: Create CamelContext
        CamelContext context = new DefaultCamelContext();

        // Step 2: Register Components - set up the ActiveMQ JMS Components
        // Creates and connects to in-memory messaging system
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        // Connects to ActiveMQ
        // ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");        	
        context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        
        
        // Step 3: Create Routes and Add to Context
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("activemq:demo").log("${body}").to("file://test");
                
                // Route with custom file name and append mode enabled
                // from("activemq:demo").log("${body}").to("file://test?fileName=demo.txt&fileExist=Append");
            }
        });
        
        
        // Step 4: Create Producer to send test messages
        ProducerTemplate template = context.createProducerTemplate();

        // Step 5: Start CamelContext
        context.start();
        
        // Step 6: Send test messages
        for (int i = 0; i < 10; i++) {
            template.sendBody("activemq:demo", "Test Message: " + i);
        }

        // wait a bit and then stop
        Thread.sleep(300000);
        
        // Step 7: Stop CamelContext
        context.stop();
    }
}