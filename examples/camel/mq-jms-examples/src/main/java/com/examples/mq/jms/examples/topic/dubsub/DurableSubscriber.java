package com.examples.mq.jms.examples.topic.dubsub;

import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;

public class DurableSubscriber implements MessageListener{

	private static final String JNDI_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_DEST_NAME = "demoTopic";
	private static final Boolean NON_TRANSACTED = false;
	private static final int NUM_MSGS = 5;
	private static final long DELAY = 100;
	
	private final CountDownLatch countDownLatch;
	
	public DurableSubscriber(CountDownLatch latch)
	{
		// defining a latch to hold the current thread till asynchronous message
		// receive thread complete
		countDownLatch = latch;
	}

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageConsumer consumer = null;
		CountDownLatch countDownLatch = new CountDownLatch(1);

		try {
			
			System.out.println("\nWaiting to receive messages... Either waiting for END message or press Ctrl+C to exit");

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_CONNECTION_FACTORY_NAME);

			// 3. create a connection
			connection = connectionFactory.createConnection();
            String clientId = "sub1";
            connection.setClientID(clientId);	

			// 4. create a session
			session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

			// 5. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_DEST_NAME);
			
			// 6. create a topic subscriber		
			consumer = session.createDurableSubscriber((Topic) destination, clientId);			

			// 7. set an synchronous message listener
			DurableSubscriber durableSubscriber = new DurableSubscriber(countDownLatch);
			consumer.setMessageListener(durableSubscriber);

			// start connection
			connection.start();
			
			// wait till all message receive complete
			countDownLatch.await();

			consumer.close();
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
	
	/*
	 * Callback method called by JMS to receive the message.
	 */
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				String msg = ((TextMessage) message).getText();
				if ("END".equalsIgnoreCase(msg)) {
					System.out.println("Received END message!");
					countDownLatch.countDown();

				} else {
					System.out.println("Received message:" + msg);
				}
			}
		} catch (Exception e) {
			//throw new JMSException(e.getMessage());
			System.out.println("Got JMS Exception in onMessage(): " + e.toString());
		}
	}	
}
