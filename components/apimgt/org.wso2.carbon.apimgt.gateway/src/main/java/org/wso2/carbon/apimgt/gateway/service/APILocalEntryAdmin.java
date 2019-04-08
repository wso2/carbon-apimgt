package org.wso2.carbon.apimgt.gateway.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryClient;

/**
 * API Local Entry Admin Service.
 */
public class APILocalEntryAdmin extends org.wso2.carbon.core.AbstractAdmin {

    private static Log log = LogFactory.getLog(APILocalEntryAdmin.class);

    /**
     * Add Local Entry to the gateway.
     *
     * @param content
     * @return Status of the operation
     * @throws AxisFault
     */
    public boolean addLocalEntry(String content) throws AxisFault {
        LocalEntryClient localEntryClient = getLocalEntryAdminClient();
        return localEntryClient.addLocalEntry(content);
    }

    /**
     * Get the Local entry client.
     *
     * @return LocalEntryClient
     * @throws AxisFault
     */
    protected LocalEntryClient getLocalEntryAdminClient() throws AxisFault {
        return new LocalEntryClient();
    }

    /**
     * Get the Local entry for given API.
     *
     * @param key key of the existing local entry.
     * @return LocalEntry
     * @throws AxisFault
     */
    public Object getEntry(String key) throws AxisFault {
        LocalEntryClient localEntryAdminClient = getLocalEntryAdminClient();
        return localEntryAdminClient.getEntry(key);
    }

    /**
     * Delete the local entry.
     *
     * @param key Key of the local entry to be deleted.
     * @return Status of the operation
     * @throws AxisFault
     */
    public Boolean deleteLocalEntry(String key) throws AxisFault {
        LocalEntryClient localEntryAdminClient = getLocalEntryAdminClient();
        return localEntryAdminClient.deleteEntry(key);
    }
}