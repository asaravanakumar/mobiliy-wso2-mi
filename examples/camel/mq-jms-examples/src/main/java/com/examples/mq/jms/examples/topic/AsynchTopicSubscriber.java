package com.examples.mq.jms.examples.topic;

import java.util.concurrent.CountDownLatch;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate Asynchronous queue receive.
 * 
 * @author Saravana
 */
public class AsynchTopicSubscriber implements MessageListener, ExceptionListener {

	private final CountDownLatch countDownLatch;

	public AsynchTopicSubscriber(CountDownLatch latch) {
		// defining a latch to hold the current thread till asynchronous message
		// receive thread complete
		countDownLatch = latch;
	}

	public static void main(String[] args) {

		final String JNDI_TOPIC_CONNECTION_FACTORY_NAME = "TopicConnectionFactory";
		final String JNDI_TOPIC_NAME = "demoTopic";
		TopicConnectionFactory topicConnectionFactory = null;
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		Topic topic = null;
		TopicSubscriber topicSubscriber = null;
		TextMessage message = null;
		CountDownLatch countDownLatch = new CountDownLatch(1);

		try {
			
			System.out.println("\nWaiting to receive messages... Either waiting for END message or press Ctrl+C to exit");

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			topicConnectionFactory = (TopicConnectionFactory) ctx.lookup(JNDI_TOPIC_CONNECTION_FACTORY_NAME);

			// 3. lookup the topic object
			topic = (Topic) ctx.lookup(JNDI_TOPIC_NAME);

			// 4. create a queue connection
			topicConnection = topicConnectionFactory.createTopicConnection();

			// 5. create queue a session
			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// 6. create a topic subscriber
			topicSubscriber = topicSession.createSubscriber(topic);
			
			// 7. set an synchronous message listener
			AsynchTopicSubscriber subscriberToTopic = new AsynchTopicSubscriber(countDownLatch);
			topicSubscriber.setMessageListener(subscriberToTopic);
			
			topicConnection.setExceptionListener(subscriberToTopic);

			// start the connection
			topicConnection.start();

			// wait till all message receive complete
			countDownLatch.await();
			
			// close subscriber and session
			topicSubscriber.close();
			topicSession.close();

		} catch (JMSException e) {
			System.out.println("Exception occurred while receiving the message: " + e.toString());
		} catch (Exception e) {
			System.out.println("Error occured: " + e.toString());
			e.printStackTrace();
		} finally {
			if (topicConnection != null) {
				try {
					// close connection handle if work completed or any error
					topicConnection.close();
				} catch (JMSException e) {
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

	/*
	 * Callback method called by JMS to handle exceptions
	 */
	public void onException(JMSException exception) {
		System.out.println("Error occured: " + exception);
	}
}