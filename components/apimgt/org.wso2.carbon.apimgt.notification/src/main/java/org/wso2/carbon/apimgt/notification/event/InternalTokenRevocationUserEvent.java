package org.wso2.carbon.apimgt.notification.event;

/**
 * Event to notify token revocation of a user by user events.
 */
public class InternalTokenRevocationUserEvent extends Event {
    private String subjectId;
    private String subjectIdType;
    private long revocationTime;
    private String organization;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectIdType() {
        return subjectIdType;
    }

    public void setSubjectIdType(String subjectIdType) {
        this.subjectIdType = subjectIdType;
    }

    public long getRevocationTime() {
        return revocationTime;
    }

    public void setRevocationTime(long revocationTime) {
        this.revocationTime = revocationTime;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
