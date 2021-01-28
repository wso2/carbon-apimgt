package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set the transport config context
 */
public class TransportConfigContext extends ConfigContextDecorator {

    private API api;
    private APIProduct apiProduct;

    public TransportConfigContext(ConfigContext context, API api) {

        super(context);
        this.api = api;
    }

    public TransportConfigContext(ConfigContext context, APIProduct apiProduct) {

        super(context);
        this.apiProduct = apiProduct;
    }

    @Override
    public void validate() throws APITemplateException, APIManagementException {

        super.validate();
    }

    @Override
    public VelocityContext getContext() {

        VelocityContext context = super.getContext();

        String transportsString = "";

        if (api != null) {
            transportsString = api.getTransports();
        } else if (apiProduct != null) {
            transportsString = apiProduct.getTransports();
        }

        setTransportInVelocityContext(context, transportsString);

        return context;
    }

    private void setTransportInVelocityContext(VelocityContext context, String transportsString) {

        String apiSecurity = null;

        if (api != null) {
            apiSecurity = api.getApiSecurity();
        } else if (apiProduct != null) {
            apiSecurity = apiProduct.getApiSecurity();
        }
        if (StringUtils.isNotEmpty(apiSecurity) &&
                (apiSecurity.contains(APIConstants.API_SECURITY_MUTUAL_SSL) ||
                        apiSecurity.contains(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY))) {
            context.put("transport", "https");
            return;
        }
        if (transportsString.contains(",")) {
            List<String> transports = new ArrayList<String>(Arrays.asList(transportsString.split(",")));
            if (transports.contains(Constants.TRANSPORT_HTTP) && transports.contains(Constants.TRANSPORT_HTTPS)) {
                context.put("transport", "");
            }
        } else {
            context.put("transport", transportsString);
        }
    }
}
