import org.wso2.carbon.apimgt.ballerina.util;
import ballerina.lang.system;

function testWait(int value) (boolean ) {
    int start = system:currentTimeMillis();
    util:wait(value);
    int end = system:currentTimeMillis();
    if(end-start >= value){
        return true;
    }
    return false;
}