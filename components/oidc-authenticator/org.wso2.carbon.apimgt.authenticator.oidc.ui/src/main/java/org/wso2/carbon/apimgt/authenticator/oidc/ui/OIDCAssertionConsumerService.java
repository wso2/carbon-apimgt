/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.authenticator.oidc.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.authenticator.OIDCUIAuthenticator;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.common.OIDCConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * OIDCAssertionConsumerService holds responsibility to handle response from OIDC server
 * Then it will do the required validations and allow user to authenticate into the system
 *
 */
public class OIDCAssertionConsumerService extends HttpServlet {


	private static final long serialVersionUID = 5451353570561170887L;
	public static final Log log = LogFactory.getLog(OIDCAssertionConsumerService.class);
	


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String state = req.getParameter(OIDCConstants.PARAM_STATE);

        // check for state, if it doesn't match we bail early
        if (state == null || !state.equals(req.getSession().getAttribute(OIDCConstants.PARAM_STATE))) {
            log.error("STATE does not match. Hence redirecting to error page. ");
            handleMalformedResponses(req, resp,
                    "STATE does not match. Hence redirecting to error page.");
            return;
        }

        try {
            handleOIDCResponses(req, resp);
        } catch (Exception e) {
            log.error("Error when processing OIDC response.", e);
            handleMalformedResponses(req, resp, OIDCConstants.ErrorMessageConstants.RESPONSE_MALFORMED);
        }

    }


    /**
     * Handle OIDC response
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void handleOIDCResponses(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String url = req.getRequestURI();
        url = url.replace("oidcacs", "carbon/admin/login_action.jsp");

        RequestDispatcher reqDispatcher = req.getRequestDispatcher(url);
        req.getSession().setAttribute("CarbonAuthenticator", new OIDCUIAuthenticator());
        reqDispatcher.forward(req, resp);

    }



    /**
     * Handle malformed Responses.
     *
     * @param req   HttpServletRequest
     * @param resp  HttpServletResponse
     * @param errorMsg  Error message to be displayed in HttpServletResponse.jsp
     * @throws IOException  Error when redirecting
     */
    private void handleMalformedResponses(HttpServletRequest req, HttpServletResponse resp,
                                          String errorMsg) throws IOException {
        HttpSession session = req.getSession();
        session.setAttribute(OIDCConstants.NOTIFICATIONS_ERROR_MSG, errorMsg);
        resp.sendRedirect(getAdminConsoleURL(req) + "oidc-acs/notifications.jsp");
        //return;
    }

    /**
     * Get the admin console url from the request.
     *
     * @param request httpServletReq that hits the ACS Servlet
     * @return Admin Console URL       https://10.100.1.221:9443/oidcacs/carbon/
     */
    private String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        if (url.contains("/oidcacs")) {
            url = url.replace("/oidcacs", "");
        }
        return url;
    }



}
