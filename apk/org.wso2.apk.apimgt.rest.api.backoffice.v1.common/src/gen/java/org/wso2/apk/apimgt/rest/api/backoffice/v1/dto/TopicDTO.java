package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class TopicDTO   {
  
    private String id = null;
    private String name = null;
    private String mode = null;
    private String description = null;

  /**
   * id
   **/
  public TopicDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1222344", value = "id")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public TopicDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\s+\\[\\]/]*$)") @Size(min=1,max=50)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public TopicDTO mode(String mode) {
    this.mode = mode;
    return this;
  }

  
  @ApiModelProperty(example = "Pizza", required = true, value = "")
  @JsonProperty("mode")
  @NotNull
 @Size(max=20)  public String getMode() {
    return mode;
  }
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   **/
  public TopicDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple API for Pizza Shack online pizza delivery store.", required = true, value = "")
  @JsonProperty("description")
  @NotNull
 @Size(max=32766)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicDTO topic = (TopicDTO) o;
    return Objects.equals(id, topic.id) &&
        Objects.equals(name, topic.name) &&
        Objects.equals(mode, topic.mode) &&
        Objects.equals(description, topic.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, mode, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TopicDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

