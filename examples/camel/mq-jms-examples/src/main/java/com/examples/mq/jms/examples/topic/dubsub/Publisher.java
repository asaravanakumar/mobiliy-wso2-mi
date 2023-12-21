package com.examples.mq.jms.examples.topic.dubsub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Publisher {

	private static final String JNDI_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_DEST_NAME = "demoTopic";	
    private static final Boolean NON_TRANSACTED = false;
    private static final int NUM_MSGS = 5;
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
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_CONNECTION_FACTORY_NAME);
			
			// 3. create a connection
			connection = connectionFactory.createConnection();
			connection.start();

			// 4. create a session
			session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
			
			// 5. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_DEST_NAME);		

			// 6. create message producer
			producer = session.createProducer(destination);


            for (int i = 0; i < NUM_MSGS; i++) {
                TextMessage message = session.createTextMessage("Message #" + i);
                System.out.println("Sending message #" + i);
                producer.send(message);
                Thread.sleep(DELAY);
            }

            // tell the subscribers we're done
            producer.send(session.createTextMessage("END"));

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
