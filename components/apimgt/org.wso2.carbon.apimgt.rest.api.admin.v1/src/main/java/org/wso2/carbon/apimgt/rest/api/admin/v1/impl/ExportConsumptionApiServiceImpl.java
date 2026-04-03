/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ExportConsumptionApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.usage.data.exporter.ConsumptionDataExportService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.ws.rs.core.Response;

public class ExportConsumptionApiServiceImpl implements ExportConsumptionApiService {

    private static final Log log = LogFactory.getLog(ExportConsumptionApiServiceImpl.class);
    /** Filename for the outer ZIP archive sent to the browser. */
    private static final String ZIP_FILENAME_TEMPLATE = "consumption-report-%s-to-%s.zip";
    /** Entry name used inside the ZIP for the JSON report — must end in .json, not .zip. */
    private static final String JSON_ENTRY_FILENAME_TEMPLATE = "consumption-report-%s-to-%s.json";

    public Response exportConsumptionData(String fromDate, String toDate, MessageContext messageContext) {

        // Only super tenant is allowed to access consumption export
        RestApiAdminUtils.checkSuperTenantAccess("consumption export");

        // Parse and validate date parameters
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(fromDate);
            endDate = LocalDate.parse(toDate);
        } catch (DateTimeParseException e) {
            String msg = "Invalid date format. Expected YYYY-MM-DD, got fromDate='"
                    + fromDate + "' toDate='" + toDate + "'";
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode((long) Response.Status.BAD_REQUEST.getStatusCode());
            errorDTO.setMessage(Response.Status.BAD_REQUEST.toString());
            errorDTO.setDescription(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
        }

        // Retrieve the OSGi service from the static holder populated by APIManagerComponent
        ConsumptionDataExportService exportService =
                ServiceReferenceHolder.getInstance().getConsumptionDataExportService();

        if (exportService == null) {
            String msg = "ConsumptionDataExportService is unavailable";
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode((long) Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            errorDTO.setMessage(Response.Status.SERVICE_UNAVAILABLE.toString());
            errorDTO.setDescription(msg);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(errorDTO).build();
        }

        try {
            String zipFilename = String.format(ZIP_FILENAME_TEMPLATE, fromDate, toDate);
            String jsonEntryFilename = String.format(JSON_ENTRY_FILENAME_TEMPLATE, fromDate, toDate);
            byte[] zipBytes = exportService.exportConsumptionDataAsZip(startDate, endDate, jsonEntryFilename);
            File tempFile = File.createTempFile("consumption-export-", ".zip");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(zipBytes);
            }
            return Response.ok(tempFile)
                    .type(RestApiConstants.APPLICATION_ZIP)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                            "attachment; filename=\"" + zipFilename + "\"")
                    .build();
        } catch (IOException e) {
            String msg = "Error writing consumption zip to temporary file for range " + fromDate + " to " + toDate;
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode((long) Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            errorDTO.setMessage(Response.Status.INTERNAL_SERVER_ERROR.toString());
            errorDTO.setDescription(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (Exception e) {
            String msg = "Error exporting consumption data for range " + fromDate + " to " + toDate;
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode((long) Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            errorDTO.setMessage(Response.Status.INTERNAL_SERVER_ERROR.toString());
            errorDTO.setDescription(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }
}
