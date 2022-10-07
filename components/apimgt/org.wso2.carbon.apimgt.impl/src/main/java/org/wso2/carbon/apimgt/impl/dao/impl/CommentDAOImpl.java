package org.wso2.carbon.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.CommentDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CommentDAOImpl implements CommentDAO {
    private static final Log log = LogFactory.getLog(CommentDAOImpl.class);
    private static CommentDAOImpl INSTANCE = new CommentDAOImpl();

    private CommentDAOImpl() {

    }

    public static CommentDAOImpl getInstance() {
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
    public String addComment(String uuid, Comment comment, String user) throws APIManagementException {

        String commentId = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            connection.setAutoCommit(false);
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg);
            }
            String addCommentQuery = SQLConstants.ADD_COMMENT_SQL;
            commentId = UUID.randomUUID().toString();
            try (PreparedStatement insertPrepStmt = connection.prepareStatement(addCommentQuery)) {
                insertPrepStmt.setString(1, commentId);
                insertPrepStmt.setString(2, comment.getText());
                insertPrepStmt.setString(3, user);
                insertPrepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
                insertPrepStmt.setInt(5, id);
                insertPrepStmt.setString(6, comment.getParentCommentID());
                insertPrepStmt.setString(7, comment.getEntryPoint());
                insertPrepStmt.setString(8, comment.getCategory());
                insertPrepStmt.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add comment data, for API with UUID " + uuid,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return commentId;
    }

    private int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException {

        int id = -1;
        String getAPIQuery = SQLConstants.GET_API_ID_SQL_BY_UUID;

        try (PreparedStatement prepStmt = connection.prepareStatement(getAPIQuery)) {
            prepStmt.setString(1, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("API_ID");
                }
                if (id == -1) {
                    String msg = "Unable to find the API with UUID : " + uuid + " in the database";
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
                }
            }
        }
        return id;
    }

    @Override
    public Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit,
                              Integer replyOffset) throws
            APIManagementException {

        String uuid;
        Identifier identifier;
        if (apiTypeWrapper.isAPIProduct()) {
            identifier = apiTypeWrapper.getApiProduct().getId();
            uuid = apiTypeWrapper.getApiProduct().getUuid();
        } else {
            identifier = apiTypeWrapper.getApi().getId();
            uuid = apiTypeWrapper.getApi().getUuid();
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            Comment comment = new Comment();
            int id = -1;
            String getCommentQuery = SQLConstants.GET_COMMENT_SQL;
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
            }
            try (PreparedStatement prepStmt = connection.prepareStatement(getCommentQuery)) {
                prepStmt.setString(1, uuid);
                prepStmt.setString(2, commentId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        comment.setId(resultSet.getString("COMMENT_ID"));
                        comment.setText(resultSet.getString("COMMENT_TEXT"));
                        comment.setUser(resultSet.getString("CREATED_BY"));
                        comment.setCreatedTime(resultSet.getTimestamp("CREATED_TIME"));
                        comment.setUpdatedTime(resultSet.getTimestamp("UPDATED_TIME"));
                        comment.setApiId(resultSet.getString("API_ID"));
                        comment.setParentCommentID(resultSet.getString("PARENT_COMMENT_ID"));
                        comment.setEntryPoint(resultSet.getString("ENTRY_POINT"));
                        comment.setCategory(resultSet.getString("CATEGORY"));
                        comment.setReplies(getComments(uuid, commentId, replyLimit, replyOffset, connection));
                        return comment;
                    }
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve comment for API " + identifier.getName() + "with comment ID " +
                    commentId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    /**
     * Returns all the Comments on an API
     *
     * @param uuid API UUID
     * @param parentCommentID Parent Comment ID
     * @param limit           The limit
     * @param offset          The offset
     * @param connection Database connection
     * @return Comment Array
     * @throws APIManagementException
     */
    private CommentList getComments(String uuid, String parentCommentID, Integer limit, Integer offset,
                                    Connection connection) throws
            APIManagementException {

        List<Comment> list = new ArrayList<Comment>();
        CommentList commentList = new CommentList();
        Pagination pagination = new Pagination();
        commentList.setPagination(pagination);
        int total = 0;
        String sqlQuery;
        String sqlQueryForCount;
        if (parentCommentID == null) {
            sqlQueryForCount = SQLConstants.GET_ROOT_COMMENTS_COUNT_SQL;
        } else {
            sqlQueryForCount = SQLConstants.GET_REPLIES_COUNT_SQL;
        }
        try (PreparedStatement prepStmtForCount = connection.prepareStatement(sqlQueryForCount)) {
            prepStmtForCount.setString(1, uuid);
            if (parentCommentID != null) {
                prepStmtForCount.setString(2, parentCommentID);
            }
            try (ResultSet resultSetForCount = prepStmtForCount.executeQuery()) {
                while (resultSetForCount.next()) {
                    total = resultSetForCount.getInt("COMMENT_COUNT");
                }
                if (total > 0 && limit > 0) {
                    if (parentCommentID == null) {
                        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_ROOT_COMMENTS_SQL");
                    } else {
                        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_REPLIES_SQL");
                    }
                    try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                        prepStmt.setString(1, uuid);
                        if (parentCommentID != null) {
                            prepStmt.setString(2, parentCommentID);
                            prepStmt.setInt(3, offset);
                            prepStmt.setInt(4, limit);
                        } else {
                            prepStmt.setInt(2, offset);
                            prepStmt.setInt(3, limit);
                        }
                        try (ResultSet resultSet = prepStmt.executeQuery()) {
                            while (resultSet.next()) {
                                Comment comment = new Comment();
                                comment.setId(resultSet.getString("COMMENT_ID"));
                                comment.setText(resultSet.getString("COMMENT_TEXT"));
                                comment.setUser(resultSet.getString("CREATED_BY"));
                                comment.setCreatedTime(resultSet.getTimestamp("CREATED_TIME"));
                                comment.setUpdatedTime(resultSet.getTimestamp("UPDATED_TIME"));
                                comment.setApiId(resultSet.getString("API_ID"));
                                comment.setParentCommentID(resultSet.getString("PARENT_COMMENT_ID"));
                                comment.setEntryPoint(resultSet.getString("ENTRY_POINT"));
                                comment.setCategory(resultSet.getString("CATEGORY"));
                                if (parentCommentID == null) {
                                    comment.setReplies(getComments(uuid, resultSet.getString("COMMENT_ID")
                                            , APIConstants.REPLYLIMIT, APIConstants.REPLYOFFSET, connection));
                                } else {
                                    CommentList emptyCommentList = new CommentList();
                                    Pagination emptyPagination = new Pagination();
                                    emptyCommentList.setPagination(emptyPagination);
                                    emptyCommentList.getPagination().setTotal(0);
                                    emptyCommentList.setCount(0);
                                    comment.setReplies(emptyCommentList);
                                }
                                list.add(comment);
                            }
                        }
                    }
                } else {
                    commentList.getPagination().setTotal(total);
                    commentList.setCount(total);
                    return commentList;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve comments for API with UUID " + uuid, e);
        }
        pagination.setLimit(limit);
        pagination.setOffset(offset);
        commentList.getPagination().setTotal(total);
        commentList.setList(list);
        commentList.setCount(list.size());
        return commentList;
    }

    @Override
    public CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID, Integer limit,
                                   Integer offset) throws APIManagementException {

        CommentList commentList = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            String uuid;
            Identifier identifier;
            String currentApiUuid;
            if (apiTypeWrapper.isAPIProduct()) {
                identifier = apiTypeWrapper.getApiProduct().getId();
                uuid = apiTypeWrapper.getApiProduct().getUuid();
                APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    currentApiUuid = apiRevision.getApiUUID();
                } else {
                    currentApiUuid = uuid;
                }
            } else {
                identifier = apiTypeWrapper.getApi().getId();
                uuid = apiTypeWrapper.getApi().getUuid();
                APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    currentApiUuid = apiRevision.getApiUUID();
                } else {
                    currentApiUuid = uuid;
                }
            }
            id = getAPIID(currentApiUuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
            }
            commentList = getComments(currentApiUuid, parentCommentID, limit, offset, connection);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve comments for  " + apiTypeWrapper.getName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return commentList;
    }

    /**
     * Get a provided api uuid is in the revision db table
     *
     * @return String apiUUID
     * @throws APIManagementException if an error occurs while checking revision table
     */
    private APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_APIID_BY_REVISION_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setApiUUID(rs.getString("API_UUID"));
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setRevisionUUID(apiUUID);
                    return apiRevision;
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to search UUID: " + apiUUID + " in the revision db table", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    @Override
    public boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws
            APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            String editCommentQuery = SQLConstants.EDIT_COMMENT;
            Identifier identifier;
            String uuid;
            if (apiTypeWrapper.isAPIProduct()) {
                identifier = apiTypeWrapper.getApiProduct().getId();
                uuid = apiTypeWrapper.getApiProduct().getUuid();
            } else {
                identifier = apiTypeWrapper.getApi().getId();
                uuid = apiTypeWrapper.getApi().getUuid();
            }
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
            }
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(editCommentQuery)) {
                prepStmt.setString(1, comment.getText());
                prepStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
                prepStmt.setString(3, comment.getCategory());
                prepStmt.setInt(4, id);
                prepStmt.setString(5, commentId);
                prepStmt.execute();
                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while editing comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
    }

    @Override
    public boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            String uuid;
            if (apiTypeWrapper.isAPIProduct()) {
                uuid = apiTypeWrapper.getApiProduct().getUuid();
            } else {
                uuid = apiTypeWrapper.getApi().getUuid();
            }
            return deleteComment(uuid, commentId, connection);
        } catch (SQLException e) {
            handleExceptionWithCode("Error while deleting comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
    }

    private boolean deleteComment(String uuid, String commentId, Connection connection) throws
            APIManagementException {

        int id = -1;
        String deleteCommentQuery = SQLConstants.DELETE_COMMENT_SQL;
        String getCommentIDsOfReplies = SQLConstants.GET_IDS_OF_REPLIES_SQL;
        ResultSet resultSet = null;
        try {
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, uuid));
            }
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmtGetReplies = connection.prepareStatement(getCommentIDsOfReplies)) {
                prepStmtGetReplies.setString(1, uuid);
                prepStmtGetReplies.setString(2, commentId);
                resultSet = prepStmtGetReplies.executeQuery();
                while (resultSet.next()) {
                    deleteComment(uuid, resultSet.getString("COMMENT_ID"), connection);
                }
                try (PreparedStatement prepStmt = connection.prepareStatement(deleteCommentQuery)) {
                    prepStmt.setInt(1, id);
                    prepStmt.setString(2, commentId);
                    prepStmt.execute();
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while deleting comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
    }





}
