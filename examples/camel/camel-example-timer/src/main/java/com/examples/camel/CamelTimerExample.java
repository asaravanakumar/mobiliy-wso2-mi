package com.examples.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Camel Timer Example
 */
public final class CamelTimerExample {

	private CamelTimerExample() {
	}

	public static void main(String args[]) throws Exception {

		// Step 1: Create CamelContext
		CamelContext context = new DefaultCamelContext();

		// Step 2: Create Routes and Add to Context
		context.addRoutes(new RouteBuilder() {
			public void configure() {
				from("timer:demo?delay=10s&period=10s").log("Timer Triggered");
			}
		});

		// Step 3: Start CamelContext
		context.start();

		// wait a bit and then stop
		Thread.sleep(300000);

		// Step 4: Stop CamelContext
		context.stop();
	}
}