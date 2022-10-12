package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.policy.APIPolicy;
import org.wso2.apk.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.apk.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.apk.apimgt.api.model.policy.Condition;
import org.wso2.apk.apimgt.api.model.policy.EventCountLimit;
import org.wso2.apk.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.apk.apimgt.api.model.policy.HeaderCondition;
import org.wso2.apk.apimgt.api.model.policy.IPCondition;
import org.wso2.apk.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.apk.apimgt.api.model.policy.Pipeline;
import org.wso2.apk.apimgt.api.model.policy.Policy;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.apk.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.apk.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.apk.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.apk.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.ThrottlePolicyConstants;
import org.wso2.apk.apimgt.impl.dao.PolicyDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolicyDAOImpl implements PolicyDAO {
    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);
    private static PolicyDAOImpl INSTANCE = new PolicyDAOImpl();

    private boolean forceCaseInsensitiveComparisons = false;

    private PolicyDAOImpl() {

    }

    public static PolicyDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    @Override
    public APIPolicy addAPIPolicy(APIPolicy policy) throws APIManagementException {

        Connection connection = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            addAPIPolicy(policy, connection);
            connection.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API,
                    policy.getTenantId(),
                    policy.getPolicyName());

            if (isAPIPolicyExists) {
                log.warn(
                        "API Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleExceptionWithCode("Failed to add API Policy: " + policy, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Api Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API,
                        policy.getTenantId(),
                        policy.getPolicyName());

                if (isAPIPolicyExists) {
                    log.warn("API Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleExceptionWithCode("Failed to add Api Policy: " + policy, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }
        return policy;
    }


    /**
     * Add a API level throttling policy to database.
     * <p>
     * If valid policy Id (not -1) is present in the <code>policy</code> object,
     * policy will be inserted with that policy Id.
     * Otherwise policy Id will be auto incremented.
     * </p>
     *
     * @param policy policy object defining the throttle policy
     * @throws SQLException
     */
    private void addAPIPolicy(APIPolicy policy, Connection conn) throws SQLException {

        ResultSet resultSet = null;
        PreparedStatement policyStatement = null;
        String addQuery = SQLConstants.ThrottleSQLConstants.INSERT_API_POLICY_SQL;
        int policyId;

        try {
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            policyStatement = conn.prepareStatement(addQuery,
                    new String[]{"POLICY_ID".toLowerCase()});
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setString(12, policy.getUserLevel());
            policyStatement.executeUpdate();
            resultSet = policyStatement.getGeneratedKeys(); // Get the inserted POLICY_ID (auto incremented value)

            // Returns only single row
            if (resultSet.next()) {

                /*
                 *  H2 doesn't return generated keys when key is provided (not generated).
                   Therefore policyId should be policy parameter's policyId when it is provided.
                 */
                policyId = resultSet.getInt(1);
                List<Pipeline> pipelines = policy.getPipelines();
                if (pipelines != null) {
                    for (Pipeline pipeline : pipelines) { // add each pipeline data to AM_CONDITION_GROUP table
                        addPipeline(pipeline, policyId, conn);
                    }
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, null, resultSet);
        }
    }


    private boolean isPolicyExist(Connection connection, String policyType, int tenantId, String policyName)
            throws APIManagementException {

        PreparedStatement isExistStatement = null;

        boolean isExist = false;
        String policyTable = null;
        if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.API_THROTTLE_POLICY_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_APPLICATION_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_GLOBAL_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_SUBSCRIPTION_TABLE;
        }
        try {
            String query = "SELECT " + PolicyConstants.POLICY_ID + " FROM " + policyTable
                    + " WHERE TENANT_ID =? AND NAME = ? ";
            connection.setAutoCommit(true);
            isExistStatement = connection.prepareStatement(query);
            isExistStatement.setInt(1, tenantId);
            isExistStatement.setString(2, policyName);
            ResultSet result = isExistStatement.executeQuery();
            if (result != null && result.next()) {
                isExist = true;
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to check is exist: " + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(isExistStatement, connection, null);
        }
        return isExist;
    }


    /**
     * Populates common attribute data of the <code>policy</code> to <code>policyStatement</code>
     *
     * @param policyStatement prepared statement initialized of policy operation
     * @param policy          <code>Policy</code> object with data
     * @throws SQLException
     */
    private void setCommonParametersForPolicy(PreparedStatement policyStatement, Policy policy) throws SQLException {

        policyStatement.setString(1, policy.getPolicyName());
        if (!StringUtils.isEmpty(policy.getDisplayName())) {
            policyStatement.setString(2, policy.getDisplayName());
        } else {
            policyStatement.setString(2, policy.getPolicyName());
        }
        policyStatement.setInt(3, policy.getTenantId());
        policyStatement.setString(4, policy.getDescription());
        policyStatement.setString(5, policy.getDefaultQuotaPolicy().getType());

        //TODO use requestCount in same format in all places
        if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getRequestCount());
            policyStatement.setString(7, null);
        } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getDataAmount());
            policyStatement.setString(7, limit.getDataUnit());
        } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getEventCount());
            policyStatement.setString(7, null);
        }

        policyStatement.setLong(8, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
        policyStatement.setString(9, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
        //policyStatement.setBoolean(9, APIUtil.isContentAwarePolicy(policy));
        policyStatement.setBoolean(10, policy.isDeployed());
        if (!StringUtils.isBlank(policy.getUUID())) {
            policyStatement.setString(11, policy.getUUID());
        } else {
            policyStatement.setString(11, UUID.randomUUID().toString());
        }
    }


    /**
     * Add throttling policy pipeline to database
     *
     * @param pipeline condition pipeline
     * @param policyID id of the policy to add pipeline
     * @param conn     database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addPipeline(Pipeline pipeline, int policyID, Connection conn) throws SQLException {

        PreparedStatement conditionStatement = null;
        ResultSet rs = null;

        try {
            String sqlAddQuery = SQLConstants.ThrottleSQLConstants.INSERT_CONDITION_GROUP_SQL;
            List<Condition> conditionList = pipeline.getConditions();

            // Add data to the AM_CONDITION table
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            conditionStatement = conn.prepareStatement(sqlAddQuery, new String[]{"CONDITION_GROUP_ID".toLowerCase()});
            conditionStatement.
                    setInt(1, policyID);
            conditionStatement.setString(2, pipeline.getQuotaPolicy().getType());

            if (PolicyConstants.REQUEST_COUNT_TYPE.equals(pipeline.getQuotaPolicy().getType())) {
                conditionStatement.setLong(3,
                        ((RequestCountLimit) pipeline.getQuotaPolicy().getLimit()).getRequestCount());
                conditionStatement.setString(4, null);
            } else if (PolicyConstants.BANDWIDTH_TYPE.equals(pipeline.getQuotaPolicy().getType())) {
                BandwidthLimit limit = (BandwidthLimit) pipeline.getQuotaPolicy().getLimit();
                conditionStatement.setLong(3, limit.getDataAmount());
                conditionStatement.setString(4, limit.getDataUnit());
            }

            conditionStatement.setLong(5, pipeline.getQuotaPolicy().getLimit().getUnitTime());
            conditionStatement.setString(6, pipeline.getQuotaPolicy().getLimit().getTimeUnit());
            conditionStatement.setString(7, pipeline.getDescription());
            conditionStatement.executeUpdate();
            rs = conditionStatement.getGeneratedKeys();

            // Add Throttling parameters which have multiple entries
            if (rs != null && rs.next()) {
                int pipelineId = rs.getInt(1); // Get the inserted
                // CONDITION_GROUP_ID (auto
                // incremented value)
                pipeline.setId(pipelineId);
                for (Condition condition : conditionList) {
                    if (condition == null) {
                        continue;
                    }
                    String type = condition.getType();
                    if (PolicyConstants.IP_RANGE_TYPE.equals(type) || PolicyConstants.IP_SPECIFIC_TYPE.equals(type)) {
                        IPCondition ipCondition = (IPCondition) condition;
                        addIPCondition(ipCondition, pipelineId, conn);
                    }

                    if (PolicyConstants.HEADER_TYPE.equals(type)) {
                        addHeaderCondition((HeaderCondition) condition, pipelineId, conn);
                    } else if (PolicyConstants.QUERY_PARAMETER_TYPE.equals(type)) {
                        addQueryParameterCondition((QueryParameterCondition) condition, pipelineId, conn);
                    } else if (PolicyConstants.JWT_CLAIMS_TYPE.equals(type)) {
                        addJWTClaimsCondition((JWTClaimsCondition) condition, pipelineId, conn);
                    }
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionStatement, null, rs);
        }
    }

    private void addIPCondition(IPCondition ipCondition, int pipelineId, Connection conn) throws SQLException {

        PreparedStatement statementIPCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_IP_CONDITION_SQL;

            statementIPCondition = conn.prepareStatement(sqlQuery);
            String startingIP = ipCondition.getStartingIP();
            String endingIP = ipCondition.getEndingIP();
            String specificIP = ipCondition.getSpecificIP();

            statementIPCondition.setString(1, startingIP);
            statementIPCondition.setString(2, endingIP);
            statementIPCondition.setString(3, specificIP);
            statementIPCondition.setBoolean(4, ipCondition.isInvertCondition());
            statementIPCondition.setInt(5, pipelineId);
            statementIPCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(statementIPCondition, null, null);
        }
    }


    /**
     * Add HEADER throttling condition to AM_HEADER_FIELD_CONDITION table
     *
     * @param headerCondition {@link HeaderCondition} with header fieled and value
     * @param pipelineId      id of the pipeline which this condition belongs to
     * @param conn            database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addHeaderCondition(HeaderCondition headerCondition, int pipelineId, Connection conn)
            throws SQLException {

        PreparedStatement psHeaderCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_HEADER_FIELD_CONDITION_SQL;
            psHeaderCondition = conn.prepareStatement(sqlQuery);
            psHeaderCondition.setInt(1, pipelineId);
            psHeaderCondition.setString(2, headerCondition.getHeaderName());
            psHeaderCondition.setString(3, headerCondition.getValue());
            psHeaderCondition.setBoolean(4, headerCondition.isInvertCondition());
            psHeaderCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psHeaderCondition, null, null);
        }
    }


    /**
     * Add QUERY throttling condition to AM_QUERY_PARAMETER_CONDITION table
     *
     * @param queryParameterCondition {@link QueryParameterCondition} with parameter name and value
     * @param pipelineId              id of the pipeline which this condition belongs to
     * @param conn                    database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addQueryParameterCondition(QueryParameterCondition queryParameterCondition, int pipelineId,
                                            Connection conn) throws SQLException {

        PreparedStatement psQueryParameterCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_QUERY_PARAMETER_CONDITION_SQL;
            psQueryParameterCondition = conn.prepareStatement(sqlQuery);
            psQueryParameterCondition.setInt(1, pipelineId);
            psQueryParameterCondition.setString(2, queryParameterCondition.getParameter());
            psQueryParameterCondition.setString(3, queryParameterCondition.getValue());
            psQueryParameterCondition.setBoolean(4, queryParameterCondition.isInvertCondition());
            psQueryParameterCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psQueryParameterCondition, null, null);
        }
    }


    /**
     * Add JWTCLAIMS throttling condition to AM_JWT_CLAIM_CONDITION table
     *
     * @param jwtClaimsCondition {@link JWTClaimsCondition} with claim url and claim attribute
     * @param pipelineId         id of the pipeline which this condition belongs to
     * @param conn               database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addJWTClaimsCondition(JWTClaimsCondition jwtClaimsCondition, int pipelineId, Connection conn)
            throws SQLException {

        PreparedStatement psJWTClaimsCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_JWT_CLAIM_CONDITION_SQL;
            psJWTClaimsCondition = conn.prepareStatement(sqlQuery);
            psJWTClaimsCondition.setInt(1, pipelineId);
            psJWTClaimsCondition.setString(2, jwtClaimsCondition.getClaimUrl());
            psJWTClaimsCondition.setString(3, jwtClaimsCondition.getAttribute());
            psJWTClaimsCondition.setBoolean(4, jwtClaimsCondition.isInvertCondition());
            psJWTClaimsCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psJWTClaimsCondition, null, null);
        }
    }

    @Override
    public void addApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {

        Connection conn = null;
        PreparedStatement policyStatement = null;
        boolean hasCustomAttrib = false;
        try {
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_APPLICATION_POLICY_SQL;
            if (hasCustomAttrib) {
                addQuery = SQLConstants.INSERT_APPLICATION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            if (hasCustomAttrib) {
                policyStatement.setBlob(12, new ByteArrayInputStream(policy.getCustomAttributes()));
            }
            policyStatement.executeUpdate();

            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isAppPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_APP, policy.getTenantId(),
                    policy.getPolicyName());

            if (isAppPolicyExists) {
                log.warn(
                        "Application Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleExceptionWithCode("Failed to add Application Policy: " + policy, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Application Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isAppPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_APP, policy.getTenantId(),
                        policy.getPolicyName());

                if (isAppPolicyExists) {
                    log.warn("Application Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleExceptionWithCode("Failed to add Application Policy: " + policy, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String policyName, int tenantId) throws APIManagementException {

        ApplicationPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APPLICATION_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new ApplicationPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get application policy: " + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }


    /**
     * Populated common attributes of policy type objects to <code>policy</code>
     * from <code>resultSet</code>
     *
     * @param policy    initiallized {@link Policy} object to populate
     * @param resultSet {@link ResultSet} with data to populate <code>policy</code>
     * @throws SQLException
     */
    private void setCommonPolicyDetails(Policy policy, ResultSet resultSet) throws SQLException {

        QuotaPolicy quotaPolicy = new QuotaPolicy();
        String prefix = "";

        if (policy instanceof APIPolicy) {
            prefix = "DEFAULT_";
        }

        quotaPolicy.setType(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE));
        if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.REQUEST_COUNT_TYPE)) {
            RequestCountLimit reqLimit = new RequestCountLimit();
            reqLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            reqLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            reqLimit.setRequestCount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(reqLimit);
        } else if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.BANDWIDTH_TYPE)) {
            BandwidthLimit bandLimit = new BandwidthLimit();
            bandLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            bandLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            bandLimit.setDataAmount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            bandLimit.setDataUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_UNIT));
            quotaPolicy.setLimit(bandLimit);
        } else if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.EVENT_COUNT_TYPE)) {
            EventCountLimit eventCountLimit = new EventCountLimit();
            eventCountLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            eventCountLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            eventCountLimit.setEventCount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(eventCountLimit);
        }

        policy.setUUID(resultSet.getString(ThrottlePolicyConstants.COLUMN_UUID));
        policy.setDescription(resultSet.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
        policy.setDisplayName(resultSet.getString(ThrottlePolicyConstants.COLUMN_DISPLAY_NAME));
        policy.setPolicyId(resultSet.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
        policy.setTenantId(resultSet.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
        // TODO:// set tenantdomain
        //policy.setTenantDomain(IdentityTenantUtil.getTenantDomain(policy.getTenantId()));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    @Override
    public void addSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {

        Connection conn = null;
        PreparedStatement policyStatement = null;
        boolean hasCustomAttrib = false;

        try {
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_SQL;
            if (hasCustomAttrib) {
                addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setInt(12, policy.getRateLimitCount());
            policyStatement.setString(13, policy.getRateLimitTimeUnit());
            policyStatement.setBoolean(14, policy.isStopOnQuotaReach());
            policyStatement.setInt(15, policy.getGraphQLMaxDepth());
            policyStatement.setInt(16, policy.getGraphQLMaxComplexity());
            policyStatement.setString(17, policy.getBillingPlan());
            if (hasCustomAttrib) {
                policyStatement.setBytes(18, policy.getCustomAttributes());
                policyStatement.setString(19, policy.getMonetizationPlan());
                policyStatement.setString(20,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(21,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(22,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(23,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                policyStatement.setInt(24, policy.getSubscriberCount());
            } else {
                policyStatement.setString(18, policy.getMonetizationPlan());
                policyStatement.setString(19,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(20,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(21,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(22,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                policyStatement.setInt(23, policy.getSubscriberCount());
            }
            policyStatement.executeUpdate();
            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB,
                    policy.getTenantId(),
                    policy.getPolicyName());

            if (isSubscriptionPolicyExists) {
                log.warn(
                        "Subscription Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleExceptionWithCode("Failed to add Subscription Policy: " + policy, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Subscription Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB,
                        policy.getTenantId(),
                        policy.getPolicyName());

                if (isSubscriptionPolicyExists) {
                    log.warn("Subscription Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleExceptionWithCode("Failed to add Subscription Policy: " + policy, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName, int tenantId) throws APIManagementException {

        SubscriptionPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new SubscriptionPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setRateLimitCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                policy.setRateLimitTimeUnit(resultSet.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                policy.setStopOnQuotaReach(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                policy.setBillingPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                policy.setGraphQLMaxDepth(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                policy.setGraphQLMaxComplexity(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                policy.setSubscriberCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get subscription policy: " + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (IOException e) {
            String error = "Error while converting input stream to byte array";
            handleExceptionWithCode(error, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, error));
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    @Override
    public void addGlobalPolicy(GlobalPolicy policy) throws APIManagementException {

        Connection conn = null;
        PreparedStatement policyStatement = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_GLOBAL_POLICY_SQL;
            policyStatement = conn.prepareStatement(addQuery);
            policyStatement.setString(1, policy.getPolicyName());
            policyStatement.setInt(2, policy.getTenantId());
            policyStatement.setString(3, policy.getKeyTemplate());
            policyStatement.setString(4, policy.getDescription());

            InputStream siddhiQueryInputStream;
            byte[] byteArray = policy.getSiddhiQuery().getBytes(Charset.defaultCharset());
            int lengthOfBytes = byteArray.length;
            siddhiQueryInputStream = new ByteArrayInputStream(byteArray);
            policyStatement.setBinaryStream(5, siddhiQueryInputStream, lengthOfBytes);
            policyStatement.setBoolean(6, false);
            policyStatement.setString(7, UUID.randomUUID().toString());
            policyStatement.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // rollback failed. exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Global Policy: " + policy.toString(), ex);
                }
            }
            handleExceptionWithCode("Failed to add Global Policy: " + policy, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    @Override
    public GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICY;

        GlobalPolicy globalPolicy = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, policyName);
            rs = ps.executeQuery();

            if (rs.next()) {
                String siddhiQuery = null;
                globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while executing SQL", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return globalPolicy;
    }

    @Override
    public APIPolicy getAPIPolicy(String policyName, int tenantId) throws APIManagementException {

        APIPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single result
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new APIPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setUserLevel(resultSet.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                policy.setPipelines(getPipelines(policy.getPolicyId()));
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get api policy: " + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves list of pipelines for the policy with policy Id: <code>policyId</code>
     *
     * @param policyId policy id of the pipelines
     * @return list of pipelines
     * @throws APIManagementException
     */
    private ArrayList<Pipeline> getPipelines(int policyId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement pipelinesStatement = null;
        ResultSet resultSet = null;
        ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>();

        try {
            connection = APIMgtDBUtil.getConnection();
            pipelinesStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_PIPELINES_SQL);
            int unitTime = 0;
            int quota = 0;
            int pipelineId = -1;
            String timeUnit = null;
            String quotaUnit = null;
            String description;
            pipelinesStatement.setInt(1, policyId);
            resultSet = pipelinesStatement.executeQuery();

            while (resultSet.next()) {
                Pipeline pipeline = new Pipeline();
                ArrayList<Condition> conditions = null;
                QuotaPolicy quotaPolicy = new QuotaPolicy();
                quotaPolicy.setType(resultSet.getString(ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE));
                timeUnit = resultSet.getString(ThrottlePolicyConstants.COLUMN_TIME_UNIT);
                quotaUnit = resultSet.getString(ThrottlePolicyConstants.COLUMN_QUOTA_UNIT);
                unitTime = resultSet.getInt(ThrottlePolicyConstants.COLUMN_UNIT_TIME);
                quota = resultSet.getInt(ThrottlePolicyConstants.COLUMN_QUOTA);
                pipelineId = resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                description = resultSet.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION);
                if (PolicyConstants.REQUEST_COUNT_TYPE.equals(quotaPolicy.getType())) {
                    RequestCountLimit requestCountLimit = new RequestCountLimit();
                    requestCountLimit.setUnitTime(unitTime);
                    requestCountLimit.setTimeUnit(timeUnit);
                    requestCountLimit.setRequestCount(quota);
                    quotaPolicy.setLimit(requestCountLimit);
                } else if (PolicyConstants.BANDWIDTH_TYPE.equals(quotaPolicy.getType())) {
                    BandwidthLimit bandwidthLimit = new BandwidthLimit();
                    bandwidthLimit.setUnitTime(unitTime);
                    bandwidthLimit.setTimeUnit(timeUnit);
                    bandwidthLimit.setDataUnit(quotaUnit);
                    bandwidthLimit.setDataAmount(quota);
                    quotaPolicy.setLimit(bandwidthLimit);
                }

                conditions = getConditions(pipelineId);
                pipeline.setConditions(conditions);
                pipeline.setQuotaPolicy(quotaPolicy);
                pipeline.setId(pipelineId);
                pipeline.setDescription(description);
                pipelines.add(pipeline);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get pipelines for policyId: " + policyId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(pipelinesStatement, connection, resultSet);
        }
        return pipelines;
    }


    /**
     * Retrieves list of Conditions for a pipeline specified by <code>pipelineId</code>
     *
     * @param pipelineId pipeline Id with conditions to retrieve
     * @return list of Conditions for a pipeline
     * @throws APIManagementException
     */
    private ArrayList<Condition> getConditions(int pipelineId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        String startingIP = null;
        String endingIP = null;
        String specificIP = null;
        boolean invert;
        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_IP_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                startingIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_STARTING_IP);
                endingIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_ENDING_IP);
                specificIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_SPECIFIC_IP);
                invert = resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_WITHIN_IP_RANGE);

                if (specificIP != null && !"".equals(specificIP)) {
                    IPCondition ipCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
                    ipCondition.setSpecificIP(specificIP);
                    ipCondition.setInvertCondition(invert);
                    conditions.add(ipCondition);
                } else if (startingIP != null && !"".equals(startingIP)) {

                     /*
                     Assumes availability of starting ip means ip range is enforced.
                     Therefore availability of ending ip is not checked.
                    */
                    IPCondition ipRangeCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
                    ipRangeCondition.setStartingIP(startingIP);
                    ipRangeCondition.setEndingIP(endingIP);
                    ipRangeCondition.setInvertCondition(invert);
                    conditions.add(ipRangeCondition);
                }
            }
            setHeaderConditions(pipelineId, conditions);
            setQueryParameterConditions(pipelineId, conditions);
            setJWTClaimConditions(pipelineId, conditions);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get conditions for pipelineId: " + pipelineId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
        return conditions;
    }


    /**
     * Add Header conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setHeaderConditions(int pipelineId, ArrayList<Condition> conditions) throws APIManagementException {

        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_HEADER_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                HeaderCondition headerCondition = new HeaderCondition();
                headerCondition.setHeader(resultSet.getString(ThrottlePolicyConstants.COLUMN_HEADER_FIELD_NAME));
                headerCondition.setValue(resultSet.getString(ThrottlePolicyConstants.COLUMN_HEADER_FIELD_VALUE));
                headerCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_HEADER_FIELD_MAPPING));
                conditions.add(headerCondition);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get header conditions for pipelineId: " + pipelineId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    /**
     * Add Query parameter conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setQueryParameterConditions(int pipelineId, ArrayList<Condition> conditions)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_QUERY_PARAMETER_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
                queryParameterCondition
                        .setParameter(resultSet.getString(ThrottlePolicyConstants.COLUMN_PARAMETER_NAME));
                queryParameterCondition.setValue(resultSet.getString(ThrottlePolicyConstants.COLUMN_PARAMETER_VALUE));
                queryParameterCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_PARAM_MAPPING));
                conditions.add(queryParameterCondition);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get query parameter conditions for pipelineId: " + pipelineId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    /**
     * Add JWT claim conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setJWTClaimConditions(int pipelineId, ArrayList<Condition> conditions) throws APIManagementException {

        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_JWT_CLAIM_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
                jwtClaimsCondition.setClaimUrl(resultSet.getString(ThrottlePolicyConstants.COLUMN_CLAIM_URI));
                jwtClaimsCondition.setAttribute(resultSet.getString(ThrottlePolicyConstants.COLUMN_CLAIM_ATTRIBUTE));
                jwtClaimsCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_CLAIM_MAPPING));
                conditions.add(jwtClaimsCondition);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get jwt claim conditions for pipelineId: " + pipelineId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    @Override
    public APIPolicy updateAPIPolicy(APIPolicy policy) throws APIManagementException {

        String updateQuery;
        int policyId = 0;
        String selectQuery;
        if (policy != null) {
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                selectQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL;
                updateQuery = SQLConstants.ThrottleSQLConstants.UPDATE_API_POLICY_SQL;
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                selectQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_BY_UUID_SQL;
                updateQuery = SQLConstants.ThrottleSQLConstants.UPDATE_API_POLICY_BY_UUID_SQL;
            } else {
                String errorMsg = "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                        + " should be provided. Name: " + policy.getPolicyName()
                        + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.BAD_POLICY_OBJECT);
            }
        } else {
            String errorMsg = "Provided Policy to update is null";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.INTERNAL_ERROR);
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 PreparedStatement deleteStatement = connection.prepareStatement(SQLConstants
                         .ThrottleSQLConstants.DELETE_CONDITION_GROUP_SQL);
                 PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                if (selectQuery.equals(SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL)) {
                    selectStatement.setString(1, policy.getPolicyName());
                    selectStatement.setInt(2, policy.getTenantId());
                } else {
                    selectStatement.setString(1, policy.getUUID());
                }
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        policyId = resultSet.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID);
                    }
                }
                deleteStatement.setInt(1, policyId);
                deleteStatement.executeUpdate();
                if (!StringUtils.isEmpty(policy.getDisplayName())) {
                    updateStatement.setString(1, policy.getDisplayName());
                } else {
                    updateStatement.setString(1, policy.getPolicyName());
                }
                updateStatement.setString(2, policy.getDescription());
                updateStatement.setString(3, policy.getDefaultQuotaPolicy().getType());
                if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                    RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                    updateStatement.setLong(4, limit.getRequestCount());
                    updateStatement.setString(5, null);
                } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                    BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
                    updateStatement.setLong(4, limit.getDataAmount());
                    updateStatement.setString(5, limit.getDataUnit());
                } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                    EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                    updateStatement.setLong(4, limit.getEventCount());
                    updateStatement.setString(5, null);
                }
                updateStatement.setLong(6, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
                updateStatement.setString(7, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());

                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(8, policy.getPolicyName());
                    updateStatement.setInt(9, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(8, policy.getUUID());
                }
                int updatedRawCount = updateStatement.executeUpdate();
                if (updatedRawCount > 0) {
                    List<Pipeline> pipelines = policy.getPipelines();
                    if (pipelines != null) {
                        for (Pipeline pipeline : pipelines) { // add each pipeline data to AM_CONDITION_GROUP table
                            addPipeline(pipeline, policyId, connection);
                        }
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // rollback failed. exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Global Policy: " + policy.toString(), ex);
                }
                handleExceptionWithCode("Failed to update API policy: "
                                + policy.getPolicyName() + '-' + policy.getTenantId()
                        , e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update API policy: "
                    + policy.getPolicyName() + '-' + policy.getTenantId(), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policy;
    }

    @Override
    public void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        boolean hasCustomAttrib = false;
        String updateQuery;

        if (policy.getTenantId() == -1 || StringUtils.isEmpty(policy.getPolicyName())) {
            String errorMsg = "Policy object doesn't contain mandatory parameters. Name: " + policy.getPolicyName() +
                    ", Tenant Id: " + policy.getTenantId();
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.BAD_POLICY_OBJECT);
        }

        try {
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL;
                }
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_BY_UUID_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL;
                }
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.BAD_POLICY_OBJECT);
            }

            updateStatement = connection.prepareStatement(updateQuery);
            if (!StringUtils.isEmpty(policy.getDisplayName())) {
                updateStatement.setString(1, policy.getDisplayName());
            } else {
                updateStatement.setString(1, policy.getPolicyName());
            }
            updateStatement.setString(2, policy.getDescription());
            updateStatement.setString(3, policy.getDefaultQuotaPolicy().getType());

            if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getRequestCount());
                updateStatement.setString(5, null);
            } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getDataAmount());
                updateStatement.setString(5, limit.getDataUnit());
            }
            updateStatement.setLong(6, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            updateStatement.setString(7, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());

            if (hasCustomAttrib) {
                updateStatement.setBlob(8, new ByteArrayInputStream(policy.getCustomAttributes()));
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(9, policy.getPolicyName());
                    updateStatement.setInt(10, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(9, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(8, policy.getPolicyName());
                    updateStatement.setInt(9, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(8, policy.getUUID());
                }
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Application Policy: " + policy.toString(), ex);
                }
            }
            handleExceptionWithCode(
                    "Failed to update application policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    @Override
    public void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        boolean hasCustomAttrib = false;
        String updateQuery;

        try {
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL;
                }
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_BY_UUID_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL;
                }
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.BAD_POLICY_OBJECT);
            }

            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(updateQuery);
            if (!StringUtils.isEmpty(policy.getDisplayName())) {
                updateStatement.setString(1, policy.getDisplayName());
            } else {
                updateStatement.setString(1, policy.getPolicyName());
            }
            updateStatement.setString(2, policy.getDescription());
            updateStatement.setString(3, policy.getDefaultQuotaPolicy().getType());

            if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getRequestCount());
                updateStatement.setString(5, null);
            } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getDataAmount());
                updateStatement.setString(5, limit.getDataUnit());
            } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getEventCount());
                updateStatement.setString(5, null);
            }

            updateStatement.setLong(6, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            updateStatement.setString(7, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            updateStatement.setInt(8, policy.getRateLimitCount());
            updateStatement.setString(9, policy.getRateLimitTimeUnit());
            updateStatement.setBoolean(10, policy.isStopOnQuotaReach());
            updateStatement.setInt(11, policy.getGraphQLMaxDepth());
            updateStatement.setInt(12, policy.getGraphQLMaxComplexity());
            updateStatement.setString(13, policy.getBillingPlan());
            if (hasCustomAttrib) {
                long lengthOfStream = policy.getCustomAttributes().length;
                updateStatement.setBinaryStream(14, new ByteArrayInputStream(policy.getCustomAttributes()),
                        lengthOfStream);
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(15, policy.getMonetizationPlan());
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(20, policy.getSubscriberCount());
                    updateStatement.setString(21, policy.getPolicyName());
                    updateStatement.setInt(22, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(15, policy.getMonetizationPlan());
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(20, policy.getSubscriberCount());
                    updateStatement.setString(21, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(19, policy.getSubscriberCount());
                    updateStatement.setString(20, policy.getPolicyName());
                    updateStatement.setInt(21, policy.getTenantId());

                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(19, policy.getSubscriberCount());
                    updateStatement.setString(20, policy.getUUID());
                }
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Subscription Policy: " + policy.toString(), ex);
                }
            }
            handleExceptionWithCode(
                    "Failed to update subscription policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    @Override
    public void updateGlobalPolicy(GlobalPolicy policy) throws APIManagementException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        InputStream siddhiQueryInputStream;

        try {
            byte[] byteArray = policy.getSiddhiQuery().getBytes(Charset.defaultCharset());
            int lengthOfBytes = byteArray.length;
            siddhiQueryInputStream = new ByteArrayInputStream(byteArray);
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateStatement = connection.prepareStatement(SQLConstants.UPDATE_GLOBAL_POLICY_SQL);
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateStatement = connection.prepareStatement(SQLConstants.UPDATE_GLOBAL_POLICY_BY_UUID_SQL);
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.BAD_POLICY_OBJECT);
            }

            updateStatement.setString(1, policy.getDescription());
            updateStatement.setBinaryStream(2, siddhiQueryInputStream, lengthOfBytes);
            updateStatement.setString(3, policy.getKeyTemplate());
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateStatement.setString(4, policy.getPolicyName());
                updateStatement.setInt(5, policy.getTenantId());
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateStatement.setString(4, policy.getUUID());
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Global Policy: " + policy.toString(), ex);
                }
            }
            handleExceptionWithCode("Failed to update global policy: "
                            + policy.getPolicyName() + '-' + policy.getTenantId(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    @Override
    public String[] getPolicyNames(String policyLevel, String username) throws APIManagementException {

        List<String> names = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = null;

        int tenantID = APIUtil.getTenantId(username);

        try {
            conn = APIMgtDBUtil.getConnection();
            if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
                sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_APP_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_SUB_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_GLOBAL_POLICY_NAMES;
            }
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
            }

        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public void removeThrottlePolicy(String policyLevel, String policyName, int tenantId)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement deleteStatement = null;
        String query = null;
        String deleteTierPermissionsQuery = null;

        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            query = SQLConstants.DELETE_APPLICATION_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            //in case of a subscription policy delete we have to remove throttle tier permissions as well
            query = SQLConstants.DELETE_SUBSCRIPTION_POLICY_SQL;
            deleteTierPermissionsQuery = SQLConstants.DELETE_THROTTLE_TIER_BY_NAME_PERMISSION_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            query = SQLConstants.ThrottleSQLConstants.DELETE_API_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            query = SQLConstants.DELETE_GLOBAL_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteStatement = connection.prepareStatement(query);
            deleteStatement.setInt(1, tenantId);
            deleteStatement.setString(2, policyName);
            deleteStatement.executeUpdate();
            if (deleteTierPermissionsQuery != null) {
                deleteStatement = connection.prepareStatement(deleteTierPermissionsQuery);
                deleteStatement.setString(1, policyName);
                deleteStatement.setInt(2, tenantId);
                deleteStatement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to remove policy " + policyLevel + '-' + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteStatement, connection, null);
        }
    }

    @Override
    public boolean isKeyTemplatesExist(GlobalPolicy policy) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            sqlQuery = SQLConstants.GET_GLOBAL_POLICY_KEY_TEMPLATE;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, policy.getTenantId());
            ps.setString(2, policy.getKeyTemplate());
            ps.setString(3, policy.getPolicyName());
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            handleException("Error while executing SQL to get GLOBAL_POLICY_KEY_TEMPLATE", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return false;
    }

    @Override
    public boolean hasApplicationPolicyAttachedToApplication(String policyName, String organization) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SQLConstants.ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_APPLICATION)) {
                preparedStatement.setString(1, organization);
                preparedStatement.setString(2,policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while checking existence of Policy "
                    + policyName + "Attached to Application.", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return false;
    }

    @Override
    public boolean hasSubscriptionPolicyAttached(String policyName, String organization) throws APIManagementException {

        String sql = SQLConstants.ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_SUBSCRIPTION_SUPER_TENANT;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, organization);
                preparedStatement.setString(2, policyName);
                preparedStatement.setString(3, organization);
                preparedStatement.setString(4, policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while checking existence of Policy "
                    + policyName + "Attached to Subscription.", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return false;
    }

    @Override
    public boolean hasAPIPolicyAttached(String policyName, String organization) throws APIManagementException {

        String sql = SQLConstants.ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_API_RESOURCE_TENANT;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, organization);
                preparedStatement.setString(2, policyName);
                preparedStatement.setString(3, organization);
                preparedStatement.setString(4, policyName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while checking existence of Policy "
                    + policyName + "Attached to API/Resouce.", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return false;
    }

    @Override
    public APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException {

        APIPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single result
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new APIPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setUserLevel(resultSet.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                policy.setPipelines(getPipelines(policy.getPolicyId()));
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get api policy: " + uuid, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException {

        ApplicationPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APPLICATION_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new ApplicationPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get application policy: " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException {

        SubscriptionPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new SubscriptionPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setRateLimitCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                policy.setRateLimitTimeUnit(resultSet.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                policy.setStopOnQuotaReach(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                policy.setBillingPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                policy.setGraphQLMaxDepth(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                policy.setGraphQLMaxComplexity(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                policy.setSubscriberCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
                if (APIConstants.COMMERCIAL_TIER_PLAN.equals(resultSet.getString(
                        ThrottlePolicyConstants.COLUMN_BILLING_PLAN))) {
                    policy.setMonetizationPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                    Map<String, String> monetizationPlanProperties = new HashMap<String, String>();
                    monetizationPlanProperties.put(APIConstants.Monetization.FIXED_PRICE,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                    monetizationPlanProperties.put(APIConstants.Monetization.BILLING_CYCLE,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                    monetizationPlanProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                    monetizationPlanProperties.put(APIConstants.Monetization.CURRENCY,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                    policy.setMonetizationPlanProperties(monetizationPlanProperties);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get subscription policy: " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (IOException e) {
            handleExceptionWithCode("Error while converting input stream to byte array", e,
                    ExceptionCodes.INTERNAL_ERROR);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    @Override
    public GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICY_BY_UUID;

        GlobalPolicy globalPolicy = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, uuid);
            rs = ps.executeQuery();

            if (rs.next()) {
                String siddhiQuery = null;
                globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving global policy by uuid " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return globalPolicy;
    }

    @Override
    public APIPolicy[] getAPIPolicies(int tenantID) throws APIManagementException {

        List<APIPolicy> policies = new ArrayList<APIPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICIES;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIPolicy apiPolicy = new APIPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(apiPolicy, rs);
                apiPolicy.setUserLevel(rs.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));

                policies.add(apiPolicy);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while executing SQL", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new APIPolicy[policies.size()]);
    }

    @Override
    public ApplicationPolicy[] getApplicationPolicies(int tenantID) throws APIManagementException {

        List<ApplicationPolicy> policies = new ArrayList<ApplicationPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_APP_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APP_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                ApplicationPolicy appPolicy = new ApplicationPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(appPolicy, rs);
                policies.add(appPolicy);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while executing SQL", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new ApplicationPolicy[policies.size()]);
    }

    @Override
    public SubscriptionPolicy[] getSubscriptionPolicies(int tenantID) throws APIManagementException {

        List<SubscriptionPolicy> policies = new ArrayList<SubscriptionPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                SubscriptionPolicy subPolicy = new SubscriptionPolicy(
                        rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(subPolicy, rs);
                subPolicy.setRateLimitCount(rs.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                subPolicy.setRateLimitTimeUnit(rs.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                subPolicy.setSubscriberCount(rs.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
                subPolicy.setStopOnQuotaReach(rs.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                subPolicy.setBillingPlan(rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                subPolicy.setGraphQLMaxDepth(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                subPolicy.setGraphQLMaxComplexity(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                subPolicy.setMonetizationPlan(rs.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                Map<String, String> monetizationPlanProperties = subPolicy.getMonetizationPlanProperties();
                monetizationPlanProperties.put(APIConstants.Monetization.FIXED_PRICE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                monetizationPlanProperties.put(APIConstants.Monetization.BILLING_CYCLE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                monetizationPlanProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST,
                        rs.getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                monetizationPlanProperties.put(APIConstants.Monetization.CURRENCY,
                        rs.getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                subPolicy.setMonetizationPlanProperties(monetizationPlanProperties);
                InputStream binary = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    subPolicy.setCustomAttributes(customAttrib);
                }
                policies.add(subPolicy);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while executing SQL", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (IOException e) {
            handleExceptionWithCode("Error while converting input stream to byte array", e, ExceptionCodes.INTERNAL_ERROR);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new SubscriptionPolicy[policies.size()]);
    }

    @Override
    public GlobalPolicy[] getGlobalPolicies(int tenantID) throws APIManagementException {

        List<GlobalPolicy> policies = new ArrayList<GlobalPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_GLOBAL_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                String siddhiQuery = null;
                GlobalPolicy globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
                policies.add(globalPolicy);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while executing SQL", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new GlobalPolicy[policies.size()]);
    }

}
