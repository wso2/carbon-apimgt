package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.TransactionCountDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.*;

/**
 * This class represent the TransactionCountDAO.
 */
public class TransactionCountDAO {

    private static final TransactionCountDAO transactionCountDAO = new TransactionCountDAO();

    private TransactionCountDAO() {
    }

    public static TransactionCountDAO getInstance() {
        return transactionCountDAO;
    }

    public boolean insertTransactionRecords(TransactionCountDTO[] dto) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement insertTransactionRecordsStatement = connection.prepareStatement(
                    SQLConstants.TransactionCountConstants.INSERT_TRANSACTION_COUNT)) {
                for (TransactionCountDTO record : dto) {
                    insertTransactionRecordsStatement.setString(1, record.getId());
                    insertTransactionRecordsStatement.setString(2, record.getHost());
                    insertTransactionRecordsStatement.setString(3, record.getServerID());
                    insertTransactionRecordsStatement.setString(4, record.getServerType());
                    insertTransactionRecordsStatement.setInt(5, record.getCount());

                    Timestamp recordedTimestamp = Timestamp.valueOf(record.getRecordedTime());
                    insertTransactionRecordsStatement.setTimestamp(6, recordedTimestamp);

                    insertTransactionRecordsStatement.addBatch();
                }
                insertTransactionRecordsStatement.executeBatch();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error while inserting transaction records", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public TransactionCountDTO getTransactionCount(String startTime, String endTime) throws APIManagementException {
        StringBuilder query = new StringBuilder(SQLConstants.TransactionCountConstants.GET_TRANSACTION_COUNT);

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement getTransactionCountStatement = connection.prepareStatement(query.toString());

            Timestamp startTimestamp = Timestamp.valueOf(startTime);
            getTransactionCountStatement.setTimestamp(1, startTimestamp);

            Timestamp endTimestamp = Timestamp.valueOf(endTime);
            getTransactionCountStatement.setTimestamp(2, endTimestamp);

            try (ResultSet resultSet = getTransactionCountStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    TransactionCountDTO dto = new TransactionCountDTO();
                    dto.setCount(count);
                    return dto;
                } else {
                    return new TransactionCountDTO();
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while fetching transaction count", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }
}