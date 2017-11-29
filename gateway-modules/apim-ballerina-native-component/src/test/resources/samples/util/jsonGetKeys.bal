import org.wso2.carbon.apimgt.ballerina.util;

function testGetKeys () (string[], string[], string[], string[]) {
    json j1 = {fname:"Jhon", lname:"Doe", age:40};
    json j2 = ["cat", "dog", "horse"];
    json j3 = "Hello";
    json j4 = 5;
    return util:getKeys(j1), util:getKeys(j2), util:getKeys(j3), util:getKeys(j4);
}