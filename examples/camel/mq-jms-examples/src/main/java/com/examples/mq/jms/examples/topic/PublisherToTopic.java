package com.examples.mq.jms.examples.topic;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate sending message(s) to topic
 * 
 * @author Saravana
 */
public class PublisherToTopic {

	public static void main(String[] args) {
		final String JNDI_TOPIC_CONNECTION_FACTORY_NAME = "TopicConnectionFactory";
		final String JNDI_TOPIC_NAME = "demoTopic";
		TopicConnectionFactory topicConnectionFactory = null;
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		Topic topic = null;
		TopicPublisher topicPublisher = null;
		TextMessage message = null;
		final int NUM_MSGS = 5;
		final String MSG_TEXT = new String("Here is a message");

		try {

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

			// 6. create a topic publisher.
			topicPublisher = topicSession.createPublisher(topic);

			// 7. create text message. Send five messages, varying text slightly.
			message = topicSession.createTextMessage();
			for (int i = 0; i < NUM_MSGS; i++) {
				message.setText(MSG_TEXT + " " + (i + 1));
				System.out.println("Sending message: " + message.getText());
				topicPublisher.publish(message);
			}

			// Send a END message indicating end of messages.
			topicPublisher.publish(topicSession.createTextMessage("END"));
		} catch (JMSException e) {
			System.out.println("Exception occurred while sending message: " + e.toString());
			e.printStackTrace();
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