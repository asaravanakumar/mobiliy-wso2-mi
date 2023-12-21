package com.examples.camel;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Camel Splitter Example
 */
public final class CamelSplitterExample {

	private CamelSplitterExample() {
	}

	public static void main(String args[]) throws Exception {

		// Step 1: Create CamelContext
		CamelContext context = new DefaultCamelContext();

		// Step 2: Register Components - set up the ActiveMQ JMS Components
		// Creates and connects to in-memory messaging system
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		// Connects to ActiveMQ
//		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

		// Step 3: Create Routes and Add to Context
		context.addRoutes(new RouteBuilder() {
			public void configure() {

				from("file://test").log("${headers}").log("${body}").to("direct:process");

				// Splitter
				from("direct:process").split(body().tokenize("\\|"))
						// .tokenize("\\|")
						.log("${body}");
			}
		});

		// Step 4: Start CamelContext
		context.start();

		// wait a bit and then stop
		Thread.sleep(300000);

		// Step 5: Stop CamelContext
		context.stop();
	}
}