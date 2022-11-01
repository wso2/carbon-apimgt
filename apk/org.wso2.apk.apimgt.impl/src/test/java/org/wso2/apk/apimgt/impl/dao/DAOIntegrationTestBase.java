package org.wso2.apk.apimgt.impl.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;

public class DAOIntegrationTestBase {
    protected DBDataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(DAOIntegrationTestBase.class);

    public DAOIntegrationTestBase() {
    }

    @BeforeClass
    public void init() throws Exception {
        dataSource = new DBDataSource();
        int maxRetries = 5;
        long maxWait = 5000;
        while (maxRetries > 0) {
            try (Connection connection = dataSource.getConnection()) {
                log.info("Database Connection Successful");
                APIMgtDBUtil.initialize(dataSource.getDatasource());

                //todo: remove hardcoded value
                String sqlFilePath = "/Users/sachini/wso2/gitclones/carbon-apimgt/apk/org.wso2.apk.apimgt.impl/src/main/resources/postgresql.sql";
                DBScriptRunnerUtil.executeSQLScript(sqlFilePath, connection);
                break;
            } catch (Exception e) {
                if (maxRetries > 0) {
                    log.warn("Couldn't connect into database retrying after next 5 seconds");
                    maxRetries--;
                    try {
                        Thread.sleep(maxWait);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    log.error("Max tries 5 exceed to connect");
                    throw e;
                }
            }
        }
    }

    /*@AfterClass
    public void tempDBCleanup() throws SQLException {
        dataSource.resetDB();
    }*/
}