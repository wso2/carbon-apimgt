package org.wso2.carbon.apimgt.governance.impl.client.apim;

import feign.Feign;
import feign.Response;
import feign.gson.GsonDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.config.dto.APIMConfigurationDTO;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * APIM client to interact with the APIM service.
 */
public class APIM {

    private static final Log log = LogFactory.getLog(APIM.class);

    private static String endpoint;
    private static int workerCount;
    private static int maxRetries;
    private static int retryDelay;

    private static APIMClient client;
    private static ExecutorService executor;

    /**
     * Initialize the APIM configuration from the service.
     */
    public static void init() {
        APIMConfigurationDTO apimConfig = ServiceReferenceHolder.getInstance()
                .getGovernanceConfigurationService().getGovernanceConfiguration().getAPIMConfiguration();

        if (apimConfig != null) {
            endpoint = apimConfig.getEndPointUrl();
            workerCount = apimConfig.getWorkerCount() != null
                    ? apimConfig.getWorkerCount()
                    : GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_WORKER_COUNT;
            maxRetries = apimConfig.getMaxRetries() != null
                    ? apimConfig.getMaxRetries()
                    : GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_MAX_RETRIES;
            retryDelay = apimConfig.getRetryInterval() != null
                    ? apimConfig.getRetryInterval()
                    : GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_RETRY_DELAY;
        } else {
            log.warn("APIM Configuration is not available. Using default values.");
            workerCount = GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_WORKER_COUNT;
            maxRetries = GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_MAX_RETRIES;
            retryDelay = GovernanceConstants.APIMConfigurations.APIM_DEFAULT_CLIENT_RETRY_DELAY;
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = Executors.newFixedThreadPool(workerCount);
    }

    /**
     * Lazily initialize and retrieve the APIM client.
     *
     * @return APIMClient instance
     * @throws GovernanceException if the endpoint URL is not configured
     */
    private static synchronized APIMClient getClient() throws GovernanceException {
        if (client == null) {
            if (endpoint == null) {
                throw new GovernanceException("APIM endpoint URL is not configured.");
            }

            client = Feign.builder()
                    .decoder(new GsonDecoder())
                    .errorDecoder(new APIMClientErrorDecoder())
                    .target(APIMClient.class, endpoint);

            log.info("APIM Client successfully initialized.");
        }
        return client;
    }


    /**
     * Get the APIM project for the given API ID.
     *
     * @param apiId      API ID
     * @param authHeader Authorization header
     * @return APIM project as an InputStream
     */
    public static InputStream getAPIMProject(String apiId, String authHeader) {
        AtomicReference<InputStream> resp = new AtomicReference<>();

        executeWithRetry(
                client -> {
                    try {
                        // Execute the client request and get the response
                        Response response = client.getAPIMProject(apiId, authHeader, GovernanceConstants.YAML);
                        if (response != null && HttpStatus.SC_OK == response.status()) {
                            resp.set(response.body().asInputStream());
                            return true; // Operation succeeded
                        }
                    } catch (GovernanceException | IOException e) {
                        log.error("Error while exporting the API with id: " + apiId + ". " +
                                "Details: " + e.getMessage());
                    }
                    return false; // Operation failed
                },
                "Error while exporting the API with id: " + apiId + " from APIM"
        );

        return resp.get();


    }


    /**
     * Execute the given operation with retry.
     *
     * @param operation    Operation to execute
     * @param errorMessage Error message to log
     */
    private static void executeWithRetry(
            Function<APIMClient, Boolean> operation,
            String errorMessage
    ) {
        APIMClient client;
        try {
            client = getClient();
        } catch (GovernanceException e) {
            log.error("Error while getting APIM client", e);
            return;
        }
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Boolean result = operation.apply(client);
                if (result) {
                    return;
                }
            } catch (RuntimeException e) {
                log.error(errorMessage + " Attempt " + attempt + " failed: " + e.getMessage());
            }
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelay); // Wait before retrying
                } catch (InterruptedException ie) {
                    log.error("Thread was interrupted while waiting to retry.", ie);
                    Thread.currentThread().interrupt();
                    return;
                }
            } else {
                log.error("Max retry attempts reached. " + errorMessage);
            }
        }
    }
}
