/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.reportgen.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.reportgen.ReportGenerator;
import org.wso2.carbon.apimgt.impl.reportgen.model.RowEntry;
import org.wso2.carbon.apimgt.impl.reportgen.model.TableData;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class ReportGenUtil {

    private static final Log log = LogFactory.getLog(ReportGenUtil.class);

    /**
     * Get microgateway request summary report as a pdf.
     * @param username
     * @param date month with year. Format should be yyyy-mm (ex: 2018-07)
     * @return inputstream InputStream of the pdf
     * @throws APIManagementException exception
     */
    public static InputStream getMicroGatewayRequestSummaryReport(String username, String date)
            throws APIManagementException {
        InputStream pdfInputStream = null;
        // get data
        String value = "0"; //default 
        String appName = "APIM_ACCESS_SUMMARY";
        String query = "from ApiUserPerAppAgg on gatewayType=='MICRO' within '" + date 
                + "-** **:**:**' per 'months' SELECT sum(totalRequestCount) as sum;";
        
        JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(appName, query);
        JSONArray jarray =  (JSONArray) jsonObj.get("records");
        if(jarray != null && jarray.size() != 0) {
            JSONArray val = (JSONArray) jarray.get(0);
            Long result = (Long) val.get(0);
            value = String.valueOf(result);
        }

        //build data object to pass for generation
        TableData table = new TableData();
        String[] columnHeaders = { "", "Date", "Number of requests" };
        table.setColumnHeaders(columnHeaders);
        
        List<RowEntry> rowData = new ArrayList<RowEntry>();
        RowEntry entry = new RowEntry();
        entry.setEntry("1");
        entry.setEntry(date);
        entry.setEntry(value);
        rowData.add(entry);
        table.setRows(rowData);

        // generate the pdf
        ReportGenerator generator = new ReportGenerator();

        try {
            pdfInputStream =  generator.generateMGRequestSummeryPDF(table);
        } catch (IOException e) {
            String msg = "Error while generating the pdf for micro gateway request summary";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return pdfInputStream;
    }

}
