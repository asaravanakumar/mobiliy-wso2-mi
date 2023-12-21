package com.examples.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class MessageProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String msg = (String) exchange.getIn().getBody(String.class);
		msg += " processed";
		exchange.getOut().setBody(msg);
	}
}
