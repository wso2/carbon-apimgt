package org.wso2.carbon.apimgt.persistence.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.APIConstants;


public class PersistenceUtil {
    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    /**
     * This method will check the validity of given url. WSDL url should be
     * contain http, https, "/t" (for tenant APIs) or file system path
     * otherwise we will mark it as invalid wsdl url. How ever here we do not
     * validate wsdl content.
     *
     * @param wsdlURL wsdl url tobe tested
     * @return true if its valid url else fale
     */
    // NO REG USAGE
    public static boolean isValidWSDLURL(String wsdlURL, boolean required) {

        if (wsdlURL != null && !"".equals(wsdlURL)) {
            if (wsdlURL.startsWith("http:") || wsdlURL.startsWith("https:") ||
                                            wsdlURL.startsWith("file:") || (wsdlURL.startsWith("/t") && !wsdlURL.endsWith(".zip"))) {
                return true;
            }
        } else if (!required) {
            // If the WSDL in not required and URL is empty, then we don't need
            // to add debug log.
            // Hence returning.
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("WSDL url validation failed. Provided wsdl url is not valid url: " + wsdlURL);
        }
        return false;
    }

    /**
     * Method used to create the file name of the wsdl to be stored in the registry
     *
     * @param provider   Name of the provider of the API
     * @param apiName    Name of the API
     * @param apiVersion API Version
     * @return WSDL file name
     */
    public static String createWsdlFileName(String provider, String apiName, String apiVersion) {

        return provider + "--" + apiName + apiVersion + ".wsdl";
    }


    /**
     * Given a wsdl resource, this method checks if the underlying document is a WSDL2
     *
     * @param wsdl byte array of wsdl definition saved in registry
     * @return true if wsdl2 definition
     * @throws APIManagementException
     */
    public static boolean isWSDL2Resource(byte[] wsdl) throws APIManagementException {

        String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
        String wsdlContent = new String(wsdl);
        return wsdlContent.indexOf(wsdl2NameSpace) > 0;
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                            APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }
}
