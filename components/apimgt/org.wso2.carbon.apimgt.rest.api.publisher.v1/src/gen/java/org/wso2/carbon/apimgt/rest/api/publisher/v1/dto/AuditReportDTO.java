package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class AuditReportDTO   {
  
    private String report = null;
    private String grade = null;
    private Integer numErrors = null;
    private String externalApiId = null;

  /**
   * The API Security Audit Report 
   **/
  public AuditReportDTO report(String report) {
    this.report = report;
    return this;
  }

  
  @ApiModelProperty(value = "The API Security Audit Report ")
  @JsonProperty("report")
  public String getReport() {
    return report;
  }
  public void setReport(String report) {
    this.report = report;
  }

  /**
   * The overall grade of the Security Audit 
   **/
  public AuditReportDTO grade(String grade) {
    this.grade = grade;
    return this;
  }

  
  @ApiModelProperty(example = "27.95", value = "The overall grade of the Security Audit ")
  @JsonProperty("grade")
  public String getGrade() {
    return grade;
  }
  public void setGrade(String grade) {
    this.grade = grade;
  }

  /**
   * The number of errors in the API Definition 
   **/
  public AuditReportDTO numErrors(Integer numErrors) {
    this.numErrors = numErrors;
    return this;
  }

  
  @ApiModelProperty(example = "20", value = "The number of errors in the API Definition ")
  @JsonProperty("numErrors")
  public Integer getNumErrors() {
    return numErrors;
  }
  public void setNumErrors(Integer numErrors) {
    this.numErrors = numErrors;
  }

  /**
   * The External API ID 
   **/
  public AuditReportDTO externalApiId(String externalApiId) {
    this.externalApiId = externalApiId;
    return this;
  }

  
  @ApiModelProperty(example = "fd21f9f7-3674-49cf-8a83-dca401f635de", value = "The External API ID ")
  @JsonProperty("externalApiId")
  public String getExternalApiId() {
    return externalApiId;
  }
  public void setExternalApiId(String externalApiId) {
    this.externalApiId = externalApiId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuditReportDTO auditReport = (AuditReportDTO) o;
    return Objects.equals(report, auditReport.report) &&
        Objects.equals(grade, auditReport.grade) &&
        Objects.equals(numErrors, auditReport.numErrors) &&
        Objects.equals(externalApiId, auditReport.externalApiId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(report, grade, numErrors, externalApiId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuditReportDTO {\n");
    
    sb.append("    report: ").append(toIndentedString(report)).append("\n");
    sb.append("    grade: ").append(toIndentedString(grade)).append("\n");
    sb.append("    numErrors: ").append(toIndentedString(numErrors)).append("\n");
    sb.append("    externalApiId: ").append(toIndentedString(externalApiId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

