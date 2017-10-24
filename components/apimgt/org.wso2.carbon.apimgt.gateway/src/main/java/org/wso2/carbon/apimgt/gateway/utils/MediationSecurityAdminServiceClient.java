/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.mediation.security.stub.MediationSecurityAdminServiceStub;

/**
 * Mediation Security Admin Service Client to encode the passwords and store
 * into the registry .
 */

public class MediationSecurityAdminServiceClient{

	static final String backendURLl = "local:///services/";
	private MediationSecurityAdminServiceStub mediationSecurityAdminServiceStub;

	public MediationSecurityAdminServiceClient() throws AxisFault {
		mediationSecurityAdminServiceStub = new MediationSecurityAdminServiceStub(null, backendURLl +"MediationSecurityAdminService");
	}




	/**
	 * encrypt the plain text password
	 *
	 * @param cipher
	 *            init cipher
	 * @param plainTextPass
	 *            plain text password
	 * @return encrypted password
	 * @throws APIManagementException
	 */
	public String doEncryption(String plainTextPass) throws APIManagementException {

		String encodedValue = null;
		try {
		 encodedValue =	 mediationSecurityAdminServiceStub.doEncrypt(plainTextPass);
//			encodedValue = CryptoUtil.getDefaultCryptoUtil()
//			                         .encryptAndBase64Encode(plainTextPass.getBytes()); //why ESB can not use this?
		} catch (Exception e) {
			String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
			throw new APIManagementException(msg, e);
		}
		return encodedValue;
	}



}
