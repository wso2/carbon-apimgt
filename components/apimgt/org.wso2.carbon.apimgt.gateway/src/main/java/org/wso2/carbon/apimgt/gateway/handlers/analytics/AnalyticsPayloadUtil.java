/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import static org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS;

/**
 * Utility for capturing the request/response payload (body) for analytics publishing.
 *
 * <p>Body capture is an opt-in feature (gated by the {@code send_payloads} analytics property) because it
 * forces the pass-through message to be built into memory and captures potentially sensitive data.
 * All content types are captured except streaming ({@code text/event-stream}) and {@code multipart/*},
 * which have no single materializable body. JSON and text payloads are captured as-is; binary payloads
 * are Base64-encoded (mirroring the Moesif standard, where the publisher tags them
 * {@code transferEncoding=base64}). A payload larger than the configurable size limit is dropped
 * (not truncated) so the publisher only ever receives a whole, valid body or nothing; the backend
 * always receives the full body regardless.</p>
 */
public class AnalyticsPayloadUtil {

    private static final Log log = LogFactory.getLog(AnalyticsPayloadUtil.class);

    // Guards the invalid-payload_size_limit warning so a misconfigured value is logged once rather than
    // on every capture attempt (getPayloadSizeLimit is called per request/response).
    private static volatile boolean invalidLimitWarned = false;

    private AnalyticsPayloadUtil() {
    }

    /**
     * Holds a captured body together with its Moesif transfer-encoding hint.
     * {@code transferEncoding} is {@code "base64"} for pre-encoded binary payloads and {@code null}
     * for JSON/text (the publisher decides JSON-vs-base64 for those via Moesif's {@code BodyParser}).
     */
    public static class CapturedBody {
        private final String body;
        private final String transferEncoding;

        CapturedBody(String body, String transferEncoding) {
            this.body = body;
            this.transferEncoding = transferEncoding;
        }

        public String getBody() {
            return body;
        }

        public String getTransferEncoding() {
            return transferEncoding;
        }
    }

    /**
     * Whether request/response body capture is enabled via the {@code send_payloads} analytics property
     * (named to match the sibling {@code send_headers} option). Defaults to {@code false}.
     *
     * @return true if body capture is enabled
     */
    public static boolean shouldSendPayloads() {
        Map<String, String> configs = APIManagerConfiguration.getAnalyticsProperties();
        return configs != null && Boolean.parseBoolean(configs.get(Constants.SEND_PAYLOAD));
    }

    /**
     * Maximum number of bytes/characters captured per body, read from the {@code payload_size_limit}
     * analytics property. Falls back to {@link Constants#DEFAULT_PAYLOAD_SIZE_LIMIT} when unset or invalid.
     *
     * @return the payload size limit
     */
    public static int getPayloadSizeLimit() {
        Map<String, String> configs = APIManagerConfiguration.getAnalyticsProperties();
        if (configs != null && configs.get(Constants.PAYLOAD_SIZE_LIMIT) != null) {
            try {
                int limit = Integer.parseInt(configs.get(Constants.PAYLOAD_SIZE_LIMIT));
                if (limit > 0) {
                    invalidLimitWarned = false;
                    return limit;
                }
                // A non-positive limit would silently drop every body (and break size math), so treat
                // it as invalid rather than honouring it.
                warnInvalidLimitOnce("Non-positive " + Constants.PAYLOAD_SIZE_LIMIT
                        + " analytics property value. Using default " + Constants.DEFAULT_PAYLOAD_SIZE_LIMIT);
            } catch (NumberFormatException e) {
                warnInvalidLimitOnce("Invalid " + Constants.PAYLOAD_SIZE_LIMIT
                        + " analytics property value. Using default " + Constants.DEFAULT_PAYLOAD_SIZE_LIMIT);
            }
        }
        return Constants.DEFAULT_PAYLOAD_SIZE_LIMIT;
    }

    /**
     * Logs the invalid-{@code payload_size_limit} warning at most once per stretch of misconfiguration,
     * so a bad value does not flood the logs on every per-request/response capture attempt. The flag is
     * reset once a valid limit is read again, so a later re-misconfiguration is warned about afresh.
     */
    private static void warnInvalidLimitOnce(String message) {
        if (!invalidLimitWarned) {
            invalidLimitWarned = true;
            log.warn(message);
        }
    }

    /**
     * Whether to capture payloads that do not declare a {@code Content-Length} (e.g. chunked
     * transfer-encoding), read from the {@code capture_payloads_without_content_length} analytics
     * property. Defaults to {@code false}.
     *
     * <p>Such a body cannot be size-checked before {@link RelayUtils#buildMessage} materializes it in
     * full, so capturing a large chunked body would buffer the whole thing into memory (an OOM risk
     * under load) only to drop it afterwards if it exceeds {@code payload_size_limit}. It is therefore
     * off by default: with the flag off these bodies are skipped (never built), so the default
     * configuration is memory-safe. An operator who needs them and has headroom can opt in, accepting
     * the memory cost. Applies symmetrically to request and response capture.</p>
     *
     * @return true if bodies without a declared Content-Length should be captured
     */
    public static boolean shouldCapturePayloadsWithoutContentLength() {
        Map<String, String> configs = APIManagerConfiguration.getAnalyticsProperties();
        return configs != null
                && Boolean.parseBoolean(configs.get(Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH));
    }

    /**
     * Builds the message (materializing the pass-through stream) and returns the captured body along
     * with its transfer-encoding hint. Streaming ({@code text/event-stream}) and {@code multipart/*}
     * payloads are skipped. JSON and text are returned verbatim with a {@code null} encoding; binary
     * payloads are Base64-encoded with encoding {@code "base64"}.
     *
     * <p>A body larger than {@code sizeLimit} is <b>dropped</b> (returns {@code null}) rather than
     * truncated, so the publisher never has to deal with a partial/invalid body. Where the payload
     * advertises its size via a {@code Content-Length} header the check is applied <b>before</b>
     * {@link RelayUtils#buildMessage} so an oversized body is never buffered into memory or
     * re-serialized. A body with no {@code Content-Length} (e.g. chunked transfer-encoding) cannot be
     * size-checked before building, so by default it is <b>skipped</b> (never built) to keep the
     * default configuration memory-safe; set {@code capture_payloads_without_content_length=true} to
     * capture it anyway (it is then built in full and dropped afterwards if it exceeds the limit,
     * accepting the memory cost). See {@link #shouldCapturePayloadsWithoutContentLength()}.</p>
     *
     * <p>Works for both the request (called from {@code handleRequestOutFlow}, just before the backend
     * send) and the response (called at event-collection time). When a body is captured,
     * {@link RelayUtils#buildMessage} sets {@code MESSAGE_BUILDER_INVOKED}, so the pass-through sender
     * re-serializes the built envelope to the backend — forwarding is preserved. This is the same
     * read-only build pattern used by the AI mediator and the threat-protection/schema validators.
     * Requests with no entity body (GET/DELETE) are skipped so they are left byte-identical.</p>
     *
     * <p>The limit is compared against bytes throughout: the declared {@code Content-Length} for the
     * pre-build gate, the decoded byte count for the binary path, and the UTF-8 encoded byte length
     * for text/JSON/XML.</p>
     *
     * <p>When {@code send_payloads} is enabled but a body is nonetheless skipped or dropped, the reason
     * is logged at debug level (keyed by {@code direction}) so operators are not left wondering why a
     * body is missing from Moesif.</p>
     *
     * @param messageContext the Synapse message context (request or response)
     * @param sizeLimit      maximum number of characters (text) or bytes (binary) to capture
     * @param direction      {@code "request"} or {@code "response"}, used only for debug log messages
     * @return the captured body, or {@code null} if there is nothing capturable or it exceeds the limit
     */
    public static CapturedBody extractPayload(MessageContext messageContext, int sizeLimit, String direction) {
        try {
            org.apache.axis2.context.MessageContext axis2MC =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            // Nothing to capture (and nothing to build) for entity-less requests such as GET/DELETE.
            if (Boolean.TRUE.equals(axis2MC.getProperty(PassThroughConstants.NO_ENTITY_BODY))) {
                if (log.isDebugEnabled()) {
                    log.debug("No " + direction + " body captured for analytics: message has no entity body "
                            + "(e.g. GET/DELETE).");
                }
                return null;
            }

            String contentType = getContentType(axis2MC);
            if (isExcludedContentType(contentType)) {
                if (log.isDebugEnabled()) {
                    log.debug("No " + direction + " body captured for analytics: content type '" + contentType
                            + "' is excluded (streaming/multipart bodies are never captured).");
                }
                return null;
            }

            // Pre-build gate: if the payload advertises its size and it exceeds the limit, drop it now
            // without building — so an oversized body is never buffered into memory or re-serialized.
            long declaredLength = getDeclaredContentLength(axis2MC);
            if (declaredLength > sizeLimit) {
                if (log.isDebugEnabled()) {
                    log.debug("Dropping " + direction + " body from analytics: declared Content-Length "
                            + declaredLength + " bytes exceeds " + Constants.PAYLOAD_SIZE_LIMIT + " of " + sizeLimit
                            + "; body not built. Increase " + Constants.PAYLOAD_SIZE_LIMIT + " to capture it.");
                }
                return null;
            }

            // No declared Content-Length (e.g. chunked transfer-encoding): the size cannot be bounded
            // before building, so building here would materialize the whole body in memory (an OOM risk
            // under load). Skip it unless the operator has explicitly opted in. Off by default keeps the
            // default configuration memory-safe; applies symmetrically to request and response.
            if (declaredLength < 0 && !shouldCapturePayloadsWithoutContentLength()) {
                if (log.isDebugEnabled()) {
                    log.debug("No " + direction + " body captured for analytics: no Content-Length header "
                            + "(e.g. chunked transfer-encoding), so the body size cannot be bounded before "
                            + "building. Set " + Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH
                            + "=true to capture it (materializes the full body in memory).");
                }
                return null;
            }

            RelayUtils.buildMessage(axis2MC);

            // JSON: return the text as-is; the publisher's BodyParser renders it as a structured object.
            if (JsonUtil.hasAJsonPayload(axis2MC)) {
                return capture(JsonUtil.jsonPayloadToString(axis2MC), sizeLimit, null, direction, "JSON");
            }

            SOAPEnvelope env = axis2MC.getEnvelope();
            if (env == null || env.getBody() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No " + direction + " body captured for analytics: message has no SOAP body.");
                }
                return null;
            }
            SOAPBody soapBody = env.getBody();
            OMElement firstElement = soapBody.getFirstElement();
            if (firstElement == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No " + direction + " body captured for analytics: message body is empty.");
                }
                return null;
            }
            env.buildWithAttachments();

            if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstElement.getQName())) {
                // Binary payload: the wrapper's text is the Base64 of the raw bytes. Decode, and drop
                // the whole body if the raw bytes exceed the limit; otherwise pass the bytes through.
                byte[] bytes = Base64.decodeBase64(firstElement.getText());
                if (bytes.length > sizeLimit) {
                    if (log.isDebugEnabled()) {
                        log.debug("Dropping " + direction + " binary body from analytics: " + bytes.length
                                + " bytes exceeds " + Constants.PAYLOAD_SIZE_LIMIT + " of " + sizeLimit
                                + ". Increase " + Constants.PAYLOAD_SIZE_LIMIT + " to capture it.");
                    }
                    return null;
                }
                return new CapturedBody(Base64.encodeBase64String(bytes), Constants.TRANSFER_ENCODING_BASE64);
            } else if (BaseConstants.DEFAULT_TEXT_WRAPPER.equals(firstElement.getQName())) {
                // Plain text payload.
                return capture(firstElement.getText(), sizeLimit, null, direction, "text");
            } else {
                // XML / SOAP payload.
                return capture(firstElement.toString(), sizeLimit, null, direction, "XML/SOAP");
            }
        } catch (IOException | XMLStreamException e) {
            log.error("Error occurred while capturing the " + direction + " message payload for analytics", e);
            return null;
        } catch (RuntimeException e) {
            // Fail safe: payload capture must never disrupt the request/response flow.
            log.error("Unexpected error while capturing the " + direction + " message payload for analytics", e);
            return null;
        }
    }

    /**
     * Wraps a text/JSON/XML payload, dropping it (returning {@code null}) when its UTF-8 encoded size
     * exceeds {@code sizeLimit} bytes so only a whole body is ever published. Measuring bytes (not
     * {@link String#length()}, which counts UTF-16 code units) keeps this consistent with the
     * byte-based pre-build {@code Content-Length} gate and binary path, and with the byte semantics of
     * {@code payload_size_limit}. A drop is logged at debug level (keyed by {@code direction}/
     * {@code kind}) so a missing body can be explained.
     */
    private static CapturedBody capture(String payload, int sizeLimit, String transferEncoding,
                                        String direction, String kind) {
        if (payload == null) {
            return null;
        }
        int byteLength = payload.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > sizeLimit) {
            if (log.isDebugEnabled()) {
                log.debug("Dropping " + direction + " " + kind + " body from analytics: " + byteLength
                        + " bytes exceeds " + Constants.PAYLOAD_SIZE_LIMIT + " of " + sizeLimit
                        + ". Increase " + Constants.PAYLOAD_SIZE_LIMIT + " to capture it.");
            }
            return null;
        }
        return new CapturedBody(payload, transferEncoding);
    }

    /**
     * The declared body size from the {@code Content-Length} transport header, or {@code -1} when it is
     * absent (e.g. chunked transfer-encoding) or unparseable. Read before building so an oversized body
     * can be skipped without buffering it. The transport-headers map is case-insensitive, matching the
     * exact-case lookup idiom used elsewhere (e.g. {@code getRequestSize}/{@code getResponseSize}).
     */
    private static long getDeclaredContentLength(org.apache.axis2.context.MessageContext axis2MC) {
        Object headersObj = axis2MC.getProperty(TRANSPORT_HEADERS);
        if (headersObj instanceof Map) {
            Object contentLength = ((Map) headersObj).get(HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                try {
                    return Long.parseLong(contentLength.toString().trim());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private static String getContentType(org.apache.axis2.context.MessageContext axis2MC) {
        Object headersObj = axis2MC.getProperty(TRANSPORT_HEADERS);
        if (headersObj instanceof Map) {
            Object contentType = ((Map) headersObj).get(HttpHeaders.CONTENT_TYPE);
            if (contentType != null) {
                return contentType.toString();
            }
        }
        return null;
    }

    /**
     * Streaming (SSE) and multipart payloads have no single materializable body and must never be
     * captured (building/buffering them would be incorrect or disruptive).
     */
    private static boolean isExcludedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String lower = contentType.toLowerCase(Locale.ROOT);
        return lower.contains("event-stream") || lower.startsWith("multipart/");
    }
}
