/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class is used to generate SDKs for a given API
 */

public class APIClientGenerationManager {

    private static final Log log = LogFactory.getLog(APIClientGenerationManager.class);
    private static final Map<String, String> langCodeGen = new HashMap<String, String>();

    public APIClientGenerationManager() {
        langCodeGen.put("java", "io.swagger.codegen.languages.JavaClientCodegen");
        langCodeGen.put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
        langCodeGen.put("csharp", "io.swagger.codegen.languages.CSharpClientCodegen");
        langCodeGen.put("cpp", "io.swagger.codegen.languages.CppRestClientCodegen");
        langCodeGen.put("dart", "io.swagger.codegen.languages.DartClientCodegen");
        langCodeGen.put("flash", "io.swagger.codegen.languages.FlashClientCodegen");
        langCodeGen.put("go", "io.swagger.codegen.languages.GoClientCodegen");
        langCodeGen.put("groovy", "io.swagger.codegen.languages.GroovyClientCodegen");
        langCodeGen.put("javascript", "io.swagger.codegen.languages.JavascriptClientCodegen");
        langCodeGen.put("jmeter", "io.swagger.codegen.languages.JMeterCodegen");
        langCodeGen.put("nodejs", "io.swagger.codegen.languages.NodeJSServerCodegen");
        langCodeGen.put("perl", "io.swagger.codegen.languages.PerlClientCodegen");
        langCodeGen.put("php", "io.swagger.codegen.languages.PhpClientCodegen");
        langCodeGen.put("python", "io.swagger.codegen.languages.PythonClientCodegen");
        langCodeGen.put("ruby", "io.swagger.codegen.languages.RubyClientCodegen");
        langCodeGen.put("scala", "io.swagger.codegen.languages.ScalaClientCodegen");
        langCodeGen.put("swift", "io.swagger.codegen.languages.SwiftCodegen");
        langCodeGen.put("clojure", "io.swagger.codegen.languages.ClojureClientCodegen");
        langCodeGen.put("aspNet5", "io.swagger.codegen.languages.AspNet5ServerCodegen");
        langCodeGen.put("asyncScala", "io.swagger.codegen.languages.AsyncScalaClientCodegen");
        langCodeGen.put("spring", "io.swagger.codegen.languages.SpringCodegen");
        langCodeGen.put("csharpDotNet2", "io.swagger.codegen.languages.CsharpDotNet2ClientCodegen");
        langCodeGen.put("haskell", "io.swagger.codegen.languages.HaskellServantCodegen");
    }

    /**
     * This method generates client side SDK for a given API
     *
     * @param sdkLanguage preferred language to generate the SDK
     * @param apiName     name of the API
     * @param apiVersion  version of the API
     * @param apiProvider provider of the API
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String sdkLanguage, String apiName, String apiVersion, String apiProvider)
            throws APIClientGenerationException {

        return null;
    }

    /**
     * This method will delete the files and directories in a given location
     *
     * @param tempDirectoryPath location of the directory to be cleaned
     */
    public void cleanTempDirectory(String tempDirectoryPath) {
        
    }

    /**
     * This method is used to retrieve the supported languages for SDK generation
     *
     * @return supported languages for SDK generation
     */
    public String getSupportedSDKLanguages() {
        return null;
    }

    /**
     * This method is used to generate SDK for a API for a given language
     *
     * @param apiName             name of the API
     * @param apiVersion          version of the API
     * @param specLocation        location of the swagger spec for the API
     * @param sdkLanguage         preferred SDK language
     * @param temporaryOutputPath temporary location where the SDK archive is saved until downloaded
     */
    private void generateClient(String apiName, String apiVersion, String specLocation,
                                String sdkLanguage, String temporaryOutputPath) {

      
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK
     *
     * @param errorMessage error message to be printed in the log
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage) throws APIClientGenerationException {
        log.error(errorMessage);
        throw new APIClientGenerationException(errorMessage);
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK (with a throwable exception)
     *
     * @param errorMessage error message to be printed in the log
     * @param throwable    throwable exception caught
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage, Throwable throwable) throws APIClientGenerationException {
        log.error(errorMessage, throwable);
        throw new APIClientGenerationException(errorMessage, throwable);
    }
}
