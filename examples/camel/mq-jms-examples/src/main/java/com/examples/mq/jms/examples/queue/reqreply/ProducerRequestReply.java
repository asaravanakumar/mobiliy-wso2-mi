package com.examples.mq.jms.examples.queue.reqreply;

import java.util.concurrent.TimeUnit;

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

public class ProducerRequestReply {
	
	private static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	private static final String JNDI_QUEUE_NAME = "demoQueue";	
    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 100;
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		Destination replyDest = null;
		MessageProducer producer = null;
		MessageConsumer replyConsumer = null;

		try {

			// 1. get the initial context
			InitialContext ctx = new InitialContext();

			// 2. lookup the connection factory
			connectionFactory = (ConnectionFactory) ctx.lookup(JNDI_QUEUE_CONNECTION_FACTORY_NAME);
			
			// 3. lookup the destination
			destination = (Destination) ctx.lookup(JNDI_QUEUE_NAME);

			// 4. create a connection
			connection = connectionFactory.createConnection();
			connection.start();

			// 5. create a session
			session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

			// 6. create destination
			//destination = session.createQueue("test-queue");

			// 7. create message producer
			producer = session.createProducer(destination);
			
			// 8. create temp queue
			replyDest = session.createTemporaryQueue();

            // 9. set up the consumer to handle the reply
            replyConsumer = session.createConsumer(replyDest);
            replyConsumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    System.out.println("*** GOT REPLY *** ");
                    TextMessage msg = (TextMessage) message;
                    try {
						System.out.println(msg + " " + msg.getJMSCorrelationID() + " "+ msg.getText());
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            });

            // 10. send a message
            TextMessage message = session.createTextMessage("I need a response for this, please");
            message.setJMSReplyTo(replyDest);

            producer.send(message);

            // wait for a response
            TimeUnit.SECONDS.sleep(DELAY);
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
