/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.healthcheck.probe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.common.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.apimgt.common.jms.utils.JMSUtils;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.endpoint.binary.BinaryClientPoolFactory;
import org.wso2.carbon.databridge.agent.endpoint.binary.BinaryEventSender;
import org.wso2.carbon.databridge.agent.endpoint.binary.BinarySecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.net.ssl.SSLException;

/**
 * Probes one Traffic Manager address, step by step, on the calling thread. {@link #probe(ProbeEndpoint)} picks the
 * right steps for the endpoint's {@link CheckType}. Every call opens its own short-lived connections and closes them
 * again, so nothing stays running between calls.
 *
 * <p><b>Throttle receiver (9611)</b> - sends one test event and waits for the receiver's own answer. It reuses the
 * databridge agent's binary client code, the same code the real throttle publisher uses under the hood:</p>
 * <ul>
 *   <li>{@link BinaryClientPoolFactory} / {@link BinarySecureClientPoolFactory} open the plain (9611) and SSL
 *       (9711) sockets, with the socket timeout, TLS protocols and ciphers from the Traffic Manager's own databridge
 *       agent configuration.</li>
 *   <li>{@link BinaryEventSender} writes the login, publish and logout messages and reads the receiver's reply.</li>
 * </ul>
 * <p>It does <b>not</b> use {@code DataPublisher}: that puts events into an in-memory ring buffer and sends them
 * later on other threads, so it can never tell us whether the receiver really accepted the event. Steps: (1) login on
 * the auth port and get a session ID, (2) publish one event on the receiver port and read the reply, (3) logout.</p>
 *
 * <p><b>Message broker (5672 / 8672)</b> - sends a test message to itself. It reuses the JMS client in
 * {@code org.wso2.carbon.apimgt.common.jms}: {@link JMSConnectionFactory} sets up the JMS (JNDI) client from the
 * configured properties and looks up the topic, and {@link JMSUtils} creates the connection, session, consumer and
 * producer. Steps: (1) connect and log in, (2) subscribe to the health check topic (the listener, only for this one
 * call), (3) send one test message carrying a random ID (the sender), (4) wait up to {@code receiveTimeoutMs} for
 * that same message to come back, (5) close everything. If the message comes back, the broker accepted <b>and</b>
 * delivered it. The topic is used only by this health check, so no real Gateway or Traffic Manager listener ever
 * sees the test message.</p>
 */
public class ProbeClient {

    private static final Log log = LogFactory.getLog(ProbeClient.class);

    private static final String TCP = DataEndpointConfiguration.Protocol.TCP.toString();
    private static final String SSL = DataEndpointConfiguration.Protocol.SSL.toString();

    /**
     * First byte of the binary receiver's reply to a publish message.
     */
    private static final int PUBLISH_REPLY_OK = 0;
    private static final int PUBLISH_REPLY_ERROR = 1;
    private static final int MAX_REPLY_TEXT_BYTES = 64 * 1024;
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";

    static final String TOPIC_NAME = "tmHealthCheckBrokerProbe";
    private static final String CONNECTION_FACTORY_NAME = "TMHealthCheckBrokerProbe";

    private final String streamId;
    private final long receiveTimeoutMs;

    /**
     * @param streamId         the stream the throttle receiver test event is sent to
     * @param receiveTimeoutMs how long to wait for the message broker test message to come back
     */
    public ProbeClient(String streamId, long receiveTimeoutMs) {

        this.streamId = streamId;
        this.receiveTimeoutMs = receiveTimeoutMs;
    }

    /**
     * Runs all probe steps against one endpoint. Never throws: every failure becomes a failed {@link ProbeResult}.
     */
    public ProbeResult probe(ProbeEndpoint endpoint) {

        long startNanos = System.nanoTime();
        try {
            if (endpoint.getCheckType() == CheckType.THROTTLE_RECEIVER) {
                return probeThrottleReceiver(endpoint, startNanos);
            }
            probeMessageBroker(endpoint);
            return ProbeResult.success(endpoint, elapsedMs(startNanos));
        } catch (ProbeStepException e) {
            return ProbeResult.failure(endpoint, e.getStage(), e.getMessage(), elapsedMs(startNanos));
        } catch (RuntimeException e) {
            return ProbeResult.failure(endpoint, ProbeStage.UNEXPECTED_ERROR, e.toString(), elapsedMs(startNanos));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Throttle receiver (9611)
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Logs in on 9711, then publishes on 9611. Handled separately from the generic {@link #probe} dispatch (rather
     * than just throwing a {@link ProbeStepException} like every other probe does) so the result can record
     * whether login itself succeeded - see {@link ProbeResult#isAuthSucceeded()} - something only known here, at
     * the exact point where login finishes and publish begins.
     */
    private ProbeResult probeThrottleReceiver(ProbeEndpoint endpoint, long startNanos) {

        String sessionId;
        try {
            sessionId = login(endpoint);
        } catch (ProbeStepException e) {
            // Never got a session, so login itself is what failed.
            return ProbeResult.failure(endpoint, e.getStage(), e.getMessage(), elapsedMs(startNanos), false);
        }
        try {
            publishTestEvent(endpoint, sessionId);
            return ProbeResult.success(endpoint, elapsedMs(startNanos));
        } catch (ProbeStepException e) {
            // Login worked (we have a session) - this failure is about publish only.
            return ProbeResult.failure(endpoint, e.getStage(), e.getMessage(), elapsedMs(startNanos), true);
        } finally {
            logoutQuietly(endpoint, sessionId);
        }
    }

    /**
     * Step 1: open an SSL socket to the auth port and log in. The receiver answers a good login with a session ID,
     * and a bad one with an error that {@link BinaryEventSender#processResponse(Socket)} throws back to us.
     */
    private String login(ProbeEndpoint endpoint) throws ProbeStepException {

        if (endpoint.getUsername() == null || endpoint.getPassword() == null) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, "No throttling username/password configured");
        }
        Socket socket = openSocket(SSL, endpoint.getAuthHost(), endpoint.getAuthPort());
        try {
            BinaryEventSender.sendBinaryLoginMessage(socket, endpoint.getUsername(), endpoint.getPassword());
            String sessionId = BinaryEventSender.processResponse(socket);
            if (sessionId == null) {
                throw new ProbeStepException(ProbeStage.LOGIN_FAILED, "Login reply did not contain a session ID");
            }
            return sessionId;
        } catch (ProbeStepException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new ProbeStepException(ProbeStage.TIMEOUT,
                    "login on " + endpoint.getAuthUrl() + " - " + rootCause(e));
        } catch (SSLException e) {
            // The TLS handshake happens on the first write, so a certificate problem shows up here.
            throw new ProbeStepException(ProbeStage.TLS_FAILED, endpoint.getAuthUrl() + " - " + rootCause(e));
        } catch (AuthenticationException e) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, endpoint.getAuthUrl() + " - " + e.getMessage());
        } catch (Exception e) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, endpoint.getAuthUrl() + " - " + rootCause(e));
        } finally {
            closeSocket(SSL, socket);
        }
    }

    /**
     * Step 2: send exactly one event to the health check stream on the receiver port and wait for the receiver's
     * reply. The receiver replies "OK", or an error such as "unknown stream" or "session expired" - see
     * {@link #readPublishReply}.
     */
    private void publishTestEvent(ProbeEndpoint endpoint, String sessionId) throws ProbeStepException {

        long now = System.currentTimeMillis();
        Event event = new Event(streamId, now, null, null, new Object[]{now});
        Socket socket = openSocket(TCP, endpoint.getHost(), endpoint.getPort());
        try {
            BinaryEventSender.sendBinaryPublishMessage(socket, Collections.singletonList(event), sessionId);
            readPublishReply(socket.getInputStream(), endpoint.getUrl());
        } catch (ProbeStepException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new ProbeStepException(ProbeStage.TIMEOUT, "publish on " + endpoint.getUrl() + " - " + rootCause(e));
        } catch (Exception e) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, endpoint.getUrl() + " - " + rootCause(e));
        } finally {
            closeSocket(TCP, socket);
        }
    }

    /**
     * Reads the receiver's reply to the publish message, in its wire format: {@code 0} for OK, or {@code 1}
     * followed by the error's class name and message (each preceded by its length).
     *
     * <p>Read here rather than with {@link BinaryEventSender#processResponse(Socket)}, which returns the same
     * {@code null} for an OK reply and for a connection the receiver closed without answering at all - so a receiver
     * that takes the connection but never accepts the event would look healthy. A closed connection, or any other
     * reply, is reported as {@link ProbeStage#PUBLISH_FAILED}.</p>
     */
    void readPublishReply(InputStream in, String target) throws IOException, ProbeStepException {

        int code = in.read();
        if (code == PUBLISH_REPLY_OK) {
            return;
        }
        if (code == -1) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED,
                    target + " - the receiver closed the connection without accepting the test event");
        }
        if (code != PUBLISH_REPLY_ERROR) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, target + " - unexpected reply code " + code);
        }
        DataInputStream data = new DataInputStream(in);
        int classNameLength = data.readInt();
        int messageLength = data.readInt();
        String errorClass = readReplyText(data, classNameLength, target);
        String message = readReplyText(data, messageLength, target);
        if (UndefinedEventTypeException.class.getName().equals(errorClass)) {
            throw new ProbeStepException(ProbeStage.STREAM_UNDEFINED, streamId + " - " + message);
        }
        if (SessionTimeoutException.class.getName().equals(errorClass)) {
            throw new ProbeStepException(ProbeStage.SESSION_EXPIRED, message);
        }
        throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, target + " - " + errorClass + ": " + message);
    }

    private static String readReplyText(DataInputStream data, int length, String target)
            throws IOException, ProbeStepException {

        if (length < 0 || length > MAX_REPLY_TEXT_BYTES) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, target + " - malformed error reply");
        }
        byte[] text = new byte[length];
        data.readFully(text);
        return new String(text, StandardCharsets.UTF_8);
    }

    /**
     * Step 3: log out so no session is left behind. A failure here is only logged - the result is already known.
     */
    private void logoutQuietly(ProbeEndpoint endpoint, String sessionId) {

        Socket socket = null;
        try {
            socket = openSocket(SSL, endpoint.getAuthHost(), endpoint.getAuthPort());
            BinaryEventSender.sendBinaryLogoutMessage(socket, sessionId);
            BinaryEventSender.processResponse(socket);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Health check probe could not log out of " + endpoint.getAuthUrl() + ": " + e);
            }
        } finally {
            if (socket != null) {
                closeSocket(SSL, socket);
            }
        }
    }

    /**
     * Opens a socket with the existing databridge factory and turns a failure into the right {@link ProbeStage}.
     */
    private Socket openSocket(String protocol, String host, int port) throws ProbeStepException {

        try {
            return createSocket(protocol, host, port);
        } catch (DataEndpointAgentConfigurationException e) {
            // The databridge agent's own configuration (data-agent-config.xml) could not be loaded. Nothing was
            // dialled, so this must not be reported as a closed port.
            throw new ProbeStepException(ProbeStage.UNEXPECTED_ERROR,
                    "databridge agent configuration - " + rootCause(e));
        } catch (Exception e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            ProbeStage stage = cause instanceof SocketTimeoutException ? ProbeStage.TIMEOUT : ProbeStage.PORT_CLOSED;
            throw new ProbeStepException(stage, protocol + "://" + host + ":" + port + " - " + cause);
        }
    }

    /**
     * Creates a connected socket using the databridge agent's own client factories. Protected so a test can
     * replace it with a socket to a fake receiver.
     *
     * <p>Checked against {@code org.wso2.carbon.databridge.agent} 5.3.11.5: {@code createClient} opens a brand-new
     * socket every time and {@code terminateClient} only closes it. The pool the real publisher uses lives in
     * {@code DataEndpointAgent} and is never touched here. The only shared state is read-only: the socket timeout,
     * TLS protocols and ciphers from {@code AgentHolder}'s agent configuration (the first use on a node creates
     * that {@code AgentHolder} singleton, as {@code DataPublisher} would).</p>
     */
    protected Socket createSocket(String protocol, String host, int port) throws Exception {

        if (SSL.equals(protocol)) {
            return (Socket) new BinarySecureClientPoolFactory(System.getProperty(TRUST_STORE_PROPERTY),
                    System.getProperty(TRUST_STORE_PASSWORD_PROPERTY)).createClient(SSL, host, port);
        }
        return (Socket) new BinaryClientPoolFactory().createClient(TCP, host, port);
    }

    /**
     * Closes a socket through the same factory that created it. Protected so a test can replace it.
     */
    protected void closeSocket(String protocol, Socket socket) {

        if (SSL.equals(protocol)) {
            new BinarySecureClientPoolFactory(null, null).terminateClient(socket);
        } else {
            new BinaryClientPoolFactory().terminateClient(socket);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Message broker (5672 / 8672)
    // ---------------------------------------------------------------------------------------------------------------

    private void probeMessageBroker(ProbeEndpoint endpoint) throws ProbeStepException {

        JMSConnectionFactory connectionFactory = null;
        Connection connection = null;
        try {
            Hashtable<String, String> parameters = toJmsParameters(endpoint.getJmsParameters());
            connectionFactory = setUpJmsClient(parameters);
            connection = connect(connectionFactory, parameters, endpoint);

            Session session = createSession(connectionFactory, connection);
            Destination topic = lookUpTopic(connectionFactory);
            MessageConsumer listener = subscribe(connectionFactory, session, topic);
            startDelivery(connection);

            String probeId = UUID.randomUUID().toString();
            send(connectionFactory, session, topic, probeId);
            awaitOwnMessage(listener, probeId);
        } finally {
            // Closing the connection also closes its session, listener and sender.
            closeQuietly(connection);
            if (connectionFactory != null) {
                connectionFactory.stop();
            }
        }
    }

    /**
     * Turns the configured JMS properties into what {@link JMSConnectionFactory} expects, plus two settings for this
     * probe: no connection caching (we close everything ourselves), and a JNDI entry for the health check topic so it
     * can be looked up.
     */
    static Hashtable<String, String> toJmsParameters(Properties jmsParameters) {

        Hashtable<String, String> parameters = new Hashtable<>();
        for (String name : jmsParameters.stringPropertyNames()) {
            parameters.put(name, jmsParameters.getProperty(name));
        }
        parameters.put(JMSConstants.PARAM_CACHE_LEVEL, "none");
        parameters.put("topic." + TOPIC_NAME, TOPIC_NAME);
        return parameters;
    }

    /**
     * Sets up the JMS client. This only reads the local configuration; it does not connect yet.
     */
    private JMSConnectionFactory setUpJmsClient(Hashtable<String, String> parameters) throws ProbeStepException {

        try {
            return createConnectionFactory(parameters);
        } catch (RuntimeException e) {
            // Only the root cause is kept: the outer message lists every property, including the password.
            throw new ProbeStepException(ProbeStage.CONFIG_UNRESOLVED, String.valueOf(rootCause(e)));
        }
    }

    /**
     * Step 1: connect and log in. The username and password are the ones in the configured connection URL (or
     * {@code transport.jms.UserName} / {@code transport.jms.Password} if those are set).
     */
    private Connection connect(JMSConnectionFactory connectionFactory, Hashtable<String, String> parameters,
                               ProbeEndpoint endpoint) throws ProbeStepException {

        try {
            return createConnection(connectionFactory, parameters);
        } catch (Exception e) {
            // Only the root cause is kept: JMS/AMQP connection failures can otherwise embed the full
            // connection string, including the password, in an outer exception's message.
            throw new ProbeStepException(classifyConnectFailure(e), endpoint.getUrl() + " - " + rootCause(e));
        }
    }

    private Session createSession(JMSConnectionFactory connectionFactory, Connection connection)
            throws ProbeStepException {

        try {
            return JMSUtils.createSession(connection, false, Session.AUTO_ACKNOWLEDGE,
                    connectionFactory.isJmsSpec11(), connectionFactory.isQueue());
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.SUBSCRIBE_FAILED, "create session - " + rootCause(e));
        }
    }

    private Destination lookUpTopic(JMSConnectionFactory connectionFactory) throws ProbeStepException {

        try {
            return connectionFactory.getDestination(TOPIC_NAME);
        } catch (RuntimeException e) {
            throw new ProbeStepException(ProbeStage.SUBSCRIBE_FAILED,
                    "look up topic " + TOPIC_NAME + " - " + rootCause(e));
        }
    }

    /**
     * Step 2: start listening on the health check topic. This must happen before sending, because a topic only
     * delivers a message to listeners that already exist.
     */
    private MessageConsumer subscribe(JMSConnectionFactory connectionFactory, Session session, Destination topic)
            throws ProbeStepException {

        try {
            return JMSUtils.createConsumer(session, topic, connectionFactory.isQueue(), null, null, false, false,
                    connectionFactory.isJmsSpec11());
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.SUBSCRIBE_FAILED,
                    "subscribe to " + TOPIC_NAME + " - " + rootCause(e));
        }
    }

    /**
     * A JMS connection delivers nothing to its listeners until it is started.
     */
    private void startDelivery(Connection connection) throws ProbeStepException {

        try {
            connection.start();
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.SUBSCRIBE_FAILED, "start connection - " + rootCause(e));
        }
    }

    /**
     * Step 3: send one test message whose text is this call's random ID. It is not persisted, and expires after the
     * receive timeout, so the broker never keeps it.
     */
    private void send(JMSConnectionFactory connectionFactory, Session session, Destination topic, String probeId)
            throws ProbeStepException {

        try {
            MessageProducer sender = JMSUtils.createProducer(session, topic, connectionFactory.isQueue(),
                    connectionFactory.isJmsSpec11());
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            sender.setTimeToLive(receiveTimeoutMs);
            sender.send(session.createTextMessage(probeId));
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, "send to " + TOPIC_NAME + " - " + rootCause(e));
        }
    }

    /**
     * Step 4: wait for our own message. Messages from other, overlapping health check calls are skipped by
     * comparing the random ID.
     */
    private void awaitOwnMessage(MessageConsumer listener, String probeId) throws ProbeStepException {

        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(receiveTimeoutMs);
        try {
            while (true) {
                long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
                // receive(0) would wait forever, so stop before that.
                Message message = remainingMs > 0 ? listener.receive(remainingMs) : null;
                if (message == null) {
                    throw new ProbeStepException(ProbeStage.NOT_RECEIVED,
                            "Test message did not come back within " + receiveTimeoutMs + " ms");
                }
                if (message instanceof TextMessage && probeId.equals(((TextMessage) message).getText())) {
                    return;
                }
            }
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.NOT_RECEIVED, "receive from " + TOPIC_NAME + " - " + rootCause(e));
        }
    }

    /**
     * Creates the JMS client from the parameters. Protected so a test can replace it.
     */
    protected JMSConnectionFactory createConnectionFactory(Hashtable<String, String> parameters) {

        return new JMSConnectionFactory(parameters, CONNECTION_FACTORY_NAME);
    }

    /**
     * Opens a new connection with {@link JMSUtils#createConnection}. This is used instead of
     * {@link JMSConnectionFactory#getConnection()} because that method logs all connection properties (including
     * the password) when the connection fails. Protected so a test can replace it.
     */
    protected Connection createConnection(JMSConnectionFactory connectionFactory, Hashtable<String, String> parameters)
            throws Exception {

        ConnectionFactory jmsConnectionFactory = JMSUtils.lookup(connectionFactory.getContext(),
                ConnectionFactory.class, parameters.get(JMSConstants.PARAM_CONFAC_JNDI_NAME));
        return JMSUtils.createConnection(jmsConnectionFactory, parameters.get(JMSConstants.PARAM_JMS_USERNAME),
                parameters.get(JMSConstants.PARAM_JMS_PASSWORD), connectionFactory.isJmsSpec11(),
                connectionFactory.isQueue(), false, null);
    }

    /**
     * Looks through the chain of causes for the real reason a broker connection failed.
     */
    static ProbeStage classifyConnectFailure(Throwable error) {

        Throwable current = error;
        for (int depth = 0; current != null && depth < 20; depth++) {
            if (current instanceof ConnectException || current instanceof NoRouteToHostException
                    || current instanceof UnknownHostException) {
                return ProbeStage.PORT_CLOSED;
            }
            if (current instanceof SocketTimeoutException) {
                return ProbeStage.TIMEOUT;
            }
            if (current instanceof SSLException) {
                return ProbeStage.TLS_FAILED;
            }
            if (current.getClass().getSimpleName().contains("Authentication")) {
                return ProbeStage.LOGIN_FAILED;
            }
            current = nextCause(current);
        }
        return ProbeStage.CONNECT_FAILED;
    }

    /**
     * JMS errors often keep the real cause as a "linked exception" instead of the normal cause.
     */
    private static Throwable nextCause(Throwable error) {

        if (error instanceof JMSException && ((JMSException) error).getLinkedException() != null
                && ((JMSException) error).getLinkedException() != error) {
            return ((JMSException) error).getLinkedException();
        }
        return error.getCause() == error ? null : error.getCause();
    }

    private static Throwable rootCause(Throwable error) {

        Throwable current = error;
        for (int depth = 0; depth < 20; depth++) {
            Throwable next = nextCause(current);
            if (next == null) {
                break;
            }
            current = next;
        }
        return current;
    }

    private static void closeQuietly(Connection connection) {

        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (JMSException e) {
            if (log.isDebugEnabled()) {
                log.debug("Health check probe could not close its message broker connection: " + e);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Shared
    // ---------------------------------------------------------------------------------------------------------------

    private static long elapsedMs(long startNanos) {

        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    /**
     * Thrown inside this client when one step fails. It carries which {@link ProbeStage} failed up to
     * {@link #probe}, which turns it into a failed {@link ProbeResult}.
     */
    static final class ProbeStepException extends Exception {

        private static final long serialVersionUID = 1L;

        private final ProbeStage stage;

        ProbeStepException(ProbeStage stage, String detail) {

            super(detail);
            this.stage = stage;
        }

        ProbeStage getStage() {

            return stage;
        }
    }
}
