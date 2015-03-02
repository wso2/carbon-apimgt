/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.db;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * A mediator that writes (i.e. inserts one row) to a table using message information
 */
public class DBReportMediator extends AbstractDBMediator {

    public boolean isUseTransaction() {
        return useTransaction;
    }

    public void setUseTransaction(boolean useTransaction) {
        this.useTransaction = useTransaction;
    }

    // Does the DBReport mediator participate in a distribute tx?
    // default do not participate in a distribute tx
    boolean useTransaction = false;

    protected void processStatement(Statement stmnt, MessageContext msgCtx) {


        SynapseLog synLog = getLog(msgCtx);

        Connection con = null;
        try {
        	con = this.getDataSource().getConnection();
            PreparedStatement ps = getPreparedStatement(stmnt, con, msgCtx);
            int count = ps.executeUpdate();

            if (count > 0) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug(
                            "Inserted " + count + " row/s using statement : " + stmnt.getRawStatement());
                }
            } else {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug(
                            "No rows were inserted for statement : " + stmnt.getRawStatement());
                }
            }

            if (!useTransaction) {
                if (!con.getAutoCommit()) {
                    con.commit();
                }
            }

        } catch (SQLException e) {
            handleException("Error execuring insert statement : " + stmnt.getRawStatement() +
                    " against DataSource : " + getDSName(), e, msgCtx);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }
}
