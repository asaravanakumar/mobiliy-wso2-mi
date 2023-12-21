package com.examples.mq.jms.examples.queue;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate Asynchronous queue receive.
 * 
 * @author Saravana
 */
public class SynchQueueReceiver {

	public static void main(String[] args) {
		final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "QueueConnectionFactory";
		final String JNDI_QUEUE_NAME = "demoQueue";
		QueueConnectionFactory queueConnectionFactory = null;
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		Queue queue = null;
		QueueReceiver queueReceiver = null;

		try {
			System.out.println("\nWaiting to receive messages... Either waiting for END message or press Ctrl+C to exit");
			
			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(JNDI_QUEUE_CONNECTION_FACTORY_NAME);

			// 3. lookup the queue object
			queue = (Queue) ctx.lookup(JNDI_QUEUE_NAME);

			// 4. create a queue connection
			queueConnection = queueConnectionFactory.createQueueConnection();

			// 5. create queue a session
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			// 6. create a queue receiver
			queueReceiver = queueSession.createReceiver(queue);

			// 7. start the connection
			queueConnection.start();

			// 8. Receive all text messages from queue until a non-text message is received
			while (true) {
				Message message = queueReceiver.receive();
				if (message instanceof TextMessage) {
					String msg = ((TextMessage) message).getText();
					if ("END".equalsIgnoreCase(msg)) {
						System.out.println("Received END message!");
						break;
					} else {
						System.out.println("Received message: " + msg);
					}
				}
			}
			
			// close receiver and sessoin
			queueReceiver.close();
			queueSession.close();
			
		} catch (JMSException e) {
			System.out.println("Exception occurred: " + e.toString());
		} catch (Exception e) {
			System.out.println("Error occured: " + e.toString());
			e.printStackTrace();
		} finally {
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException e) {
				}
			}
		}
	}
}