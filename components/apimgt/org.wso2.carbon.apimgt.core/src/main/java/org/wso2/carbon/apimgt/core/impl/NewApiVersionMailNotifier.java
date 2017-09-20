/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.configuration.models.MailConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * Class to send Email notification.
 */
public class NewApiVersionMailNotifier extends Notifier {
    private static final Log log = LogFactory.getLog(NewApiVersionMailNotifier.class);
    static MailConfigurations mailConfigurations = ServiceReferenceHolder.getInstance().getAPIMConfiguration().
            getNotificationConfigurations().getMailConfigurations();
    private static final String SMTP_HOST_NAME = mailConfigurations.getSmtpHostname();
    private static final String SMTP_AUTH_USER = mailConfigurations.getSmtpAuthUser();
    private static final String SMTP_AUTH_PWD = mailConfigurations.getSmtpAuthPwd();


    @Override
    public void sendNotifications(NotificationDTO notificationDTO) throws
            APIManagementException {


        Properties props = notificationDTO.getProperties();
        //get Notifier email List
        Set<String> emailList = getEmailNotifierList(notificationDTO);

        if (emailList.isEmpty()) {
            log.debug("Email Notifier Set is Empty");
            return;
        }
        for (String mail : emailList) {
            try {
                Authenticator auth = new SMTPAuthenticator();
                Session mailSession = Session.getDefaultInstance(props, auth);
                MimeMessage message = new MimeMessage(mailSession);
                notificationDTO.setTitle((String) notificationDTO.getProperty(NotifierConstants.TITLE_KEY));
                notificationDTO.setMessage((String) notificationDTO.getProperty(NotifierConstants.TEMPLATE_KEY));
                notificationDTO = loadMailTemplate(notificationDTO);
                message.setSubject(notificationDTO.getTitle());
                message.setContent(notificationDTO.getMessage(), NotifierConstants.TEXT_TYPE);
                message.setFrom(new InternetAddress(mailConfigurations.getFromUser()));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail));
                Transport.send(message);
            } catch (MessagingException e) {
                log.error("Exception Occurred during Email notification Sending", e);
            }

        }
    }

    /**
     * Get UserNotifier Email Set.
     */
    private Set<String> getEmailNotifierList(NotificationDTO notificationDTO) throws
            APIManagementException {
        Set<String> mailSet = new HashSet<>();
        Set<String> subscriberList = (Set<String>) notificationDTO.
                getProperty(NotifierConstants.SUBSCRIBERS_PER_API);
        if (!subscriberList.isEmpty()) {
            for (Iterator<String> it = subscriberList.iterator(); it.hasNext(); ) {
                String userName = it.next();
                APIManagerFactory apiManagerFactory = APIManagerFactory.getInstance();
                String userId = apiManagerFactory.getIdentityProvider().getIdOfUser(userName);
                mailSet.add(apiManagerFactory.getIdentityProvider().getEmailOfUser(userId));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Subscriber List is Empty");
            }
        }
        return mailSet;
    }

    /**
     * Class to Authenticate User.
     */
    private static class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }

    /**
     * Retrieves the message configurations from notificationConfiguration and sets the notification properties to.
     * NotificationDTO
     */

    public NotificationDTO loadMailTemplate(NotificationDTO notificationDTO) {
        String title = notificationDTO.getTitle();
        title = title.replaceAll("\\$1", (String) notificationDTO.getProperty(NotifierConstants.NEW_API_VERSION));
        title = title.replaceAll("\\$2", (String) notificationDTO.getProperty(NotifierConstants.API_NAME));

        String content = notificationDTO.getMessage();
        if (content != null && !content.isEmpty()) {
            content = content.replaceAll("\\$1", (String) notificationDTO.getProperty
                    (NotifierConstants.NEW_API_VERSION));
            content = content.replaceAll("\\$2", (String) notificationDTO.getProperty(NotifierConstants.API_NAME));
        }
        notificationDTO.setTitle(title);
        notificationDTO.setMessage(content);
        return notificationDTO;
    }
}
