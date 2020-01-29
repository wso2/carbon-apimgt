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
package org.wso2.carbon.apimgt.impl.wsdl;

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
import org.wso2.carbon.apimgt.impl.wsdl.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLOperation;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLParamDefinition;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.wsdl.util.SwaggerFieldsExcludeStrategy;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap12.SOAP12Operation;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants.COMPLEX_TYPE_NODE_NAME;
import static org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME;
import static org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants.TARGET_NAMESPACE_ATTRIBUTE;

/**
 * Class that reads wsdl soap operations and maps with the types.
 */
public class WSDL11SOAPOperationExtractor extends WSDL11ProcessorImpl {
    private static final Logger log = LoggerFactory.getLogger(WSDL11SOAPOperationExtractor.class);

    private String[] primitiveTypes = { "string", "byte", "short", "int", "long", "float", "double", "boolean" };
    private List primitiveTypeList = Arrays.asList(primitiveTypes);
    private boolean canProcess = false;

    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";

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
    private Property currentProperty;

    protected Map<String, Definition> pathToDefinitionMap;

    public WSDL11SOAPOperationExtractor() {
    }

    public WSDL11SOAPOperationExtractor(APIMWSDLReader wsdlReader) {
    }

    @Override
    public boolean init(URL url) throws APIMgtWSDLException {
        super.init(url);
        return initModels();
    }

    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        super.init(wsdlContent);
        return initModels();
    }

    @Override
    public boolean initPath(String pathToExtractedZip) throws APIMgtWSDLException {
        super.initPath(pathToExtractedZip);
        return initModels();
    }

    /**
     * Initiallize SOAP to REST Operations
     *
     * @return true if extracting operations was successful
     */
    private boolean initModels() {
        wsdlDefinition = getWSDLDefinition();
        boolean canProcess = true;
        targetNamespace = wsdlDefinition.getTargetNamespace();
        Types types = wsdlDefinition.getTypes();
        if (types != null) {
            typeList = types.getExtensibilityElements();
        }
        if (typeList != null) {
            for (Object ext : typeList) {
                if (ext instanceof Schema) {
                    Schema schema = (Schema) ext;
                    Map importedSchemas = schema.getImports();
                    Element schemaElement = schema.getElement();
                    NodeList schemaNodes = schemaElement.getChildNodes();
                    schemaNodeList.addAll(SOAPOperationBindingUtils.list(schemaNodes));
                    //gets types from imported schemas from the parent wsdl. Nested schemas will not be imported.
                    if (importedSchemas != null) {
                        for (Object importedSchemaObj : importedSchemas.keySet()) {
                            String schemaUrl = (String) importedSchemaObj;
                            if (importedSchemas.get(schemaUrl) != null) {
                                Vector vector = (Vector) importedSchemas.get(schemaUrl);
                                for (Object schemaVector : vector) {
                                    if (schemaVector instanceof SchemaImport) {
                                        Schema referencedSchema = ((SchemaImport) schemaVector)
                                                .getReferencedSchema();
                                        if (referencedSchema != null && referencedSchema.getElement() != null) {
                                            if (referencedSchema.getElement().hasChildNodes()) {
                                                schemaNodeList.addAll(SOAPOperationBindingUtils
                                                        .list(referencedSchema.getElement().getChildNodes()));
                                            } else {
                                                log.warn("The referenced schema : " + schemaUrl
                                                        + " doesn't have any defined types");
                                            }
                                        } else {
                                            boolean isInlineSchema = false;
                                            for (Object aSchema : typeList) {
                                                if (schemaUrl.equalsIgnoreCase(
                                                        ((Schema) aSchema).getElement()
                                                                .getAttribute(TARGET_NAMESPACE_ATTRIBUTE))) {
                                                    isInlineSchema = true;
                                                    break;
                                                }
                                            }
                                            if (isInlineSchema) {
                                                log.debug(schemaUrl + " is already defined inline. Hence continue.");
                                            } else {
                                                log.warn("Cannot access referenced schema for the schema defined at: " + schemaUrl);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("No any imported schemas found in the given wsdl.");
                    }

                    if (log.isDebugEnabled()) {
                        Gson gson = new GsonBuilder().setExclusionStrategies(new SwaggerFieldsExcludeStrategy())
                                .create();
                        log.debug("swagger definition model map from the wsdl: " + gson.toJson(parameterModelMap));
                    }
                    if (schemaNodeList == null) {
                        log.warn("No schemas found in the type element for target namespace:" + schema
                                .getDocumentBaseURI());
                    }
                }
            }
            if (schemaNodeList != null) {
                for (Node node : schemaNodeList) {
                    WSDLParamDefinition wsdlParamDefinition = new WSDLParamDefinition();
                    ModelImpl model = new ModelImpl();
                    Property currentProperty = null;
                    traverseTypeElement(node, null, model, currentProperty);
                    if (StringUtils.isNotBlank(model.getName())) {
                        parameterModelMap.put(model.getName(), model);
                    }
                    if (wsdlParamDefinition.getDefinitionName() != null) {
                        wsdlParamDefinitions.add(wsdlParamDefinition);
                    }
                }
            } else {
                log.info("No schema is defined in the wsdl document");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                    + " with a single WSDL.");
        }
        return canProcess;
    }

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
            wsdlInfo.setHasSoap12BindingOperations(hasSoap12BindingOperations());
            if (parameterModelMap.size() > 0) {
                wsdlInfo.setParameterModelMap(parameterModelMap);
            }
        } else {
            throw new APIMgtWSDLException("WSDL Definition is not initialized.");
        }
        return wsdlInfo;
    }

    private void traverseTypeElement(Node element, Node prevNode, ModelImpl model, Property currentProp) {

        if (log.isDebugEnabled()) {
            if (element.hasAttributes()
                    && element.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                log.debug(element.getNodeName() + " with name attr:" + element.getAttributes()
                        .getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) + " and " + prevNode);
            } else {
                log.debug(element.getNodeName() + " and " + prevNode);
            }
        }
        if (prevNode != null) {
            currentProperty = generateSwaggerModelForComplexType(element, model, currentProp, true);
            setNamespaceDetails(model, element);
        } else {
            currentProperty = generateSwaggerModelForComplexType(element, model, currentProp, false);
            setNamespaceDetails(model, element);
        }
        NodeList nodeList = element.getChildNodes();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    traverseTypeElement(currentNode, prevNode, model, currentProperty);
                }
                prevNode = element;
            }
        }
    }

    /**
     * Generates swagger property for a given wsdl document node
     *
     * @param current     current type element node
     * @param model       swagger model element
     * @param currentProp current wsdl type element
     * @return swagger property for the wsdl element
     */
    private Property generateSwaggerModelForComplexType(Node current, ModelImpl model, Property currentProp,
                                                        boolean prevNodeExist) {
        if (WSDL_ELEMENT_NODE.equals(current.getLocalName())) {
            if (StringUtils.isNotBlank(getNodeName(current))) {
                addModelDefinition(current, model, SOAPToRESTConstants.EMPTY_STRING, prevNodeExist);
            } else if (StringUtils.isNotBlank(getRefNodeName(current))) {
                if (current.getParentNode() != null) {
                    addModelDefinition(current, model, SOAPToRESTConstants.EMPTY_STRING, prevNodeExist);
                }
            }
        } else if (COMPLEX_TYPE_NODE_NAME.equals(current.getLocalName())) {
            if (StringUtils.isNotBlank(getNodeName(current))) {
                if (current.getParentNode() != null) {
                    addModelDefinition(current, model, SOAPToRESTConstants.EMPTY_STRING, prevNodeExist);
                }
            }
        } else if (SIMPLE_TYPE_NODE_NAME.equals(current.getLocalName())) {
            if (StringUtils.isNotBlank(getNodeName(current))) {
                if (current.getParentNode() != null) {
                    addModelDefinition(current, model, SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME, prevNodeExist);
                }
            }
        } else if (SOAPToRESTConstants.RESTRICTION_ATTR.equals(current.getLocalName())) {
            if (current.getParentNode() != null) {
                addModelDefinition(current, model, SOAPToRESTConstants.RESTRICTION_ATTR, prevNodeExist);
            }
        }
        return currentProp;
    }

    /**
     * Adds swagger type definitions to swagger model
     *
     * @param current current wsdl node
     * @param model   swagger model element
     * @param type    wsdl node type{i.e: complexType, simpleType}
     */
    private void addModelDefinition(Node current, ModelImpl model, String type, boolean prevNodeExist) {
        if (current.getParentNode() != null) {
            String xPath = getXpathFromNode(current);
            if (log.isDebugEnabled()) {
                log.debug("Processing current document node: " + getNodeName(current) + " with the xPath:" + xPath);
            }
            String[] elements = xPath.split("\\.");
            if (getNodeName(current).equals(elements[elements.length - 1]) || getNodeName(current)
                    .equals(elements[elements.length - 1].substring(elements[elements.length - 1].indexOf(":") + 1))
                    || type.equals(SOAPToRESTConstants.RESTRICTION_ATTR)) {
                if (StringUtils.isBlank(model.getName())) {
                    model.setName(getNodeName(current));
                    if (!SOAPToRESTConstants.SIMPLE_TYPE_NODE_NAME.equals(type)) {
                        if (!prevNodeExist) {
                            Property prop = createPropertyFromNode(current, false);
                            Map<String, Property> propertyMap = new HashMap<>();
                            propertyMap.put(getNodeName(current), prop);
                            model.setProperties(propertyMap);
                        }
                        if (isArrayType(current)) {
                            model.setType(ArrayProperty.TYPE);
                        } else {
                            model.setType(ObjectProperty.TYPE);
                        }
                        String elementFormDefault = null;
                        if (current.getParentNode().getAttributes() != null && current.getParentNode().getAttributes()
                                .getNamedItem(SOAPToRESTConstants.ELEMENT_FORM_DEFAULT) != null) {
                            elementFormDefault = current.getParentNode().getAttributes()
                                    .getNamedItem(SOAPToRESTConstants.ELEMENT_FORM_DEFAULT).getNodeValue();
                        }
                        if (StringUtils.isNotEmpty(elementFormDefault) &&
                                SOAPToRESTConstants.QUALIFIED.equals(elementFormDefault)) {
                            model.setVendorExtension(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED, true);
                        } else {
                            model.setVendorExtension(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED, false);
                        }
                    }
                } else if (model.getProperties() == null) {
                    if (SOAPToRESTConstants.RESTRICTION_ATTR.equals(type)) {
                        Property restrictionProp = createPropertyFromNode(current, true);
                        if (!(restrictionProp instanceof RefProperty || restrictionProp instanceof ObjectProperty
                                || restrictionProp instanceof ArrayProperty)) {
                            model.setType(restrictionProp.getType());
                        } else {
                            model.setType(StringProperty.TYPE);
                        }
                    } else {
                        Map<String, Property> propertyMap = new HashMap<>();
                        Property prop = createPropertyFromNode(current, true);
                        propertyMap.put(getNodeName(current), prop);
                        model.setProperties(propertyMap);
                    }
                } else {
                    Property parentProp = null;
                    int pos = 0;
                    for (String element : elements) {
                        if (model.getName().equals(element)) {
                            //do nothing
                        } else if (model.getProperties().containsKey(element)) {
                            parentProp = model.getProperties().get(element);
                            if (SOAPToRESTConstants.RESTRICTION_ATTR.equals(type) && pos == elements.length - 1) {
                                model.getProperties().remove(element);
                                parentProp = createPropertyFromNode(current, true);
                                parentProp.setName(element);
                                model.addProperty(element, parentProp);
                            }
                        } else {
                            if (parentProp instanceof ArrayProperty) {
                                if (((ArrayProperty) parentProp).getItems().getName() == null) {
                                    Property currentProp = createPropertyFromNode(current, true);
                                    if (currentProp instanceof ObjectProperty) {
                                        ((ArrayProperty) parentProp).setItems(currentProp);
                                    } else if (currentProp instanceof ArrayProperty) {
                                        Map<String, Property> arrayPropMap = new HashMap();
                                        arrayPropMap.put(getNodeName(current), currentProp);
                                        ObjectProperty arrayObjProp = new ObjectProperty();
                                        arrayObjProp.setProperties(arrayPropMap);
                                        ((ArrayProperty) parentProp).setItems(arrayObjProp);
                                    } else {
                                        ((ArrayProperty) parentProp).setItems(currentProp);
                                    }
                                } else {
                                    ((ArrayProperty) parentProp).setItems(createPropertyFromNode(current, true));
                                }
                                parentProp = ((ArrayProperty) parentProp).getItems();
                            } else if (parentProp instanceof ObjectProperty) {
                                if (SOAPToRESTConstants.RESTRICTION_ATTR.equals(type) && pos == elements.length - 1) {
                                    parentProp = createPropertyFromNode(current, true);
                                    parentProp.setName(element);
                                } else {
                                    if (((ObjectProperty) parentProp).getProperties() == null) {
                                        Map<String, Property> propertyMap = new HashMap<>();
                                        ((ObjectProperty) parentProp).setProperties(propertyMap);
                                    }
                                    Property childProp = createPropertyFromNode(current, true);
                                    if (((ObjectProperty) parentProp).getProperties().get(element) == null) {
                                        ((ObjectProperty) parentProp).getProperties()
                                                .put(getNodeName(current), childProp);
                                    }
                                    if (childProp instanceof ObjectProperty) {
                                        parentProp = ((ObjectProperty) parentProp).getProperties().get(element);
                                    } else if (childProp instanceof ArrayProperty) {
                                        parentProp = childProp;
                                    }

                                }
                            } else if (parentProp == null) {
                                if (StringUtils.isNotBlank(getNodeName(current))) {
                                    model.addProperty(getNodeName(current), createPropertyFromNode(current, true));
                                } else if (StringUtils.isNotBlank(getRefNodeName(current))) {
                                    model.addProperty(getRefNodeName(current), createPropertyFromNode(current, true));
                                }
                            }
                        }
                        pos++;
                    }

                }
            }
        }
    }

    private String getXPath(Node node) {

        if (node != null) {
            Node parent = node.getParentNode();
            if (parent == null && node.hasAttributes()
                    && node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                return "/" + node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
            }
            if (node.hasAttributes() && node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                return getXPath(parent) + "/" + node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE)
                        .getNodeValue();
            } else if (node.hasAttributes()
                    && node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
                return getXPath(parent) + "/" + node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE)
                        .getNodeValue();
            }
            return getXPath(parent) + "/";
        }
        return SOAPToRESTConstants.EMPTY_STRING;
    }

    private String getXpathFromNode(Node node) {

        if (node.getParentNode() != null) {
            String xPath = getXPath(node);
            xPath = xPath.replaceAll("/+", ".");
            if (xPath.startsWith(".")) {
                xPath = xPath.substring(1);
                return xPath;
            }
        }
        return null;
    }

    private String getNodeName(Node node) {

        if (node.hasAttributes() && node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
            return node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
        }
        if (node.hasAttributes() && node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
            return node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue().contains(":") ?
                    node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue().split(":")[1] :
                    node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue();
        }
        return SOAPToRESTConstants.EMPTY_STRING;
    }

    private String getRefNodeName(Node node) {
        if (node.hasAttributes() && node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
            return node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue().contains(":") ?
                    node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue().split(":")[1] :
                    node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue();
        }
        return SOAPToRESTConstants.EMPTY_STRING;
    }

    /**
     * Creates a swagger property from given wsdl node.
     *
     * @param node wsdl node
     * @return generated swagger property
     */
    private Property createPropertyFromNode(Node node, boolean prevNodeExist) {

        Property property = null;
        if (node.hasAttributes()) {
            if (node.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE) != null) {
                if (node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                    String dataType = node.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                            .getNodeValue().contains(":") ?
                            node.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE).getNodeValue()
                                    .split(":")[1] :
                            node.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE).getNodeValue();
                    property = getPropertyFromDataType(dataType);
                    if (property instanceof RefProperty) {
                        ((RefProperty) property).set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + dataType);
                    }
                    property.setName(
                            node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue());
                }
            } else if (node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE) != null) {
                property = new RefProperty();
                String dataType = node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue()
                        .contains(":") ?
                        node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue()
                                .split(":")[1] :
                        node.getAttributes().getNamedItem(SOAPToRESTConstants.REF_ATTRIBUTE).getNodeValue();
                ((RefProperty) property).set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + dataType);
                property.setName(dataType);
            } else if (node.getAttributes().getNamedItem("base") != null) {
                String dataType = node.getAttributes().getNamedItem("base").getNodeValue().contains(":") ?
                        node.getAttributes().getNamedItem("base").getNodeValue().split(":")[1] :
                        node.getAttributes().getNamedItem("base").getNodeValue();
                property = getPropertyFromDataType(dataType);
                if (property instanceof RefProperty) {
                    ((RefProperty) property).set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + dataType);
                }
                property.setName(dataType);
            } else if (node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null && prevNodeExist) {
                property = new ObjectProperty();
                property.setName(getNodeName(node));
            }
            if (isArrayType(node)) {
                Property arrayProperty = new ArrayProperty();
                ((ArrayProperty) arrayProperty).setItems(property);
                return arrayProperty;
            }
        }
        return property;
    }

    private Property getPropertyFromDataType(String dataType) {

        switch (dataType) {
        case "string":
            return new StringProperty();
        case "boolean":
            return new BooleanProperty();
        case "int":
            return new IntegerProperty();
        case "nonNegativeInteger":
            return new IntegerProperty();
        case "integer":
            return new IntegerProperty();
        case "positiveInteger":
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
            List extensibilityElements = binding.getExtensibilityElements();
            for (Object extensibilityElement : extensibilityElements) {
                if (extensibilityElement instanceof SOAPBinding || extensibilityElement instanceof SOAP12Binding) {
                    for (Object opObj : binding.getBindingOperations()) {
                        BindingOperation bindingOperation = (BindingOperation) opObj;
                        WSDLSOAPOperation wsdlSoapOperation = getSOAPOperation(bindingOperation);
                        if (wsdlSoapOperation != null) {
                            allBindingOperations.add(wsdlSoapOperation);
                        } else {
                            log.warn("Unable to get soap operation details from binding operation: " + bindingOperation
                                    .getName());
                        }
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
                wsdlOperation.setTargetNamespace(getTargetNamespace(bindingOperation));
                wsdlOperation.setStyle(soapOperation.getStyle());
                wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
                wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
            } else if (boExtElement instanceof SOAP12Operation) {
                SOAP12Operation soapOperation = (SOAP12Operation) boExtElement;
                wsdlOperation = new WSDLSOAPOperation();
                wsdlOperation.setName(bindingOperation.getName());
                wsdlOperation.setSoapAction(soapOperation.getSoapActionURI());
                wsdlOperation.setTargetNamespace(getTargetNamespace(bindingOperation));
                wsdlOperation.setStyle(soapOperation.getStyle());
                wsdlOperation.setInputParameterModel(getSoapInputParameterModel(bindingOperation));
                wsdlOperation.setOutputParameterModel(getSoapOutputParameterModel(bindingOperation));
            }
        }
        return wsdlOperation;
    }

    /**
     * Gets the target namespace given the soap binding operation
     *
     * @param bindingOperation soap operation
     * @return target name space
     */
    private String getTargetNamespace(BindingOperation bindingOperation) {

        Operation operation = bindingOperation.getOperation();
        if (operation != null) {
            Input input = operation.getInput();

            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map partMap = message.getParts();

                    for (Object obj : partMap.entrySet()) {
                        Map.Entry entry = (Map.Entry) obj;
                        Part part = (Part) entry.getValue();
                        if (part != null) {
                            if (part.getElementName() != null) {
                                return part.getElementName().getNamespaceURI();
                            }
                        }
                    }
                }
            }
        }
        return targetNamespace;
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
                        if (part != null) {
                            if (part.getElementName() != null) {
                                inputParameterModelList.add(parameterModelMap.get(part.getElementName()
                                        .getLocalPart()));
                            } else {
                                if (part.getTypeName() != null && parameterModelMap
                                        .containsKey(part.getTypeName().getLocalPart())) {
                                    inputParameterModelList
                                            .add(parameterModelMap.get(part.getTypeName().getLocalPart()));
                                } else {
                                    ModelImpl model = new ModelImpl();
                                    model.setType(ObjectProperty.TYPE);
                                    model.setName(message.getQName().getLocalPart());
                                    if (getPropertyFromDataType(part.getTypeName().getLocalPart()) instanceof RefProperty) {
                                        RefProperty property = (RefProperty) getPropertyFromDataType(part.getTypeName()
                                                .getLocalPart());
                                        property.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT
                                                + part.getTypeName().getLocalPart());
                                        model.addProperty(part.getName(), property);
                                    } else {
                                        model.addProperty(part.getName(),
                                                getPropertyFromDataType(part.getTypeName().getLocalPart()));
                                    }
                                    parameterModelMap.put(model.getName(), model);
                                    inputParameterModelList.add(model);
                                }
                            }
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
                        if (part != null) {
                            if (part.getElementName() != null) {
                                outputParameterModelList.add(parameterModelMap.get(part.getElementName()
                                        .getLocalPart()));
                            } else {
                                if (part.getTypeName() != null && parameterModelMap
                                        .containsKey(part.getTypeName().getLocalPart())) {
                                    outputParameterModelList
                                            .add(parameterModelMap.get(part.getTypeName().getLocalPart()));
                                } else {
                                    ModelImpl model = new ModelImpl();
                                    model.setType(ObjectProperty.TYPE);
                                    model.setName(message.getQName().getLocalPart());
                                    if (getPropertyFromDataType(part.getTypeName().getLocalPart()) instanceof RefProperty) {
                                        RefProperty property = (RefProperty) getPropertyFromDataType(part.getTypeName()
                                                .getLocalPart());
                                        property.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT +
                                                part.getTypeName().getLocalPart());
                                        model.addProperty(part.getName(), property);
                                    } else {
                                        model.addProperty(part.getName(),
                                                getPropertyFromDataType(part.getTypeName().getLocalPart()));
                                    }
                                    parameterModelMap.put(model.getName(), model);
                                    outputParameterModelList.add(model);
                                }
                            }
                        }
                    }
                }
            }
        }
        return outputParameterModelList;
    }

    /**
     * Returns if the provided WSDL definition contains SOAP binding operations
     *
     * @return whether the provided WSDL definition contains SOAP binding operations
     */
    private boolean hasSoapBindingOperations() {
        if (wsdlDefinition == null) {
            return false;
        }
        for (Object bindingObj : wsdlDefinition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                for (Object ex : binding.getExtensibilityElements()) {
                    if (ex instanceof SOAPBinding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns if the provided WSDL definition contains SOAP 1.2 binding operations
     *
     * @return whether the provided WSDL definition contains SOAP 1.2 binding operations
     */
    private boolean hasSoap12BindingOperations() {
        if (wsdlDefinition == null) {
            return false;
        }
        for (Object bindingObj : wsdlDefinition.getAllBindings().values()) {
            if (bindingObj instanceof Binding) {
                Binding binding = (Binding) bindingObj;
                for (Object ex : binding.getExtensibilityElements()) {
                    if (ex instanceof SOAP12Binding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Map<String, ModelImpl> getParameterModelMap() {
        return parameterModelMap;
    }
}
