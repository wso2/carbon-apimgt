import org.wso2.carbon.apimgt.ballerina.maps;

function testMapRemove(string key, any value) (any) {
    maps:putMapEntry(key, value);
    maps:removeMapEntry(key);
    any mapVal = maps:getMapEntry(key);
    if (mapVal != null) {
            return true;
        } else {
            return false;
        }
}
