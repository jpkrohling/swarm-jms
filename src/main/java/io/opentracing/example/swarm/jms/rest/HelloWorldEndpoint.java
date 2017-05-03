package io.opentracing.example.swarm.jms.rest;


import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.opentracing.SpanContext;
import io.opentracing.contrib.jms.TracingMessageProducer;
import io.opentracing.contrib.jms.common.JmsTextMapInjectAdapter;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import io.opentracing.util.GlobalTracer;

@Path("/hello")
@Stateless
public class HelloWorldEndpoint {
	private static final Logger logger = Logger.getLogger(HelloWorldEndpoint.class);

	@Resource(mappedName = HelloMessageListener.HELLO_QUEUE)
	Queue helloQueue;

	@Resource(mappedName = "java:/JmsXA")
	ConnectionFactory connectionFactory;

	@GET
	@Produces("text/plain")
	public Response doGet(@Context HttpServletRequest request) throws JMSException {
		try (Connection connection = connectionFactory.createConnection()) {
			String message = "text";
			logger.info(String.format("Creating JMS message with content: %s", message));
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer publisher = new TracingMessageProducer(session.createProducer(helloQueue));
			connection.start();

			TextMessage textMessage = session.createTextMessage(message);

			SpanContext requestContext = (SpanContext) request.getAttribute(TracingFilter.SERVER_SPAN_CONTEXT);
			GlobalTracer.get().inject(requestContext, TEXT_MAP, new JmsTextMapInjectAdapter(textMessage));
			publisher.send(textMessage);

			logger.info("JMS message submitted");
			return Response.ok("Hello from WildFly Swarm!").build();
		}
	}
}