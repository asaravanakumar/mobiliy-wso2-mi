package com.examples.mq.jms.examples.queue;

import java.util.concurrent.CountDownLatch;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Sample program to demonstrate Asynchronous queue receive.
 * 
 * @author Saravana
 */
public class AsynchQueueReceiver implements MessageListener, ExceptionListener {

	private final CountDownLatch countDownLatch;

	public AsynchQueueReceiver(CountDownLatch latch) {
		// defining a latch to hold the current thread till asynchronous message
		// receive thread complete
		countDownLatch = latch;
	}

	public static void main(String[] args) {

		final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "QueueConnectionFactory";
		final String JNDI_QUEUE_NAME = "demoQueue";
		QueueConnectionFactory queueConnectionFactory = null;
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		Queue queue = null;
		QueueReceiver queueReceiver = null;
		CountDownLatch countDownLatch = new CountDownLatch(1);

		try {
			System.out
					.println("\nWaiting to receive messages... Either waiting for END message or press Ctrl+C to exit");

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

			// 6. create a queue receiver
			queueReceiver = queueSession.createReceiver(queue);

			// 7. set an synchronous message listener
			AsynchQueueReceiver asynchReceiver = new AsynchQueueReceiver(countDownLatch);
			queueReceiver.setMessageListener(asynchReceiver);

			// start the connection
			queueConnection.start();

			// wait till all message receive complete
			countDownLatch.await();
			
			// close receiver and sessoin
			queueReceiver.close();
			queueSession.close();

		} catch (JMSException e) {
			System.out.println("Exception occurred while receiving the message: " + e.toString());
		} catch (Exception e) {
			System.out.println("Error occured: " + e.toString());
			e.printStackTrace();
		} finally {
			if (queueConnection != null) {
				try {
					// close connection handle if work completed or any error
					queueConnection.close();
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
		} catch (JMSException e) {
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