package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold Version configuration parameters
 */
@Configuration(description = "Version Configurations")
public class NewVersionNotifierConfigurations {

    @Element(description = "notifiers")
    List<NotifierConfigurations> notifierConfigurations = new ArrayList<>();

    public NewVersionNotifierConfigurations() {
        notifierConfigurations.add(new NotifierConfigurations());
    }

    public List<NotifierConfigurations> getNotifierConfigurations() {
        return notifierConfigurations;
    }

    public void setNotifierConfigurations(List<NotifierConfigurations> notifierConfigurations) {
        this.notifierConfigurations = notifierConfigurations;
    }
}
