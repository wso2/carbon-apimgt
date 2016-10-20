package org.wso2.carbon.apimgt.impl.service;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;

public class APIConsumerAdmin extends org.wso2.carbon.core.AbstractAdmin{
	

	public JSONObject resumeWorkflow(Object[] args, String username) throws APIManagementException {
		APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(username);
		return consumer.resumeWorkflow(args);
	}

}
