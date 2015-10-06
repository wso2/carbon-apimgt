package org.wso2.carbon.apimgt.usage.client;

import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;

import java.util.List;

/**
 * Created by rukshan on 10/6/15.
 */
public interface APIUsageStatisticsClientI {
    List<APIUsageDTO> perAppPerAPIUsage(String subscriberName, String groupId, String fromDate, String toDate,
            int limit)
            throws APIMgtUsageQueryServiceClientException;

    List<AppUsageDTO> getTopAppUsers(String subscriberName, String groupId, String fromDate, String toDate, int limit)
                    throws APIMgtUsageQueryServiceClientException;

    List<AppCallTypeDTO> getAppApiCallType(String subscriberName, String groupId, String fromDate, String toDate,
            int limit)
                            throws APIMgtUsageQueryServiceClientException;

    List<APIResponseFaultCountDTO> getPerAppFaultCount(String subscriberName, String groupId, String fromDate,
            String toDate, int limit)
                                    throws APIMgtUsageQueryServiceClientException;

    List<APIUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate)
                                            throws APIMgtUsageQueryServiceClientException;

    List<APIResponseTimeDTO> getResponseTimesByAPIs(String providerName, String fromDate, String toDate, int limit)
                                                    throws APIMgtUsageQueryServiceClientException;

    List<APIVersionLastAccessTimeDTO> getLastAccessTimesByAPI(String providerName, String fromDate, String toDate,
            int limit)
                                                            throws APIMgtUsageQueryServiceClientException;

    List<APIResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate)
                                                                    throws APIMgtUsageQueryServiceClientException;

    List<APIDestinationUsageDTO> getAPIUsageByDestination(String providerName, String fromDate, String toDate)
                                                                            throws APIMgtUsageQueryServiceClientException;

    List<APIUsageDTO> getUsageByAPIs(String providerName, String fromDate, String toDate, int limit)
                                                                                    throws APIMgtUsageQueryServiceClientException;

    List<APIResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate, String toDate)
                                                                                            throws APIMgtUsageQueryServiceClientException;
}
