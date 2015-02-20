package org.apache.synapse.rest.version;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;

public class ContextVersionStrategy extends AbstractVersionStrategy {
    String versionParam;

    public ContextVersionStrategy(API api, String version, String versionParam) {
        super(api, version, VersionStrategyFactory.TYPE_CONTEXT);
        this.versionParam = versionParam;

        // We resolve the API Context here.
        String context = api.getContext();

        // We are resolving the context here. No need to check whether the context param exists.
        context = context.replace(RESTConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE, version);
        api.setContext(context);
    }

    /*
    * This method will return whether the given version string matches the incoming one.
    * Since in ContextVersionStrategy, the version is part of the context, there is no requirement to check whether
    * the version matches.
    * We only need to check whether the request path starts with the context. If so the version is matched.
    *
    * Ex:- context - /1.0.0/foo
    *      incoming path - /1.0.0/foo/bar
    * */
    public boolean isMatchingVersion(Object versionInfoObj) {
        MessageContext msgContext = (MessageContext) versionInfoObj;

        String path = RESTUtils.getFullRequestPath(msgContext);
        String context = getAPI().getContext();

        return path.startsWith(context);
    }

    public String getVersionParam() {
        return versionParam;
    }
}
