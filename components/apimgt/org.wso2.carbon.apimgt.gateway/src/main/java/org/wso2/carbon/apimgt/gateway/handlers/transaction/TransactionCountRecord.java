package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import java.time.Instant;
import java.util.UUID;

public class TransactionCountRecord {

    private final String id = UUID.randomUUID().toString();
    private Integer count;
    private final String recordedTime;

    public TransactionCountRecord(Integer count) {
        this.count = count;
        this.recordedTime = Instant.now().toString();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

}
