package org.wso2.apk.apimgt.api.dto;

/**
 * This class represents API endpoint validation DTO
 */
public class APIEndpointValidationDTO {

    private Integer statusCode = null;
    private String statusMessage = null;
    private String error = null;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
