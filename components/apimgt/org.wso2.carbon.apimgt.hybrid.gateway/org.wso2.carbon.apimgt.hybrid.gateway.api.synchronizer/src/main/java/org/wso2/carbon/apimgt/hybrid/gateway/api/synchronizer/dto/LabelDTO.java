package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class LabelDTO  {


    @NotNull
    private String name = null;


    private List<String> accessUrls = new ArrayList<String>();

    private String lastUpdatedTime = null;

    private String createdTime = null;

    /**
     * gets and sets the lastUpdatedTime for LabelDTO
     **/
    @JsonIgnore
    public String getLastUpdatedTime(){
        return lastUpdatedTime;
    }
    public void setLastUpdatedTime(String lastUpdatedTime){
        this.lastUpdatedTime=lastUpdatedTime;
    }

    /**
     * gets and sets the createdTime for a LabelDTO
     **/

    @JsonIgnore
    public String getCreatedTime(){
        return createdTime;
    }
    public void setCreatedTime(String createdTime){
        this.createdTime=createdTime;
    }


    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("accessUrls")
    public List<String> getAccessUrls() {
        return accessUrls;
    }
    public void setAccessUrls(List<String> accessUrls) {
        this.accessUrls = accessUrls;
    }



    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class LabelDTO {\n");

        sb.append("  name: ").append(name).append("\n");
        sb.append("  accessUrls: ").append(accessUrls).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
