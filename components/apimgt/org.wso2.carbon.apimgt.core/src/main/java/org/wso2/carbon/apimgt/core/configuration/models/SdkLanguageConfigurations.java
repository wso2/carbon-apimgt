package org.wso2.carbon.apimgt.rest.api.store.configuration;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.HashMap;
import java.util.Map;

@Configuration(namespace = "wso2.carbon.apimgt.rest.api.store.sdklanguageconfig"
            ,description = "SDK generation language configuration")
public class SdkLanguageConfigurations {

    public SdkLanguageConfigurations(){
        sdkGenLanguages.put("java", "io.swagger.codegen.languages.JavaClientCodegen");
        sdkGenLanguages.put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
        sdkGenLanguages.put("python", "io.swagger.codegen.languages.PythonClientCodegen");
    }

    @Element(description = "SDK generation supported languages")
    private Map<String,String> sdkGenLanguages = new HashMap<>();

    public Map<String, String> getSdkGenLanguages() {
        return sdkGenLanguages;
    }

    public void setSdkGenLanguages(Map<String, String> sdkGenLanguages) {
        this.sdkGenLanguages = sdkGenLanguages;
    }
}
