/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.soaptorest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.soaptorest.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLOperation;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLParamDefinition;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SwaggerFieldsExcludeStrategy;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that reads wsdl soap operations and maps with the types.
 */
public class WSDL11SOAPOperationExtractor implements WSDLSOAPOperationExtractor {
    private static final Logger log = LoggerFactory.getLogger(WSDL11SOAPOperationExtractor.class);

    private String[] primitiveTypes = { "string", "byte", "short", "int", "long", "float", "double", "boolean" };
    private List primitiveTypeList = Arrays.asList(primitiveTypes);

    private final String WSDL_ELEMENT_NODE = "element";
    private static final String WSDL_VERSION_11 = "1.1";

    private Definition wsdlDefinition;
    private String targetNamespace;

    private List typeList = null;
    private List<Node> complexElemList = new ArrayList<>();
    private List<Node> simpleElemList = new ArrayList<>();
    private List<Node> schemaNodeList = new ArrayList<>();
    private List<WSDLParamDefinition> wsdlParamDefinitions = new ArrayList<>();
    private Map<String, ModelImpl> parameterModelMap = new HashMap<>();

    private static volatile APIMWSDLReader wsdlReader;

    public WSDL11SOAPOperationExtractor(APIMWSDLReader wsdlReader) {
        WSDL11SOAPOperationExtractor.wsdlReader = wsdlReader;
    }

    @Override public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        boolean canProcess;
        try {
            wsdlDefinition = wsdlReader.getWSDLDefinitionFromByteContent(wsdlContent, true);
            canProcess = true;
            targetNamespace = wsdlDefinition.getTargetNamespace();
            Types types = wsdlDefinition.getTypes();
            if (types != null) {
                typeList = types.getExtensibilityElements();
            }
            if (typeList != null) {
                for (Object ext : typeList) {
                    if (ext instanceof Schema) {
                        Schema schema = (Schema) ext;
                        Element schemaElement = schema.getElement();
                        NodeList schemaNodes = schemaElement.getChildNodes();
                        schemaNodeList.addAll(SOAPOperationBindingUtils.list(schemaNodes));
                        if (schemaNodeList != null) {
                            for (Node node : schemaNodeList) {
                                WSDLParamDefinition wsdlParamDefinition = new WSDLParamDefinition();
                                ModelImpl model = new ModelImpl();
                                traverseTypeElement(node, null, wsdlParamDefinition, model, null);
                                if (StringUtils.isNotBlank(model.getName())) {
                                    parameterModelMap.put(model.getName(), model);
                                }
                                if (wsdlParamDefinition.getDefinitionName() != null) {
                                    wsdlParamDefinitions.add(wsdlParamDefinition);
                                }
                            }
                        } else {
                            log.warn("No schemas found in the type element for target namespace:" + schema
                                    .getDocumentBaseURI());
                        }
                        if (log.isDebugEnabled()) {
                            Gson gson = new GsonBuilder().setExclusionStrategies(new SwaggerFieldsExcludeStrategy())
                                    .create();
                            log.debug("swagger definition model map from the wsdl: " + gson.toJson(parameterModelMap));
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                        + " with a single WSDL.");
            }
        } catch (APIManagementException e) {
            log.error("Cannot process the WSDL by " + this.getClass().getName(), e);
            canProcess = false;
        }
        return canProcess;
    }

    private void traverseTypeElement(Node element, Node prevNode, WSDLParamDefinition wsdlParamDefinition,
            ModelImpl model, String propertyName) {

        if (log.isDebugEnabled()) {
            if (element.hasAttributes()
                    && element.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                log.debug(element.getNodeName() + " with name attr:" + element.getAttributes()
                        .getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) + " and " + prevNode);
            } else {
                log.debug(element.getNodeName() + " and " + prevNode);
            }
        }
        String currentProperty = generateSwaggerModelForComplexType(element, prevNode, model, propertyName);
        NodeList nodeList = element.getChildNodes();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    traverseTypeElement(currentNode, prevNode, wsdlParamDefinition, model, currentProperty);
                }
                prevNode = element;
            }
        }
    }
    /**
     * Generates swagger model for a given complex type
     *
     * @param current      current type element node
     * @param previous     previous type element node
     * @param model        swagger model element
     * @param propertyName definition property name
     * @return swagger string for the model
     */
    private String generateSwaggerModelForComplexType(Node current, Node previous, ModelImpl model,
            String propertyName) {

        if (WSDL_ELEMENT_NODE.equals(current.getLocalName()) || SOAPToRESTConstants.COMPLEX_TYPE_NODE_NAME
                .equals(current.getLocalName()) || SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME
                .equals(current.getLocalName())) {
            if (current.hasAttributes()
                    && current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                //first type node
                if (previous == null) {
                    setNamespaceDetails(model, current);
                    if (current.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE) != null
                            && SOAPToRESTConstants.UNBOUNDED
                            .equals(current.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE)
                                    .getNodeValue())) {
                        model.setType(ArrayProperty.TYPE);
                    } else {
                        model.setType(ObjectProperty.TYPE);
                    }
                    model.setName(
                            current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue());
                    return current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
                } else if (SOAPToRESTConstants.COMPLEX_TYPE_NODE_NAME.equals(current.getLocalName())) {
                    if (current.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE) != null
                            && SOAPToRESTConstants.UNBOUNDED
                            .equals(current.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE)
                                    .getNodeValue())) {
                        ArrayProperty prop = new ArrayProperty();
                        setNamespaceDetails(prop, current);
                        model.addProperty(
                                current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue(),
                                prop);
                    } else {
                        ObjectProperty prop = new ObjectProperty();
                        setNamespaceDetails(prop, current);
                        model.addProperty(
                                current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue(),
                                prop);
                    }
                    return current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
                } else if (WSDL_ELEMENT_NODE.equals(current.getLocalName())) {
                    if (StringUtils.isNotBlank(propertyName) && model.getProperties() != null && model.getProperties()
                            .containsKey(propertyName)) {
                        Property parentProperty = model.getProperties().get(propertyName);
                        if (log.isDebugEnabled()) {
                            log.debug("Property: " + propertyName + " in the model: " + model.getName() + "is logged.");
                        }
                        if (current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE) != null) {
                            String dataType = current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                    .getNodeValue().contains(":") ?
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                            .getNodeValue().split(":")[1] :
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                            .getNodeValue();
                            Property property = getPropertyFromDataType(dataType);
                            String childPropertyName =
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null ?
                                            current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE)
                                                    .getNodeValue() :
                                            SOAPToRESTConstants.EMPTY_STRING;
                            if (parentProperty instanceof ArrayProperty && !(property instanceof RefProperty)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Property: " + propertyName + " is array property and previous node: "
                                            + previous + " in the model" + model.getName() + "is logged.");
                                }
                                if (previous.hasChildNodes() && previous.getChildNodes().getLength() > 1) {
                                    if (((ArrayProperty) parentProperty).getItems() != null && StringUtils
                                            .isNotBlank(childPropertyName)) {
                                        ((ObjectProperty) ((ArrayProperty) parentProperty).getItems()).getProperties()
                                                .put(childPropertyName, property);
                                    } else if (((ArrayProperty) parentProperty).getItems() == null) {
                                        ObjectProperty objProperty = new ObjectProperty();
                                        Map<String, Property> localPropertyMap = new HashMap<>();
                                        localPropertyMap.put(childPropertyName, property);
                                        objProperty.setProperties(localPropertyMap);
                                        ((ArrayProperty) parentProperty).setItems(objProperty);
                                    }
                                } else if (previous.hasChildNodes()) {
                                    ((ArrayProperty) parentProperty).setItems(property);
                                }
                            } else if (parentProperty instanceof ObjectProperty && !(property instanceof RefProperty)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Property: " + propertyName + " is object property and previous node: "
                                            + previous + " in the model" + model.getName() + "is logged.");
                                }
                                if (previous.hasChildNodes() && previous.getChildNodes().getLength() > 1) {
                                    if (((ObjectProperty) parentProperty).getProperties() != null && StringUtils
                                            .isNotBlank(childPropertyName)) {
                                        ((ObjectProperty) parentProperty).getProperties()
                                                .put(childPropertyName, property);
                                    } else if (((ObjectProperty) parentProperty).getProperties() == null) {
                                        Map<String, Property> localPropertyMap = new HashMap<>();
                                        localPropertyMap.put(childPropertyName, property);
                                        ((ObjectProperty) parentProperty).setProperties(localPropertyMap);
                                    }
                                } else if (previous.hasChildNodes()) {
                                    Map<String, Property> localPropertyMap = new HashMap<>();
                                    localPropertyMap.put(childPropertyName, property);
                                    ((ObjectProperty) parentProperty).setProperties(localPropertyMap);
                                }
                            }
                        }
                    } else if (StringUtils.isNotBlank(propertyName) && propertyName.equals(model.getName())) {
                        if (current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE) != null) {
                            String dataType = current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                    .getNodeValue().contains(":") ?
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                            .getNodeValue().split(":")[1] :
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                            .getNodeValue();
                            addPropertyToSwaggerModel(dataType,
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE)
                                            .getNodeValue(), current, model);
                        } else if (current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
                            String dataType = current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE)
                                    .getNodeValue().contains(":") ?
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE)
                                            .getNodeValue().split(":")[1] :
                                    current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE)
                                            .getNodeValue();
                            addPropertyToSwaggerModel(dataType, dataType, current, model);
                        } else if (current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                            if (!(current.hasChildNodes() && SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME
                                    .equals(current.getFirstChild().getLocalName()))) {
                                if (isArrayType(current)) {
                                    ArrayProperty prop = new ArrayProperty();
                                    model.addProperty(
                                            current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE)
                                                    .getNodeValue(), prop);
                                } else {
                                    ObjectProperty prop = new ObjectProperty();
                                    model.addProperty(
                                            current.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE)
                                                    .getNodeValue(), prop);
                                }
                            }
                        }
                    }
                } else if (SOAPToRESTConstants.RESTRICTION_ATTR.equals(current.getLocalName())) {
                    if (current.hasAttributes()
                            && current.getAttributes().getNamedItem(SOAPToRESTConstants.BASE_ATTR) != null) {
                        String dataType = current.getAttributes().getNamedItem(SOAPToRESTConstants.BASE_ATTR)
                                .getNodeValue().contains(":") ?
                                current.getAttributes().getNamedItem(SOAPToRESTConstants.BASE_ATTR).getNodeValue()
                                        .split(":")[1] :
                                current.getAttributes().getNamedItem(SOAPToRESTConstants.BASE_ATTR).getNodeValue();
                        Property property = getPropertyFromDataType(dataType);
                        model.addProperty(propertyName, property);
                    }
                }
            } else if (SOAPToRESTConstants.COMPLEX_TYPE_NODE_NAME.equals(current.getLocalName())) {
                if (previous != null && previous.hasAttributes()
                        && previous.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                    return previous.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
                }
            } else if (SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME.equals(current.getLocalName())) {
                if (previous != null && previous.hasAttributes()
                        && previous.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                    return previous.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
                }
            } else if (current.hasAttributes()
                    && current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
                String dataType = current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue()
                        .contains(":") ?
                        current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue()
                                .split(":")[1] :
                        current.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue();
                addPropertyToSwaggerModel(dataType, dataType, current, model);
            }
        }
        return propertyName;
    }

    private void addPropertyToSwaggerModel(String dataType, String propName, Node current, ModelImpl model) {

        Property property = getPropertyFromDataType(dataType);
        if (isArrayType(current)) {
            ArrayProperty prop = new ArrayProperty();
            if (property instanceof RefProperty) {
                RefProperty refProperty = new RefProperty();
                refProperty.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + dataType);
                refProperty.getRefFormat();
                prop.setItems(refProperty);
            } else {
                prop.setItems(property);
            }
            setNamespaceDetails(prop, current);
            model.addProperty(
                    propName, prop);
        } else {
            if (property instanceof RefProperty) {
                RefProperty refProperty = new RefProperty();
                refProperty.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT  + dataType);
                model.addProperty(propName, refProperty);
            } else {
                model.addProperty(propName, property);
            }
        }
    }

    private Property getPropertyFromDataType(String dataType) {

        switch (dataType) {
        case "string":
            return new StringProperty();
        case "boolean":
            return new BooleanProperty();
        case "int":
            return new IntegerProperty();
        case "double":
            return new DoubleProperty();
        case "float":
            return new FloatProperty();
        case "long":
            return new LongProperty();
        case "date":
            return new DateProperty();
        default:
            return new RefProperty();
        }
    }

    private boolean isArrayType(Node node) {

        return node.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE) != null
                && SOAPToRESTConstants.UNBOUNDED
                .equals(node.getAttributes().getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE).getNodeValue());
    }

    private void setNamespaceDetails(ModelImpl model, Node currentNode) {

        Xml xml = new Xml();
        xml.setNamespace(currentNode.getNamespaceURI());
        xml.setPrefix(currentNode.getPrefix());
        model.setXml(xml);
    }

    private void setNamespaceDetails(Property property, Node currentNode) {

        Xml xml = new Xml();
        xml.setNamespace(currentNode.getNamespaceURI());
        xml.setPrefix(currentNode.getPrefix());
        property.setXml(xml);
    }

    @Override
    public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
        WSDLInfo wsdlInfo = new WSDLInfo();
        if (wsdlDefinition != null) {
            Set<WSDLSOAPOperation> soapOperations = getSoapBindingOperations(wsdlDefinition);
            wsdlInfo.setVersion(WSDL_VERSION_11);

            if (!soapOperations.isEmpty()) {
                wsdlInfo.setHasSoapBindingOperations(true);
                wsdlInfo.setSoapBindingOperations(soapOperations);
            } else {
                wsdlInfo.setHasSoapBindingOperations(false);
            }
            wsdlInfo.setHasSoapBindingOperations(hasSoapBindingOperations());
            if (parameterModelMap.size() > 0) {
                wsdlInfo.setParameterModelMap(parameterModelMap);
            }
        } else {
            throw new APIMgtWSDLException("WSDL Definition is not initialized.");
        }
        return wsdlInfo;
    }

    /**
     * Retrieves all the operations defined in the provided WSDL definition.
     *
     * @param definition WSDL Definition
     * @return a set of {@link WSDLOperation} defined in the provided WSDL definition
     */
    private Set<WSDLSOAPOperation> getSoapBindingOperations(Definition definition) throws APIMgtWSDLException {
        Set<WSDLSOAPOperation> allOperations = new HashSet<>();
        for (Object bindingObj : definition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                Set<WSDLSOAPOperation> operations = getSOAPBindingOperations(binding);
                allOperations.addAll(operations);
            }
        }
        return allOperations;
    }

    /**
     * Retrieves all the operations defined in the provided Binding.
     *
     * @param binding WSDL binding
     * @return a set of {@link WSDLOperation} defined in the provided Binding
     */
    private Set<WSDLSOAPOperation> getSOAPBindingOperations(Binding binding) throws APIMgtWSDLException {
        Set<WSDLSOAPOperation> allBindingOperations = new HashSet<>();
        if (binding.getExtensibilityElements() != null && binding.getExtensibilityElements().size() > 0) {
            if (binding.getExtensibilityElements().get(0) instanceof SOAPBinding) {
                for (Object opObj : binding.getBindingOperations()) {
                    BindingOperation bindingOperation = (BindingOperation) opObj;
                    WSDLSOAPOperation wsdlSoapOperation = getSOAPOperation(bindingOperation);
                    if (wsdlSoapOperation != null) {
                        allBindingOperations.add(wsdlSoapOperation);
                    }
                }
            }
        } else {
            throw new APIMgtWSDLException("Cannot further process to get soap binding operations");
        }
        return allBindingOperations;
    }

    /**
     * Retrieves WSDL operation given the soap binding operation
     *
     * @param bindingOperation {@link BindingOperation} object
     * @return a set of {@link WSDLOperation} defined in the provided Binding
     */
    private WSDLSOAPOperation getSOAPOperation(BindingOperation bindingOperation) throws APIMgtWSDLException {
        WSDLSOAPOperation wsdlOperation = null;
        for (Object boExtElement : bindingOperation.getExtensibilityElements()) {
            if (boExtElement instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation) boExtElement;
                wsdlOperation = new WSDLSOAPOperation();
                wsdlOperation.setName(bindingOperation.getName());
                wsdlOperation.setSoapAction(soapOperation.getSoapActionURI());
                wsdlOperation.setTargetNamespace(targetNamespace);
                wsdlOperation.setStyle(soapOperation.getStyle());

                wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
                wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
            }
        }
        return wsdlOperation;
    }

    /**
     * Gets swagger input parameter model for a given soap operation
     *
     * @param bindingOperation soap operation
     * @return list of swagger models for the parameters
     * @throws APIMgtWSDLException
     */
    private List<ModelImpl> getSoapInputParameterModel(BindingOperation bindingOperation) throws APIMgtWSDLException {

        List<ModelImpl> inputParameterModelList = new ArrayList<>();
        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Input input = operation.getInput();

            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map map = message.getParts();

                    for (Object obj : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) obj;
                        Part part = (Part) entry.getValue();
                        if (part.getElementName() != null) {
                            inputParameterModelList.add(parameterModelMap.get(part.getElementName().getLocalPart()));
                        } else {
                            inputParameterModelList.add(parameterModelMap.get(part.getName()));
                        }
                    }
                }
            }
        }
        return inputParameterModelList;
    }

    /**
     * Gets swagger output parameter model for a given soap operation
     *
     * @param bindingOperation soap operation
     * @return list of swagger models for the parameters
     * @throws APIMgtWSDLException
     */
    private List<ModelImpl> getSoapOutputParameterModel(BindingOperation bindingOperation) throws APIMgtWSDLException {
        List<ModelImpl> outputParameterModelList = new ArrayList<>();
        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Output output = operation.getOutput();
            if (output != null) {
                Message message = output.getMessage();
                if (message != null) {
                    Map map = message.getParts();

                    for (Object obj : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) obj;
                        Part part = (Part) entry.getValue();
                        if (part.getElementName() != null) {
                            outputParameterModelList.add(parameterModelMap.get(part.getElementName().getLocalPart()));
                        } else {
                            outputParameterModelList.add(parameterModelMap.get(part.getName()));
                        }
                    }
                }
            }
        }
        return outputParameterModelList;
    }

    /**
     * Returns if any of the WSDLs (initialized) contains SOAP binding operations
     *
     * @return whether the WSDLs (initialized) contains SOAP binding operations
     */
    private boolean hasSoapBindingOperations() {
        return wsdlDefinition != null && hasSoapBindingOperations(wsdlDefinition);
    }

    /**
     * Returns if the provided WSDL definition contains SOAP binding operations
     *
     * @param definition WSDL definition
     * @return whether the provided WSDL definition contains SOAP binding operations
     */
    private boolean hasSoapBindingOperations(Definition definition) {
        for (Object bindingObj : definition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                for (Object ex : binding.getExtensibilityElements()) {
                    if (ex instanceof SOAPBinding || ex instanceof SOAP12Binding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
