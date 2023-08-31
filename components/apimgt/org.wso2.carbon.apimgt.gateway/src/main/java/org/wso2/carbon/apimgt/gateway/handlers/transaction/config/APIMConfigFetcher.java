package org.wso2.carbon.apimgt.gateway.handlers.transaction.config;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.TransactionCounterConstants;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.exception.TransactionCounterConfigurationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public class APIMConfigFetcher implements ConfigFetcher {

    private static APIMConfigFetcher instance = null;
    private final static HashMap<String, Object> configMap = new HashMap<>();

    private APIMConfigFetcher() throws TransactionCounterConfigurationException {
        try {
            Class<?> configClass = Class.forName(TransactionCounterConstants.APIM_CONFIG_CLASS);

            Object serviceReferenceHolder = configClass.getMethod("getInstance").invoke(null);
            Object apiManagerConfigurationService = configClass.getMethod("getAPIManagerConfigurationService")
                    .invoke(serviceReferenceHolder);
            Object apiManagerConfiguration = apiManagerConfigurationService.getClass()
                    .getMethod("getAPIManagerConfiguration").invoke(apiManagerConfigurationService);
            Method getFirstProperty = apiManagerConfiguration.getClass().getMethod("getFirstProperty",
                    String.class);

            // Reading the config values
            String  temp;

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_SERVER_ID);
            String SERVER_ID = Objects.requireNonNull( temp, "Server ID cannot be null");

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_STORE_CLASS);
            String TRANSACTION_COUNT_STORE_CLASS = Objects.requireNonNull(
                    temp, "Transaction count store class cannot be null");

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_QUEUE_SIZE);
            temp = Objects.requireNonNull(temp, "Transaction record queue size cannot be null");
            Integer TRANSACTION_RECORD_QUEUE_SIZE = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_PRODUCER_THREAD_POOL_SIZE);
            temp = Objects.requireNonNull(temp, "Producer thread pool size cannot be null");
            Integer PRODUCER_THREAD_POOL_SIZE = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_RECORD_INTERVAL);
            temp = Objects.requireNonNull(temp, "Transaction count record interval cannot be null");
            Integer TRANSACTION_COUNT_RECORD_INTERVAL = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_MAX_TRANSACTION_COUNT);
            temp = Objects.requireNonNull(temp, "Max transaction count cannot be null");
            Double MAX_TRANSACTION_COUNT = Double.parseDouble(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_MIN_TRANSACTION_COUNT);
            temp = Objects.requireNonNull(temp, "Min transaction count cannot be null");
            Double MIN_TRANSACTION_COUNT = Double.parseDouble(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_CONSUMER_COMMIT_INTERVAL);
            temp = Objects.requireNonNull(temp, "Consumer commit interval cannot be null");
            Integer CONSUMER_COMMIT_INTERVAL = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_MAX_TRANSACTION_RECORDS_PER_COMMIT);
            temp = Objects.requireNonNull(temp, "Max transaction records per commit cannot be null");
            Integer MAX_TRANSACTION_RECORDS_PER_COMMIT = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_MAX_RETRY_COUNT);
            temp = Objects.requireNonNull(temp, "Max retry count cannot be null");
            Integer MAX_RETRY_COUNT = Integer.parseInt(temp);

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_SERVICE);
            String TRANSACTION_COUNT_SERVICE = Objects.requireNonNull(temp,
                    "Transaction count service cannot be null");

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_SERVICE_USERNAME);
            String TRANSACTION_COUNT_SERVICE_USERNAME = Objects.requireNonNull(temp,
                    "Transaction count service username cannot be null");

            temp = (String) getFirstProperty.invoke(apiManagerConfiguration,
                    TransactionCounterConstants.GATEWAY_SERVICE_PASSWORD);
            String TRANSACTION_COUNT_SERVICE_PASSWORD = Objects.requireNonNull(temp,
                    "Transaction count service password cannot be null");

            configMap.put(TransactionCounterConstants.SERVER_ID, SERVER_ID);
            configMap.put(TransactionCounterConstants.TRANSACTION_COUNT_STORE_CLASS, TRANSACTION_COUNT_STORE_CLASS);
            configMap.put(TransactionCounterConstants.TRANSACTION_RECORD_QUEUE_SIZE, TRANSACTION_RECORD_QUEUE_SIZE);
            configMap.put(TransactionCounterConstants.PRODUCER_THREAD_POOL_SIZE, PRODUCER_THREAD_POOL_SIZE);
            configMap.put(TransactionCounterConstants.TRANSACTION_COUNT_RECORD_INTERVAL , TRANSACTION_COUNT_RECORD_INTERVAL);
            configMap.put(TransactionCounterConstants.MAX_TRANSACTION_COUNT, MAX_TRANSACTION_COUNT);
            configMap.put(TransactionCounterConstants.MIN_TRANSACTION_COUNT, MIN_TRANSACTION_COUNT);
            configMap.put(TransactionCounterConstants.CONSUMER_COMMIT_INTERVAL, CONSUMER_COMMIT_INTERVAL);
            configMap.put(TransactionCounterConstants.MAX_TRANSACTION_RECORDS_PER_COMMIT, MAX_TRANSACTION_RECORDS_PER_COMMIT);
            configMap.put(TransactionCounterConstants.MAX_RETRY_COUNT, MAX_RETRY_COUNT);
            configMap.put(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE, TRANSACTION_COUNT_SERVICE);
            configMap.put(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE_USERNAME, TRANSACTION_COUNT_SERVICE_USERNAME);
            configMap.put(TransactionCounterConstants.TRANSACTION_COUNT_SERVICE_PASSWORD, TRANSACTION_COUNT_SERVICE_PASSWORD);

        } catch (ClassNotFoundException e) {
            // This error won't be thrown here because it is already checked in TransactionCountConfig
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new TransactionCounterConfigurationException();
        } catch (NumberFormatException | NullPointerException e) {
            throw new TransactionCounterConfigurationException("Error while reading the config values", e);
        }
    }

    public static ConfigFetcher getInstance() throws TransactionCounterConfigurationException {
        if (instance == null) {
            instance = new APIMConfigFetcher();
        }
        return instance;
    }

    @Override
    public String getConfigValue(String key) {
        return configMap.get(key).toString();
    }

}
