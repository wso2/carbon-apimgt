package org.wso2.carbon.apimgt.solace.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import io.apicurio.datamodels.core.models.Extension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;

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

    private static final Log log = LogFactory.getLog(SolaceApiParser.class);

    /**
     * This method will extract the vendor provider or the API specification form the extensions list
     *
     * @param definition String
     * @return String
     */
    @Override
    public  String getVendorFromExtension(String definition)  {
        if (log.isDebugEnabled()) {
            log.debug("Extracting vendor information from API definition extension");
        }
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(definition);
        Extension origin = aai20Document.info.getExtension("x-origin");
        if (origin != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map originMap = objectMapper.convertValue(origin.value, Map.class);
            if (originMap.containsKey("vendor")) {
                if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(originMap.get("vendor").toString())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Identified Solace vendor from API definition extension");
                    }
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
