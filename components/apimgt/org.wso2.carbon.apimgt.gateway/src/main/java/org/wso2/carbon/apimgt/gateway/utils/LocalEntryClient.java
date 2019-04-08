package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminServiceStub;

import java.rmi.RemoteException;

/**
 * This LocalEntryClient class for operating the synapse localEntries
 */
public class LocalEntryClient {

    private LocalEntryAdminServiceStub localEntryAdminServiceStub;

    static final String backendURLl = "local:///services/";

    public LocalEntryClient() throws AxisFault {
        localEntryAdminServiceStub = new LocalEntryAdminServiceStub(null,
                backendURLl + "LocalEntryAdmin");
    }

    /**
     * Add Local entry
     *
     * @param content Swagger Content
     * @return Status of the add operation
     * @throws AxisFault
     */

    public Boolean addLocalEntry(String content) throws AxisFault {

        Boolean value;
        try {
            value = localEntryAdminServiceStub.addEntry(content);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while generating the response ", e.getMessage(), e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while adding the local entry", e.getMessage(), e);
        }
        return value;

    }

    /**
     * Get Local entry for given API
     *
     * @param key API Id to be retrived
     * @return LocalEntry for the given API
     * @throws AxisFault
     */
    public Object getEntry(String key) throws AxisFault {
        Object object;
        try {
            object = localEntryAdminServiceStub.getEntry(key);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while retrieving the local entry", e.getMessage(), e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while create the admin client", e.getMessage(), e);
        }
        return object;
    }

    /**
     * Delete the local entry
     *
     * @param key APT Id to be deleted
     * @return Stataus of the delete operation
     * @throws AxisFault
     */

    public boolean deleteEntry(String key) throws AxisFault {
        try {
            return localEntryAdminServiceStub.deleteEntry(key);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while create the admin client", e.getMessage(), e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while deleting the local entry", e.getMessage(), e);
        }
    }
}