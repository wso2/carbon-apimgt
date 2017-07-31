import org.wso2.carbon.apimgt.ballerina.util as utils;
import ballerina.lang.errors;

function testGetProperty () (boolean) {
    message m = {};
    utils:setProperty(m, "TEST_OBJECT", "WSO2");
    any getObject = utils:getProperty(m, "TEST_OBJECT");
    if (getObject != null) {
        errors:TypeCastError err;
        string value;
        value, err = (string)getObject;
        if (value == "WSO2") {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}