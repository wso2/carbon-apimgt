package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GovernanceApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedListDTO;

import javax.ws.rs.core.Response;

/**
 * Implementation of the Unmanaged APIs (Governance / API Discovery) admin
 * REST resource.
 *
 * <p>This is a Round 7 scaffold that returns empty payloads so the build
 * passes and the auto-generated JAX-RS interface is satisfied. Round 8
 * replaces these stubs with real proxying to the API Discovery Server
 * (see claude/specs/phase4_admin_portal.md §4.1).</p>
 *
 * @since 4.7.0
 */
public class GovernanceApiServiceImpl implements GovernanceApiService {

    /**
     * GET /governance/discovery/summary — aggregate counts of unmanaged APIs.
     *
     * @param messageContext CXF message context
     * @return 200 with an empty DiscoverySummaryDTO (Round 7 stub)
     * @throws APIManagementException never in this stub
     */
    public final Response getDiscoverySummary(
            final MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(new DiscoverySummaryDTO()).build();
    }

    /**
     * GET /governance/discovery/apis — paginated list of unmanaged APIs.
     *
     * @param classification optional classification filter (shadow|drift)
     * @param service        optional service_identity filter
     * @param internal       optional internal-only flag
     * @param limit          pagination limit
     * @param offset         pagination offset
     * @param messageContext CXF message context
     * @return 200 with an empty DiscoveredAPIListDTO (Round 7 stub)
     * @throws APIManagementException never in this stub
     */
    public final Response getDiscoveredAPIs(final String classification,
                                            final String service,
                                            final String internal,
                                            final Integer limit,
                                            final Integer offset,
                                            final MessageContext messageContext)
            throws APIManagementException {
        return Response.ok().entity(new DiscoveredAPIListDTO()).build();
    }

    /**
     * GET /governance/discovery/apis/{discoveredApiId} — detail.
     *
     * @param discoveredApiId discovered API id
     * @param messageContext  CXF message context
     * @return 200 with a placeholder body (Round 7 stub)
     * @throws APIManagementException never in this stub
     */
    public final Response getDiscoveredAPIById(
            final String discoveredApiId,
            final MessageContext messageContext) throws APIManagementException {
        // Round 8 will load the row from the discovery server and return
        // a populated DiscoveredAPIDTO; for now an empty 200 keeps the
        // contract green.
        return Response.ok().build();
    }

    /**
     * GET /governance/discovery/untrafficked — managed APIs with no traffic.
     *
     * @param messageContext CXF message context
     * @return 200 with an empty UntraffickedListDTO (Round 7 stub)
     * @throws APIManagementException never in this stub
     */
    public final Response getUntrafficked(
            final MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(new UntraffickedListDTO()).build();
    }
}
