package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.HashSet;
import java.util.Set;

public class ScopesEvent extends Event{

    private Set<ScopeEvent> scopes = new HashSet<>();

    public ScopesEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
    }

    public void addScope(ScopeEvent scopeEvent) {
        scopes.add(scopeEvent);
    }

    public Set<ScopeEvent> getScopes() {
        return scopes;
    }
}
