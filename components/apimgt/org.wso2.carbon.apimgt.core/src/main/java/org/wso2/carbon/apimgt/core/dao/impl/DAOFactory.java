/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.FunctionDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Constructs DB vendor specific DAO implementations in a transparent manner.
 */
public class DAOFactory {

    private static final Logger log = LoggerFactory.getLogger(DAOFactory.class);
    
    private static final String MYSQL = "MySQL";
    private static final String H2 = "H2";
    private static final String DB2 = "DB2";
    private static final String MICROSOFT = "Microsoft";
    private static final String MS_SQL = "MS SQL";
    private static final String POSTGRE = "PostgreSQL";
    private static final String ORACLE = "Oracle";
    private static final String EDITOR_SAVE_PATH = "editorSavePath";
    private static final String EDITOR_MODE = "editorMode";

    public static ApiDAO getApiDAO() throws APIMgtDAOException {
        ApiDAO apiDAO = null;

        if (System.getProperty(EDITOR_MODE) != null) {
            String filePath;
            if ((filePath = System.getProperty(EDITOR_SAVE_PATH)) != null) {
                apiDAO = new ApiFileDAOImpl(filePath);
                return apiDAO;

            } else {
                throw new APIMgtDAOException("Editor archive storage path not provided");
            }
        }

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL)) {
                apiDAO = new ApiDAOImpl(new MysqlSQLStatements());
            } else if (driverName.contains(H2)) {
                apiDAO = new ApiDAOImpl(new H2SQLStatements());

            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                apiDAO = new ApiDAOImpl(new MssqlSQLStatements());
            } else if (driverName.contains(POSTGRE)) {
                apiDAO = new ApiDAOImpl(new PostgresSQLStatements());

            } else if (driverName.contains(ORACLE)) {
                apiDAO = new ApiDAOImpl(new OracleSQLStatements());

            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return apiDAO;
    }

    public static ApplicationDAO getApplicationDAO() throws APIMgtDAOException {
        ApplicationDAO appDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains(POSTGRE)) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains(ORACLE)) {
                appDAO = new ApplicationDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return appDAO;
    }

    public static APISubscriptionDAO getAPISubscriptionDAO() throws APIMgtDAOException {
        APISubscriptionDAO apiSubscriptionDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();

            } else if (driverName.contains(POSTGRE)) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();

            } else if (driverName.contains(ORACLE)) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return apiSubscriptionDAO;
    }

    public static PolicyDAO getPolicyDAO() throws APIMgtDAOException {
        PolicyDAO policyDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                policyDAO = new PolicyDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                policyDAO = new PolicyDAOImpl();

            } else if (driverName.contains(POSTGRE)) {
                policyDAO = new PolicyDAOImpl();

            } else if (driverName.contains(ORACLE)) {
                policyDAO = new PolicyDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return policyDAO;
    }

    public static TagDAO getTagDAO() throws APIMgtDAOException {
        TagDAO tagDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                tagDAO = new TagDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                tagDAO = new TagDAOImpl();
            } else if (driverName.contains(POSTGRE)) {
                tagDAO = new TagDAOImpl();
            } else if (driverName.contains(ORACLE)) {
                tagDAO = new TagDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return tagDAO;
    }

    public static LabelDAO getLabelDAO() throws APIMgtDAOException {
        LabelDAO labelDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                labelDAO = new LabelDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                labelDAO = new LabelDAOImpl();
            } else if (driverName.contains(POSTGRE)) {
                labelDAO = new LabelDAOImpl();
            } else if (driverName.contains(ORACLE)) {
                labelDAO = new LabelDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return labelDAO;
    }
    
    public static WorkflowDAO getWorkflowDAO() throws APIMgtDAOException {
        WorkflowDAO workflowDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                workflowDAO = new WorkflowDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                workflowDAO = new WorkflowDAOImpl();
            } else if (driverName.contains(POSTGRE)) {
                workflowDAO = new WorkflowDAOImpl();
            } else if (driverName.contains(ORACLE)) {
                workflowDAO = new WorkflowDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return workflowDAO;
    }

    /**
     * To get the FunctionDAO object. Depends on different vendors.
     *
     * @return FunctionDAO object
     * @throws APIMgtDAOException In case of unhandled DB type or SQLException
     */
    public static FunctionDAO getFunctionDAO() throws APIMgtDAOException {
        FunctionDAO functionDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                functionDAO = new FunctionDAOImpl();
            } else if (driverName.contains(DB2)) {

            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                functionDAO = new FunctionDAOImpl();

            } else if (driverName.contains(POSTGRE)) {
                functionDAO = new FunctionDAOImpl();

            } else if (driverName.contains(ORACLE)) {
                functionDAO = new FunctionDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return functionDAO;
    }

    private static void setup() throws APIMgtDAOException {
        ApiDAOImpl.initResourceCategories();
        ApiDAOImpl.initApiTypes();
        LabelDAOImpl.initDefaultLabels();
    }
}
