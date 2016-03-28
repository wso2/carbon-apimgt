package org.wso2.carbon.apimgt.impl.Notification;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Sam on 2/12/16.
 */
public abstract class Notifier {

    /**
     * Sends notifications implement your own logic to send the notifications
     * @param notification
     * @throws APIManagementException
     */
    public abstract void sendNotifications(NotificationDTO notification) throws APIManagementException;

    /**
     * Should return a map with notiification type as key and notifier list as value
     * @param subscriberList
     * @return
     * @throws APIManagementException
     */
    public abstract Map<String,ArrayList<String>> getNotifierMap(Set<Subscriber> subscriberList)
            throws APIManagementException;

    /**
     * Returns a SET of subscribers for the passed API
     * @param api
     * @return
     * @throws APIManagementException
     */
    public Set<Subscriber> getSubscriberList(APIIdentifier api) throws APIManagementException {

        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        return dao.getSubscribersOfAPI(api);
    }


    /**
     * Retrieves the message configurations from tenant-config.json and sets the notification properties to NotificationDTO
     * @param notificationDTO
     * @return
     * @throws APIManagementException
     */
    public NotificationDTO loadMessageTemplate(NotificationDTO notificationDTO) throws APIManagementException {

        String content=null;
        JSONObject apiTenantConfig;

        try {

            APIIdentifier api = (APIIdentifier) notificationDTO.getProperties().get(NotifierConstants.API_KEY);
            if(NotifierConstants.NEW_API_KEY.equalsIgnoreCase(notificationDTO.getType())){
                api = (APIIdentifier) notificationDTO.getProperties().get(NotifierConstants.NEW_API_KEY);
            }
            String notificationType=notificationDTO.getType();

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId
                    (tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry
                    (tenantId);

            if (registry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TENANT_CONF_LOCATION);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }

            if (content != null) {

                JSONParser parser = new JSONParser();
                apiTenantConfig = (JSONObject) parser.parse(content);
                apiTenantConfig = (JSONObject) apiTenantConfig.get(NotifierConstants.Notifications_KEY);

                JSONArray notificationArray = (JSONArray) apiTenantConfig.get(NotifierConstants.Notification_KEY);

                for (Object obj : notificationArray) {

                    JSONObject object = (JSONObject) obj;

                    try {
                        String type = object.get(NotifierConstants.TYPE_KEY).toString();
                        String title = object.get(NotifierConstants.TITLE_KEY).toString();
                        String message = object.get(NotifierConstants.MESSAGE_KEY).toString();

                        // String replacement
                        title = title.replaceAll("\\$1", api.getApiName());
                        title = title.replaceAll("\\$2", api.getVersion());

                        message = message.replaceAll("\\$1", api.getApiName());
                        message = message.replaceAll("\\$2", api.getVersion());

                        if (notificationType.equalsIgnoreCase(type)) {

                            notificationDTO.setTitle( title);
                            notificationDTO.setMessage(message);
                            return notificationDTO;
                        }
                    } catch (NullPointerException e) {
                        throw new APIManagementException("Invalid Content in tenant-config.json ",e);
                    }
                }

            } else {
                throw new APIManagementException("No content in tenant-config.json");
            }

        } catch (NullPointerException e) {
            throw new APIManagementException("Error while Reading tenant-conf.json ", e);
        } catch (ParseException e) {
            throw new APIManagementException("Error while passing the content to JSON ", e);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while getting tenant ID ", e);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while getting the registry resource ", e);
        }

        return notificationDTO;
    }
}


