/*
 * Copyright 2017 Juraci Paixão Kröhling
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.example.swarm.jms.rest;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = HelloMessageListener.HELLO_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
})
@JMSDestinationDefinition(name = HelloMessageListener.HELLO_QUEUE, interfaceName = "javax.jms.Queue", destinationName = "helloQueue", description = "Hello Queue")
public class HelloMessageListener implements MessageListener {
    private static final Logger logger = Logger.getLogger(HelloWorldEndpoint.class);
    static final String HELLO_QUEUE = "java:/jms/queue/helloQueue";

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage request = (TextMessage) message;
            String payload = request.getText();
            logger.info(String.format("Got the following message: %s", payload));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
