package org.wso2.carbon.apimgt.impl.notifier.events;

public class TenantEvent extends Event {
    private String admin;
    private boolean active;
    private String firstname;
    private String lastName;
    private String email;

    public TenantEvent(String eventId, long timeStamp, String type, int tenantId, String tenantDomain, String admin, boolean active, String firstname,String lastName, String email) {
        super(eventId, timeStamp, type, tenantId, tenantDomain);
        this.admin = admin;
        this.active = active;
        this.firstname = firstname;
        this.email = email;
        this.lastName = lastName;
    }

    public TenantEvent() {
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
