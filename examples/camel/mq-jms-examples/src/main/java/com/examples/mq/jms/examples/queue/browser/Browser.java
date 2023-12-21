package com.examples.mq.jms.examples.queue.browser;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Browser {

	private static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_QUEUE_NAME = "demoQueue";
	private static final Boolean NON_TRANSACTED = false;
	private static final long DELAY = 100;
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		QueueBrowser browser = null;

		try {
			System.out.println("\nBrowsing the messages...");

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_QUEUE_CONNECTION_FACTORY_NAME);

			// 3. create a connection
			connection = connectionFactory.createConnection();
			connection.start();

			// 4. create a session
			session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

			// 5. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_QUEUE_NAME);

			// 6. create message consumer
			browser = session.createBrowser((Queue)destination);

            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                TextMessage message = (TextMessage) enumeration.nextElement();
                System.out.println("Browsing: " + message + " " + message.getText());
                TimeUnit.MILLISECONDS.sleep(DELAY);
            }
            
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
