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
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.FunctionDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.UserMappingDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;

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

    public ApiDAO getApiDAO() throws APIMgtDAOException {
        ApiDAO apiDAO = null;

        if (System.getProperty(EDITOR_MODE) != null) {
            String filePath;
            if ((filePath = System.getProperty(EDITOR_SAVE_PATH)) != null) {
                apiDAO = new ApiFileDAOImpl(filePath);
                return apiDAO;

            } else {
                throw new APIMgtDAOException("Editor archive storage path not provided",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting ApiDAO", e);
        }

        return apiDAO;
    }

    public ApplicationDAO getApplicationDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting ApplicationDAO", e);
        }

        return appDAO;
    }

    public APISubscriptionDAO getAPISubscriptionDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting APISubscriptionDAO", e);
        }

        return apiSubscriptionDAO;
    }

    public PolicyDAO getPolicyDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting PolicyDAO", e);
        }

        return policyDAO;
    }

    public TagDAO getTagDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting TagDAO", e);
        }

        return tagDAO;
    }

    public LabelDAO getLabelDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting LabelDAO", e);
        }

        return labelDAO;
    }

    public WorkflowDAO getWorkflowDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting WorkflowDAO", e);
        }

        return workflowDAO;
    }

    /**
     * To get the FunctionDAO object. Depends on different vendors.
     *
     * @return FunctionDAO object
     * @throws APIMgtDAOException In case of unhandled DB type or SQLException
     */
    public FunctionDAO getFunctionDAO() throws APIMgtDAOException {
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
                throw new APIMgtDAOException("Unhandled DB driver: " + driverName + " detected",
                        ExceptionCodes.APIM_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting FunctionDAO", e);
        }

        return functionDAO;
    }

    /**
     * To get the AnalyticsDao object. Depends on different vendors.
     *
     * @return AnalyticsDAO object
     * @throws APIMgtDAOException if error during getting analytics database connection
     */
    public AnalyticsDAO getAnalyticsDAO() throws APIMgtDAOException {
        AnalyticsDAO analyticsDAO;
        boolean isAnalyticsEnabled = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getAnalyticsConfigurations().isEnabled();
        if (isAnalyticsEnabled) {
            try (Connection connection = DAOUtil.getAnalyticsConnection()) {
                analyticsDAO = getAnalyticsDaoImplForVendor(connection);
            } catch (SQLException e) {
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting FunctionDAO", e);
            }
        } else {
            // if analytics is not enabled create a normal AMDB data connection to check db driver
            try (Connection connection = DAOUtil.getConnection()) {
                analyticsDAO = getAnalyticsDaoImplForVendor(connection);
            } catch (SQLException e) {
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting FunctionDAO", e);
            }
        }
        return analyticsDAO;
    }

    public ThreatProtectionDAO getThreatProtectionDAO() {
        return new ThreatProtectionDAOImpl();
    }

    private AnalyticsDAO getAnalyticsDaoImplForVendor(Connection connection)
            throws SQLException, APIMgtDAOException {
        AnalyticsDAO analyticsDAO = null;
        String driverName = connection.getMetaData().getDriverName();
        if (driverName.contains(MYSQL) || driverName.contains(H2)) {
            analyticsDAO = new AnalyticsDAOImpl();
        } else if (driverName.contains(DB2)) {

        } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
            analyticsDAO = new AnalyticsDAOImpl();

        } else if (driverName.contains(POSTGRE)) {
            analyticsDAO = new AnalyticsDAOImpl();

        } else if (driverName.contains(ORACLE)) {
            analyticsDAO = new AnalyticsDAOImpl();
        } else {
            throw new APIMgtDAOException("Unhandled DB Type detected");
        }
        return analyticsDAO;
    }

    public SystemApplicationDao getSystemApplicationDao() throws APIMgtDAOException {
        return new SystemApplicationDaoImpl();
    }

    public void setup() throws APIMgtDAOException {
        ApiDAOImpl.initResourceCategories();
        ApiDAOImpl.initApiTypes();
        LabelDAOImpl.initDefaultLabels();
    }

    public UserMappingDAO getUserMappingDAO() {
        return new UserMappingDAOImpl();
    }

}
