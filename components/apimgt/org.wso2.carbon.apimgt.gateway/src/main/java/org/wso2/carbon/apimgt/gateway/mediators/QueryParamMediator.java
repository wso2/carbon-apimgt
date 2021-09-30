package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This @QueryParamMediator mediator is used to evaluate the REST_URL_POST_FIX after the requested query param is
 * removed
 */
public class QueryParamMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(DigestAuthMediator.class);
    private String queryParamToRemove;

    /**
     * This method performs query param removal
     *
     * @param messageContext
     * @return A boolean value.True if successful and false if not.
     */
    public boolean mediate(MessageContext messageContext) {
        String urlPostfix = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty("REST_URL_POSTFIX");
        String modifiedURL = removeQueryParam(urlPostfix);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX", modifiedURL);

        //todo: remove log
        log.info("modified url : " + modifiedURL);
        return true;
    }

    /**
     *  Removes the specified query parameter from the URL postfix using below method.
     *
     *  X denotes query param location.
     *  (L=T) or (L=F) denotes whether there are more query params to the left.
     *  (R=T) or (R=F) denotes whether there are more query params to the right.
     *
     *     L   QP  R
     * 1.  F   X   F  => there's only one query param on the list of form ?param=key$ -> should be replaced with ''(empty string)
     * 2.  F   X   T  => param to be removed is the first on the list, and there are more to its right of form ?param=key& -> should be replaced with ?
     * 3.  T   X   F  => param to be removed is the last on the list, of form &param=key$ -> should be replaced with ''(empty string)
     * 4.  T   X   T  => param to be removed is sandwiched, form &param=key& -> should be replaced with '&'
     *
     * @param url URL to be processed
     * @return
     */
    private String removeQueryParam(String url) {
        if (log.isDebugEnabled()) {
            log.debug("Processing URL : " + url);
        }

        Pattern patternOne = Pattern.compile("(.*)\\?" + queryParamToRemove + "=[^&]*$");
        Pattern patternTwo = Pattern.compile("(.*)\\?" + queryParamToRemove + "=[^&]*&(.*)"); //group(1)?group(2)
        Pattern patternThree = Pattern.compile("(.*)&" + queryParamToRemove + "=[^&]*$"); //group(1)
        Pattern patternFour = Pattern.compile("(.*)&" + queryParamToRemove + "=[^&]*&(.*)"); // group(1)&group(2)

        Pattern[] patterns = { patternOne, patternTwo, patternThree, patternFour };
        Matcher matcher = null;
        int index = 1;
        boolean found = false;
        String modifiedURL = url;
        for (Pattern pattern : patterns) {
            matcher = pattern.matcher(url);
            if (matcher.find()) {
                found = true;
                break;
            }
            index++;
        }

        if (found) {
            switch (index) {
                case 2 :
                    modifiedURL = matcher.group(1) + "?" + matcher.group(2);
                    break;
                case 1 :
                case 3 :
                    modifiedURL = matcher.group(1);
                    break;
                case 4 :
                    modifiedURL = matcher.group(1) + "&" + matcher.group(2);
            }

            if (log.isDebugEnabled()) {
                log.debug("Modified URL after removing query param : " + modifiedURL);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Query parameter " + queryParamToRemove + " is not there in the provided URL " + url);
            }
        }

        return modifiedURL;
    }

    public void setQueryParamToRemove(String queryParamToRemove) {
        this.queryParamToRemove = queryParamToRemove;
    }

    public String getQueryParamToRemove() {
        return queryParamToRemove;
    }
}
