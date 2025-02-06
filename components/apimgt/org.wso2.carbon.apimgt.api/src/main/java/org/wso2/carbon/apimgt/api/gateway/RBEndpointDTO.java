package org.wso2.carbon.apimgt.api.gateway;

public class RBEndpointDTO {

    private String model;
    private String endpointId;
    private double weight;

    public String getModel() {

        return model;
    }

    public void setModel(String model) {

        this.model = model;
    }

    public String getEndpointId() {

        return endpointId;
    }

    public void setEndpointId(String endpointId) {

        this.endpointId = endpointId;
    }

    public double getWeight() {

        return weight;
    }

    public void setWeight(double weight) {

        this.weight = weight;
    }
}
