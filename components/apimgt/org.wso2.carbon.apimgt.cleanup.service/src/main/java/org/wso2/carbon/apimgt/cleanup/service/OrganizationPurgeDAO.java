package org.wso2.carbon.apimgt.cleanup.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrganizationPurgeDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
    private static OrganizationPurgeDAO INSTANCE = null;
    private boolean multiGroupAppSharingEnabled = false;

    public OrganizationPurgeDAO() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
    }

    /**
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link OrganizationPurgeDAO} instance
     */
    public static OrganizationPurgeDAO getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new OrganizationPurgeDAO();
        }

        return INSTANCE;
    }

    /**
     * Remove pending subscriptions for a given application id list
     *
     * @param organization Organization
     * @throws APIManagementException if failed to remove pending subscriptions
     */
    public void removePendingSubscriptions(String organization) throws APIManagementException {

        String query = SQLConstants.DELETE_PENDING_SUBSCRIPTIONS;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            preparedStatement.setString(1, organization);

            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            String msg = "Error occurred while removing pending subscriptions for organization: "+organization;
            handleException(msg, e);
        }
    }

    /**
     * Remove application creation workflows
     *
     * @param organization Organization
     * @throws APIManagementException if failed to remove application creation workflows
     */
    public void removeApplicationCreationWorkflows(String organization)
            throws APIManagementException {

        String query = SQLConstants.DELETE_APPLICATION_CREATION_WORKFLOWS;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);
            preparedStatement.setString(1, organization);

            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            String msg = "Error occurred while removing application creation workflows for organization: "+organization;
            handleException(msg, e);
        }
    }

    /**
     * Remove pending Application Registrations
     *
     * @param organization Organization
     * @throws APIManagementException when failed to delete pending application registrations
     */
    public void deletePendingApplicationRegistrations(String organization) throws APIManagementException {
        String query = SQLConstants.REMOVE_PENDING_APPLICATION_REGISTRATIONS;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, organization);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException("Error while deleting pending application registrations for organization: " + organization,
                    e);
        }
    }

    /**
     * This method is used to obtain subscribers for a given organization
     *
     * @param organization Organization
     * @throws APIManagementException if failed to obtain subscribers
     */
    public List<Integer> getSubscribersForOrganization(String organization) throws APIManagementException {
        List<Integer> subscriberIdList = new ArrayList<>();
        String query = SQLConstants.GET_SUBSCRIBERS_FOR_ORG_ID;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, organization);

            try (ResultSet rs = preparedStatement.executeQuery();) {
                while (rs.next()) {
                    subscriberIdList.add(rs.getInt("SUBSCRIBER_ID"));
                }
            }

        } catch (SQLException e) {
            handleException("Error while retrieving subscribers for organization: " + organization, e);
        }

        return subscriberIdList;
    }

    /**
     * This method is used to obtain mapped organization list for a particular subscriber
     *
     * @param subscriberId Subscriber id
     * @return list of mapped organizations
     * @throws APIManagementException if failed to obtain mapped organizations
     */
    public List<String> getOrganizationsOfSubscriber(int subscriberId) throws APIManagementException {
        List<String> organizationList = new ArrayList<>();
        String query = SQLConstants.GET_MAPPED_ORGANIZATIONS_FOR_SUBSCRIBER_ID;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);) {

            preparedStatement.setInt(1, subscriberId);

            try (ResultSet rs = preparedStatement.executeQuery();) {
                while (rs.next()) {
                    organizationList.add(rs.getString("ORGANIZATION"));
                }
            }

        } catch (SQLException e) {
            handleException("Error while retrieving organizations for subscriber id " + subscriberId, e);
        }
        return organizationList;
    }

    /**
     * Remove subscriber organization mapping
     *
     * @param subscriberId Subscriber id
     * @param organization Organization
     * @throws APIManagementException if failed to remove subscriber organization mapping
     */
    public void removeOrganizationFromSubscriber(int subscriberId, String organization)
            throws APIManagementException {

        String query = SQLConstants.DELETE_SUBSCRIBER_ORGANIZATION_MAPPING;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);){

            preparedStatement.setInt(1, subscriberId);
            preparedStatement.setString(2, organization);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException("Error while removing subscriber with subscriber id " + subscriberId+ " for organization: "
                    +organization, e);
        }
    }

    /**
     * Remove a subscriber, given the subscriber id
     *
     * @param subscriberId Subscriber id
     * @throws APIManagementException if failed to remove the subscriber
     */
    public void removeSubscriber(int subscriberId) throws APIManagementException {

        String query = SQLConstants.DELETE_SUBSCRIBER;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1, subscriberId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException("Error while removing subscriber with subscriber id " + subscriberId, e);
        }
    }


    /**
     * Deletes Applications along with subscriptions, keys and registration data
     *
     * @param organization Organization
     * @throws APIManagementException if failed to delete applications for organization
     */
    public void deleteApplicationList(String organization) throws APIManagementException {

        Connection connection = null;
        PreparedStatement deleteMappingQuery = null;
        PreparedStatement prepStmtGetConsumerKey = null;
        PreparedStatement deleteRegistrationQuery = null;
        PreparedStatement deleteSubscription = null;
        PreparedStatement deleteDomainApp = null;
        PreparedStatement deleteAppKey = null;
        PreparedStatement deleteApp = null;
        ResultSet rs = null;

        String getConsumerKeyQuery = SQLConstants.GET_CONSUMER_KEYS_OF_APPLICATION_LIST_SQL;

        String deleteSubscriptionsQuery = SQLConstants.REMOVE_APPLICATION_LIST_FROM_SUBSCRIPTIONS_SQL;

        String deleteApplicationKeyQuery = SQLConstants.REMOVE_APPLICATION_LIST_FROM_APPLICATION_KEY_MAPPINGS_SQL;

        String deleteDomainAppQuery = SQLConstants.REMOVE_APPLICATION_FROM_DOMAIN_MAPPINGS_SQL;

        String deleteApplicationQuery = SQLConstants.REMOVE_APPLICATION_LIST_FROM_APPLICATIONS_SQL;

        String deleteRegistrationEntry = SQLConstants.REMOVE_APPLICATION_LIST_FROM_APPLICATION_REGISTRATIONS_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            if (multiGroupAppSharingEnabled) {
                updateGroupIDMappingsBulk(connection, organization);
            }

            prepStmtGetConsumerKey = connection.prepareStatement(getConsumerKeyQuery);
            prepStmtGetConsumerKey.setString(1, organization);

            rs = prepStmtGetConsumerKey.executeQuery();

            deleteDomainApp = connection.prepareStatement(deleteDomainAppQuery);

            while (rs.next()) {
                String consumerKey = rs.getString(APIConstants.FIELD_CONSUMER_KEY);
                String keyManagerName = rs.getString("NAME");
                String keyManagerOrganization = rs.getString("ORGANIZATION");

                // This is true when OAuth App has been created by pasting consumer key/secret in the screen.
                String mode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {

                    deleteDomainApp.setString(1, consumerKey);
                    deleteDomainApp.addBatch();
                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(keyManagerOrganization,
                            keyManagerName);

                    if (keyManager != null) {
                        try {
                            keyManager.deleteMappedApplication(consumerKey);
                            log.info("Mapped application deleted for consumer key: " + consumerKey
                                    + " and organization: " + organization);
                        } catch (APIManagementException e) {
                            handleException("Error while Deleting Client Application for consumer key: " + consumerKey
                                    + " and organization: " + organization, e);
                        }
                    }

                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if (!APIConstants.OAuthAppMode.MAPPED.name().equals(mode)) {
                        //delete on oAuthorization server.
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting Oauth application with consumer key " + consumerKey + " from the "
                                    + "Oauth server for organization: " + organization);
                        }
                        if (keyManager != null) {
                            try {
                                keyManager.deleteApplication(consumerKey);
                                log.info("Client application deleted for consumer key: " + consumerKey
                                        + " and organization: " + organization);
                            } catch (APIManagementException e) {
                                handleException(
                                        "Error while Deleting Client Application for organization: " + organization, e);
                            }

                        }
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Subscription Key mapping details are deleted successfully for Applications for "
                        + "organization: " + organization);
            }

            deleteRegistrationQuery = connection.prepareStatement(deleteRegistrationEntry);
            deleteRegistrationQuery.setString(1, organization);

            deleteRegistrationQuery.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application Registration details are deleted successfully for Applications for "
                        + "organization: " + organization);
            }

            deleteSubscription = connection.prepareStatement(deleteSubscriptionsQuery);
            deleteSubscription.setString(1, organization);

            deleteSubscription.execute();

            if (log.isDebugEnabled()) {
                log.debug("Subscription details are deleted successfully for Applications for organization: "
                        + organization);
            }

            deleteDomainApp.executeBatch();

            deleteAppKey = connection.prepareStatement(deleteApplicationKeyQuery);
            deleteAppKey.setString(1, organization);

            deleteAppKey.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application Key Mapping details are deleted successfully for Application for "
                        + "organization: " + organization);
            }

            deleteApp = connection.prepareStatement(deleteApplicationQuery);
            deleteApp.setString(1, organization);

            deleteApp.execute();

            if (log.isDebugEnabled()) {
                log.debug("Applications are deleted successfully for organization: " + organization);
            }

            connection.commit();

        } catch (SQLException e) {
            handleException(
                    "Error while removing application details from the database for organization: " + organization, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtGetConsumerKey, connection, rs);
            APIMgtDBUtil.closeAllConnections(deleteApp, null, null);
            APIMgtDBUtil.closeAllConnections(deleteAppKey, null, null);
            APIMgtDBUtil.closeAllConnections(deleteMappingQuery, null, null);
            APIMgtDBUtil.closeAllConnections(deleteRegistrationQuery, null, null);
            APIMgtDBUtil.closeAllConnections(deleteSubscription, null, null);
            APIMgtDBUtil.closeAllConnections(deleteDomainApp, null, null);
            APIMgtDBUtil.closeAllConnections(deleteAppKey, null, null);
            APIMgtDBUtil.closeAllConnections(deleteApp, null, null);
        }
    }

    /**
     * Purge records in Application Group Mappings
     *
     * @param conn Connection
     * @param organization Organization
     * @return
     * @throws APIManagementException when failed to execute the application groups update
     */
    private void updateGroupIDMappingsBulk(Connection conn, String organization)
            throws APIManagementException {

        PreparedStatement removeMigratedGroupIdsStatement = null;
        PreparedStatement deleteStatement = null;

        String deleteQuery = SQLConstants.REMOVE_GROUP_ID_MAPPING_BULK_SQL;
        String removeGroupIdsQuery = SQLConstants.REMOVE_MIGRATED_GROUP_ID_SQL_BULK;

        try {
            removeMigratedGroupIdsStatement = conn.prepareStatement(removeGroupIdsQuery);

            removeMigratedGroupIdsStatement.setString(1, organization);
            removeMigratedGroupIdsStatement.executeUpdate();

            deleteStatement = conn.prepareStatement(deleteQuery);

            deleteStatement.setString(1, organization);
            deleteStatement.executeUpdate();

        } catch (SQLException e) {
            handleException("Failed to update bulk groupId mappings for organization: "+organization, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(removeMigratedGroupIdsStatement, null, null);
            APIMgtDBUtil.closeAllConnections(deleteStatement, null, null);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
