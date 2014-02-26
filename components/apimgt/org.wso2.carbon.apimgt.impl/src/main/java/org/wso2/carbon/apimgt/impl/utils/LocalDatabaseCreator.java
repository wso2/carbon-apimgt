/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import javax.sql.DataSource;
import java.io.File;

public class LocalDatabaseCreator extends DatabaseCreator {

    private DataSource dataSource;

    private static final Log log = LogFactory.getLog(LocalDatabaseCreator.class);
    public LocalDatabaseCreator(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }


    /**
     * Creates registry database if the script exists; returns otherwise.
     *
     * @throws Exception
     */
    public void createRegistryDatabase() throws Exception{

        String databaseType = DatabaseCreator.getDatabaseType(this.dataSource.getConnection());
        String dbscriptName = getDbScriptLocation(databaseType);

        String scripPath = getDbScriptLocation(databaseType);
        File scripFile = new File(scripPath);
        if(scripFile.exists()){
            super.createRegistryDatabase();
        }else {
            return;
        }
    }

    protected String getDbScriptLocation(String databaseType) {
        String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        String carbonHome = System.getProperty("carbon.home");
        return  carbonHome +
                "/dbscripts/apimgt/" + scriptName;

    }
}
