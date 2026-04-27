package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for the API Discovery Server's REST surface
 * ({@code /api/v1/governance/discovery/*}). Wraps Apache HttpClient and
 * Jackson; no new dependencies beyond what the carbon-apimgt module
 * already pulls in transitively.
 *
 * <p>Configuration keys (read via APIManagerConfiguration; provisioned by
 * deployment.toml.j2's {@code [apim.governance.discovery]} block in
 * product-apim):</p>
 * <pre>
 *   APIM.Governance.Discovery.Endpoint    https://ads.internal:8443
 *   APIM.Governance.Discovery.TimeoutMs   5000   (default if unset)
 * </pre>
 *
 * <p>Bearer token strategy: the BFF FORWARDS the user's incoming bearer
 * token to the daemon. The daemon's auth middleware accepts either
 * {@code apim:admin} or {@code apim:admin_discovery_view}; the user's
 * APIM token already carries one of these by virtue of being authorized
 * to hit this BFF. No service-account password grant needed.</p>
 *
 * @since 4.7.0
 */
// Not declared final so Mockito can mock the class in unit tests.
public class DiscoveryApiServerClient {

    private static final Log LOG = LogFactory.getLog(DiscoveryApiServerClient.class);

    private static final String CFG_ENDPOINT     = "APIM.Governance.Discovery.Endpoint";
    private static final String CFG_TIMEOUT_MS   = "APIM.Governance.Discovery.TimeoutMs";
    private static final String DEFAULT_ENDPOINT = "https://localhost:8443";
    private static final int    DEFAULT_TIMEOUT_MS = 5000;

    private static final String PATH_SUMMARY      = "/api/v1/governance/discovery/summary";
    private static final String PATH_LIST         = "/api/v1/governance/discovery/apis";
    private static final String PATH_DETAIL_FMT   = "/api/v1/governance/discovery/apis/%s";
    private static final String PATH_UNTRAFFICKED = "/api/v1/governance/discovery/untrafficked";

    private static volatile DiscoveryApiServerClient instance;

    private final ObjectMapper mapper;
    private final String baseUrl;
    private final int timeoutMs;

    private DiscoveryApiServerClient(final String baseUrl, final int timeoutMs) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Returns the singleton, lazy-constructed on first call from the
     * APIManagerConfiguration values.
     *
     * @return shared client instance
     */
    public static DiscoveryApiServerClient getInstance() {
        if (instance == null) {
            synchronized (DiscoveryApiServerClient.class) {
                if (instance == null) {
                    instance = build();
                }
            }
        }
        return instance;
    }

    /**
     * Test seam — replaces the singleton with a hand-built instance.
     * Production code calls {@link #getInstance()} only.
     *
     * @param mock a pre-built client; pass {@code null} to reset
     */
    public static void setInstanceForTesting(final DiscoveryApiServerClient mock) {
        instance = mock;
    }

    private static DiscoveryApiServerClient build() {
        final APIManagerConfiguration cfg = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String endpoint = cfg.getFirstProperty(CFG_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = System.getProperty(CFG_ENDPOINT, DEFAULT_ENDPOINT);
        }
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        int timeout = DEFAULT_TIMEOUT_MS;
        final String timeoutStr = cfg.getFirstProperty(CFG_TIMEOUT_MS);
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                LOG.warn(CFG_TIMEOUT_MS + " not an integer (" + timeoutStr
                        + "), defaulting to " + DEFAULT_TIMEOUT_MS + "ms");
            }
        }
        return new DiscoveryApiServerClient(endpoint, timeout);
    }

    /**
     * GET /summary on the daemon and return the wire payload.
     *
     * @param bearerToken caller's bearer token, forwarded to the daemon
     * @return parsed summary
     * @throws UnavailableException on network / 5xx / parse failure
     */
    public DiscoverySummaryWire getSummary(final String bearerToken)
            throws UnavailableException {
        try {
            final byte[] body = doGet(PATH_SUMMARY, bearerToken);
            return parse(body, DiscoverySummaryWire.class);
        } catch (NotFoundException e) {
            // Defensive — /summary is collection-shaped and should never
            // 404; surface as Unavailable so the BFF returns 500.
            throw new UnavailableException("discovery summary not found", e);
        }
    }

    /**
     * GET /apis with optional filters and pagination.
     *
     * @param bearerToken caller's bearer token
     * @param filter      query parameter bundle
     * @return parsed list
     * @throws UnavailableException on network / 5xx / parse failure
     */
    public DiscoveryListWire listApis(final String bearerToken,
                                      final DiscoveryListFilter filter)
            throws UnavailableException {
        final String path = PATH_LIST + buildQuery(filter);
        try {
            final byte[] body = doGet(path, bearerToken);
            return parse(body, DiscoveryListWire.class);
        } catch (NotFoundException e) {
            throw new UnavailableException("list endpoint returned 404", e);
        }
    }

    /**
     * GET /apis/{id}.
     *
     * @param bearerToken caller's bearer token
     * @param id          discovered API id
     * @return parsed detail
     * @throws NotFoundException     when the daemon returns 404
     * @throws UnavailableException  on network / other 5xx / parse failure
     */
    public DiscoveryDetailWire getApiById(final String bearerToken, final String id)
            throws UnavailableException, NotFoundException {
        final String path = String.format(PATH_DETAIL_FMT,
                URLEncoder.encode(id, StandardCharsets.UTF_8));
        final byte[] body = doGet(path, bearerToken);
        return parse(body, DiscoveryDetailWire.class);
    }

    /**
     * GET /untrafficked.
     *
     * @param bearerToken caller's bearer token
     * @return parsed list
     * @throws UnavailableException on network / 5xx / parse failure
     */
    public UntraffickedListWire listUntrafficked(final String bearerToken)
            throws UnavailableException {
        try {
            final byte[] body = doGet(PATH_UNTRAFFICKED, bearerToken);
            return parse(body, UntraffickedListWire.class);
        } catch (NotFoundException e) {
            throw new UnavailableException("untrafficked endpoint returned 404", e);
        }
    }

    private byte[] doGet(final String path, final String bearerToken)
            throws UnavailableException, NotFoundException {
        final URI uri;
        try {
            uri = new URI(baseUrl + path);
        } catch (URISyntaxException e) {
            throw new UnavailableException("invalid discovery server URL: "
                    + baseUrl + path, e);
        }

        final HttpResponse resp;
        final byte[] body;
        try (CloseableHttpClient http = (CloseableHttpClient)
                APIUtil.getHttpClient(uri.getPort(), uri.getScheme())) {

            final HttpGet req = new HttpGet(uri);
            req.setHeader("Accept", "application/json");
            if (bearerToken != null && !bearerToken.isEmpty()) {
                req.setHeader("Authorization", "Bearer " + bearerToken);
            }

            resp = http.execute(req);
            body = resp.getEntity() == null
                    ? new byte[0]
                    : EntityUtils.toByteArray(resp.getEntity());
        } catch (Exception e) {
            throw new UnavailableException("discovery server request failed: "
                    + path, e);
        }

        final int status = resp.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException("discovery server returned 404 for " + path);
        }
        if (status != HttpStatus.SC_OK) {
            throw new UnavailableException("discovery server returned HTTP "
                    + status + " for " + path);
        }
        return body;
    }

    private <T> T parse(final byte[] body, final Class<T> type)
            throws UnavailableException {
        try {
            return mapper.readValue(body, type);
        } catch (Exception e) {
            throw new UnavailableException("failed to parse discovery server response", e);
        }
    }

    private static String buildQuery(final DiscoveryListFilter f) {
        if (f == null) {
            return "";
        }
        final List<String> parts = new ArrayList<>();
        if (f.getClassification() != null && !f.getClassification().isEmpty()) {
            parts.add("classification=" + URLEncoder.encode(
                    f.getClassification(), StandardCharsets.UTF_8));
        }
        if (f.getService() != null && !f.getService().isEmpty()) {
            parts.add("service=" + URLEncoder.encode(
                    f.getService(), StandardCharsets.UTF_8));
        }
        if (f.getInternal() != null && !f.getInternal().isEmpty()) {
            parts.add("internal=" + URLEncoder.encode(
                    f.getInternal(), StandardCharsets.UTF_8));
        }
        if (f.getLimit() > 0) {
            parts.add("limit=" + f.getLimit());
        }
        if (f.getOffset() > 0) {
            parts.add("offset=" + f.getOffset());
        }
        if (parts.isEmpty()) {
            return "";
        }
        return "?" + String.join("&", parts);
    }

    /** Discovery server unreachable / 5xx / network / parse failure. */
    public static final class UnavailableException extends Exception {
        private static final long serialVersionUID = 1L;
        public UnavailableException(final String message) { super(message); }
        public UnavailableException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /** Discovery server returned 404 — the requested row doesn't exist. */
    public static final class NotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
        public NotFoundException(final String message) { super(message); }
    }
}
