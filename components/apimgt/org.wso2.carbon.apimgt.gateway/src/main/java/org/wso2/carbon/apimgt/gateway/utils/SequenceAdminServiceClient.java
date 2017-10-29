/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.sequences.stub.types.SequenceAdminServiceStub;

/**
 * SequenceAdmin service client to deploy the custom sequences to multiple gateway environemnets.
 * 
 */

public class SequenceAdminServiceClient {
	
	static final String backendURLl = "local:///services/";
	private SequenceAdminServiceStub sequenceAdminStub;

	public SequenceAdminServiceClient() throws AxisFault {
		sequenceAdminStub = new SequenceAdminServiceStub(null, backendURLl +"SequenceAdminService");
	}


	/**
	 * Deploy the sequence to the gateway
	 * 
	 * @param sequence  - The sequence element , which to be deployed in synapse
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addSequence(OMElement sequence) throws AxisFault {
		try {
			sequenceAdminStub.addSequence(sequence);

		} catch (Exception e) {
			throw new AxisFault("Error while adding new sequence", e);
		}
	}
	
	public void addSequenceForTenant(OMElement sequence, String tenantDomain) throws AxisFault {
		try {
			sequenceAdminStub.addSequenceForTenant(sequence, tenantDomain);

		} catch (Exception e) {
			throw new AxisFault("Error while adding new sequence", e);
		}
	}

	/**
	 * Undeploy the sequence from gateway
	 * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteSequence(String sequenceName) throws AxisFault {
		try {
			sequenceAdminStub.deleteSequence(sequenceName);
		} catch (Exception e) {
			throw new AxisFault("Error while deleting sequence", e);
		}
	}
	
	public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
		try {
			sequenceAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);

		} catch (Exception e) {
			throw new AxisFault("Error while deleting sequence", e);
		}
	}

	/**
	 * get the sequence from gateway
	 * 
	 * @param sequenceName
	 *            -The sequence name,
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public OMElement getSequence(String sequenceName) throws AxisFault {
		try {
			return  (OMElement) sequenceAdminStub.getSequence(sequenceName);
			
		} catch (Exception e) {
			throw new AxisFault("Error while retriving the sequence", e);
		}
	}
	
	public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
		try {
			return (OMElement) sequenceAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
			
		} catch (Exception e) {
			throw new AxisFault("Error while retriving the sequence", e);
		}
	}

    public boolean isExistingSequence(String sequenceName) throws AxisFault{
        try{
        	return sequenceAdminStub.isExistingSequence(sequenceName);
        }catch (Exception e){
            throw new AxisFault("Error while checking for existence of sequence : " + sequenceName , e);
        }
    }
    
    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault{
        try{
        	return sequenceAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
        }catch (Exception e){
            throw new AxisFault("Error while checking for existence of sequence : " + sequenceName +
                    " in tenant " + tenantDomain, e);
        }
    }

    protected void setSequenceAdminStub(SequenceAdminServiceStub sequenceAdminStub) {
		this.sequenceAdminStub = sequenceAdminStub;
	}
}
