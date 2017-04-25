package org.wso2.carbon.apimgt.core.dao.impl;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Limit;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains Implementation for policy
 */
public class PolicyDAOImpl implements PolicyDAO {

    private static final Logger log = LoggerFactory.getLogger(PolicyDAOImpl.class);

    public static final String FIFTY_PER_MIN_TIER = "50PerMin";
    private static final String AM_API_POLICY_TABLE_NAME = "AM_API_POLICY";
    private static final String AM_APPLICATION_POLICY_TABLE_NAME = "AM_APPLICATION_POLICY";
    private static final String AM_SUBSCRIPTION_POLICY_TABLE_NAME = "AM_SUBSCRIPTION_POLICY";
    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String GOLD_TIER = "Gold";
    public static final String SILVER_TIER = "Silver";
    public static final String BRONZE_TIER = "Bronze";
    public static final String API_TIER_LEVEL = "API";
    public static final String REQUEST_COUNT_TYPE = "requestCount";
    public static final String SECONDS_TIMUNIT = "s";
    public static final String MINUTE_TIMEUNIT = "min";
    public static final String QUOTA_UNIT = "REQ";
    public static final String TWENTY_PER_MIN_TIER = "20PerMin";
    public static final String FIFTY_PER_MIN_TIER_DESCRIPTION = "50PerMin Tier";
    public static final String TWENTY_PER_MIN_TIER_DESCRIPTION = "20PerMin Tier";

    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIMgtDAOException {
        try {
            if (APIMgtConstants.ThrottlePolicyConstants.API_LEVEL.equals(policyLevel)) {
                return getAPIPolicy(policyName);
            } else if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(policyLevel)) {
                return getApplicationPolicy(policyName);
            } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(policyLevel)) {
                return getSubscriptionPolicy(policyName);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Couldn't find " + policyName + ' ' + policyLevel + "Policy", e);
        }
        return null;
    }

    @Override public List<Policy> getPolicies(String policyLevel) throws APIMgtDAOException {
        try {
            if (APIMgtConstants.ThrottlePolicyConstants.API_LEVEL.equals(policyLevel)) {
                return getAPIPolicies();
            } else if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(policyLevel)) {
                return getApplicationPolicies();
            } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(policyLevel)) {
                return getSubscriptionPolicies();
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Couldn't find " + policyLevel + " policies", e);
        }
        return null;
    }

    @Override
    public void addPolicy(String policyLevel, Policy policy) throws APIMgtDAOException {

        Connection connection;
        try {
            connection = DAOUtil.getConnection();

            //TODO : instead of checking policyLevel, check class type, and remove passing policy level to here
            if (policy instanceof APIPolicy) {
                String type = policy.getDefaultQuotaPolicy().getType();
                Limit limit = policy.getDefaultQuotaPolicy().getLimit();
                if (type.equals(PolicyConstants.REQUEST_COUNT_TYPE)) {
                    if (limit instanceof RequestCountLimit) {
                        addAPIPolicy(connection, policy.getPolicyName(), policy.getDisplayName(),
                                policy.getDescription(),
                                policy.getDefaultQuotaPolicy().getType(),
                                10,
                                policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                                policy.getDefaultQuotaPolicy().getLimit().getTimeUnit(),
                                ((APIPolicy) policy).getPipelines(), API_TIER_LEVEL);
                    }
                } else if (type.equals(PolicyConstants.BANDWIDTH_TYPE)) {
                    if (limit instanceof BandwidthLimit) {
                        addAPIPolicy(connection, policy.getPolicyName(), policy.getDisplayName(),
                                policy.getDescription(), policy.getDefaultQuotaPolicy().getType(),
                                0,
                                limit.getUnitTime(), limit.getTimeUnit(),
                                ((APIPolicy) policy).getPipelines(), API_TIER_LEVEL);

                    }
                }
            } else {
                if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(policyLevel)) {
                    addApplicationPolicy(connection, policy.getPolicyName(), policy.getDisplayName(),
                            policy.getDescription(), policy.getDefaultQuotaPolicy().getType(), 0, "",
                            (int) policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                            policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
                } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(policyLevel)) {
                    addSubscriptionPolicy(connection, policy.getPolicyName(), policy.getDisplayName(),
                            policy.getDescription(), policy.getDefaultQuotaPolicy().getType(), 0, "",
                            (int) policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                            policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new APIMgtDAOException(e);
        }
    }

    @Override
    public void deletePolicy(String policyName, String policyLevel) throws APIMgtDAOException {

        if (APIMgtConstants.ThrottlePolicyConstants.API_LEVEL.equals(policyLevel))  {
            deleteAPIPolicy(policyName);
        } else if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(policyLevel))   {
            deleteApplicationPolicy(policyName);
        } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(policyLevel))   {
            deleteSubscriptionPolicy(policyName);
        }

    }

    /**
     * Retrieves {@link APIPolicy} with name <code>policyName</code>
     * <p>This will retrieve complete details about the APIPolicy with all pipelins and conditions.</p>
     *
     * @param policyName name of the policy to retrieve from the database
     * @return {@link APIPolicy}
     */
    private APIPolicy getAPIPolicy(String policyName) throws SQLException {
        APIPolicy policy = null;
        String sqlQuery = "SELECT * from AM_API_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    policy = new APIPolicy(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(policy, resultSet);
                    policy.setUserLevel(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_APPLICABLE_LEVEL));
                    policy.setPipelines(getPipelines(policy.getUuid(), connection));
                }
            }
        }
        return policy;
    }

    /**
     * Retrieves all API policies.
     *
     * @return
     * @throws SQLException
     */
    private List<Policy> getAPIPolicies() throws SQLException {
        List<Policy> policyList = new ArrayList<>();
        String sqlQuery = "SELECT * from AM_API_POLICY";

        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIPolicy apiPolicy = new APIPolicy(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(apiPolicy, resultSet);
                    apiPolicy.setUserLevel(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                    apiPolicy.setPipelines(getPipelines(apiPolicy.getUuid(), connection));

                    policyList.add(apiPolicy);
                }
            }
        }
        return policyList;
    }

    /**
     * Retrieves all Application policies.
     *
     * @return
     * @throws SQLException
     */
    private List<Policy> getApplicationPolicies() throws SQLException {
        List<Policy> policyList = new ArrayList<>();
        String sqlQuery = "SELECT * from AM_APPLICATION_POLICY";

        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    ApplicationPolicy applicationPolicy = new ApplicationPolicy(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(applicationPolicy, resultSet);
                    policyList.add(applicationPolicy);
                }
            }
        }
        return policyList;
    }

    /**
     * Retrieves all Subscription policies.
     *
     * @return  List of subscriptions.
     * @throws SQLException     If error occurs while retrieving subscription level policies.
     */
    private List<Policy> getSubscriptionPolicies() throws SQLException {
        List<Policy> policyList = new ArrayList<>();
        String sqlQuery = "SELECT * from AM_SUBSCRIPTION_POLICY";

        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(subscriptionPolicy, resultSet);
                    policyList.add(subscriptionPolicy);
                }
            }
        }
        return policyList;
    }

    /**
     * Retrieves {@link ApplicationPolicy} with name <code>policyName</code>
     * <p>This will retrieve complete details about the ApplicationPolicy with all pipelins and conditions.</p>
     *
     * @param policyName name of the policy to retrieve from the database
     * @return {@link ApplicationPolicy}
     */
    private ApplicationPolicy getApplicationPolicy(String policyName) throws SQLException {
        ApplicationPolicy policy = null;
        String sqlQuery = "SELECT * from AM_APPLICATION_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    policy = new ApplicationPolicy(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(policy, resultSet);
                }
            }
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

        quotaPolicy.setType(resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants
                .COLUMN_QUOTA_POLICY_TYPE));
        if (resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.REQUEST_COUNT_TYPE)) {
            RequestCountLimit reqLimit = new RequestCountLimit();
            reqLimit.setUnitTime(resultSet.getLong(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            reqLimit.setTimeUnit(
                    resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            reqLimit.setRequestCount(resultSet.getInt(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(reqLimit);
        } else if (resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.BANDWIDTH_TYPE)) {
            BandwidthLimit bandLimit = new BandwidthLimit();
            bandLimit.setUnitTime(resultSet.getInt(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            bandLimit.setTimeUnit(
                    resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            bandLimit.setDataAmount(resultSet.getInt(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA));
            bandLimit.setDataUnit(
                    resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT));
            quotaPolicy.setLimit(bandLimit);
        }

        policy.setUuid(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID));
        policy.setDescription(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION));
        policy.setDisplayName(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DISPLAY_NAME));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    /**
     * Retrieves list of pipelines for the policy with policy Id: <code>policyId</code>
     *
     * @param policyId policy id of the pipelines
     * @return list of pipelines
     * @throws SQLException
     */
    private ArrayList<Pipeline> getPipelines(String policyId, Connection connection) throws SQLException {
        ArrayList<Pipeline> pipelines = new ArrayList<>();
        final String sqlQuery = "SELECT CONDITION_GROUP_ID,QUOTA_TYPE,QUOTA,QUOTA_UNIT,UNIT_TIME,TIME_UNIT," +
                "DESCRIPTION FROM AM_CONDITION_GROUP WHERE UUID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            int unitTime;
            int quota;
            int pipelineId;
            String timeUnit;
            String quotaUnit;
            String description;
            preparedStatement.setString(1, policyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    Pipeline pipeline = new Pipeline();
                    ArrayList<Condition> conditions;
                    QuotaPolicy quotaPolicy = new QuotaPolicy();
                    quotaPolicy.setType(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_QUOTA_POLICY_TYPE));

                    timeUnit = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT);
                    quotaUnit = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT);
                    unitTime = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME);
                    quota = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA);
                    pipelineId = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                    description = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION);
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
                    conditions = getConditions(pipelineId, connection);
                    pipeline.setConditions(conditions);
                    pipeline.setQuotaPolicy(quotaPolicy);
                    pipeline.setId(pipelineId);
                    pipeline.setDescription(description);
                    pipelines.add(pipeline);
                }
            }
        }
        return pipelines;
    }

    /**
     * Retrieves list of Conditions for a pipeline specified by <code>pipelineId</code>
     *
     * @param pipelineId pipeline Id with conditions to retrieve
     * @return list of Conditions for a pipeline
     * @throws SQLException
     */
    private ArrayList<Condition> getConditions(int pipelineId, Connection connection) throws SQLException {
        final String sqlQuery = "SELECT " + "STARTING_IP, " + "ENDING_IP, " + "SPECIFIC_IP,WITHIN_IP_RANGE " + "FROM " +
                "" + "AM_IP_CONDITION " + "WHERE " + "CONDITION_GROUP_ID = ? ";

        ArrayList<Condition> conditions = new ArrayList<>();
        String startingIP;
        String endingIP;
        String specificIP;
        boolean invert;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    startingIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_STARTING_IP);
                    endingIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_ENDING_IP);
                    specificIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_SPECIFIC_IP);
                    invert = resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_WITHIN_IP_RANGE);

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
                setHeaderConditions(pipelineId, conditions, connection);
                setQueryParameterConditions(pipelineId, conditions, connection);
                setJWTClaimConditions(pipelineId, conditions, connection);
            }
        }
        return conditions;
    }

    /**
     * Add Header conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param connection Connection of the database
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setHeaderConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection) throws
            SQLException {
        final String query = "SELECT " + "HEADER_FIELD_NAME, " + "HEADER_FIELD_VALUE , IS_HEADER_FIELD_MAPPING "
                + " FROM " + "AM_HEADER_FIELD_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    HeaderCondition headerCondition = new HeaderCondition();
                    headerCondition.setHeader(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_NAME));
                    headerCondition.setValue(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_VALUE));
                    headerCondition.setInvertCondition(resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_IS_HEADER_FIELD_MAPPING));
                    conditions.add(headerCondition);
                }
            }
        }
    }

    /**
     * Add Query parameter conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setQueryParameterConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection)
            throws SQLException {
        final String query = "SELECT " + "PARAMETER_NAME,PARAMETER_VALUE , IS_PARAM_MAPPING " + "FROM " +
                "AM_QUERY_PARAMETER_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
                    queryParameterCondition
                            .setParameter(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                                    .COLUMN_PARAMETER_NAME));
                    queryParameterCondition.setValue(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_PARAMETER_VALUE));
                    queryParameterCondition.setInvertCondition(
                            resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_IS_PARAM_MAPPING));
                    conditions.add(queryParameterCondition);
                }
            }
        }
    }

    /**
     * Add JWT claim conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setJWTClaimConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection) throws
            SQLException {
        final String query = "SELECT " + "CLAIM_URI, " + "CLAIM_ATTRIB , IS_CLAIM_MAPPING " + "FROM " +
                "AM_JWT_CLAIM_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
                    jwtClaimsCondition.setClaimUrl(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_URI));
                    jwtClaimsCondition.setAttribute(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_ATTRIBUTE));
                    jwtClaimsCondition.setInvertCondition(resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_IS_CLAIM_MAPPING));
                    conditions.add(jwtClaimsCondition);
                }
            }
        }
    }
    /**
     * Retrieves the name of Subscription Policy
     *
     * @param policyId  Subscription policy ID
     * @return Tier name of given Subscription policy ID
     * @throws APIMgtDAOException   If failed to get subscription.
     */
    public String getSubscriptionTierName(String policyId) throws APIMgtDAOException {

        return null;
    }

    /**
     * @see PolicyDAO#getSubscriptionPolicy(String) 
     */
    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIMgtDAOException {
        final String query = "SELECT UUID, NAME, DISPLAY_NAME, DESCRIPTION, IS_DEPLOYED, CUSTOM_ATTRIBUTES " +
                "FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";
        SubscriptionPolicy subscriptionPolicy;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    subscriptionPolicy = new SubscriptionPolicy(rs.getString("NAME"));
                    subscriptionPolicy.setUuid(rs.getString("UUID"));
                    subscriptionPolicy.setDisplayName(rs.getString("DISPLAY_NAME"));
                    subscriptionPolicy.setDescription(rs.getString("DESCRIPTION"));
                    subscriptionPolicy.setDeployed(rs.getBoolean("IS_DEPLOYED"));
                    subscriptionPolicy.setCustomAttributes(rs.getString("CUSTOM_ATTRIBUTES"));
                    return subscriptionPolicy;
                }
            }
        } catch (SQLException e) {
            log.error("An Error occurred while retrieving Policy with name [" + policyName + "], " , e);
            throw new APIMgtDAOException("Couldn't retrieve subscription tier for name : " + policyName, e);
        }
        return null;
    }

    /**
     * Retrieves Application Policy by UUID
     *
     * @param policyId Application policy ID
     * @return {@link ApplicationPolicy} of given UUID
     * @throws APIMgtDAOException   If failed to get application policy.
     */
    @Override
    public ApplicationPolicy getApplicationPolicyById(String policyId) throws APIMgtDAOException {
        final String query = "SELECT UUID, NAME, DISPLAY_NAME, DESCRIPTION, IS_DEPLOYED, CUSTOM_ATTRIBUTES " +
                "FROM AM_APPLICATION_POLICY WHERE UUID = ?";
        ApplicationPolicy applicationPolicy;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, policyId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    applicationPolicy = new ApplicationPolicy(rs.getString("NAME"));
                    applicationPolicy.setUuid(rs.getString("UUID"));
                    applicationPolicy.setDisplayName(rs.getString("DISPLAY_NAME"));
                    applicationPolicy.setDescription(rs.getString("DESCRIPTION"));
                    applicationPolicy.setDeployed(rs.getBoolean("IS_DEPLOYED"));
                    InputStream inputStream = rs.getBinaryStream("CUSTOM_ATTRIBUTES");
                    if (inputStream != null) {
                        applicationPolicy.setCustomAttributes(IOUtils.toString(inputStream));
                    } else {
                        applicationPolicy.setCustomAttributes("");
                    }
                    return applicationPolicy;
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException("Couldn't retrieve subscription tier for id : " + policyId, e);
        }
        return null;
    }

    /**
     * @see PolicyDAO#getLastUpdatedTimeOfThrottlingPolicy(String, String)
     */
    @Override
    public String getLastUpdatedTimeOfThrottlingPolicy(String policyLevel, String policyName)
            throws APIMgtDAOException {
        if (APIMgtConstants.ThrottlePolicyConstants.API_LEVEL.equals(policyLevel)) {
            return getLastUpdatedTimeOfAPIPolicy(policyName);
        } else if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(policyLevel)) {
            return getLastUpdatedTimeOfApplicationPolicy(policyName);
        } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(policyLevel)) {
            return getLastUpdatedTimeOfSubscriptionPolicy(policyName);
        } else {
            throw new APIMgtDAOException("Invalid policy level " + policyLevel);
        }
    }

    /**
     * @see PolicyDAO#getLastUpdatedTimeOfAPIPolicy(String)
     */
    @Override
    public String getLastUpdatedTimeOfAPIPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_API_POLICY_TABLE_NAME, policyName);
    }

    /**
     * @see PolicyDAO#getLastUpdatedTimeOfApplicationPolicy(String)
     */
    @Override
    public String getLastUpdatedTimeOfApplicationPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_APPLICATION_POLICY_TABLE_NAME, policyName);
    }

    /**
     * @see PolicyDAO#getLastUpdatedTimeOfSubscriptionPolicy(String)
     */
    @Override
    public String getLastUpdatedTimeOfSubscriptionPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_SUBSCRIPTION_POLICY_TABLE_NAME, policyName);
    }

    static void initDefaultPolicies() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                if (!isDefaultPoliciesExist(connection)) {
                    connection.setAutoCommit(false);

                    addAPIPolicy(connection, UNLIMITED_TIER, UNLIMITED_TIER, UNLIMITED_TIER,
                            REQUEST_COUNT_TYPE, 1, 60,
                            SECONDS_TIMUNIT, null, API_TIER_LEVEL);
                    addAPIPolicy(connection, GOLD_TIER, GOLD_TIER, GOLD_TIER, REQUEST_COUNT_TYPE,
                            1, 60, SECONDS_TIMUNIT, null, API_TIER_LEVEL);
                    addAPIPolicy(connection, SILVER_TIER, SILVER_TIER, SILVER_TIER, REQUEST_COUNT_TYPE,
                            1, 60, SECONDS_TIMUNIT, null, API_TIER_LEVEL);
                    addAPIPolicy(connection, BRONZE_TIER, BRONZE_TIER, BRONZE_TIER, REQUEST_COUNT_TYPE,
                            1, 60, SECONDS_TIMUNIT, null, API_TIER_LEVEL);

                    addSubscriptionPolicy(connection, UNLIMITED_TIER, UNLIMITED_TIER, UNLIMITED_TIER,
                            REQUEST_COUNT_TYPE,
                            Integer.MAX_VALUE, QUOTA_UNIT, 1, MINUTE_TIMEUNIT);
                    addSubscriptionPolicy(connection, GOLD_TIER, GOLD_TIER, GOLD_TIER, REQUEST_COUNT_TYPE, 5000,
                            QUOTA_UNIT, 1,
                            MINUTE_TIMEUNIT);
                    addSubscriptionPolicy(connection, SILVER_TIER, SILVER_TIER, SILVER_TIER, REQUEST_COUNT_TYPE, 2000,
                            QUOTA_UNIT, 1,
                            MINUTE_TIMEUNIT);
                    addSubscriptionPolicy(connection, BRONZE_TIER, BRONZE_TIER, BRONZE_TIER, REQUEST_COUNT_TYPE, 1000,
                            QUOTA_UNIT, 1,
                            MINUTE_TIMEUNIT);

                    addApplicationPolicy(connection, FIFTY_PER_MIN_TIER, FIFTY_PER_MIN_TIER,
                            FIFTY_PER_MIN_TIER_DESCRIPTION, REQUEST_COUNT_TYPE, 10,
                            QUOTA_UNIT, 1,
                            SECONDS_TIMUNIT);
                    addApplicationPolicy(connection, TWENTY_PER_MIN_TIER, TWENTY_PER_MIN_TIER,
                            TWENTY_PER_MIN_TIER_DESCRIPTION, REQUEST_COUNT_TYPE, 50,
                            QUOTA_UNIT, 1,
                            SECONDS_TIMUNIT);
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    private static boolean isDefaultPoliciesExist(Connection connection) throws SQLException {
        final String query = "SELECT 1 FROM AM_API_POLICY";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void addAPIPolicy(Connection connection, String name, String displayName,
                                     String description, String quotaType, int quota, long unitTime,
                                     String timeUnit, List<Pipeline> pipelines,
                                     String applicableLevel) throws SQLException {

        final String query = "INSERT INTO AM_API_POLICY (UUID, NAME, DISPLAY_NAME, DESCRIPTION, " +
                "DEFAULT_QUOTA_TYPE, DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DEFAULT_UNIT_TIME," +
                " DEFAULT_TIME_UNIT, APPLICABLE_LEVEL) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        String policyID = UUID.randomUUID().toString();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyID);
            statement.setString(2, name);
            statement.setString(3, displayName);
            statement.setString(4, description);
            statement.setString(5, quotaType);
            statement.setInt(6, quota);
            statement.setString(7, "MB");
            statement.setLong(8, unitTime);
            statement.setString(9, timeUnit);
            statement.setString(10, applicableLevel);

            statement.execute();

            if (pipelines != null) {
                addAPIPipeline(connection, pipelines, policyID);
            }
        }
    }

    /**
     * Adding pipelines for API policy
     * @param connection
     * @param pipelines
     * @param uuid
     * @throws SQLException*/

    private static void addAPIPipeline(Connection connection, List<Pipeline> pipelines, String uuid)
            throws SQLException {

        final String query = "INSERT INTO AM_CONDITION_GROUP (UUID, " +
                "QUOTA_TYPE, QUOTA, QUOTA_UNIT, UNIT_TIME, TIME_UNIT) " +
                "VALUES (?,?,?,?,?,?)";
        final String conIDquery = "SELECT CONDITION_GROUP_ID FROM AM_CONDITION_GROUP WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Pipeline pipeline : pipelines) {
                statement.setString(1, uuid);
                statement.setString(2, pipeline.getQuotaPolicy().getType());
                Limit limit = pipeline.getQuotaPolicy().getLimit();

                if (pipeline.getQuotaPolicy().getType().equals(PolicyConstants.BANDWIDTH_TYPE)) {
                    if (limit instanceof BandwidthLimit) {
                        statement.setLong(3, ((BandwidthLimit) limit).getDataAmount());
                        statement.setString(4, ((BandwidthLimit) limit).getDataUnit());
                    }
                } else if (pipeline.getQuotaPolicy().getType().equals(PolicyConstants.REQUEST_COUNT_TYPE)) {
                    if (limit instanceof RequestCountLimit) {
                        statement.setLong(3, ((RequestCountLimit) limit).getRequestCount());
                        statement.setString(4, "");
                    }
                }
                statement.setLong(5, pipeline.getQuotaPolicy().getLimit().getUnitTime());
                statement.setString(6, pipeline.getQuotaPolicy().getLimit().getTimeUnit());
                statement.execute();

                try (PreparedStatement statement1 = connection.prepareStatement(conIDquery)) {
                    statement1.setString(1, uuid);
                    try (ResultSet resultSet = statement1.executeQuery()) {
                        while (resultSet.next()) {
                            if (resultSet.last()) {
                                int conID = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.
                                        COLUMN_CONDITION_ID);
                                List<Condition> conditionList = pipeline.getConditions();
                                for (Condition condition : conditionList) {
                                    if (condition instanceof IPCondition) {
                                        addIPCondition(connection, (IPCondition) condition, conID);
                                    } else if (condition instanceof HeaderCondition) {
                                        addHeaderCondition(connection,
                                                (HeaderCondition) condition, conID);
                                    } else if (condition instanceof JWTClaimsCondition) {
                                        addJWTClaimCondition(connection,
                                                (JWTClaimsCondition) condition, conID);
                                    } else if (condition instanceof QueryParameterCondition) {
                                        addParamCondition(connection,
                                                (QueryParameterCondition) condition, conID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adding IP Condition to DB
     * @param connection
     * @param ipCondition
     * @param conId
     * @throws SQLException
     */
    private static void addIPCondition(Connection connection, IPCondition ipCondition, int conId)
            throws SQLException {
        String query = "INSERT INTO AM_IP_CONDITION (STARTING_IP, ENDING_IP, SPECIFIC_IP, " +
                "CONDITION_GROUP_ID) VALUES (?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ipCondition.getStartingIP());
            statement.setString(2, ipCondition.getEndingIP());
            statement.setString(3, ipCondition.getSpecificIP());
            statement.setInt(4, conId);         //Con id represents condition group id

            statement.execute();
        }
    }

    /**
     * Adding header conditions to DB
     * @param connection
     * @param headerCondition
     * @param conId
     * @throws SQLException
     */
    private static void addHeaderCondition(Connection connection, HeaderCondition headerCondition,
                                           int conId) throws SQLException {
        String query = "INSERT INTO AM_HEADER_FIELD_CONDITION (HEADER_FIELD_NAME, HEADER_FIELD_VALUE, " +
                "CONDITION_GROUP_ID) VALUES (?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, headerCondition.getHeaderName());
            statement.setString(2, headerCondition.getValue());
            statement.setInt(3, conId);         //Con id represents condition group id

            statement.execute();
        }
    }

    /**
     * adding jwt claim condition to DB
     * @param connection
     * @param jwtClaimsCondition
     * @param conID
     * @throws SQLException
     */
    private static void addJWTClaimCondition(Connection connection,
                                             JWTClaimsCondition jwtClaimsCondition, int conID)
            throws SQLException {
        String query = "INSERT INTO AM_JWT_CLAIM_CONDITION (CLAIM_URI, CLAIM_ATTRIB, " +
                "CONDITION_GROUP_ID) VALUES (?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, jwtClaimsCondition.getClaimUrl());
            statement.setString(2, jwtClaimsCondition.getAttribute());
            statement.setInt(3, conID);         //Con id represents condition group id

            statement.execute();
        }
    }

    /**
     * Adding query parameter condition to DB
     * @param connection
     * @param queryParamCondition
     * @param conID
     * @throws SQLException
     */
    private static void addParamCondition(Connection connection,
                                             QueryParameterCondition queryParamCondition, int conID)
            throws SQLException {
        String query = "INSERT INTO AM_QUERY_PARAMETER_CONDITION (PARAMETER_NAME, " +
                "PARAMETER_VALUE,CONDITION_GROUP_ID) VALUES (?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, queryParamCondition.getParameter());
            statement.setString(2, queryParamCondition.getValue());
            statement.setInt(3, conID);         //Con id represents condition group id

            statement.execute();
        }
    }
    private static void addSubscriptionPolicy(Connection connection, String name, String displayName,
            String description, String quotaType, int quota, String quotaUnit, int unitTime, String timeUnit)
            throws SQLException {
        final String query =
                "INSERT INTO AM_SUBSCRIPTION_POLICY (UUID, NAME, DISPLAY_NAME, DESCRIPTION, QUOTA_TYPE, QUOTA, "
                        + "QUOTA_UNIT, UNIT_TIME, TIME_UNIT) VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, name);
            statement.setString(3, displayName);
            statement.setString(4, description);
            statement.setString(5, quotaType);
            statement.setInt(6, quota);
            statement.setString(7, quotaUnit);
            statement.setInt(8, unitTime);
            statement.setString(9, timeUnit);

            statement.execute();
        }
    }

    private static void addApplicationPolicy(Connection connection, String name, String displayName, String description,
                                      String quotaType, int quota, String quotaUnit, int unitTime, String timeUnit)
                                                                                                throws SQLException {
        final String query = "INSERT INTO AM_APPLICATION_POLICY (UUID, NAME, DISPLAY_NAME, " +
                "DESCRIPTION, QUOTA_TYPE, QUOTA, QUOTA_UNIT, UNIT_TIME, TIME_UNIT) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, name);
            statement.setString(3, displayName);
            statement.setString(4, description);
            statement.setString(5, quotaType);
            statement.setInt(6, quota);
            statement.setString(7, quotaUnit);
            statement.setInt(8, unitTime);
            statement.setString(9, timeUnit);

            statement.execute();
        }
    }

    public void deleteAPIPolicy(String policyName) throws APIMgtDAOException {
        String sqlQuery = "DELETE FROM AM_API_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))    {
            deletePipelines(connection, policyName);
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        } catch (SQLException e)    {
            log.error("An Error occurred while deleting Policy with name [" + policyName + "], " , e);
            throw new APIMgtDAOException("Error occurred while deleting Policy with name : " + policyName, e);
        }
    }

    /**
     * to delete pipelines of api policy
     * @param connection
     * @param policyName
     * @throws APIMgtDAOException
     */
    private void deletePipelines(Connection connection, String policyName) throws
            APIMgtDAOException, SQLException {
        String queryUUID = "SELECT UUID FROM AM_API_POLICY WHERE NAME = ?";
        String query = "SELECT CONDITION_GROUP_ID FROM AM_CONDITION_GROUP WHERE UUID = ?";
        String deleteQuery = "DELETE FROM AM_CONDITION_GROUP WHERE UUID =?";
        try (PreparedStatement statement = connection.prepareStatement(queryUUID)) {
            statement.setString(1, policyName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String uuid = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID);
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        //get pipeline ids from condition_group
                        preparedStatement.setString(1, uuid);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                //delete conditions
                                int policyID = rs.getInt
                                        (APIMgtConstants.ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                                deleteConditions(connection, policyID);
                            }
                        }
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                        preparedStatement.setString(1, uuid);
                        preparedStatement.execute();
                    }
                }
            }
        }
    }

    /**
     * delete condition of a particular pipeline
     * @param connection
     * @param conID
     * @throws SQLException
     */
    private void deleteConditions(Connection connection, int conID) throws SQLException {
        String query1 = "DELETE FROM AM_IP_CONDITION WHERE CONDITION_GROUP_ID = ?";
        String query2 = "DELETE FROM AM_HEADER_FIELD_CONDITION WHERE CONDITION_GROUP_ID = ?";
        String query3 = "DELETE FROM AM_JWT_CLAIM_CONDITION WHERE CONDITION_GROUP_ID = ?";
        String query4 = "DELETE FROM AM_QUERY_PARAMETER_CONDITION WHERE CONDITION_GROUP_ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query3)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query4)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
    }

    public void deleteApplicationPolicy(String policyName) throws APIMgtDAOException {
        String sqlQuery = "DELETE FROM AM_APPLICATION_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))    {
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        } catch (SQLException e)    {
            log.error("An Error occurred while deleting Policy with name [" + policyName + "], " , e);
            throw new APIMgtDAOException("Error occurred while deleting Policy with name : " + policyName, e);
        }
    }

    public void deleteSubscriptionPolicy(String policyName) throws APIMgtDAOException {
        String sqlQuery = "DELETE FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))    {
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        } catch (SQLException e)    {
            log.error("An Error occurred while deleting Policy with name [" + policyName + "], " , e);
            throw new APIMgtDAOException("Error occurred while deleting Policy with name : " + policyName, e);
        }
    }


}
