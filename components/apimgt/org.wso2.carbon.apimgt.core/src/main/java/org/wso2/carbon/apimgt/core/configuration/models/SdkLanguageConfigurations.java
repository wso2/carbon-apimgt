package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold SDK Language configurations
 */
@Configuration(description = "SDK Generation Language Configuration")
public class SdkLanguageConfigurations {

    public SdkLanguageConfigurations() {
        sdkGenLanguages.put("java", "io.swagger.codegen.languages.JavaClientCodegen");
        sdkGenLanguages.put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
        sdkGenLanguages.put("python", "io.swagger.codegen.languages.PythonClientCodegen");
    }

    @Element(description = "SDK Generation Supported Languages")
    private Map<String, String> sdkGenLanguages = new HashMap<>();

    public Map<String, String> getSdkGenLanguages() {
        return sdkGenLanguages;
    }

    public void setSdkGenLanguages(Map<String, String> sdkGenLanguages) {
        this.sdkGenLanguages = sdkGenLanguages;
    }
}
