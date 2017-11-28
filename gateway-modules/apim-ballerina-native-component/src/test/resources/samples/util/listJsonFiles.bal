import org.wso2.carbon.apimgt.ballerina.util;

    function listJsonFiles () (string[], string[]) {
        string[] notADir = util:listJSONFiles("samples/util/notADir");
        string[] testDirList = util:listJSONFiles("samples/util/testDir");
        return notADir, testDirList;
}
