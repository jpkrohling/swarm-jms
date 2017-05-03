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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.logging.Logger;

import com.uber.jaeger.Tracer;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UDPSender;

import io.opentracing.util.GlobalTracer;

/**
 * @author Juraci Paixão Kröhling
 */
@WebListener
public class TracerSetter implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(TracerSetter.class);
    private static final String JAEGER_SERVER_URL = System.getenv("JAEGER_SERVER_URL");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info(String.format("Using Jaeger Tracer at '%s'", JAEGER_SERVER_URL));
        Sender sender = new UDPSender(JAEGER_SERVER_URL, 0, 0);
        Metrics metrics = new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
        Reporter reporter = new RemoteReporter(sender,100,50, metrics);
        Tracer tracer = new Tracer.Builder("swarm-jms", reporter, new ProbabilisticSampler(1.0)).build();
        GlobalTracer.register(tracer);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
