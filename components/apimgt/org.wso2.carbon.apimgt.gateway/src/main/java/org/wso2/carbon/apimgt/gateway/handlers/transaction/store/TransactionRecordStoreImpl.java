package org.wso2.carbon.apimgt.gateway.handlers.transaction.store;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.config.TransactionCounterConfig;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.record.TransactionRecord;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class TransactionRecordStoreImpl implements TransactionRecordStore {

    private static final Log LOG = LogFactory.getLog(TransactionRecordStoreImpl.class);
    private static HttpClient httpClient;
    private static String ENDPOINT;
    private static String encodedCredentials;

    @Override
    public void init(String endpoint, String username, String password) {
        
        ENDPOINT = endpoint;
        String credentials = username + ":" + password;
        encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        URL url = null;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());
    }

    @Override
    public boolean commit(ArrayList<TransactionRecord> transactionRecordList, int maxRetryCount) {

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(transactionRecordList);

        HttpPost httpPost = new HttpPost(ENDPOINT);
        HttpEntity stringEntity = new StringEntity(jsonPayload , ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);

        int retryCount = 0;
        boolean retry;
        do {
            try {
                retry = false;
                HttpResponse result = httpClient.execute(httpPost);
                int statusCode = result.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("Status Code: " + statusCode);
                }
            } catch (IOException ex) {
                retryCount++;
                if (retryCount < maxRetryCount) {
                    retry = true;
                    LOG.warn("Failed to persist transaction count records to remote endpoint. Retrying after 1s");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error("Could not persist following transaction count records: " + jsonPayload, e);
                    }
                } else {
                    LOG.error("Could not persist transaction count records. Added back to the queue. Error: " +
                            ex.getMessage(), ex);
                    return false;
                }
            }
        } while (retry);

        return true;
    }

    @Override
    public void clenUp() {
        // To be implemented
    }
}
