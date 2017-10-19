/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class is used to send email notifications
 * email configurations should be saved in <APIM_HOME>/repository/conf/output-event-adapters.xml
 */
public class NewAPIVersionEmailNotifier extends Notifier {

    private static final Log log = LogFactory.getLog(NewAPIVersionEmailNotifier.class);
    private static final ArrayList<String> adapterList = new ArrayList<String>();


    @Override
    public void sendNotifications(NotificationDTO notificationDTO) throws NotificationException {

        APIIdentifier api = (APIIdentifier) notificationDTO.getProperties().get(NotifierConstants.API_KEY);

        Set<Subscriber> subscriberList = (Set<Subscriber>) notificationDTO.getProperty(NotifierConstants
                .SUBSCRIBERS_PER_API);
        Map<String, String> emailProperties = null;

        // Notifications are sent only if there are subscribers
        if (subscriberList.size() > 0) {

            Set<String> notifierSet=getNotifierSet(notificationDTO);
            notificationDTO.setNotifierSet(notifierSet);
            notificationDTO = loadMessageTemplate(notificationDTO);

            emailProperties = getEmailProperties(notificationDTO);
            if (emailProperties != null) {
                String tenantDomain = getTenantDomain();
                String adapterName = NotifierConstants.ADAPTER_NAME + tenantDomain;
                String message = notificationDTO.getMessage();
                try {

                    synchronized (Notifier.class) {
                        if (!adapterList.contains(adapterName)) {
                            OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                                    createOutputEventAdapterConfiguration(adapterName, NotifierConstants
                                            .EMAIL_ADAPTER_TYPE);
                            createOutputEventAdapterService(outputEventAdapterConfiguration);
                            getOutputEventAdapterTypes();
                            adapterList.add(adapterName);
                        }
                    }

                    publishNotification(emailProperties, adapterName, message);

                    log.info("notification sent to Email Adapter ");

                } catch (OutputEventAdapterException e) {
                    throw new NotificationException("Adapter Creation Failed ", e);
                }
            } else {
                log.info("Empty email list. Please set subscriber's email addresses");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No exiting Subscribers to send notifications for " + api.getApiName() + api.getVersion());
            }
        }
    }

    /**
     *
     * @param notificationDTO
     * @return a set of email ids
     * @throws NotificationException
     */
    public Set<String> getNotifierSet(NotificationDTO notificationDTO) throws NotificationException {

        Set<Subscriber> subscriberList = (Set<Subscriber>) notificationDTO.getProperty(NotifierConstants
                .SUBSCRIBERS_PER_API);

        String claimsRetrieverImplClass = (String) notificationDTO.getProperty(NotifierConstants
                .CLAIMS_RETRIEVER_IMPL_CLASS);
        ClaimsRetriever claimsRetriever = null;
        Set<String> emailset = new HashSet<String>();

        try {

            for (Subscriber subscriber : subscriberList) {
                String tenantUserName = subscriber.getName();
                claimsRetriever = getClaimsRetriever(claimsRetrieverImplClass);
                claimsRetriever.init();
                String email = claimsRetriever.getClaims(tenantUserName).get(NotifierConstants.EMAIL_CLAIM);

                if (email != null && !email.isEmpty()) {
                    emailset.add(email);
                }
            }

        } catch (IllegalAccessException e) {
            throw new NotificationException("Error while retrieving Email Claims ", e);
        } catch (InstantiationException e) {
            throw new NotificationException("Error while retrieving Email Claims ", e);
        } catch (ClassNotFoundException e) {
            throw new NotificationException("Cannot find claimsRetrieverImplClass ", e);
        } catch (APIManagementException e) {
            throw new NotificationException("Error while retrieving Email Claims ", e);
        }
        return emailset;
    }

    /**
     * Returns a map which will be used to create an Email event adapter
     * email addresses are extracted from notificationDTO and added as a comma separated string value
     *
     * @param notificationDTO
     * @return Map contains email properties such as message, title ...
     */
    private Map<String, String> getEmailProperties(NotificationDTO notificationDTO) {

        Map<String, String> emailProperties = null;

        Set<String> emailNotifierList = notificationDTO.getNotifierSet();
        if (emailNotifierList != null && emailNotifierList.size() > 0) {

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
     * Creates output event adapter Configurations
     *
     * @param name Output Event Adapter name
     * @param type Output Event Adapter type
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private OutputEventAdapterConfiguration createOutputEventAdapterConfiguration(String name, String type) {

        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(name);
        outputEventAdapterConfiguration.setType(type);
        outputEventAdapterConfiguration.setMessageFormat(type);

        return outputEventAdapterConfiguration;
    }

    /**
     * Retrieves the message configurations from tenant-config.json and sets the notification properties to
     * NotificationDTO
     *
     * @param notificationDTO
     * @return NotificationDTO after setting meesage and title
     * @throws NotificationException
     */
    public NotificationDTO loadMessageTemplate(NotificationDTO notificationDTO) throws NotificationException {

        APIIdentifier api = (APIIdentifier) notificationDTO.getProperties().get(NotifierConstants.API_KEY);
        APIIdentifier newApi = (APIIdentifier) notificationDTO.getProperties().get(NotifierConstants.NEW_API_KEY);

        String title = (String) notificationDTO.getProperty(NotifierConstants.TITLE_KEY);
        title = title.replaceAll("\\$1", newApi.getApiName());
        title = title.replaceAll("\\$2", newApi.getVersion());

        // Getting the message template from registry file
        String content = "";
        try {
            String template = (String) notificationDTO.getProperty(NotifierConstants.TEMPLATE_KEY);
            int tenantId = notificationDTO.getTenantID();
            Registry registry = getConfigSystemRegistry(tenantId);

            if (registry.resourceExists(template)) {
                if (log.isDebugEnabled()) {
                    log.debug("Getting message template from registry resource : " + template);
                }
                Resource resource = registry.get(template);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            } else {
                content = template;
            }

        } catch (RegistryException e) {
            throw new NotificationException("Error while getting registry resource", e);
        }

        if (content != null && !content.isEmpty()) {
            content = content.replaceAll("\\$1", newApi.getApiName());
            content = content.replaceAll("\\$2", newApi.getVersion());
            content = content.replaceAll("\\$3", newApi.getProviderName());
            content = content.replaceAll("\\$4", api.getApiName());
            content = content.replaceAll("\\$5", api.getVersion());
        }

        notificationDTO.setTitle(title);
        notificationDTO.setMessage(content);
        return notificationDTO;
    }

    /**
     * @param tenantId tenant Id of the current tenant
     * @return configuration registry
     * @throws RegistryException
     */
    protected Registry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry
                (tenantId);
    }

    /**
     * Load the class specified by the <code>claimsRetrieverImplClass</>
     *
     * @param claimsRetrieverImplClass
     * @return class specified by <code>claimsRetrieverImplClass</code>
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    protected ClaimsRetriever getClaimsRetriever(String claimsRetrieverImplClass)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return (ClaimsRetriever) APIUtil.getClassForName(claimsRetrieverImplClass).newInstance();
    }

    /**
     * Publish the notification to selected emails
     *
     * @param emailProperties details of email notification
     * @param adapterName output event adapter name
     * @param message message to send in the notification
     */
    protected void publishNotification(Map<String, String> emailProperties, String adapterName, String message) {
        ServiceReferenceHolder.getInstance().getOutputEventAdapterService().publish(adapterName,
                emailProperties, message);
    }

    /**
     * @return Retrieve tenant domain fot the current user
     */
    protected String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    protected void getOutputEventAdapterTypes() {
        ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                .getOutputEventAdapterTypes();
    }

    /**
     * Configure output adapter service
     *
     * @param outputEventAdapterConfiguration
     * @throws OutputEventAdapterException
     */
    protected void createOutputEventAdapterService(OutputEventAdapterConfiguration outputEventAdapterConfiguration)
            throws OutputEventAdapterException {
        ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create
                (outputEventAdapterConfiguration);
    }

}