package org.wso2.carbon.apimgt.usage.client.pojo;

/**
 * Created by rukshan on 10/7/15.
 */
public class APIFirstAccess {
    private String year;
    private String month;
    private String day;
    //private long requestCount;

    public APIFirstAccess(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }
}
