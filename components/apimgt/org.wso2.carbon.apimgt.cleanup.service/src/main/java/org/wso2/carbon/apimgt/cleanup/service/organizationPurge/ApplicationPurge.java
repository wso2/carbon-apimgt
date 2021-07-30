package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.api.model.Application;
import java.util.List;

public class ApplicationPurge implements OrganizationPurge{
    protected ApiMgtDAO apiMgtDAO;

    public ApplicationPurge(ApiMgtDAO apiMgtDAO) {
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    @Override
    public void deleteOrganization(String organizationId) {
        List<Application> applicationList = apiMgtDAO.getApplicationsByOrgId(organizationId);
        int[] applicationIdList = new int[applicationList.size()];

        for (int i = 0; i < applicationList.size(); i++) {
            applicationIdList[i] = applicationList.get(i).getId();
        }

        try {
            //removing pending subscriptions
            removePendingSubscriptions(applicationIdList);
            // removing pending application registrations
            deletePendingApplicationRegistrations(applicationIdList);
            // removing applications list
            deleteApplicationList(applicationIdList);
            // removing subscribers
            //TODO
            deleteSubscribers();
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
    }


    private void removePendingSubscriptions(int[] applicationIdList) throws APIManagementException {
        apiMgtDAO.removePendingSubscriptions(applicationIdList);
    }

    private void deleteApplicationList(int[] applicationIdList) throws APIManagementException {
        apiMgtDAO.deleteApplicationList(applicationIdList);
    }

    // cleanup pending application regs
    private void deletePendingApplicationRegistrations(int[] applicationIdList) throws APIManagementException {
        List<String> keyManagerViseProductionKeyState = apiMgtDAO.
                getPendingRegistrationsForApplicationList(applicationIdList, "PRODUCTION");

        for (String km : keyManagerViseProductionKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList,km,"PRODUCTION");
        }

        List<String> keyManagerViseSandboxKeyState = apiMgtDAO.
                getPendingRegistrationsForApplicationList(applicationIdList, "SANDBOX");

        for (String km : keyManagerViseSandboxKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList,km,"SANDBOX");
        }
    }

    //TODO
    private void deleteSubscribers() {

    }

}
