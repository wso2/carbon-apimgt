import org.wso2.carbon.apimgt.ballerina.util;
import ballerina.lang.system;

function testIpToLongConvert () (boolean) {
    string ip = "203.94.95.4";
    int longValue = util:convertIpToLong(ip);
    system:println("long value:"+longValue);
    if (longValue == 3411959556) {
        return true;
    }else{
        return false;
    }
}