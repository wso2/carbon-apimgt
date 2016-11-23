package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.PolicyDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides access to Policy data layer
 */
public class PolicyDAOImpl implements PolicyDAO {

    /**
     * Retrieves the name of Subscription Policy
     *
     * @param policyId  Subscription policy ID
     * @return Tier name of given Subscription policy ID
     * @throws SQLException
     */
    public String getSubscriptionTierName(String policyId) throws SQLException {
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
        }
        return null;
    }
}
