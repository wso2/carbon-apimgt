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

package org.wso2.carbon.apimgt.tm.healthcheck;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Stage 1 of the two-stage connectivity check (Research &sect;3): a bounded-timeout network-level connect that
 * answers only "is it there", not "does it work". A plain TCP connect for {@link ControlPlaneConnectionId}s that
 * are not TLS ({@link ResolvedConnectionConfig#isTlsEnabled()} {@code false}), plus a TLS handshake on top of
 * that same connect for the ones that are.
 *
 * <p>Success here is a necessary but not sufficient condition for a connection to be reported {@code UP} - see
 * {@link ThrottlingDataAcceptConfirmationChecker}, {@link MessageBrokerAcceptConfirmationChecker}, and
 * {@link ManagementApiAcceptConfirmationChecker} for the stage-2 "does it work" step that runs only after this
 * stage succeeds.</p>
 */
public class PortResponsivenessChecker {

    /**
     * Attempts a bounded-timeout connect (plus a TLS handshake, when applicable) to the given resolved
     * connection, never blocking longer than {@code connectTimeoutMs}.
     *
     * @param config           the live host/port/TLS details to dial, resolved fresh for this invocation
     * @param connectTimeoutMs the bounded timeout, in milliseconds, for the connect and (if applicable) the
     *                         handshake
     * @return {@link ConnectionCheckResult#up(ControlPlaneConnectionId)} if the connect (and handshake)
     *         succeeded; otherwise a {@code DOWN} result with a {@link FailureReason} classified by cause
     */
    public ConnectionCheckResult check(ResolvedConnectionConfig config, int connectTimeoutMs) {

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), connectTimeoutMs);
            if (config.isTlsEnabled()) {
                performTlsHandshake(socket, config, connectTimeoutMs);
            }
            return ConnectionCheckResult.up(config.getConnectionId());
        } catch (SocketTimeoutException e) {
            return ConnectionCheckResult.down(config.getConnectionId(), FailureReason.TIMED_OUT, e.getMessage());
        } catch (ConnectException | SSLHandshakeException e) {
            return ConnectionCheckResult.down(config.getConnectionId(), FailureReason.CONNECTION_REFUSED,
                    e.getMessage());
        } catch (IOException e) {
            return ConnectionCheckResult.down(config.getConnectionId(), FailureReason.UNREACHABLE, e.getMessage());
        }
    }

    /**
     * Layers a TLS handshake on top of an already-connected plain socket, bounded by the same
     * {@code connectTimeoutMs} safety bound used for the connect itself.
     *
     * @param plainSocket      the already-connected TCP socket to upgrade
     * @param config           identifies the host/port being dialed, for the TLS wrapper
     * @param connectTimeoutMs bounds how long the handshake itself may block
     * @throws IOException if the handshake fails or times out
     */
    private void performTlsHandshake(Socket plainSocket, ResolvedConnectionConfig config, int connectTimeoutMs)
            throws IOException {

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket layeredSocket = sslSocketFactory.createSocket(plainSocket, config.getHost(), config.getPort(), true);
        if (!(layeredSocket instanceof SSLSocket)) {
            throw new IOException("SSLSocketFactory did not return an SSLSocket for " + config.getConnectionId());
        }
        try (SSLSocket sslSocket = (SSLSocket) layeredSocket) {
            sslSocket.setSoTimeout(connectTimeoutMs);
            sslSocket.startHandshake();
        }
    }
}
