package org.wso2.carbon.apimgt.gatewaybridge.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.WebhookSubscriptionGetService;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.WebhookSubscriptionGetServiceImpl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deploy apis in external gateways.
 */
public class APIDeployerImpl implements APIDeployer {
    List<WebhookSubscriptionDTO> subscriptionsList = new ArrayList<>();
    private static final Log log = LogFactory.getLog(APIDeployerImpl.class);


    /**
     * Sending gateway artifacts to subscribed gateways.
     * Retrieves subscription list from database and
     * send webhooks.
     * @param gatewayAPIDTO     the API DTO contains API details
     * @param topic             the topic subscribed
     */
    @Override
    public void deployArtifacts(GatewayAPIDTO gatewayAPIDTO, String topic) throws Exception {

        WebhookSubscriptionGetService webhookSubscriptionGetService = new WebhookSubscriptionGetServiceImpl();
        subscriptionsList = webhookSubscriptionGetService.getWebhookSubscription(topic);
        Iterator<WebhookSubscriptionDTO> iterator = subscriptionsList.iterator();
        HttpPost post;

        while (iterator.hasNext()) {

            post = new HttpPost(iterator.next().getCallback());

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("name", gatewayAPIDTO.getName()));
            urlParameters.add(new BasicNameValuePair("version", gatewayAPIDTO.getVersion()));
            urlParameters.add(new BasicNameValuePair("provider", gatewayAPIDTO.getProvider()));
            urlParameters.add(new BasicNameValuePair("apiId", gatewayAPIDTO.getApiId()));
            urlParameters.add(new BasicNameValuePair("apiDefinition", gatewayAPIDTO.getApiDefinition()));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                log.debug(EntityUtils.toString(response.getEntity()));
            }

        }
    }

    @Override
    public void unDeployArtifacts(String artifactName) {

    }


}
