import org.wso2.carbon.apimgt.ballerina.util;
import ballerina.lang.system;
import ballerina.lang.errors;

function testParseJson () (boolean) {
    string actual = "{\"enabled\":false,\"typeAttr\":null,\"username\":null,\"password\":null}";
    json parsedJson = util:parse(actual);
    system:println(parsedJson);
    if (parsedJson != null) {
        system:println("object exist");
        errors:TypeCastError err;
        boolean enabled;
        enabled, err = (boolean)parsedJson.enabled;
        string typeAttr;
        string username;
        string password;
        if(parsedJson.typeAttr != null){
            typeAttr, err = (string)parsedJson.typeAttr;
        }
        if(parsedJson.username != null){
            username, err = (string)parsedJson.username;
        }
        if(parsedJson.password != null){
            password, err = (string)parsedJson.password;
        }
        if (!enabled) {
            return true;
        }else{
            return false;
        }
    } else {
        return false;
    }
}

function testInvalidJson () (boolean) {
    string invalidJsonString = "{Invlid JSON}";
    try{
        json parsedJson = util:parse(invalidJsonString);
    } catch (errors:Error err) {
        return false;
    }
    return true;
}