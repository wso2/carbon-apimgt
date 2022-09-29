package org.wso2.carbon.apimgt.rest.api.util.interceptors.response;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.impl.utils.UserTokenUtil;

/**
 * This class cleanup resources such as Thread Locals
 */
public class CleanUpInterceptor extends AbstractPhaseInterceptor<Message> {

    public CleanUpInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        UserTokenUtil.clear();
    }
}
