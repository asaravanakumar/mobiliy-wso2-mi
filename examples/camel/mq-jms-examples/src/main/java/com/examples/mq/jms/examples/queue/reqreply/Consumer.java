package com.examples.mq.jms.examples.queue.reqreply;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Consumer {

	private static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_QUEUE_NAME = "demoQueue";
	private static final Boolean NON_TRANSACTED = false;
	private static final long TIMEOUT = 20000;

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageConsumer consumer = null;

		try {
			System.out.println("\nWaiting to receive messages... Either waiting for END message or press Ctrl+C to exit");
			
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
			consumer = session.createConsumer(destination);

            int i = 0;
            while (true) {
                Message message = consumer.receive(TIMEOUT);

                if (message != null) {
                    if (message instanceof TextMessage) {
                        String text = ((TextMessage) message).getText();
                        System.out.println("Got " + i++ + ". message: " + text);
                        Destination replyTo = message.getJMSReplyTo();
                        MessageProducer producer = session.createProducer(replyTo);
                        TextMessage msg = session.createTextMessage();
                        msg.setText("You made it to the consumer, here is your response");
                        msg.setJMSCorrelationID(message.getJMSMessageID());
                        producer.send(msg);
                        producer.close();
                    }
                } else {
                    break;
                }
            }

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
}
