package org.wso2.carbon.apimgt.gatewaybridge.listeners;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.WebhookSubscriptionGetService;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.WebhookSubscriptionGetServiceImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.DBRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Class for listening to the JMS Events for a specific topic.
 */
public class JMSEventListener implements MessageListener {
    private static final Log log = LogFactory.getLog(JMSEventListener.class);

    ArtifactRetriever artifactRetriever = new DBRetriever();
    List<WebhookSubscriptionDTO> subscriptionsList = new ArrayList<>();
    /**
     * This constructs a JMSEventListener instance.
     */
    public JMSEventListener() {
      try {
          if (log.isDebugEnabled()) {
              log.debug("Called JMSEventListener");
          }
          this.setSubscriber();
      } catch (NamingException | JMSException | InterruptedException e) {
          log.debug("Unexpected Error: " + e);
      }

    }

    /**
     * Listens to JMS events
     * This method retrieves the events published to a specific topic.
     * Retrieved message is decoded and extract
     * the event details.
     * <p>
     * This method in retrieves the gateway run time artifacts
     *
     * @param message the JMS message received from the topic
     */
    public void onMessage(Message message) {

        log.debug("Event Received: " + message);
        WebhookSubscriptionGetService webhookSubscriptionGetService;
        try {
            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                Map<String, Object> map = new HashMap<>();
                Enumeration enumeration = mapMessage.getMapNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    map.put(key, mapMessage.getObject(key));
                }
                byte[] eventDecoded = Base64.decodeBase64((String) map.get(APIConstants.EVENT_PAYLOAD));
                DeployAPIInGatewayEvent gatewayEvent = new Gson().
                        fromJson(new String(eventDecoded , "UTF8") , DeployAPIInGatewayEvent.class);




                if ((APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(map.get(APIConstants.EVENT_TYPE)))) {

                    log.debug("GatewayLabels" + gatewayEvent.getGatewayLabels());

                    String gatewayLabel = gatewayEvent.getGatewayLabels().iterator().next();
                    String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(Integer.toString(gatewayEvent.
                            getApiId()), gatewayLabel);
                    if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                        GatewayAPIDTO gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                        log.debug("GatewayAPIDTO Name       :" + gatewayAPIDTO.getName());

                        webhookSubscriptionGetService = new WebhookSubscriptionGetServiceImpl();
                        subscriptionsList = webhookSubscriptionGetService.getWebhookSubscription(gatewayLabel);
                    }

                }

            }
        } catch (JMSException | UnsupportedEncodingException |
                APIManagementException | ArtifactSynchronizerException e) {
            log.debug("Exception" + e);
        }
    }

    /**
     * Subscribe to a topic at the startup
     * Creates a non-durable topic consumer.
     * <p>
     * This method subscribe to a created topic and
     * listens to the topic.
     *
     * @throws NamingException      If an authentication error occurs while accessing the JNDI naming service.
     * @throws JMSException         If a JMS services error occurs while using JMS service.
     * @throws InterruptedException If an error occurs while executing the thread.
     */
    public void setSubscriber() throws NamingException, JMSException, InterruptedException {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        properties.setProperty("connectionfactory.TopicConnectionFactory",
                "amqp://admin:admin@clientid/carbon?brokerlist='tcp://localhost:5672?retries='5'%26connectdelay='50';" +
                        "tcp://localhost:5672?retries='5'%26connectdelay='50';'");
        properties.setProperty("topic.notification", "notification");
        Context context = new InitialContext(properties);

        ConnectionFactory connectionFactory
                = (ConnectionFactory) context.lookup(APIConstants.TOPIC_CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection("admin", "admin");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topicLabel = (Topic) context.lookup(APIConstants.TopicNames.TOPIC_NOTIFICATION);

        MessageConsumer subscriber1 = ((TopicSession) session).createSubscriber(topicLabel);

        subscriber1.setMessageListener(this);

        log.debug("Listening to the Topic" + topicLabel);
        Thread.sleep(10000);


    }
}
