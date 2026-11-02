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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.wso2.andes.transport.TransportException;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.common.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.net.ssl.SSLHandshakeException;

/**
 * Tests {@link ProbeClient} without a running server. Receiver probes use a fake binary receiver on localhost and
 * broker probes use mocked JMS objects.
 */
public class ProbeClientTest {


    private static final byte LOGIN = 0;
    private static final byte LOGOUT = 1;
    private static final byte PUBLISH = 2;
    private static final String STREAM_ID = "org.wso2.carbon.apimgt.healthcheck.marker:1.0.0";

    private static final ProbeEndpoint BROKER = ProbeEndpoint.messageBroker("health_checker", "tm-node-1", 5672,
            false, "admin", "admin", new Properties());

    private FakeReceiver receiver;

    private final JMSConnectionFactory connectionFactory = Mockito.mock(JMSConnectionFactory.class);
    private final Connection connection = Mockito.mock(Connection.class);
    private final Session session = Mockito.mock(Session.class);
    private final MessageConsumer listener = Mockito.mock(MessageConsumer.class);
    private final MessageProducer sender = Mockito.mock(MessageProducer.class);
    private final AtomicReference<String> sentText = new AtomicReference<>();
    private Exception connectError;

    @After
    public void stopReceiver() throws IOException {

        if (receiver != null) {
            receiver.close();
        }
    }

    @Before
    public void setUpFakeBroker() throws JMSException {

        Topic topic = Mockito.mock(Topic.class);
        Mockito.when(connectionFactory.isJmsSpec11()).thenReturn(true);
        Mockito.when(connectionFactory.isQueue()).thenReturn(false);
        Mockito.when(connectionFactory.getDestination(ProbeClient.TOPIC_NAME)).thenReturn(topic);
        Mockito.when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        Mockito.when(session.createConsumer(topic, null, false)).thenReturn(listener);
        Mockito.when(session.createProducer(topic)).thenReturn(sender);
        Mockito.when(session.createTextMessage(ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            sentText.set(invocation.getArgument(0));
            return textMessage(invocation.getArgument(0));
        });
    }

    @Test
    public void reportsSuccessWhenReceiverAcceptsTheEvent() throws IOException {

        receiver = new FakeReceiver(null, null);

        ProbeResult result = newClient(50).probe(receiver.endpoint());

        Assert.assertTrue(result.getDetail(), result.isSuccess());
        Assert.assertTrue("9711's login must have succeeded for 9611's publish to even be attempted",
                result.isAuthSucceeded());
        Assert.assertEquals("login, publish and logout should each be sent once",
                "[0, 2, 1]", receiver.receivedMessageTypes.toString());
    }

    @Test
    public void reportsLoginFailedWhenReceiverRejectsCredentials() throws IOException {

        receiver = new FakeReceiver(AuthenticationException.class, null);

        ProbeResult result = newClient(50).probe(receiver.endpoint("wrong"));

        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals(ProbeStage.LOGIN_FAILED, result.getFailedStage());
        Assert.assertFalse("the 9711 login step itself is what failed here", result.isAuthSucceeded());
        Assert.assertTrue("the receiver rejected the credentials themselves", result.isCredentialsRejected());
    }

    @Test
    public void reportsStreamUndefinedWhenReceiverDoesNotKnowTheStream() throws IOException {

        receiver = new FakeReceiver(null, UndefinedEventTypeException.class);

        ProbeResult result = newClient(50).probe(receiver.endpoint());

        Assert.assertEquals(ProbeStage.STREAM_UNDEFINED, result.getFailedStage());
        Assert.assertTrue("9711's login succeeded - only the 9611 publish afterwards failed",
                result.isAuthSucceeded());
        Assert.assertTrue("should still log out after a failed publish", receiver.receivedMessageTypes.contains(
                (int) LOGOUT));
    }

    /**
     * The databridge client reads a closed connection the same as "OK", so this must not count as success.
     */
    @Test
    public void reportsPublishFailedWhenReceiverClosesWithoutAcceptingTheEvent() throws IOException {

        receiver = new FakeReceiver(null, null, true);

        ProbeResult result = newClient(50).probe(receiver.endpoint());

        Assert.assertFalse("a closed connection is not an accepted event", result.isSuccess());
        Assert.assertEquals(ProbeStage.PUBLISH_FAILED, result.getFailedStage());
        Assert.assertTrue(result.getDetail(), result.getDetail().contains("closed the connection"));
        Assert.assertTrue("9711's login succeeded - only the 9611 publish afterwards failed",
                result.isAuthSucceeded());
    }

    @Test
    public void reportsSessionExpiredWhenReceiverRejectsTheSession() throws IOException {

        receiver = new FakeReceiver(null, SessionTimeoutException.class);

        ProbeResult result = newClient(50).probe(receiver.endpoint());

        Assert.assertEquals(ProbeStage.SESSION_EXPIRED, result.getFailedStage());
        Assert.assertTrue(result.isAuthSucceeded());
    }

    @Test
    public void reportsPublishFailedWithTheReceiversReasonForAnyOtherRejection() throws IOException {

        receiver = new FakeReceiver(null, IllegalStateException.class);

        ProbeResult result = newClient(50).probe(receiver.endpoint());

        Assert.assertEquals(ProbeStage.PUBLISH_FAILED, result.getFailedStage());
        Assert.assertTrue(result.getDetail(), result.getDetail().contains("rejected by fake receiver"));
    }

    /**
     * Connecting to the unroutable 10.255.255.1 normally hangs, so the probe must give up after its own timeout.
     */
    @Test
    public void givesUpConnectingAfterItsOwnConnectTimeout() {

        ProbeEndpoint unroutable = ProbeEndpoint.throttleReceiver("health_checker", "10.255.255.1", 9611,
                "10.255.255.1", 9711, "admin", "admin");

        long start = System.nanoTime();
        ProbeResult result = new ProbeClient(STREAM_ID, 50, 300).probe(unroutable);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        Assert.assertTrue("should give up after the 300ms connect timeout, took " + elapsedMs + "ms",
                elapsedMs < 5000);
        boolean noRoute = result.getDetail().contains("NoRouteToHostException")
                || result.getDetail().contains("unreachable");
        if (!noRoute) {
            Assert.assertEquals(result.getDetail(), ProbeStage.TIMEOUT, result.getFailedStage());
            Assert.assertTrue("the connect itself should have waited its timeout, took " + elapsedMs + "ms",
                    elapsedMs >= 250);
        }
    }

    @Test
    public void reportsPortClosedWhenNothingIsListening() throws IOException {

        int unusedPort;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            unusedPort = serverSocket.getLocalPort();
        }
        ProbeEndpoint endpoint = localReceiver(unusedPort, "admin");

        ProbeResult result = newClient(50).probe(endpoint);

        Assert.assertEquals(ProbeStage.PORT_CLOSED, result.getFailedStage());
        Assert.assertFalse(result.isAuthSucceeded());
        Assert.assertFalse("never reached the user store, so not a rejected login", result.isCredentialsRejected());
    }

    /**
     * A dropped connection can time out with a {@link ConnectException} worded as a timeout.
     */
    @Test
    public void reportsTimeoutNotPortClosedWhenTheConnectItselfTimesOut() {

        ProbeClient client = new ProbeClient(STREAM_ID, 50) {

            @Override
            protected Socket createSocket(String protocol, String host, int port) throws Exception {

                throw new ConnectException("Operation timed out (Connection timed out)");
            }
        };

        ProbeResult result = client.probe(localReceiver(1, "admin"));

        Assert.assertEquals(ProbeStage.TIMEOUT, result.getFailedStage());
    }

    @Test
    public void reportsUnexpectedErrorNotPortClosedWhenTheAgentConfigurationCannotBeLoaded() {

        ProbeClient client = new ProbeClient(STREAM_ID, 50) {

            @Override
            protected Socket createSocket(String protocol, String host, int port) throws Exception {

                throw new DataEndpointAgentConfigurationException("data-agent-config.xml not found");
            }
        };

        ProbeResult result = client.probe(localReceiver(1, "admin"));

        Assert.assertEquals(ProbeStage.UNEXPECTED_ERROR, result.getFailedStage());
        Assert.assertTrue(result.getDetail(), result.getDetail().contains("databridge agent configuration"));
    }

    @Test
    public void reportsLoginFailedWithoutConnectingWhenNoCredentials() {

        ProbeEndpoint endpoint = localReceiver(1, null);

        ProbeResult result = newClient(50).probe(endpoint);

        Assert.assertEquals(ProbeStage.LOGIN_FAILED, result.getFailedStage());
        Assert.assertEquals(ProbeClient.NO_CREDENTIALS, result.getDetail());
    }

    @Test
    public void reportsBrokerLoginFailedWithoutConnectingWhenNoCredentials() {

        ProbeEndpoint endpoint = ProbeEndpoint.messageBroker("test", "tm-node-1", 5672, false, "admin", null,
                new Properties());
        ProbeClient client = Mockito.spy(newClient(50));

        ProbeResult result = client.probe(endpoint);

        Assert.assertEquals(ProbeStage.LOGIN_FAILED, result.getFailedStage());
        Assert.assertEquals(ProbeClient.NO_CREDENTIALS, result.getDetail());
        Mockito.verify(client, Mockito.never()).createConnectionFactory(ArgumentMatchers.any());
    }

    private static ProbeEndpoint localReceiver(int port, String password) {

        return ProbeEndpoint.throttleReceiver("test", "localhost", port, "localhost", port, "admin", password);
    }

    /**
     * A message from another health check call arrives first and must be skipped.
     */
    @Test
    public void passesWhenTheTestMessageComesBack() throws JMSException {

        Mockito.when(listener.receive(ArgumentMatchers.anyLong()))
                .thenAnswer(invocation -> textMessage("someone-else"))
                .thenAnswer(invocation -> textMessage(sentText.get()));

        ProbeResult result = newClient(1000).probe(BROKER);

        Assert.assertTrue(String.valueOf(result.getDetail()), result.isSuccess());
        Mockito.verify(connection).start();
        Mockito.verify(sender).setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
        Mockito.verify(connection).close();
        Mockito.verify(connectionFactory).stop();
    }

    @Test
    public void reportsNotReceivedWhenTheMessageNeverComesBack() throws JMSException {

        Mockito.when(listener.receive(ArgumentMatchers.anyLong())).thenReturn(null);

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.NOT_RECEIVED, result.getFailedStage());
        Mockito.verify(connection).close();
    }

    @Test
    public void reportsPublishFailedWhenTheBrokerRejectsTheMessage() throws JMSException {

        Mockito.doThrow(new JMSException("rejected")).when(sender)
                .send(ArgumentMatchers.any(javax.jms.Message.class));

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.PUBLISH_FAILED, result.getFailedStage());
    }

    @Test
    public void reportsSubscribeFailedWhenTheListenerCannotBeCreated() throws JMSException {

        Mockito.when(session.createConsumer(ArgumentMatchers.any(), ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(false))).thenThrow(new JMSException("no access"));

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.SUBSCRIBE_FAILED, result.getFailedStage());
    }

    @Test
    public void reportsPortClosedWhenNothingListensOnThePort() {

        JMSException error = new JMSException("connection failed");
        error.setLinkedException(new ConnectException("Connection refused"));
        connectError = error;

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.PORT_CLOSED, result.getFailedStage());
        Mockito.verify(connectionFactory).stop();
    }

    @Test
    public void keepsTheOriginalFailureWhenStoppingTheConnectionFactoryAlsoThrows() {

        connectError = new ConnectException("Connection refused");
        Mockito.doThrow(new RuntimeException("simulated cleanup failure")).when(connectionFactory).stop();

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.PORT_CLOSED, result.getFailedStage());
    }

    @Test
    public void doesNotLeakTheConnectionStringWhenTheBrokerConnectionFails() {

        JMSException error = new JMSException("Error connecting to broker: amqp://admin:s3cr3t@localhost:8672");
        error.setLinkedException(new ConnectException("Connection refused"));
        connectError = error;

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertFalse(result.getDetail().contains("s3cr3t"));
        Assert.assertTrue(result.getDetail().contains("Connection refused"));
    }

    /**
     * Exercises the outer catch-all in {@link ProbeClient#probe} with an unchecked exception from the JMS client.
     */
    @Test
    public void doesNotLeakTheConnectionStringWhenAnUncheckedExceptionEscapesTheBrokerProbe() throws JMSException {

        RuntimeException wrapper = new IllegalStateException("Connection to amqp://admin:s3cr3t@localhost:5672 "
                + "failed");
        wrapper.initCause(new IllegalStateException("Connection refused"));
        Mockito.doThrow(wrapper).when(connection).start();

        ProbeResult result = newClient(50).probe(BROKER);

        Assert.assertEquals(ProbeStage.UNEXPECTED_ERROR, result.getFailedStage());
        Assert.assertFalse(result.getDetail(), result.getDetail().contains("s3cr3t"));
        Assert.assertTrue(result.getDetail(), result.getDetail().contains("Connection refused"));
    }

    @Test
    public void classifiesConnectFailures() {

        Assert.assertEquals(ProbeStage.TLS_FAILED,
                ProbeClient.classifyConnectFailure(new JMSException("x") {
                    {
                        initCause(new SSLHandshakeException("bad certificate"));
                    }
                }));
        Assert.assertEquals(ProbeStage.LOGIN_FAILED,
                ProbeClient.classifyConnectFailure(new FakeAuthenticationException()));
        Assert.assertEquals(ProbeStage.CONNECT_FAILED,
                ProbeClient.classifyConnectFailure(new JMSException("closed by broker")));
        Assert.assertEquals(ProbeStage.TIMEOUT,
                ProbeClient.classifyConnectFailure(new ConnectException("Operation timed out (Connection timed out)")));
        Assert.assertEquals(ProbeStage.PORT_CLOSED,
                ProbeClient.classifyConnectFailure(new ConnectException("Connection refused")));
    }

    /**
     * The outer message of an escaping Error can carry the connection string, so only the root cause is kept.
     */
    @Test
    public void reducesAnErrorFromAProbeThreadToItsRootCause() {

        JMSException jms = new JMSException("Error connecting to broker: amqp://admin:s3cr3t@localhost:8672");
        jms.setLinkedException(new ConnectException("Connection refused"));
        Error escaped = new ExceptionInInitializerError(jms);

        String detail = String.valueOf(ProbeClient.rootCause(escaped));

        Assert.assertFalse(detail, detail.contains("s3cr3t"));
        Assert.assertTrue(detail, detail.contains("Connection refused"));
    }

    /**
     * Qpid throws a cause-less {@link TransportException} for both drops and refusals, so the message cannot say why.
     */
    @Test
    public void classifiesAQpidTransportExceptionAsConnectFailedWithANonSpecificMessage() {

        ProbeStage stage = ProbeClient.classifyConnectFailure(new TransportException("Could not open connection"));

        Assert.assertEquals(ProbeStage.CONNECT_FAILED, stage);
        Assert.assertEquals("Could not connect to the broker", stage.getMessage());
        Assert.assertFalse(stage.getMessage(), stage.getMessage().toLowerCase(Locale.ROOT).contains("refused"));
    }

    @Test
    public void addsTheTopicAndTurnsOffConnectionCaching() {

        Properties properties = new Properties();
        properties.setProperty(JMSConstants.CONNECTION_STRING_TOPIC, "amqp://u:p@clientid/carbon?brokerlist='x'");

        Hashtable<String, String> parameters = ProbeClient.toJmsParameters(properties);

        Assert.assertEquals("none", parameters.get(JMSConstants.PARAM_CACHE_LEVEL));
        Assert.assertEquals(ProbeClient.TOPIC_NAME, parameters.get("topic." + ProbeClient.TOPIC_NAME));
        Assert.assertEquals("amqp://u:p@clientid/carbon?brokerlist='x'",
                parameters.get(JMSConstants.CONNECTION_STRING_TOPIC));
    }

    /**
     * A client that uses plain sockets to the fake receiver and the mocked JMS objects for the broker.
     */
    private ProbeClient newClient(long receiveTimeoutMs) {

        return new ProbeClient(STREAM_ID, receiveTimeoutMs) {

            @Override
            protected Socket createSocket(String protocol, String host, int port) throws Exception {

                Socket socket = new Socket(host, port);
                socket.setSoTimeout(5000);
                return socket;
            }

            @Override
            protected void closeSocket(String protocol, Socket socket) {

                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }

            @Override
            protected JMSConnectionFactory createConnectionFactory(Hashtable<String, String> parameters) {

                return connectionFactory;
            }

            @Override
            protected Connection createConnection(JMSConnectionFactory factory, Hashtable<String, String> parameters)
                    throws Exception {

                if (connectError != null) {
                    throw connectError;
                }
                return connection;
            }
        };
    }

    private static TextMessage textMessage(String text) throws JMSException {

        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn(text);
        return message;
    }

    /**
     * Named like the broker client's real login error ({@code AMQAuthenticationException}).
     */
    private static final class FakeAuthenticationException extends JMSException {

        private static final long serialVersionUID = 1L;

        private FakeAuthenticationException() {

            super("not authorized");
        }
    }

    /**
     * Accepts connections and answers one binary message per connection, in the receiver's wire format.
     */
    private static final class FakeReceiver {

        private final ServerSocket serverSocket;
        private final Class<? extends Exception> loginError;
        private final Class<? extends Exception> publishError;
        private final boolean closeWithoutReplyingToPublish;
        private final List<Integer> receivedMessageTypes = new CopyOnWriteArrayList<>();

        private FakeReceiver(Class<? extends Exception> loginError, Class<? extends Exception> publishError)
                throws IOException {

            this(loginError, publishError, false);
        }

        /**
         * @param closeWithoutReplyingToPublish close the connection instead of answering the publish message
         */
        private FakeReceiver(Class<? extends Exception> loginError, Class<? extends Exception> publishError,
                             boolean closeWithoutReplyingToPublish) throws IOException {

            this.serverSocket = new ServerSocket(0);
            this.loginError = loginError;
            this.publishError = publishError;
            this.closeWithoutReplyingToPublish = closeWithoutReplyingToPublish;
            Thread thread = new Thread(this::serve, "fake-binary-receiver");
            thread.setDaemon(true);
            thread.start();
        }

        private ProbeEndpoint endpoint() {

            return endpoint("admin");
        }

        private ProbeEndpoint endpoint(String password) {

            return localReceiver(serverSocket.getLocalPort(), password);
        }

        private void serve() {

            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    int messageType = in.readByte();
                    byte[] body = new byte[in.readInt()];
                    in.readFully(body);
                    receivedMessageTypes.add(messageType);
                    answer(messageType, socket.getOutputStream());
                } catch (IOException e) {
                }
            }
        }

        private void answer(int messageType, OutputStream out) throws IOException {

            if (messageType == LOGIN && loginError != null) {
                writeError(out, loginError);
            } else if (messageType == LOGIN) {
                byte[] sessionId = "session-1".getBytes(StandardCharsets.UTF_8);
                out.write(ByteBuffer.allocate(5 + sessionId.length).put((byte) 2).putInt(sessionId.length)
                        .put(sessionId).array());
            } else if (messageType == PUBLISH && closeWithoutReplyingToPublish) {
                return;
            } else if (messageType == PUBLISH && publishError != null) {
                writeError(out, publishError);
            } else {
                out.write(0);
            }
            out.flush();
        }

        private void writeError(OutputStream out, Class<? extends Exception> errorClass) throws IOException {

            byte[] className = errorClass.getName().getBytes(StandardCharsets.UTF_8);
            byte[] message = "rejected by fake receiver".getBytes(StandardCharsets.UTF_8);
            out.write(ByteBuffer.allocate(9 + className.length + message.length).put((byte) 1)
                    .putInt(className.length).putInt(message.length).put(className).put(message).array());
        }

        private void close() throws IOException {

            serverSocket.close();
        }
    }
}
