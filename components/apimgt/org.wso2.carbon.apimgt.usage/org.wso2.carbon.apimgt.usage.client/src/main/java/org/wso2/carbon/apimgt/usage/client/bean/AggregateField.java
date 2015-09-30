package org.wso2.carbon.apimgt.usage.client.bean;

/**
 * Created by rukshan on 9/28/15.
 */
public class AggregateField {

    String fieldName;
    String aggregate;
    String alias;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public AggregateField(String fieldName, String aggregate, String alias) {
        super();
        this.fieldName = fieldName;
        this.aggregate = aggregate;
        this.alias = alias;
    }
}
