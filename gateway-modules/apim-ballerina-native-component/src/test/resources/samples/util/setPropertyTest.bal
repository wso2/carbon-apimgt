import org.wso2.carbon.apimgt.ballerina.util;
import ballerina.lang.system;

function testSetProperty () (boolean) {
    ABC abc = {};
    abc.a = "a";
    cde.b = "b";
    message m = {};
    utils:setProperty(m,"TEST_OBJECT",abc);
    any getObject = utils:getProperty(m,"TEST_OBJECT");
    if(getObject != null){
        system:println("object exist");
        ABC anotherAbc = (ABC)getObject;
        if((anotherAbc.a == "a") &&(anotherAbc.b == "b")){
            system:println("object can cast to ABC");
            return true;
        }
        }else{
        return false;
    }
}
struct ABC{
    string a;
    string b;
}