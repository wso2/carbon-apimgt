package org.wso2.carbon.apimgt.api.model;

/**
 * Created by dinushad on 2/10/16.
 */
public class DateCondition extends Condition{
    private String startingDate;
    private String endingDate;
    private String specificDate;

    public String getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public String getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(String specificDate) {
        this.specificDate = specificDate;
    }

    public String getStartingDate() {

        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }
}
