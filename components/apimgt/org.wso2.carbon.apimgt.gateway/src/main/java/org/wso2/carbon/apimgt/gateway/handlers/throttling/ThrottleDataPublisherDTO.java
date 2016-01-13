package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class contains the data needed to be published to CEP server for the throttling purpose.
 *
 */
public class ThrottleDataPublisherDTO {


    private static final Log log = LogFactory.getLog(APIThrottleHandler.class);

    boolean isInitialized = false;
    String APIThrottleKey;
    String ApplicationThrottleKey;
    String ResourceThrottleKey;
    String APIThrottleTier;
    String ApplicationThrottleTier;
    String ResourceThrottleTier;
    String MetaKey;          //concatanation of authorizeduser and consumerkey with ":"
    long IPAddressinLong;   //Since IP is check if it's in a range, the IP has to be in the form of a long
    String httpVerb;

    public long getIPAddress() {

        return IPAddressinLong;
    }

    public void setIPAddress(String IPAddress) {
        IPAddressinLong = 0;
        if(IPAddress!=null){
            //convert ipaddress into a long
            String[] ipAddressArray = IPAddress.split("\\.");    //split by "." and add to an array

            for(int i=0; i< ipAddressArray.length;i++){
                int power = 3 -i;
                long ip = Long.parseLong(ipAddressArray[i]);   //parse to long
                IPAddressinLong += ip*Math.pow(256,power);
            }

        }
    }

    public String getHTTPVerb() {
        return httpVerb;
    }

    public void setHTTPVerb(String HTTPVerb) {
        httpVerb = HTTPVerb;
    }

    public String getAPIThrottleKey() {
        return APIThrottleKey;
    }

    public void setAPIThrottleKey(String APIThrottleKey) {
        this.APIThrottleKey = APIThrottleKey;
    }

    public String getApplicationThrottleKey() {
        return ApplicationThrottleKey;
    }

    public void setApplicationThrottleKey(String applicationThrottleKey) {
        ApplicationThrottleKey = applicationThrottleKey;
    }

    public String getResourceThrottleKey() {
        return ResourceThrottleKey;
    }

    public void setResourceThrottleKey(String resourceThrottleKey) {
        ResourceThrottleKey = resourceThrottleKey;
    }

    public void setMetaKey(String consumerkey, String username){
        MetaKey = consumerkey+":"+username;
    }

    public String getMetaKey(){
        return MetaKey;
    }
    public String getAPIThrottleTier() {
        return APIThrottleTier;
    }

    public void setAPIThrottleTier(String APIThrottleTier) {
        this.APIThrottleTier = APIThrottleTier;
    }

    public String getApplicationThrottleTier() {
        return ApplicationThrottleTier;
    }

    public void setApplicationThrottleTier(String applicationThrottleTier) {
        ApplicationThrottleTier = applicationThrottleTier;
    }

    public String getResourceThrottleTier() {
        return ResourceThrottleTier;
    }

    public void setResourceThrottleTier(String resourceThrottleTier) {
        ResourceThrottleTier = resourceThrottleTier;
    }



}
