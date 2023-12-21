package com.examples.mq.jms.examples.queue;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate sending message(s) to queue
 * 
 * @author Saravana
 */
public class SenderToQueue {

	public static void main(String[] args) {
		final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "QueueConnectionFactory";
		final String JNDI_QUEUE_NAME = "demoQueue";
		QueueConnectionFactory queueConnectionFactory = null;
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		Queue queue = null;
		QueueSender queueSender = null;
		TextMessage message = null;
		final int NUM_MSGS = 5;
		final String MSG_TEXT = new String("Here is a message");

		try {

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

			// 6. create a queue sender.
			queueSender = queueSession.createSender(queue);

			// 7. create text message. Send five messages, varying text
			// slightly.
			message = queueSession.createTextMessage();
			for (int i = 0; i < NUM_MSGS; i++) {
				
				message.setText(MSG_TEXT + " " + (i + 1));
				System.out.println("Sending message: " + message.getText());
				//message.setJMSPriority(9);
				queueSender.send(message);				
			}

			// Send a END message indicating end of messages.
			 queueSender.send(queueSession.createTextMessage("END"));
		} catch (JMSException e) {
			System.out.println("Exception occurred while sending message: " + e.toString());
			e.printStackTrace();
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