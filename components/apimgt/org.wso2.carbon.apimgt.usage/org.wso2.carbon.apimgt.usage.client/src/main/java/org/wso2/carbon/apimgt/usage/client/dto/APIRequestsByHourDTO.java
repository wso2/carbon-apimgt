package org.wso2.carbon.apimgt.usage.client.dto;

/**
 * Created by nisala on 3/24/15.
 */
public class APIRequestsByHourDTO {
    private String api;
    private String api_version;
    private String Date;
    private String requestCount;
    private String tier;

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(String requestCount) {
        this.requestCount = requestCount;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getApi() {

        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

	public String getDateTierCount() {
		return this.getDate().concat("|").concat(this.getTier()).concat("|").concat(this.getRequestCount());
	}
}
