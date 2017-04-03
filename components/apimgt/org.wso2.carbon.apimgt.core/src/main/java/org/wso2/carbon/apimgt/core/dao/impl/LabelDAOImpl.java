package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to Labels which maybe shared across multiple entities
 */
public class LabelDAOImpl implements LabelDAO {

    private static final Logger log = LoggerFactory.getLogger(LabelDAOImpl.class);

    // Package private to prevent direct instance creation outside the package.
    // Use the DAOFactory to instantiate a new instance
    LabelDAOImpl() {
    }

    @Override
    public List<Label> getLabels() throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID, NAME FROM AM_LABELS";

        List<Label> labels = new ArrayList<>();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label.Builder().
                            id(rs.getString("LABEL_ID")).
                            name(rs.getString("NAME")).
                            accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build();

                    labels.add(label);
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving labels";
            log.error(message, e);
            throw new APIMgtDAOException(e);
        }

        return labels;
    }

    @Override
    public void addLabels(List<Label> labels) throws APIMgtDAOException {

        if (!labels.isEmpty()) {

            List<String> labelNames = new ArrayList<>();

            for (Label label : labels) {
                labelNames.add(label.getName());
            }
            List<Label> existingLabels = getLabelsByName(labelNames);

            if (!existingLabels.isEmpty()) {

                List<Label> labelsToRemove = new ArrayList<>();

                for (Label existingLabel : existingLabels) {
                    for (Label label : labels) {
                        if (existingLabel.getName().equals(label.getName())) {
                            labelsToRemove.add(label);
                        }
                    }
                }
                labels.removeAll(labelsToRemove);    // Remove already existing labels from the list
            }

            if (!labels.isEmpty()) { // Add labels that don't already exist
                insertNewLabels(labels);
            }
        }
    }

    private void insertNewLabels(List<Label> labels) throws APIMgtDAOException {

        if (!labels.isEmpty()) {

            final String query = "INSERT INTO AM_LABELS (LABEL_ID, NAME) VALUES (?,?)";
            Map<String, List<String>> urlMap = new HashMap<>();

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (Label label : labels) {
                    statement.setString(1, label.getId());
                    statement.setString(2, label.getName());
                    statement.addBatch();
                    urlMap.put(label.getId(), label.getAccessUrls());
                }
                statement.executeBatch();
            } catch (SQLException e) {
                String message = "Error while adding label data";
                log.error(message, e);
                throw new APIMgtDAOException(e);
            } finally {

                if (!urlMap.isEmpty()) {
                    for (Map.Entry<String, List<String>> entry : urlMap.entrySet()) {
                        insertAccessUrlMappings(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    private void insertAccessUrlMappings(String labelId, List<String> accessUrls) throws APIMgtDAOException {

        if (!accessUrls.isEmpty()) {

            List<String> existingAccessUrls = getLabelAccessUrls(labelId);
            if (!existingAccessUrls.isEmpty()) {
                accessUrls.removeAll(existingAccessUrls);
            }

            final String query = "INSERT INTO AM_LABEL_ACCESS_URL_MAPPING (LABEL_ID, ACCESS_URL) " +
                    "VALUES (?,?)";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (String accessUrl : accessUrls) {
                    statement.setString(1, labelId);
                    statement.setString(2, accessUrl);
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (SQLException e) {
                String message = "Error while adding access url mappings for [label id] " + labelId;
                log.error(message, e);
                throw new APIMgtDAOException(e);
            }
        }
    }

    private List<String> getLabelAccessUrls(String labelId) throws APIMgtDAOException {

        final String query = "SELECT ACCESS_URL FROM AM_LABEL_ACCESS_URL_MAPPING WHERE LABEL_ID = ?";
        List<String> accessUrls = new ArrayList<>();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, labelId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    accessUrls.add(rs.getString("ACCESS_URL"));
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving access url for [label id] " + labelId;
            log.error(message, e);
            throw new APIMgtDAOException(e);
        }

        return accessUrls;
    }

    @Override
    public Label getLabelByName(String labelName) throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID, NAME FROM AM_LABELS WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, labelName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Label.Builder().
                            id(rs.getString("LABEL_ID")).
                            name(rs.getString("NAME")).
                            accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build();
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving label [label name] " + labelName;
            log.error(message, e);
            throw new APIMgtDAOException(e);
        }

    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @Override
    public List<Label> getLabelsByName(List<String> labelNames) throws APIMgtDAOException {

        List<Label> matchingLabels = new ArrayList<>();

        if (!labelNames.isEmpty()) {
            final String query = "SELECT LABEL_ID, NAME FROM AM_LABELS WHERE NAME IN (" +
                    DAOUtil.getParameterString(labelNames.size()) + ")";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < labelNames.size(); ++i) {
                    statement.setString(i + 1, labelNames.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Label label = new Label.Builder().
                                id(rs.getString("LABEL_ID")).
                                name(rs.getString("NAME")).
                                accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build();

                        matchingLabels.add(label);
                    }
                }
            } catch (SQLException e) {
                String message = "Error while retrieving labels";
                log.error(message, e);
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

    static String getLabelID(String labelName) throws SQLException {

        final String query = "SELECT LABEL_ID from AM_LABELS where NAME=?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, labelName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("LABEL_ID");
                }
            }
        }
        throw new SQLException("Label " + labelName + ", does not exist");
    }

    @Override
    public void deleteLabel(String labelId) throws APIMgtDAOException {

        final String query = "DELETE FROM AM_LABELS WHERE LABEL_ID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, labelId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String message = "Error while deleting the label [label id] " + labelId;
                log.error(message, e);
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

    }

    @Override
    public void updateLabel(Label updatedLabel) throws APIMgtDAOException {

        try {
            String labelId = getLabelID(updatedLabel.getName());
            deleteLabelAccessUrlMappings(labelId);
            insertAccessUrlMappings(labelId, updatedLabel.getAccessUrls());
        } catch (SQLException e) {
            String message = "Error while updating the label [label name] " + updatedLabel.getName();
            log.error(message, e);
            throw new APIMgtDAOException(e);
        }

    }

    private void deleteLabelAccessUrlMappings(String labelId) throws APIMgtDAOException {

        final String query = "DELETE FROM AM_LABEL_ACCESS_URL_MAPPING WHERE LABEL_ID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, labelId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String message = "Error while deleting the label access url mappings [label id] " + labelId;
                log.error(message, e);
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

    }

}


