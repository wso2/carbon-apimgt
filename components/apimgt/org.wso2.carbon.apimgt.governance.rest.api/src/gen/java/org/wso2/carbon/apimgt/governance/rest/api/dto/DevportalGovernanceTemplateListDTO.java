package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

/**
 * A list of Devportal Governance templates.
 **/
@ApiModel(description = "A list of Devportal Governance templates.")
public class DevportalGovernanceTemplateListDTO {

    private Integer count = null;
    private List<DevportalGovernanceTemplateDTO> list = new ArrayList<DevportalGovernanceTemplateDTO>();
    private PaginationDTO pagination = null;

    public DevportalGovernanceTemplateListDTO count(Integer count) {

        this.count = count;
        return this;
    }

    @ApiModelProperty(example = "2", value = "Number of templates returned.")
    @JsonProperty("count")
    public Integer getCount() {

        return count;
    }

    public void setCount(Integer count) {

        this.count = count;
    }

    public DevportalGovernanceTemplateListDTO list(List<DevportalGovernanceTemplateDTO> list) {

        this.list = list;
        return this;
    }

    @ApiModelProperty(value = "List of Devportal Governance templates.")
    @JsonProperty("list")
    @Valid
    public List<DevportalGovernanceTemplateDTO> getList() {

        return list;
    }

    public void setList(List<DevportalGovernanceTemplateDTO> list) {

        this.list = list;
    }

    public DevportalGovernanceTemplateListDTO pagination(PaginationDTO pagination) {

        this.pagination = pagination;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("pagination")
    @Valid
    public PaginationDTO getPagination() {

        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {

        this.pagination = pagination;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevportalGovernanceTemplateListDTO that = (DevportalGovernanceTemplateListDTO) o;
        return Objects.equals(count, that.count) &&
                Objects.equals(list, that.list) &&
                Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {

        return Objects.hash(count, list, pagination);
    }
}
