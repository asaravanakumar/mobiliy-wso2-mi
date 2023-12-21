package com.examples.mq.jms.examples.topic;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate synchronous topic receive.
 *  
 * @author Saravana
 */
public class SynchTopicSubscriber {

	public static void main(String[] args) {
		final String JNDI_TOPIC_CONNECTION_FACTORY_NAME = "TopicConnectionFactory";
		final String JNDI_TOPIC_NAME = "demoTopic";
		TopicConnectionFactory topicConnectionFactory = null;
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		Topic topic = null;
		TopicSubscriber topicSubscriber = null;

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

			// 7. start the connection
			topicConnection.start();

			// 8. Receive all text messages from queue until END message is received
			while (true) {
				Message message = topicSubscriber.receive();
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
			
			// close receiver and session
			topicSubscriber.close();
			topicSession.close();
			
		} catch (JMSException e) {
			System.out.println("Exception occurred: " + e.toString());
		} catch (Exception e) {
			System.out.println("Error occured: " + e.toString());
			e.printStackTrace();
		} finally {
			if (topicConnection != null) {
				try {
					topicConnection.close();
				} catch (JMSException e) {
				}
			}
		}
	}
}