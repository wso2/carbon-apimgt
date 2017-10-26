import org.wso2.carbon.apimgt.ballerina.util;

    function listJsonFiles () (string[], string[]) {
        //system:print(system:getEnv("$PWD");
        //string home = system:getEnv("$HOME");
        ////but for windows?? %userprofile%
        //string[] notADir = util:listJSONFiles(home + "/notADir");
        //string[] allJsonDirList = util:listJSONFiles(home + "/allJsonDir");
        //string[] noJsonDirList = util:listJSONFiles(home + "/noJsonDir");
        ////string[] notADir = util:listJSONFiles("/home/sabeena/Desktop/notADir");
        //return notADir, allJsonDirList, noJsonDirList;


        //string home = system:getEnv("$HOME");
        ////but for windows?? %userprofile%
        string[] notADir = util:listJSONFiles("samples/util/notADir");
        string[] testDirList = util:listJSONFiles("samples/util/testDir");
        //string[] noJsonDirList = util:listJSONFiles("samples/util/noJsonDir");
        //string[] notADir = util:listJSONFiles("/home/sabeena/Desktop/notADir");
        return notADir, testDirList;

}
