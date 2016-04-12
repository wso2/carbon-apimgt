package org.wso2.carbon.apimgt.hostobjects.util;


import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import org.wso2.carbon.apimgt.hostobjects.util.*;
/*This is a temporary solution to convert swagger object to json object. This class will be deleted
 once a fixed swagger parser version is released */
public class DeserializationModule extends SimpleModule {

    public DeserializationModule(boolean includePathDeserializer,
            boolean includeResponseDeserializer) {

        if (includePathDeserializer) {
            this.addDeserializer(Path.class, new PathDeserializer());
        }
        if (includeResponseDeserializer) {
            this.addDeserializer(Response.class, new ResponseDeserializer());
        }

        this.addDeserializer(Property.class, new PropertyDeserializer());
        this.addDeserializer(Model.class, new ModelDeserializer());
        this.addDeserializer(Parameter.class, new ParameterDeserializer());
        this.addDeserializer(SecuritySchemeDefinition.class, new SecurityDefinitionDeserializer());
    }

    public DeserializationModule() {
        this(true, true);
    }
}