package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GovernanceApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryApiServerClient;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryDetailWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListFilter;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoverySummaryWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.UntraffickedListWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.DiscoveryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation of the Unmanaged APIs (Governance / API Discovery) admin
 * REST resource. Proxies the API Discovery Server (separate process) and
 * translates wire payloads into the auto-generated REST DTOs.
 *
 * <p>This is a thin pass-through: it does not store state, transform data
 * semantically, or enforce business rules beyond what the daemon already
 * does. It validates scopes (via the OpenAPI spec's security: block) and
 * forwards the user's bearer token to the daemon, which introspects it
 * back against APIM.</p>
 *
 * @since 4.7.0
 */
public class GovernanceApiServiceImpl implements GovernanceApiService {

    /** Class-level logger. */
    private static final Log LOG =
            LogFactory.getLog(GovernanceApiServiceImpl.class);

    /** Default page size applied when the caller passes a null limit. */
    private static final int DEFAULT_PAGE_LIMIT = 25;

    /** Default offset applied when the caller passes a null offset. */
    private static final int DEFAULT_PAGE_OFFSET = 0;

    /** HTTP client to the API Discovery Server. */
    private final DiscoveryApiServerClient client;

    /** Production constructor — wires the singleton client. */
    public GovernanceApiServiceImpl() {
        this.client = DiscoveryApiServerClient.getInstance();
    }

    /**
     * Test seam — inject a mock client.
     *
     * @param mockClient pre-built (typically mocked) discovery client
     */
    GovernanceApiServiceImpl(final DiscoveryApiServerClient mockClient) {
        this.client = mockClient;
    }

    /**
     * GET /governance/discovery/summary.
     *
     * @param messageContext CXF message context
     * @return 200 with DiscoverySummaryDTO, or RestApiUtil-mapped error
     * @throws APIManagementException if the daemon is unreachable
     */
    public final Response getDiscoverySummary(
            final MessageContext messageContext) throws APIManagementException {
        try {
            final String bearer = extractBearer(messageContext);
            final DiscoverySummaryWire wire = client.getSummary(bearer);
            return Response.ok()
                    .entity(DiscoveryMappingUtil.fromWireSummary(wire)).build();
        } catch (DiscoveryApiServerClient.UnavailableException e) {
            RestApiUtil.handleInternalServerError(
                    "API Discovery Server unavailable while fetching summary",
                    e, LOG);
            return null;
        }
    }

    /**
     * GET /governance/discovery/apis.
     *
     * @param classification optional classification filter
     * @param service        optional service_identity filter
     * @param internal       optional internal-only flag
     * @param limit          pagination limit
     * @param offset         pagination offset
     * @param messageContext CXF message context
     * @return 200 with DiscoveredAPIListDTO, or RestApiUtil-mapped error
     * @throws APIManagementException if the daemon is unreachable
     */
    public final Response getDiscoveredAPIs(final String classification,
                                      final String service,
                                      final String internal,
                                      final Integer limit,
                                      final Integer offset,
                                      final MessageContext messageContext)
            throws APIManagementException {
        try {
            final String bearer = extractBearer(messageContext);
            final int pageLimit;
            if (limit == null) {
                pageLimit = DEFAULT_PAGE_LIMIT;
            } else {
                pageLimit = limit;
            }
            final int pageOffset;
            if (offset == null) {
                pageOffset = DEFAULT_PAGE_OFFSET;
            } else {
                pageOffset = offset;
            }
            final DiscoveryListFilter f = new DiscoveryListFilter()
                    .classification(classification)
                    .service(service)
                    .internal(internal)
                    .pagination(pageLimit, pageOffset);
            final DiscoveryListWire wire = client.listApis(bearer, f);
            return Response.ok()
                    .entity(DiscoveryMappingUtil.fromWireList(wire)).build();
        } catch (DiscoveryApiServerClient.UnavailableException e) {
            RestApiUtil.handleInternalServerError(
                    "API Discovery Server unavailable while listing discovered APIs",
                    e, LOG);
            return null;
        }
    }

    /**
     * GET /governance/discovery/apis/{discoveredApiId}.
     *
     * @param discoveredApiId discovered API id
     * @param messageContext  CXF message context
     * @return 200 with DiscoveredAPIDTO, 404 if not found, 500 on
     *         daemon failure
     * @throws APIManagementException if the daemon is unreachable
     */
    public final Response getDiscoveredAPIById(
            final String discoveredApiId,
            final MessageContext messageContext) throws APIManagementException {
        try {
            final String bearer = extractBearer(messageContext);
            final DiscoveryDetailWire wire = client.getApiById(bearer, discoveredApiId);
            return Response.ok()
                    .entity(DiscoveryMappingUtil.fromWireDetail(wire)).build();
        } catch (DiscoveryApiServerClient.NotFoundException e) {
            RestApiUtil.handleResourceNotFoundError(
                    "Discovered API", discoveredApiId, e, LOG);
            return null;
        } catch (DiscoveryApiServerClient.UnavailableException e) {
            RestApiUtil.handleInternalServerError(
                    "API Discovery Server unavailable while fetching API detail",
                    e, LOG);
            return null;
        }
    }

    /**
     * GET /governance/discovery/untrafficked.
     *
     * @param messageContext CXF message context
     * @return 200 with UntraffickedListDTO, or RestApiUtil-mapped error
     * @throws APIManagementException if the daemon is unreachable
     */
    public final Response getUntrafficked(
            final MessageContext messageContext) throws APIManagementException {
        try {
            final String bearer = extractBearer(messageContext);
            final UntraffickedListWire wire = client.listUntrafficked(bearer);
            return Response.ok()
                    .entity(DiscoveryMappingUtil.fromWireUntrafficked(wire))
                    .build();
        } catch (DiscoveryApiServerClient.UnavailableException e) {
            RestApiUtil.handleInternalServerError(
                    "API Discovery Server unavailable while listing untrafficked APIs",
                    e, LOG);
            return null;
        }
    }

    /**
     * Extracts the raw bearer token from the inbound Authorization header
     * so we can forward it to the daemon. Returns "" if absent so the
     * daemon's auth middleware will reject with 401 (the user reaches a
     * normal error path rather than a server-side NPE).
     *
     * @param mc CXF message context
     * @return raw bearer token, or "" if no Authorization header
     */
    private static String extractBearer(final MessageContext mc) {
        if (mc == null || mc.getHttpHeaders() == null) {
            return "";
        }
        final List<String> auths =
                mc.getHttpHeaders().getRequestHeader("Authorization");
        if (auths == null || auths.isEmpty()) {
            return "";
        }
        final String header = auths.get(0);
        if (header == null || !header.startsWith("Bearer ")) {
            return "";
        }
        return header.substring("Bearer ".length()).trim();
    }
}
