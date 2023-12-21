package com.examples.mq.jms.examples.queue.transaction;

import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class TransactionClient {

	private static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_QUEUE_NAME = "demoQueue";	
    private static final Boolean NON_TRANSACTED = false;
    private static final Boolean TRANSACTED = true;
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session producerSession = null;
		Session consumerSession = null;
		Destination destination = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;

		try {

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_QUEUE_CONNECTION_FACTORY_NAME);
			
			// 3. create a connection
			connection = connectionFactory.createConnection();
			connection.start();

			// 4. create a producer session
			producerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);

			// 5. create a consumer session
			consumerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
			
			// 6. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_QUEUE_NAME);

			// 6. create destination
			//destination = session.createQueue("test-queue");

			// 7. create message producer
			producer = producerSession.createProducer(destination);
			
			// 8. create message consumer and add listener to receive message
			consumer = consumerSession.createConsumer(destination);
			final Session receiverSession = consumerSession;
			consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        try {
                            String value = ((TextMessage) message).getText();
                            System.out.println("We received a new message: " + value);
                            if(value.equalsIgnoreCase("invalid"))
                            {
                            	throw Exception("Invalid value received");
                            }
                           receiverSession.commit();
                        } catch (JMSException e) {
                            System.out.println("Could not read the receiver's topic because of a JMSException");
                        } catch (Exception e)
                        {
                        	System.out.println("Unexpected error occured. " + e.getMessage());
                        	System.out.println("Rolling back transaction.");
                        	try {
								receiverSession.rollback();
							} catch (JMSException e1) {
								e1.printStackTrace();
							}
                        }
                    }
                }

				private Exception Exception(String errorMsg) {
					return new Exception(errorMsg);
				}
            });

			// 9. sending the messages to queue
			connection.start();
			acceptInputFromUser(producerSession, producer);
			producerSession.close();
			consumerSession.close();

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
	
    private static void acceptInputFromUser(Session producerSession, MessageProducer producer) throws JMSException {
        System.out.println("Type a message. Type COMMIT to send to receiver, type ROLLBACK to cancel");
        Scanner inputReader = new Scanner(System.in);

        while (true) {
            String line = inputReader.nextLine();
            if (line == null || line.length() == 0) {
                System.out.println("Done!");
                break;
            } else if (line.length() > 0) {
                if (line.trim().equals("ROLLBACK")) {
                    System.out.println("Rolling back...");
                    producerSession.rollback();
                    System.out.println("Messages have been rolledback");
                } else if (line.trim().equals("COMMIT")) {
                    System.out.println("Committing... ");
                    producerSession.commit();
                    System.out.println("Messages should have been sent");
                } else {
                    TextMessage message = producerSession.createTextMessage();
                    message.setText(line);
                    System.out.println("Batching up:'" + message.getText() + "'");
                    producer.send(message);
                }
            }
        }
    }	
}