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

            List<String> labelNameList = new ArrayList<>();

            for (Label label : labels) {
                labelNameList.add(label.getName());
            }

            List<Label> existingLabels = getLabelsByName(labelNameList);

            if (!existingLabels.isEmpty()) {
                labels.removeAll(existingLabels);   // Remove already existing labels from list
            }

            if (!labels.isEmpty()) {                // Add labels that don't already exist
                try {
                    insertNewLabels(labels);
                } catch (SQLException e) {
                    throw new APIMgtDAOException(e);
                }
            }
        }
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @Override
    public List<Label> getLabelsByName(List<String> labels) throws APIMgtDAOException {

        List<Label> matchingLabels = new ArrayList<>();

        if (!labels.isEmpty()) {
            final String query = "SELECT NAME,ACCESS_URL FROM AM_LABELS WHERE NAME IN (" +
                    DAOUtil.getParameterString(labels.size()) + ")";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < labels.size(); ++i) {
                    statement.setString(i + 1, labels.get(i));
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


    private static void insertNewLabels(List<Label> labels)
            throws SQLException {
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
        }
    }

}


