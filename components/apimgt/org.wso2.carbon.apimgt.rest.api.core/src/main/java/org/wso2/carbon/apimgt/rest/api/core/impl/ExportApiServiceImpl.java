package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.APIThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.ApplicationThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.CustomThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.SubscriptionThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.msf4j.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * This class contains the implementation of export policies api.
 */
public class ExportApiServiceImpl extends ExportApiService {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);
    private static final String APPLICATION = "application_";
    private static final String SUBSCRIPTION = "subscription_";
    private static final String RESOURCE = "resource_";
    private static final String DEFAULT = "_default";
    private static final String CUSTOM = "custom_";
    private static final String ZIP = ".zip";

    /**
     * Export throttle policies containing zip.
     *
     * @param accept  Accept header value
     * @param request msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response exportPoliciesThrottleGet(String accept, Request request) throws NotFoundException {

        String archiveName = "exported-policies";
        //files will be written to following directory
        String exportedPoliciesDirName = "exported-policies";
        //archive will be here at following location tmp directory
        String archiveDir = System.getProperty("java.io.tmpdir");

        if (log.isDebugEnabled()) {
            log.debug("Received export policies GET request ");
        }
        try {
            //retrieve all policies under each policy level
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            List<APIPolicy> apiPolicies = apiMgtAdminService.getApiPolicies();
            List<ApplicationPolicy> applicationPolicies = apiMgtAdminService.getApplicationPolicies();
            List<SubscriptionPolicy> subscriptionPolicies = apiMgtAdminService.getSubscriptionPolicies();
            List<CustomPolicy> customPolicies = apiMgtAdminService.getCustomRules();
            //write all execution Plans/Siddhi Apps to exportPoliciesDirName directory
            APIFileUtils.createDirectory(exportedPoliciesDirName);
            if (!apiPolicies.isEmpty()) {
                for (Map<String, String> map : getApiPolicySiddhiApps(apiPolicies)) {
                    prepareFile(exportedPoliciesDirName, map);
                }
            }
            if (!applicationPolicies.isEmpty()) {
                prepareFile(exportedPoliciesDirName, getAppPolicySiddhiApps(applicationPolicies));
            }
            if (!subscriptionPolicies.isEmpty()) {
                prepareFile(exportedPoliciesDirName, getSubscriptionPolicySiddhiApps(subscriptionPolicies));
            }
            if (!customPolicies.isEmpty()) {
                prepareFile(exportedPoliciesDirName, getCustomPolicySiddhiApps(customPolicies));
            }
            //create archive and get the archive location
            String zippedFilePath = createArchiveFromPolicies(exportedPoliciesDirName, archiveDir, archiveName);
            APIFileUtils.deleteDirectory(exportedPoliciesDirName);
            File exportedApiArchiveFile = new File(zippedFilePath);
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK);
            responseBuilder
                    .header("Content-Disposition", "attachment; filename=\"" + exportedApiArchiveFile.getName() + "\"");
            Response response = responseBuilder.build();
            return response;
        } catch (APIManagementException e) {
            String errorMessage = "Error while exporting policies";
            log.error(errorMessage, e);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Create Archive from policies.
     *
     * @param sourceDir   Source directory containing execution plans
     * @param archiveDir  Archive containing directory
     * @param archiveName name of the archive
     * @return String object of the file path
     * @throws APIMgtDAOException if creating archive failed
     */
    private String createArchiveFromPolicies(String sourceDir, String archiveDir, String archiveName)
            throws APIMgtDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Creating archive for execution plans.");
        }
        APIFileUtils.archiveDirectory(sourceDir, archiveDir, archiveName);
        String archivePath = archiveDir + File.separator + archiveName + ZIP;
        log.info("Created archive for execution plans: " + archivePath);
        return archivePath;
    }

    /**
     * write execution plan to a file.
     *
     * @param exportDir location contains the files
     * @param policies  Map of execution plan names and execution plans
     */
    private void prepareFile(String exportDir, Map<String, String> policies) {
        if (log.isDebugEnabled()) {
            log.debug("Writing files for execution plans.");
        }
        policies.forEach((name, siddhiApp) -> {
            try {
                APIFileUtils.createFile(exportDir + File.separator + name);
                APIFileUtils.writeToFile(exportDir + File.separator + name, siddhiApp);
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error while preparing the file with policy name: " + name;
                log.error(errorMsg, e);
            }
        });
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param apiPolicies APIPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private List<Map<String, String>> getApiPolicySiddhiApps(List<APIPolicy> apiPolicies) throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for API policies.");
        }
        List<Map<String, String>> policies = new ArrayList<>();
        Map<String, String> siddhiApps = new HashMap<>();
        APIThrottlePolicyTemplateBuilder templateBuilder;
        for (APIPolicy apiPolicy : apiPolicies) {
            templateBuilder = new APIThrottlePolicyTemplateBuilder(apiPolicy);
            if (apiPolicy.getPipelines() != null) {
                siddhiApps = templateBuilder.getThrottlePolicyTemplateForPipelines();
            }
            siddhiApps.put(RESOURCE + apiPolicy.getPolicyName() + DEFAULT,
                    templateBuilder.getThrottlePolicyTemplateForAPILevelDefaultCondition());
            policies.add(siddhiApps);
        }
        return policies;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param applicationPolicies ApplicationPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getAppPolicySiddhiApps(List<ApplicationPolicy> applicationPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for application policy.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        ApplicationThrottlePolicyTemplateBuilder templateBuilder;
        for (ApplicationPolicy policy : applicationPolicies) {
            templateBuilder = new ApplicationThrottlePolicyTemplateBuilder(policy);
            name = APPLICATION + policy.getPolicyName();
            executionPlan = templateBuilder.getThrottlePolicyForAppLevel();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param subscriptionPolicies SubscriptionPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getSubscriptionPolicySiddhiApps(List<SubscriptionPolicy> subscriptionPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for Subscription policies.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        SubscriptionThrottlePolicyTemplateBuilder templateBuilder;
        for (SubscriptionPolicy policy : subscriptionPolicies) {
            name = SUBSCRIPTION + policy.getPolicyName();
            templateBuilder = new SubscriptionThrottlePolicyTemplateBuilder(policy);
            executionPlan = templateBuilder.getThrottlePolicyForSubscriptionLevel();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param customPolicies custom policy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getCustomPolicySiddhiApps(List<CustomPolicy> customPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for custom policies.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        CustomThrottlePolicyTemplateBuilder templateBuilder;
        for (CustomPolicy policy : customPolicies) {
            templateBuilder = new CustomThrottlePolicyTemplateBuilder(policy);
            name = CUSTOM + policy.getPolicyName();
            executionPlan = templateBuilder.getThrottlePolicyTemplateForCustomPolicy();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }
}
