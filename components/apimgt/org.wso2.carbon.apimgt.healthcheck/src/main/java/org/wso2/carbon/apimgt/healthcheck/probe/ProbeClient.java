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
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
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
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
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
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Probes one Traffic Manager address on the calling thread, opening and closing its own connections each call. The
 * throttle receiver probe logs in, publishes one event and logs out using the databridge binary client; the message
 * broker probe sends a message to a health-check-only topic and waits for it to come back.
 */
public class ProbeClient {

    private static final Log log = LogFactory.getLog(ProbeClient.class);

    private static final String TCP = DataEndpointConfiguration.Protocol.TCP.toString();
    private static final String SSL = DataEndpointConfiguration.Protocol.SSL.toString();

    private static final int PUBLISH_REPLY_OK = 0;
    private static final int PUBLISH_REPLY_ERROR = 1;
    private static final int MAX_REPLY_TEXT_BYTES = 64 * 1024;
    /** The databridge agent whose configuration the receiver sockets use. */
    private static final String BINARY_AGENT = "BINARY";

    static final String TOPIC_NAME = "tmHealthCheckBrokerProbe";
    private static final String CONNECTION_FACTORY_NAME = "TMHealthCheckBrokerProbe";
    static final String NO_CREDENTIALS = "No username/password configured in the " + TMHealthChecker.CHECKER_NAME
            + " properties";

    private final String streamId;
    private final long receiveTimeoutMs;
    private final int connectTimeoutMs;

    /**
     * @param streamId         the stream the throttle receiver test event is sent to
     * @param receiveTimeoutMs how long to wait for the message broker test message to come back
     */
    public ProbeClient(String streamId, long receiveTimeoutMs) {

        this(streamId, receiveTimeoutMs, TMHealthChecker.DEFAULT_PROBE_TIMEOUT_MS);
    }

    /**
     * @param connectTimeoutMs how long to wait for a receiver connection to open
     */
    public ProbeClient(String streamId, long receiveTimeoutMs, int connectTimeoutMs) {

        this.streamId = streamId;
        this.receiveTimeoutMs = receiveTimeoutMs;
        this.connectTimeoutMs = connectTimeoutMs;
    }

    /**
     * Runs all probe steps against one endpoint and never throws. Unexpected errors keep only the root cause, so a
     * connection string with a password cannot leak.
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
            return ProbeResult.failure(endpoint, ProbeStage.UNEXPECTED_ERROR, String.valueOf(rootCause(e)),
                    elapsedMs(startNanos));
        }
    }

    /**
     * Logs in, publishes, then logs out, recording in the result whether login itself succeeded.
     */
    private ProbeResult probeThrottleReceiver(ProbeEndpoint endpoint, long startNanos) {

        String sessionId;
        try {
            sessionId = login(endpoint);
        } catch (ProbeStepException e) {
            if (e.isCredentialsRejected()) {
                return ProbeResult.credentialsRejected(endpoint, e.getMessage(), elapsedMs(startNanos));
            }
            return ProbeResult.failure(endpoint, e.getStage(), e.getMessage(), elapsedMs(startNanos), false);
        }
        try {
            publishTestEvent(endpoint, sessionId);
            return ProbeResult.success(endpoint, elapsedMs(startNanos));
        } catch (ProbeStepException e) {
            return ProbeResult.failure(endpoint, e.getStage(), e.getMessage(), elapsedMs(startNanos), true);
        } finally {
            logoutQuietly(endpoint, sessionId);
        }
    }

    /**
     * Logs in over SSL on the auth port and returns the session ID. TLS handshake failures surface on the first write.
     */
    private String login(ProbeEndpoint endpoint) throws ProbeStepException {

        if (endpoint.getUsername() == null || endpoint.getPassword() == null) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, NO_CREDENTIALS);
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
            throw new ProbeStepException(ProbeStage.TLS_FAILED, endpoint.getAuthUrl() + " - " + rootCause(e));
        } catch (AuthenticationException e) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, endpoint.getAuthUrl() + " - " + e.getMessage(),
                    true);
        } catch (Exception e) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, endpoint.getAuthUrl() + " - " + rootCause(e));
        } finally {
            closeSocket(SSL, socket);
        }
    }

    /**
     * Sends one event to the health check stream and checks the receiver's reply.
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
     * Reads the publish reply ({@code 0} for OK, {@code 1} plus error class and message). Not done with
     * {@link BinaryEventSender#processResponse(Socket)} because it cannot tell OK from a closed connection.
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
     * Logs out so no session is left behind; a failure is only logged at debug level.
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
     * Opens a socket and maps a failure to a {@link ProbeStage}; an agent configuration error is not a closed port.
     */
    private Socket openSocket(String protocol, String host, int port) throws ProbeStepException {

        try {
            return createSocket(protocol, host, port);
        } catch (DataEndpointAgentConfigurationException e) {
            throw new ProbeStepException(ProbeStage.UNEXPECTED_ERROR,
                    "databridge agent configuration - " + rootCause(e));
        } catch (Exception e) {
            Throwable cause = rootCause(e);
            ProbeStage stage = isTimeout(cause) ? ProbeStage.TIMEOUT : ProbeStage.PORT_CLOSED;
            throw new ProbeStepException(stage, protocol + "://" + host + ":" + port + " - " + cause);
        }
    }

    /**
     * Opens a socket like the databridge binary client factories do, but with a connect timeout, which they lack.
     * Protected so a test can replace it.
     */
    protected Socket createSocket(String protocol, String host, int port) throws Exception {

        Socket plain = new Socket();
        try {
            plain.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            AgentConfiguration agentConfiguration =
                    AgentHolder.getInstance().getDataEndpointAgent(BINARY_AGENT).getAgentConfiguration();
            Socket socket = plain;
            if (SSL.equals(protocol)) {
                Socket wrapped = ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(plain, host, port, true);
                if (!(wrapped instanceof SSLSocket)) {
                    wrapped.close();
                    throw new SSLException("The default SSL socket factory did not return an SSL socket");
                }
                SSLSocket ssl = (SSLSocket) wrapped;
                String protocols = agentConfiguration.getSslEnabledProtocols();
                if (protocols != null && !protocols.isEmpty()) {
                    ssl.setEnabledProtocols(protocols.split(","));
                }
                String ciphers = agentConfiguration.getCiphers();
                ssl.setEnabledCipherSuites(ciphers != null && !ciphers.isEmpty()
                        ? ciphers.split(",") : ssl.getSupportedCipherSuites());
                socket = ssl;
            }
            socket.setSoTimeout(agentConfiguration.getSocketTimeoutMS());
            return socket;
        } catch (IOException | DataEndpointAgentConfigurationException | RuntimeException e) {
            plain.close();
            throw e;
        }
    }

    /**
     * Closes a socket through the matching databridge factory. Protected so a test can replace it.
     */
    protected void closeSocket(String protocol, Socket socket) {

        if (SSL.equals(protocol)) {
            new BinarySecureClientPoolFactory(null, null).terminateClient(socket);
        } else {
            new BinaryClientPoolFactory().terminateClient(socket);
        }
    }

    /**
     * Sends a message to the health check topic and waits for it to come back. Cleanup is guarded so it cannot mask
     * the real failure.
     */
    private void probeMessageBroker(ProbeEndpoint endpoint) throws ProbeStepException {

        if (endpoint.getUsername() == null || endpoint.getPassword() == null) {
            throw new ProbeStepException(ProbeStage.LOGIN_FAILED, NO_CREDENTIALS);
        }
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
            closeQuietly(connection);
            if (connectionFactory != null) {
                stopQuietly(connectionFactory);
            }
        }
    }

    /**
     * Builds the JMS parameters with caching disabled and a JNDI entry for the health check topic.
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
     * Sets up the JMS client without connecting. Only the root cause is kept, since the outer message lists the
     * password.
     */
    private JMSConnectionFactory setUpJmsClient(Hashtable<String, String> parameters) throws ProbeStepException {

        try {
            return createConnectionFactory(parameters);
        } catch (RuntimeException e) {
            throw new ProbeStepException(ProbeStage.CONFIG_UNRESOLVED, String.valueOf(rootCause(e)));
        }
    }

    /**
     * Connects and logs in. Only the root cause is kept, since outer JMS exceptions can embed the connection string.
     */
    private Connection connect(JMSConnectionFactory connectionFactory, Hashtable<String, String> parameters,
                               ProbeEndpoint endpoint) throws ProbeStepException {

        try {
            return createConnection(connectionFactory, parameters);
        } catch (Exception e) {
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
     * Subscribes to the health check topic; must run before sending, as a topic only delivers to existing listeners.
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

    private void startDelivery(Connection connection) throws ProbeStepException {

        try {
            connection.start();
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.SUBSCRIBE_FAILED, "start connection - " + rootCause(e));
        }
    }

    /**
     * Sends one non-persistent message carrying the probe ID. No TTL is set, so clock skew with the broker cannot
     * expire it.
     */
    private void send(JMSConnectionFactory connectionFactory, Session session, Destination topic, String probeId)
            throws ProbeStepException {

        try {
            MessageProducer sender = JMSUtils.createProducer(session, topic, connectionFactory.isQueue(),
                    connectionFactory.isJmsSpec11());
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            sender.send(session.createTextMessage(probeId));
        } catch (JMSException e) {
            throw new ProbeStepException(ProbeStage.PUBLISH_FAILED, "send to " + TOPIC_NAME + " - " + rootCause(e));
        }
    }

    /**
     * Waits for this call's own message, skipping messages from overlapping probes.
     */
    private void awaitOwnMessage(MessageConsumer listener, String probeId) throws ProbeStepException {

        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(receiveTimeoutMs);
        try {
            while (true) {
                long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
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
     * Protected so a test can replace it.
     */
    protected JMSConnectionFactory createConnectionFactory(Hashtable<String, String> parameters) {

        return new JMSConnectionFactory(parameters, CONNECTION_FACTORY_NAME);
    }

    /**
     * Opens a connection without {@link JMSConnectionFactory#getConnection()}, which logs the password on failure.
     * Protected so a test can replace it.
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
     * Maps a broker connect failure to a {@link ProbeStage} from its cause chain, or {@link ProbeStage#CONNECT_FAILED}.
     */
    static ProbeStage classifyConnectFailure(Throwable error) {

        Throwable current = error;
        for (int depth = 0; current != null && depth < 20; depth++) {
            if (isTimeout(current)) {
                return ProbeStage.TIMEOUT;
            }
            if (current instanceof ConnectException || current instanceof NoRouteToHostException
                    || current instanceof UnknownHostException) {
                return ProbeStage.PORT_CLOSED;
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
     * Some platforms report an OS connect timeout as a {@link ConnectException} saying "timed out", so that counts too.
     */
    private static boolean isTimeout(Throwable cause) {

        if (cause instanceof SocketTimeoutException) {
            return true;
        }
        return cause instanceof ConnectException && cause.getMessage() != null
                && cause.getMessage().toLowerCase(Locale.ROOT).contains("timed out");
    }

    /**
     * Follows a JMS linked exception before the normal cause.
     */
    private static Throwable nextCause(Throwable error) {

        if (error instanceof JMSException && ((JMSException) error).getLinkedException() != null
                && ((JMSException) error).getLinkedException() != error) {
            return ((JMSException) error).getLinkedException();
        }
        return error.getCause() == error ? null : error.getCause();
    }

    static Throwable rootCause(Throwable error) {

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
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Health check probe could not close its message broker connection: " + e);
            }
        }
    }

    private static void stopQuietly(JMSConnectionFactory connectionFactory) {

        try {
            connectionFactory.stop();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Health check probe could not stop its JMS connection factory: " + e);
            }
        }
    }

    private static long elapsedMs(long startNanos) {

        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    /**
     * Thrown when one probe step fails, carrying the failed {@link ProbeStage}.
     */
    static final class ProbeStepException extends Exception {

        private static final long serialVersionUID = 1L;

        private final ProbeStage stage;
        private final boolean credentialsRejected;

        ProbeStepException(ProbeStage stage, String detail) {

            this(stage, detail, false);
        }

        ProbeStepException(ProbeStage stage, String detail, boolean credentialsRejected) {

            super(detail);
            this.stage = stage;
            this.credentialsRejected = credentialsRejected;
        }

        ProbeStage getStage() {

            return stage;
        }

        boolean isCredentialsRejected() {

            return credentialsRejected;
        }
    }
}
