package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides access to Labels which maybe shared across multiple entities
 */
public class LabelDAOImpl implements LabelDAO {

    // Package private to prevent direct instance creation outside the package.
    // Use the DAOFactory to instantiate a new instance
    LabelDAOImpl() {
    }

    @Override
    public List<Label> getLabels() throws APIMgtDAOException {

        final String query = "SELECT NAME, ACCESS_URL FROM AM_LABELS";

        List<Label> labels = new ArrayList<>();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label.Builder().
                            name(rs.getString("NAME")).
                            accessUrl(rs.getString("ACCESS_URL")).build();

                    labels.add(label);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        return labels;
    }

    @Override
    public void addLabels(List<Label> labels) throws APIMgtDAOException {

        if (!labels.isEmpty()) {

            final String query = "INSERT INTO AM_LABELS (LABEL_ID, NAME, ACCESS_URL) VALUES (?,?,?)";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (Label label : labels) {
                    String labelId = UUID.randomUUID().toString();
                    statement.setString(1, labelId);
                    statement.setString(2, label.getName());
                    statement.setString(3, label.getAccessUrl());
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (SQLException e) {
                throw new APIMgtDAOException(e);
            }
        }
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @Override
    public Label getLabelByName(String labelName) throws APIMgtDAOException {

        final String query = "SELECT NAME,ACCESS_URL FROM AM_LABELS WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, labelName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Label.Builder().
                            name(rs.getString("NAME")).
                            accessUrl(rs.getString("ACCESS_URL")).build();
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @Override
    public List<Label> getLabelsByName(List<String> labelNames) throws APIMgtDAOException {

        List<Label> matchingLabels = new ArrayList<>();

        if (!labelNames.isEmpty()) {
            final String query = "SELECT NAME,ACCESS_URL FROM AM_LABELS WHERE NAME IN (" +
                    DAOUtil.getParameterString(labelNames.size()) + ")";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < labelNames.size(); ++i) {
                    statement.setString(i + 1, labelNames.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Label label = new Label.Builder().
                                name(rs.getString("NAME")).
                                accessUrl(rs.getString("ACCESS_URL")).build();

                        matchingLabels.add(label);
                    }
                }
            } catch (SQLException e) {
                throw new APIMgtDAOException(e);
            }
        }

        return matchingLabels;
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static List<String> getLabelNamesByIDs(List<String> labelIDs) throws SQLException {
        List<String> labelNames = new ArrayList<>();

        if (!labelIDs.isEmpty()) {
            final String query = "SELECT NAME FROM AM_LABELS WHERE LABEL_ID IN (" +
                    DAOUtil.getParameterString(labelIDs.size()) + ")";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < labelIDs.size(); ++i) {
                    statement.setString(i + 1, labelIDs.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        labelNames.add(rs.getString("NAME"));
                    }
                }
            }
        }
        return labelNames;
    }

    @Override
    public void deleteLabel(String labelName) throws APIMgtDAOException {

        final String query = "DELETE FROM AM_LABELS WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, labelName);
                statement.execute();
                connection.commit();
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

}


