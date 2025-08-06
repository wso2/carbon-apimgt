package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LLMModel implements Serializable {
    private String modelVendor;
    private List<String> values = new ArrayList<>();

    public LLMModel(String modelVendor, List<String> values) {
        this.modelVendor = modelVendor;
        this.values = values;
    }

    public LLMModel() {
    }

    public String getModelVendor() {
        return modelVendor;
    }

    public void setModelVendor(String modelVendor) {
        this.modelVendor = modelVendor;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
