package org.wso2.carbon.apimgt.impl.Notification;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This class is used to send email notifications
 * email configurations should be saved in <APIM_HOME>/repository/conf/output-event-adapters.xml
 */
public class EmailNotifier extends Notifier {

    private static final Log log = LogFactory.getLog(EmailNotifier.class);
    private static final ArrayList<String> adapterList = new ArrayList<String>();


    @Override
    public void sendNotifications(NotificationDTO notification) throws APIManagementException {

        APIIdentifier api = (APIIdentifier) notification.getProperties().get(NotifierConstants.API_KEY);

        Set<Subscriber> subscriberList = getSubscriberList(api);
        Map<String, String> emailProperties = null;

        // Notifications are sent only if there is subscribers
        if (subscriberList.size() > 0) {

            Map<String, ArrayList<String>> notifierMap = getNotifierMap(subscriberList);
            notification.setNotifierMap(notifierMap);
            notification = loadMessageTemplate(notification);

            emailProperties = getEmailProperties(notification);
            if (emailProperties != null) {
                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                String adapterName = NotifierConstants.ADAPTER_NAME + tenantDomain;
                String message = notification.getMessage();
                try {

                    synchronized (EmailNotifier.class) {
                        if (!adapterList.contains(adapterName)) {
                            OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                                    createOutputEventAdapterConfiguration(adapterName, NotifierConstants
                                            .EMAIL_ADAPTER_TYPE);
                            ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create
                                    (outputEventAdapterConfiguration);
                            ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                                    .getOutputEventAdapterTypes();
                            adapterList.add(adapterName);
                        }
                    }

                    ServiceReferenceHolder.getInstance().getOutputEventAdapterService().publish(adapterName, emailProperties,message);

                    log.info("Notification sent to Email Adapter ");

                }  catch (OutputEventAdapterException e) {
                    throw new APIManagementException("Adapter Creation Failed ",e);
                }catch (NullPointerException e) {
                    throw new APIManagementException("Adapter Creation Failed ",e);
                }
            }else{
                log.info("Empty email list. Please set subscriber's email addresses");
            }
        }{
            if(log.isDebugEnabled()) {
                log.debug("No exiting Subscribers for " + api.getApiName() + api.getVersion());
            }
        }
    }

    @Override
    public Map<String, ArrayList<String>> getNotifierMap(Set<Subscriber> subscriberList) throws APIManagementException {

        String claimsRetrieverImplClass = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CLAIMS_RETRIEVER_IMPL_CLASS);
        ClaimsRetriever claimsRetriever = null;
        Map<String, ArrayList<String>> notifyMap = null;
        ArrayList<String> emailList = new ArrayList<String>();

        try {
            for (Subscriber subscriber : subscriberList) {

                String tenantUserName = subscriber.getName();
                claimsRetriever = (ClaimsRetriever) APIUtil.getClassForName(claimsRetrieverImplClass).newInstance();
                claimsRetriever.init();

                String email = claimsRetriever.getClaims(tenantUserName).get(NotifierConstants.EMAIL_CLAIM);

                if (email != null && !email.isEmpty()) {
                    emailList.add(email);
                }
            }

            notifyMap = new HashedMap();
            notifyMap.put(NotifierConstants.EMAIL_ADAPTER_TYPE, emailList);

        }  catch (IllegalAccessException e) {
            throw new APIManagementException("Error while retrieving Email Claims ",e);
        } catch (InstantiationException e) {
            throw new APIManagementException("Error while retrieving Email Claims ",e);
        } catch (ClassNotFoundException e) {
            throw new APIManagementException("Error while retrieving Email Claims ",e);
        } catch (NullPointerException e) {
            throw new APIManagementException("ClaimsRetrieverImplClass is not set in api-manager.xml ",e);
        }
        return notifyMap;
    }

    /**
     * returns a property map which is be used to create an Email event adapter
     * email addresses are extracted from notificationDTOaddeed and added as a String separated by commas
     * @param notificationDTO
     * @return Map contains email properties such as message, title ...
     */
    private Map<String, String> getEmailProperties(NotificationDTO notificationDTO) {

        Map<String, String> emailProperties = null;

        ArrayList<String> emailNotifierList = notificationDTO.getNotifierMap().get(NotifierConstants.EMAIL_ADAPTER_TYPE);

        if(emailNotifierList !=null && emailNotifierList.size()>0) {

            emailProperties = new HashMap<String, String>();

            // Adding emails as comma separated list
            String emailList = null;
            for (String email : emailNotifierList) {
                if (emailList == null) {
                    emailList = email;
                } else {
                    emailList = emailList + "," + email;
                }
            }

            emailProperties.put(NotifierConstants.EMAIL_ADDRESS_KEY, emailList);
            emailProperties.put(NotifierConstants.EMAIL_SUBJECT_KEY, notificationDTO.getTitle());
            emailProperties.put(NotifierConstants.EMAIL_TYPE_KEY, NotifierConstants.EMAIL_FORMAT_HTML);
        }
        return emailProperties;
    }

    /**
     * Creates output avent adapter Configurations
     * @param name      Output Event Adapter name
     * @param type      Output Event Adapter type
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private OutputEventAdapterConfiguration createOutputEventAdapterConfiguration(String name, String type) {

        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(name);
        outputEventAdapterConfiguration.setType(type);
        outputEventAdapterConfiguration.setMessageFormat(type);

        return outputEventAdapterConfiguration;
    }
}