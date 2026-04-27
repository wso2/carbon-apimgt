package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIServiceManagedAPIsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByReachabilityDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByServiceDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryByTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedAPIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryDetailWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListItemWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoverySummaryWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.UntraffickedListWire;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure-function translators between the Discovery Server wire format
 * and the auto-generated REST DTOs.
 *
 * <p>Field-name translation: wire is snake_case, DTOs are camelCase.
 * Some lists/numerical types differ subtly (e.g., wire status_codes is
 * Integer[]; DTO has it as List&lt;Integer&gt;) — these helpers normalize.</p>
 *
 * @since 4.7.0
 */
public final class DiscoveryMappingUtil {

    private DiscoveryMappingUtil() {
        // static helpers only
    }

    /**
     * Convert a wire summary into the public REST DTO.
     *
     * @param wire wire payload, non-null
     * @return populated DiscoverySummaryDTO
     */
    public static DiscoverySummaryDTO fromWireSummary(final DiscoverySummaryWire wire) {
        final DiscoverySummaryDTO dto = new DiscoverySummaryDTO();
        dto.setTotal((int) wire.getTotal());
        dto.setManaged((int) wire.getManaged());
        dto.setUnmanaged((int) wire.getUnmanaged());
        dto.setSkipInternal(wire.isSkipInternal());

        if (wire.getByType() != null) {
            final DiscoverySummaryByTypeDTO byType = new DiscoverySummaryByTypeDTO();
            byType.setShadow((int) wire.getByType().getShadow());
            byType.setDrift((int) wire.getByType().getDrift());
            dto.setByType(byType);
        }
        if (wire.getByReachability() != null) {
            final DiscoverySummaryByReachabilityDTO byReach =
                    new DiscoverySummaryByReachabilityDTO();
            byReach.setExternal((int) wire.getByReachability().getExternal());
            byReach.setInternal((int) wire.getByReachability().getInternal());
            dto.setByReachability(byReach);
        }
        if (wire.getByService() != null) {
            final List<DiscoverySummaryByServiceDTO> byService = new ArrayList<>();
            for (DiscoverySummaryWire.ByServiceEntry e : wire.getByService()) {
                final DiscoverySummaryByServiceDTO s = new DiscoverySummaryByServiceDTO();
                s.setServiceIdentity(e.getServiceIdentity());
                s.setFullyGoverned(e.isFullyGoverned());
                s.setShadow((int) e.getShadow());
                s.setDrift((int) e.getDrift());
                byService.add(s);
            }
            dto.setByService(byService);
        }
        return dto;
    }

    /**
     * Convert a wire list response into the paginated REST DTO.
     *
     * @param wire wire payload, non-null
     * @return populated DiscoveredAPIListDTO
     */
    public static DiscoveredAPIListDTO fromWireList(final DiscoveryListWire wire) {
        final DiscoveredAPIListDTO dto = new DiscoveredAPIListDTO();
        dto.setCount(wire.getCount());
        final List<DiscoveredAPIDTO> items = new ArrayList<>();
        if (wire.getList() != null) {
            for (DiscoveryListItemWire item : wire.getList()) {
                items.add(fromWireListItem(item));
            }
        }
        dto.setList(items);
        if (wire.getPagination() != null) {
            final PaginationDTO p = new PaginationDTO();
            p.setOffset(wire.getPagination().getOffset());
            p.setLimit(wire.getPagination().getLimit());
            p.setTotal(wire.getPagination().getTotal());
            dto.setPagination(p);
        }
        return dto;
    }

    /**
     * Convert a wire detail row into the REST DTO.
     *
     * @param wire wire payload, non-null
     * @return populated DiscoveredAPIDTO
     */
    public static DiscoveredAPIDTO fromWireDetail(final DiscoveryDetailWire wire) {
        final DiscoveredAPIDTO dto = new DiscoveredAPIDTO();
        dto.setId(wire.getId());
        dto.setServiceIdentity(wire.getServiceIdentity());
        dto.setEnvKind(toEnvKindEnum(wire.getEnvKind()));
        dto.setNamespace(wire.getNamespace());
        dto.setServiceName(wire.getServiceName());
        dto.setSamplePod(wire.getSamplePod());
        dto.setSampleWorkload(wire.getSampleWorkload());
        dto.setMethod(wire.getMethod());
        dto.setNormalizedPath(wire.getNormalizedPath());
        dto.setRawPathSamples(wire.getRawPathSamples() != null
                ? wire.getRawPathSamples() : new ArrayList<>());
        dto.setClassification(toClassificationEnum(wire.getClassification()));
        dto.setIsInternal(wire.isInternal());
        dto.setFirstSeenAt(wire.getFirstSeenAt());
        dto.setLastSeenAt(wire.getLastSeenAt());
        dto.setObservationCount(wire.getObservationCount());
        dto.setDistinctClientCount(wire.getDistinctClientCount());
        dto.setDistinctClientsSample(wire.getDistinctClientsSample() != null
                ? wire.getDistinctClientsSample() : new ArrayList<>());
        dto.setStatusCodes(wire.getStatusCodes() != null
                ? wire.getStatusCodes() : new ArrayList<>());
        dto.setAvgDurationUs(BigDecimal.valueOf(wire.getAvgDurationUs()));
        dto.setMatchedApimApiIds(wire.getMatchedApimApiIds() != null
                ? wire.getMatchedApimApiIds() : new ArrayList<>());

        final List<DiscoveredAPIServiceManagedAPIsDTO> svcManaged = new ArrayList<>();
        if (wire.getServiceManagedAPIs() != null) {
            for (DiscoveryDetailWire.APIRef ref : wire.getServiceManagedAPIs()) {
                final DiscoveredAPIServiceManagedAPIsDTO r =
                        new DiscoveredAPIServiceManagedAPIsDTO();
                r.setApimApiId(ref.getApimApiId());
                r.setApimApiName(ref.getApimApiName());
                r.setApimApiVersion(ref.getApimApiVersion());
                svcManaged.add(r);
            }
        }
        dto.setServiceManagedAPIs(svcManaged);
        return dto;
    }

    /**
     * Convert a wire untrafficked list into the REST DTO.
     *
     * @param wire wire payload, non-null
     * @return populated UntraffickedListDTO
     */
    public static UntraffickedListDTO fromWireUntrafficked(
            final UntraffickedListWire wire) {
        final UntraffickedListDTO dto = new UntraffickedListDTO();
        dto.setCount(wire.getCount());
        final List<UntraffickedAPIDTO> items = new ArrayList<>();
        if (wire.getList() != null) {
            for (UntraffickedListWire.UntraffickedItemWire item : wire.getList()) {
                final UntraffickedAPIDTO d = new UntraffickedAPIDTO();
                d.setApimApiId(item.getApimApiId());
                d.setApimApiName(item.getApimApiName());
                d.setApimApiVersion(item.getApimApiVersion());
                d.setMethod(item.getMethod());
                d.setGatewayPath(item.getGatewayPath());
                d.setServiceIdentity(item.getServiceIdentity());
                d.setLastSyncedAt(item.getLastSyncedAt());
                items.add(d);
            }
        }
        dto.setList(items);
        return dto;
    }

    private static DiscoveredAPIDTO fromWireListItem(final DiscoveryListItemWire wire) {
        final DiscoveredAPIDTO dto = new DiscoveredAPIDTO();
        dto.setId(wire.getId());
        dto.setServiceIdentity(wire.getServiceIdentity());
        dto.setEnvKind(toEnvKindEnum(wire.getEnvKind()));
        dto.setMethod(wire.getMethod());
        dto.setNormalizedPath(wire.getNormalizedPath());
        dto.setClassification(toClassificationEnum(wire.getClassification()));
        dto.setIsInternal(wire.isInternal());
        dto.setObservationCount(wire.getObservationCount());
        dto.setDistinctClientCount(wire.getDistinctClientCount());
        dto.setLastSeenAt(wire.getLastSeenAt());
        dto.setMatchedApimApiIds(wire.getMatchedApimApiIds() != null
                ? wire.getMatchedApimApiIds() : new ArrayList<>());
        return dto;
    }

    private static DiscoveredAPIDTO.EnvKindEnum toEnvKindEnum(final String wire) {
        if (wire == null) {
            return null;
        }
        switch (wire) {
            case "k8s":     return DiscoveredAPIDTO.EnvKindEnum.K8S;
            case "legacy":  return DiscoveredAPIDTO.EnvKindEnum.LEGACY;
            case "unknown": return DiscoveredAPIDTO.EnvKindEnum.UNKNOWN;
            default:        return null;
        }
    }

    private static DiscoveredAPIDTO.ClassificationEnum toClassificationEnum(
            final String wire) {
        if (wire == null) {
            return null;
        }
        switch (wire) {
            case "shadow": return DiscoveredAPIDTO.ClassificationEnum.SHADOW;
            case "drift":  return DiscoveredAPIDTO.ClassificationEnum.DRIFT;
            default:       return null;
        }
    }
}
