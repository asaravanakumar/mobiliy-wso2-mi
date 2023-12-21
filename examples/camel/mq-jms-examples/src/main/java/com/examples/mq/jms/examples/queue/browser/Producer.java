package com.examples.mq.jms.examples.queue.browser;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Producer {

	private static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_QUEUE_NAME = "demoQueue";
	private static final Boolean NON_TRANSACTED = false;
	private static final int NUM_MSGS = 10;
	private static final long DELAY = 100;

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;

		try {

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_QUEUE_CONNECTION_FACTORY_NAME);

			// 3. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_QUEUE_NAME);

			// 3. create a connection
			connection = connectionFactory.createConnection();
			connection.start();

			// 4. create queue a session
			session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

			// 5. create destination
			// destination = session.createQueue("test-queue");

			// 6. create message producer
			producer = session.createProducer(destination);

			for (int i = 0; i < NUM_MSGS; i++) {
				TextMessage message = session.createTextMessage("Message #" + i);
				System.out.println("Sending message #" + i);				
				producer.send(message);
				Thread.sleep(DELAY);
			}

			producer.close();
			session.close();

		} catch (JMSException e) {
			System.out.println("Exception occurred while sending message: " + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error occured: " + e.toString());
			e.printStackTrace();

		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					System.out.println("Could not close an open connection...");
				}
			}
		}
	}

}
