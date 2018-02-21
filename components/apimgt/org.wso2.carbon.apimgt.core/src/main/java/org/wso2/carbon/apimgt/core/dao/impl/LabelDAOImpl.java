package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Provides access to Labels which maybe shared across multiple entities
 */
public class LabelDAOImpl implements LabelDAO {

    private static final Logger log = LoggerFactory.getLogger(LabelDAOImpl.class);

    // Package private to prevent direct instance creation outside the package.
    // Use the DAOFactory to instantiate a new instance
    LabelDAOImpl() {
    }

    /**
     * @see LabelDAO#getLabels()
     */
    @Override
    public List<Label> getLabels() throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID, NAME, TYPE_NAME FROM AM_LABELS";

        List<Label> labels = new ArrayList<>();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label.Builder().
                            id(rs.getString("LABEL_ID")).
                            name(rs.getString("NAME")).
                            type(rs.getString("TYPE_NAME")).
                            accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build();

                    labels.add(label);
                }
            }
        } catch (SQLException e) {
            String message = DAOUtil.DAO_ERROR_PREFIX + "retrieving labels";
            throw new APIMgtDAOException(message, e);
        }

        return labels;
    }

    /**
     * @see LabelDAO#addLabels(List)
     */
    @Override
    public void addLabels(List<Label> labels) throws APIMgtDAOException {

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

                if (!urlMap.isEmpty()) {
                    for (Map.Entry<String, List<String>> entry : urlMap.entrySet()) {
                        if (entry.getValue() != null) {
                            insertAccessUrlMappings(entry.getKey(), entry.getValue());
                        }
                    }
                }
            } catch (SQLException e) {
                String message = DAOUtil.DAO_ERROR_PREFIX + "adding label data";
                throw new APIMgtDAOException(message, e);
            }
        }
    }

    /**
     * Add a new label
     *
     * @param label the label to ADD
     * @return {@link Label} a label object with the newly added label ID
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    public static Label addLabel(Label label) throws APIMgtDAOException {
        final String query = "INSERT INTO AM_LABELS (LABEL_ID, NAME, TYPE_NAME) VALUES (?,?,?)";
        String labelId = null;

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            labelId = UUID.randomUUID().toString();
            statement.setString(1, labelId);
            statement.setString(2, label.getName());
            statement.setString(3, label.getType().toUpperCase(Locale.ENGLISH));
            statement.executeUpdate();

            connection.commit();
            if (!label.getAccessUrls().isEmpty()) {
                insertAccessUrlMappings(labelId, label.getAccessUrls());
            }
            return new Label.Builder().id(labelId).name(label.getName()).description(label.getDescription()).
                    accessUrls(label.getAccessUrls()).type(label.getType()).build();
        } catch (SQLException e) {
            String message = "Error occured while trying to add label " + labelId;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_ADDING_FAILED);
        }
    }

    /**
     * Add access url mappings
     *
     * @param labelId    Id of the label
     * @param accessUrls List of access urls
     * @throws APIMgtDAOException if error occurs while adding access url mappings
     */
    private static void insertAccessUrlMappings(String labelId, List<String> accessUrls) throws APIMgtDAOException {

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
                String message = DAOUtil.DAO_ERROR_PREFIX + "adding access url mappings for [label id] " + labelId;
                throw new APIMgtDAOException(message, e);
            }
        }
    }


    /**
     * Retrieve access urls of a label by label Id
     *
     * @param labelId Id of the label
     * @return List of access urls of the label
     * @throws APIMgtDAOException if error occurs while retrieving access urls
     */
    private static List<String> getLabelAccessUrls(String labelId) throws APIMgtDAOException {

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
            String message = DAOUtil.DAO_ERROR_PREFIX + "retrieving access url for [label id] " + labelId;
            throw new APIMgtDAOException(message, e);
        }

        return accessUrls;
    }

    /**
     * @see LabelDAO#getLabelByID(String)
     */
    @Override
    public Label getLabelByID(String labelID) throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID, NAME, TYPE_NAME FROM AM_LABELS WHERE LABEL_ID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, labelID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Label.Builder().
                            id(rs.getString("LABEL_ID")).
                            name(rs.getString("NAME")).
                            type(rs.getString("TYPE_NAME")).
                            accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build();
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving label [label ID] " + labelID;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }

    }


    /**
     * @see LabelDAO#getLabelIdByNameAndType(String, String)
     */
    @Override
    public String getLabelIdByNameAndType(String name, String type) throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID FROM AM_LABELS WHERE NAME = ? AND TYPE_NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, type);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("LABEL_ID");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving label ID of label [label Name] " + name;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }

    }

    /**
     * @see LabelDAO#getLabelsByName(List)
     */
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
                String message = DAOUtil.DAO_ERROR_PREFIX + "retrieving labels";
                throw new APIMgtDAOException(message, e);
            }
        }

        return matchingLabels;
    }


    /**
     * Retrieve label names by label ids
     *
     * @param labelIDs List of label ids
     * @return List of label names
     * @throws SQLException if error occurs while retrieving label names
     */
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static Set<String> getLabelNamesByIDs(List<String> labelIDs) throws SQLException {
        Set<String> labelNames = new HashSet<>();

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

    /**
     * Retrieve label id by label name
     *
     * @param labelName Name of the label
     * @return Id of the label
     * @throws SQLException if error occurs while retrieving label id
     */
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

    /**
     * @see LabelDAO#deleteLabel(String)
     */
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
                String message = DAOUtil.DAO_ERROR_PREFIX + "deleting the label [label id] " + labelId;
                throw new APIMgtDAOException(message, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String message = DAOUtil.DAO_ERROR_PREFIX + "deleting the label [label id] " + labelId;
            throw new APIMgtDAOException(message, e);
        }

    }

    /**
     * @see LabelDAO#updateLabel(Label)
     */
    @Override
    public Label updateLabel(Label updatedLabel) throws APIMgtDAOException {
        final String query = "UPDATE AM_LABELS SET NAME=? , TYPE_NAME=? WHERE LABEL_ID=?";
        String labelId = updatedLabel.getId();
        List<String> accessURLs = updatedLabel.getAccessUrls();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, updatedLabel.getName());
                if (updatedLabel.getType() != null) {
                    statement.setString(2, updatedLabel.getType().toUpperCase(Locale.ENGLISH));
                } else {
                    statement.setString(2, updatedLabel.getType());
                }
                statement.setString(3, updatedLabel.getId());
                statement.executeUpdate();
                connection.commit();
                deleteLabelAccessUrlMappings(labelId);
                if (APIMgtConstants.LABEL_TYPE_GATEWAY.equalsIgnoreCase(updatedLabel.getType())) {
                    insertAccessUrlMappings(labelId, accessURLs);
                } else {
                    accessURLs = new ArrayList<>();
                }
            } catch (SQLException e) {
                connection.rollback();
                String message = "Error while updating the label" +
                        " [label id] " + labelId;
                throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_UPDATE_FAILED);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
            return new Label.Builder().id(labelId).name(updatedLabel.getName()).
                    description(updatedLabel.getDescription()).accessUrls(accessURLs).
                    type(updatedLabel.getType()).build();
        } catch (SQLException e) {
            String message = "Error while updating the label [label ID] " + labelId;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_UPDATE_FAILED);
        }

    }

    /**
     * Delete access urls of a label by label id
     *
     * @param labelId Id of the label
     * @throws APIMgtDAOException if error occurs while deleting label access urls
     */
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
                String message = DAOUtil.DAO_ERROR_PREFIX + "deleting the label access url mappings [label id] "
                        + labelId;
                throw new APIMgtDAOException(message, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String message = DAOUtil.DAO_ERROR_PREFIX + "deleting the label access url mappings [label id] " + labelId;
            throw new APIMgtDAOException(message, e);
        }

    }

    /**
     * Add default labels.
     *
     * @throws APIMgtDAOException If an error occurred while adding labels.
     */
    static void initDefaultLabels() throws APIMgtDAOException {
        if (!isLabelsExists(APIMgtConstants.DEFAULT_LABEL_NAME, APIMgtConstants.LABEL_TYPE_GATEWAY)) {
            //Todo : default labels need to be configurable
            List<String> accessUrls = new ArrayList<>();
            Label.Builder labelBuilder = new Label.Builder();
            labelBuilder.id(UUID.randomUUID().toString());
            labelBuilder.name(APIMgtConstants.DEFAULT_LABEL_NAME);
            labelBuilder.type(APIMgtConstants.LABEL_TYPE_GATEWAY);
            accessUrls.add(APIMgtConstants.DEFAULT_LABEL_ACCESS_URL);
            labelBuilder.accessUrls(accessUrls);
            addLabel(labelBuilder.build());
        }
        if (!isLabelsExists(APIMgtConstants.DEFAULT_LABEL_NAME, APIMgtConstants.LABEL_TYPE_STORE)) {
            //Todo : default labels need to be configurable
            List<String> accessUrls = new ArrayList<>();
            Label.Builder labelBuilder = new Label.Builder();
            labelBuilder.name(APIMgtConstants.DEFAULT_LABEL_NAME);
            labelBuilder.type(APIMgtConstants.LABEL_TYPE_STORE);
            accessUrls.add(APIMgtConstants.DEFAULT_LABEL_ACCESS_URL);
            labelBuilder.accessUrls(accessUrls);
            addLabel(labelBuilder.build());
        }
    }

    /**
     * Checks if any labels added to DB and already available
     *
     * @return true if there are any labels available in the system
     * @throws APIMgtDAOException If an error occurs while checking labels existence
     */
    private static boolean isLabelsExists(String name, String type) throws APIMgtDAOException {
        final String query = "SELECT * FROM AM_LABELS WHERE NAME=? AND TYPE_NAME=?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, type);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving label [label type] " + type;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }
        return false;
    }


    @Override
    public List<Label> getLabelsByType(String type) throws APIMgtDAOException {
        final String query = "SELECT LABEL_ID, NAME, TYPE_NAME FROM AM_LABELS WHERE TYPE_NAME = ?";
        List<Label> labels = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, type.toUpperCase(Locale.ENGLISH));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    labels.add(new Label.Builder().
                            id(rs.getString("LABEL_ID")).
                            name(rs.getString("NAME")).
                            type(rs.getString("TYPE_NAME")).
                            accessUrls(getLabelAccessUrls(rs.getString("LABEL_ID"))).build());
                }
            }
            return labels;
        } catch (SQLException e) {
            String message = "Error while retrieving label [label type] " + type;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }
    }


    /**
     * @see LabelDAO#getLabelByName(String)
     */
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
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }

    }
}
