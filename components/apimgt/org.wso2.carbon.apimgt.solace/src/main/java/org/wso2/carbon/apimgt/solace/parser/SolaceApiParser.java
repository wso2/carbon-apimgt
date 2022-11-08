package org.wso2.carbon.apimgt.solace.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import io.apicurio.datamodels.core.models.Extension;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;

import java.util.Map;

/**
 * This Parser class will validate the Solace Async API Specifications.
 */
@Component(
        name = "solace.async.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
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
                if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(originMap.get("vendor").toString())) {
                    return SolaceConstants.SOLACE_ENVIRONMENT;
                }
            }
        }
        return null;
    }

    @Override
    public String getType() {
        return SolaceConstants.SOLACE_ENVIRONMENT;
    }
}
