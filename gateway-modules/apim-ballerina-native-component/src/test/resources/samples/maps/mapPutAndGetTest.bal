import org.wso2.carbon.apimgt.ballerina.maps;

function testMapPutAndGet(string key, any value) (any) {
    maps:putMapEntry(key, value);
    return maps:getMapEntry(key);
}
