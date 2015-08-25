/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import javax.xml.stream.XMLStreamException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class to call external workflow to have human interaction on
 * Application Registration process.
 * 
 */
public class ApplicationRegistrationWSWorkflowExecutor extends AbstractApplicationRegistrationWorkflowExecutor {


	private String serviceEndpoint;

	private String username;

	private String password;

	private String contentType;

	private static final Log log = LogFactory.getLog(ApplicationRegistrationWSWorkflowExecutor.class);


	@Override
	public void execute(WorkflowDTO workflowDTO) throws WorkflowException {

		if (log.isDebugEnabled()) {
			log.info("Executing Application registration Workflow..");
		}
		try {
			ServiceClient client = new ServiceClient(ServiceReferenceHolder.getContextService()
			                                                               .getClientConfigContext(),
			                                         null);

			Options options = new Options();
            options.setAction("http://workflow.application.apimgt.carbon.wso2.org/initiate");
			options.setTo(new EndpointReference(serviceEndpoint));

			if (contentType != null) {
				options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
			} else {
				options.setProperty(Constants.Configuration.MESSAGE_TYPE,
				                    HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
			}

			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

			if (username != null && password != null) {
				auth.setUsername(username);
				auth.setPassword(password);
				auth.setPreemptiveAuthentication(true);
				List<String> authSchemes = new ArrayList<String>();
				authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
				auth.setAuthSchemes(authSchemes);
				options.setProperty(HTTPConstants.AUTHENTICATE,
				                    auth);
				options.setManageSession(true);
			}

			client.setOptions(options);

			String payload =
			                 "<wor:ApplicationRegistrationWorkFlowProcessRequest xmlns:wor=\"http://workflow.application.apimgt.carbon.wso2.org\">\n"
			                         + "        <wor:applicationName>$1</wor:applicationName>\n"			                         
			                         + "        <wor:applicationTier>$2</wor:applicationTier>\n"
			                         + "        <wor:applicationCallbackUrl>$3</wor:applicationCallbackUrl>\n"
			                         + "        <wor:applicationDescription>$4</wor:applicationDescription>\n"	
			                         + "        <wor:tenantDomain>$5</wor:tenantDomain>\n"	
			                         + "        <wor:userName>$6</wor:userName>\n"
			                         + "        <wor:workflowExternalRef>$7</wor:workflowExternalRef>\n"
			                         + "        <wor:callBackURL>$8</wor:callBackURL>\n"
                                     + "        <wor:keyType>$9</wor:keyType>\n"
			                         + "      </wor:ApplicationRegistrationWorkFlowProcessRequest>";

			ApplicationRegistrationWorkflowDTO appRegDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;
			Application application = appRegDTO.getApplication();
			String callBackURL = appRegDTO.getCallbackUrl();
            String applicationCallbackUrl = application.getCallbackUrl();
            String applicationDescription = application.getDescription();

			
			payload = payload.replace("$1", application.getName());		
			payload = payload.replace("$2", application.getTier());
			payload = payload.replace("$3", applicationCallbackUrl != null ? applicationCallbackUrl: "?");
			payload = payload.replace("$4", applicationDescription != null ? applicationDescription:"?");
			payload = payload.replace("$5", appRegDTO.getTenantDomain());
			payload = payload.replace("$6", appRegDTO.getUserName());
			payload = payload.replace("$7", appRegDTO.getExternalWorkflowReference());
			payload = payload.replace("$8", callBackURL != null ? callBackURL : "?");
            payload = payload.replace("$9", appRegDTO.getKeyType());

			client.fireAndForget(AXIOMUtil.stringToOM(payload));
            super.execute(workflowDTO);
			
		} catch (AxisFault axisFault) {
			log.error("Error sending out message", axisFault);
			throw new WorkflowException("Error sending out message", axisFault);
		} catch (XMLStreamException e) {
			log.error("Error converting String to OMElement", e);
			throw new WorkflowException("Error converting String to OMElement", e);
		}

	}

	/**
	 * Complete the external process status.
	 * Based on the workflow , we will update the status column of the
	 * AM_APPLICATION_KEY_MAPPING table
	 * 
	 * @param workFlowDTO
	 */
	@Override
	public void complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());

        log.info("Application Registration [Complete] Workflow Invoked. Workflow ID : " + workFlowDTO.getExternalWorkflowReference() + "Workflow State : " + workFlowDTO.getStatus());
        super.complete(workFlowDTO);
        if(WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())){
        	
        	Connection conn = null;
            try {
               conn = APIMgtDBUtil.getConnection();
               generateKeysForApplication((ApplicationRegistrationWorkflowDTO) workFlowDTO, conn);
               APIMgtDBUtil.transactionCommit(conn);
            } catch (APIManagementException e) {
            	APIMgtDBUtil.transactionRollback(conn);
                String msg = "Error occurred when updating the status of the Application Registration " +
                        "process";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }catch (Exception e) {
            	APIMgtDBUtil.transactionRollback(conn);
            	log.error("occured when updating the status of the Application creation process", e);
                throw new WorkflowException("occured when updating the status of the Application creation process", e);
    		}finally{
    			APIMgtDBUtil.closeConnection(conn);
    		}
        }

	}

	@Override
	public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
