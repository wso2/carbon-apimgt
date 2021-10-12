package org.wso2.carbon.apimgt.solace.Parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import io.apicurio.datamodels.core.models.Extension;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;

import java.util.Map;

public class SolaceApiParser extends AsyncApiParser {


    /**
     * This method will extract the vendor provider or the API specification form the extensions list
     *
     * @param definition String
     * @return String
     */
    @Override
    public  String getVendorFromExtension(String definition)  {
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(definition);
        Extension origin = aai20Document.info.getExtension("x-origin");
        if (origin != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map originMap = objectMapper.convertValue(origin.value, Map.class);
            if (originMap.containsKey("vendor")) {
                if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(originMap.get("vendor").toString())) {
                    return APIConstants.SOLACE_ENVIRONMENT;
                }
            }
        }
        return null;
    }

    public static boolean isSolaceAPIFromAsyncAPIDefinition(String definitionJSON)  {
        return APIConstants.SOLACE_ENVIRONMENT.equals(new SolaceApiParser().getVendorFromExtension(definitionJSON));
    }
}
