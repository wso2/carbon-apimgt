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
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.sequences.services.SequenceAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * SequenceAdmin service client to deploy the custom sequences to multiple gateway environemnets.
 */

public class SequenceAdminServiceProxy {

    private SequenceAdmin sequenceAdmin;
    private String tenantDomain;

    public SequenceAdminServiceProxy(String tenantDomain) {

        this.tenantDomain = tenantDomain;
        sequenceAdmin = ServiceReferenceHolder.getInstance().getSequenceAdmin();

    }

    /**
     * Deploy the sequence to the gateway
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @throws AxisFault
     */
    public void addSequence(OMElement sequence) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                sequenceAdmin.addSequence(sequence);
            } else {
                sequenceAdmin.addSequenceForTenant(sequence, tenantDomain);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while adding new sequence", e);
        }
    }

    /**
     * Undeploy the sequence from gateway
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @throws AxisFault
     */
    public void deleteSequence(String sequenceName) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                sequenceAdmin.deleteSequence(sequenceName);
            } else {
                sequenceAdmin.deleteSequenceForTenant(sequenceName, tenantDomain);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting sequence", e);
        }
    }

    /**
     * get the sequence from gateway
     *
     * @param sequenceName -The sequence name,
     * @throws AxisFault
     */
    public OMElement getSequence(String sequenceName) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return sequenceAdmin.getSequence(sequenceName);
            } else {
                return sequenceAdmin.getSequenceForTenant(sequenceName, tenantDomain);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while retriving the sequence", e);
        }
    }

    public boolean isExistingSequence(String sequenceName) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return sequenceAdmin.isExistingSequence(sequenceName);
            } else {
                return sequenceAdmin.isExistingSequenceForTenant(sequenceName, tenantDomain);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while checking for existence of sequence : " + sequenceName, e);
        }
    }

    protected void setSequenceAdmin(SequenceAdmin sequenceAdmin) {

        this.sequenceAdmin = sequenceAdmin;
    }
}
