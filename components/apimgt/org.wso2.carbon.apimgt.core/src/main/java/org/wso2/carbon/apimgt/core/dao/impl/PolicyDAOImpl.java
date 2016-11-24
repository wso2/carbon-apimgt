package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.util.APIConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Contains Implementation for policy
 */
public class PolicyDAOImpl implements PolicyDAO {
    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIMgtDAOException {
        if (APIConstants.ThrottlePolicyConstants.API_LEVEL.equals(policyLevel)) {
            try {
                return getAPIPolicy(policyName);
            } catch (SQLException e) {
                throw new APIMgtDAOException("Couldn't find " + policyName + ' ' + policyLevel + "Policy", e);
            }
        }
        return null;
    }

    @Override
    public void addPolicy(String policyLevel, Policy policy) throws APIMgtDAOException {

    }

    @Override
    public void deletePolicy(String policyName) {

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
        String sqlQuery = "SELECT * from AM_API_THROTTLE_POLICY WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    policy = new APIPolicy(resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    setCommonPolicyDetails(policy, resultSet);
                    policy.setUserLevel(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_APPLICABLE_LEVEL));
                    policy.setPipelines(getPipelines(policy.getPolicyId(), connection));
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

        quotaPolicy.setType(resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants
                .COLUMN_QUOTA_POLICY_TYPE));
        if (resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.REQUEST_COUNT_TYPE)) {
            RequestCountLimit reqLimit = new RequestCountLimit();
            reqLimit.setUnitTime(resultSet.getInt(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            reqLimit.setTimeUnit(resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            reqLimit.setRequestCount(resultSet.getInt(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(reqLimit);
        } else if (resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.BANDWIDTH_TYPE)) {
            BandwidthLimit bandLimit = new BandwidthLimit();
            bandLimit.setUnitTime(resultSet.getInt(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            bandLimit.setTimeUnit(resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            bandLimit.setDataAmount(resultSet.getInt(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA));
            bandLimit.setDataUnit(resultSet.getString(prefix + APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT));
            quotaPolicy.setLimit(bandLimit);
        }

        policy.setUuid(resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_UUID));
        policy.setDescription(resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION));
        policy.setDisplayName(resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_DISPLAY_NAME));
        policy.setPolicyId(resultSet.getInt(APIConstants.ThrottlePolicyConstants.COLUMN_POLICY_ID));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(APIConstants.ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    /**
     * Retrieves list of pipelines for the policy with policy Id: <code>policyId</code>
     *
     * @param policyId policy id of the pipelines
     * @return list of pipelines
     * @throws SQLException
     */
    private ArrayList<Pipeline> getPipelines(int policyId, Connection connection) throws SQLException {
        ArrayList<Pipeline> pipelines = new ArrayList<>();
        final String sqlQuery = "SELECT CONDITION_GROUP_ID,QUOTA_TYPE,QUOTA,QUOTA_UNIT,UNIT_TIME,TIME_UNIT," +
                "DESCRIPTION FROM AM_CONDITION_GROUP WHERE POLICY_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            int unitTime;
            int quota;
            int pipelineId;
            String timeUnit;
            String quotaUnit;
            String description;
            preparedStatement.setInt(1, policyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    Pipeline pipeline = new Pipeline();
                    ArrayList<Condition> conditions;
                    QuotaPolicy quotaPolicy = new QuotaPolicy();
                    quotaPolicy.setType(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_QUOTA_POLICY_TYPE));

                    timeUnit = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT);
                    quotaUnit = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT);
                    unitTime = resultSet.getInt(APIConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME);
                    quota = resultSet.getInt(APIConstants.ThrottlePolicyConstants.COLUMN_QUOTA);
                    pipelineId = resultSet.getInt(APIConstants.ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                    description = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION);
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
                    startingIP = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_STARTING_IP);
                    endingIP = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_ENDING_IP);
                    specificIP = resultSet.getString(APIConstants.ThrottlePolicyConstants.COLUMN_SPECIFIC_IP);
                    invert = resultSet.getBoolean(APIConstants.ThrottlePolicyConstants.COLUMN_WITHIN_IP_RANGE);

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
                    headerCondition.setHeader(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_NAME));
                    headerCondition.setValue(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_VALUE));
                    headerCondition.setInvertCondition(resultSet.getBoolean(APIConstants.ThrottlePolicyConstants
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
                            .setParameter(resultSet.getString(APIConstants.ThrottlePolicyConstants
                                    .COLUMN_PARAMETER_NAME));
                    queryParameterCondition.setValue(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_PARAMETER_VALUE));
                    queryParameterCondition.setInvertCondition(resultSet.getBoolean(APIConstants.ThrottlePolicyConstants
                            .COLUMN_IS_PARAM_MAPPING));
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
                    jwtClaimsCondition.setClaimUrl(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_URI));
                    jwtClaimsCondition.setAttribute(resultSet.getString(APIConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_ATTRIBUTE));
                    jwtClaimsCondition.setInvertCondition(resultSet.getBoolean(APIConstants.ThrottlePolicyConstants
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
     * @throws APIMgtDAOException
     */
    public String getSubscriptionTierName(String policyId) throws APIMgtDAOException {
        final String query = "SELECT NAME FROM AM_APPLICATION_POLICY WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, policyId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Couldn't retrieve subscription tier for id : " + policyId, e);
        }
        return null;
    }
}
