package org.wso2.carbon.apimgt.api.model;

public class ApiTypeWrapper {
    private API api;
    private APIProduct apiProduct;
    private boolean isAPIProduct;

    public ApiTypeWrapper(API api) {
        this.api = api;
        isAPIProduct = false;
    }

    public ApiTypeWrapper(APIProduct apiProduct) {
        this.apiProduct = apiProduct;
        isAPIProduct = true;
    }

    public boolean isAPIProduct() {
        return isAPIProduct;
    }

    public API getApi() {
        return api;
    }

    public APIProduct getApiProduct() {
        return apiProduct;
    }

    public String getName() {
        if (isAPIProduct) {
            return apiProduct.getId().getName();
        } else {
            return api.getId().getName();
        }
    }

    public String getStatus() {
        if (isAPIProduct) {
            return apiProduct.getState();
        } else {
            return api.getStatus();
        }
    }

    public void setTier(String tier) {
        if (isAPIProduct) {
            apiProduct.getId().setTier(tier);
        } else {
            api.getId().setTier(tier);
        }
    }
}
