package org.wso2.apk.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class EventCountLimitAllOfDTO   {
  
    private Long eventCount = null;

  /**
   * Maximum number of events allowed
   **/
  public EventCountLimitAllOfDTO eventCount(Long eventCount) {
    this.eventCount = eventCount;
    return this;
  }

  
  @ApiModelProperty(example = "3000", required = true, value = "Maximum number of events allowed")
  @JsonProperty("eventCount")
  @NotNull
  public Long getEventCount() {
    return eventCount;
  }
  public void setEventCount(Long eventCount) {
    this.eventCount = eventCount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventCountLimitAllOfDTO eventCountLimitAllOf = (EventCountLimitAllOfDTO) o;
    return Objects.equals(eventCount, eventCountLimitAllOf.eventCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventCountLimitAllOfDTO {\n");
    
    sb.append("    eventCount: ").append(toIndentedString(eventCount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

