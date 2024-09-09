package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class TransactionRecordDTO   {
  
    private String id = null;
    private String serverID = null;
    private String serverType = null;
    private String host = null;
    private Integer count = null;
    private String recordedTime = null;

  /**
   **/
  public TransactionRecordDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public TransactionRecordDTO serverID(String serverID) {
    this.serverID = serverID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serverID")
  public String getServerID() {
    return serverID;
  }
  public void setServerID(String serverID) {
    this.serverID = serverID;
  }

  /**
   **/
  public TransactionRecordDTO serverType(String serverType) {
    this.serverType = serverType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serverType")
  public String getServerType() {
    return serverType;
  }
  public void setServerType(String serverType) {
    this.serverType = serverType;
  }

  /**
   **/
  public TransactionRecordDTO host(String host) {
    this.host = host;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("host")
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }

  /**
   **/
  public TransactionRecordDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("count")
  @NotNull
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public TransactionRecordDTO recordedTime(String recordedTime) {
    this.recordedTime = recordedTime;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("recordedTime")
  @NotNull
  public String getRecordedTime() {
    return recordedTime;
  }
  public void setRecordedTime(String recordedTime) {
    this.recordedTime = recordedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionRecordDTO transactionRecord = (TransactionRecordDTO) o;
    return Objects.equals(id, transactionRecord.id) &&
        Objects.equals(serverID, transactionRecord.serverID) &&
        Objects.equals(serverType, transactionRecord.serverType) &&
        Objects.equals(host, transactionRecord.host) &&
        Objects.equals(count, transactionRecord.count) &&
        Objects.equals(recordedTime, transactionRecord.recordedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, serverID, serverType, host, count, recordedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionRecordDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    serverID: ").append(toIndentedString(serverID)).append("\n");
    sb.append("    serverType: ").append(toIndentedString(serverType)).append("\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    recordedTime: ").append(toIndentedString(recordedTime)).append("\n");
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

