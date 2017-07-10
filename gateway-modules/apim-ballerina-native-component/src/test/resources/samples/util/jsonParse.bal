import org.wso2.carbon.apimgt.ballerina.util;
import ballerina.lang.system;

function testParseJson () (boolean) {
    string actual = "{\"enabled\":false,\"type\":null,\"username\":null,\"password\":null}";
    json parsedJson = util:parse(actual);
    system:println(parsedJson);
    if (parsedJson != null) {
        system:println("object exist");
        boolean enabled = (boolean)parsedJson.enabled;
        string type;
        string username;
        string password;
        if(parsedJson.type != null){
            type = (string)parsedJson.type;
        }
        if(parsedJson.username != null){
            username = (string)parsedJson.username;
        }
        if(parsedJson.password != null){
            password = (string)parsedJson.password;
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