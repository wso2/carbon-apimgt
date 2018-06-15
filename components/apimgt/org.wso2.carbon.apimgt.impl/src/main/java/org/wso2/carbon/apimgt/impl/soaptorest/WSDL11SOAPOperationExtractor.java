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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.soaptorest.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLComplexType;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLOperation;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLOperationParam;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPToRESTConstants;
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
    private List<Node> elemList = new ArrayList<>();

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
            typeList = types.getExtensibilityElements();

            for (Object ext : typeList) {
                if (ext instanceof Schema) {
                    Schema schema = (Schema) ext;
                    Element schemaElement = schema.getElement();
                    String nodeName = schemaElement.getNodeName();
                    String nodeNS = nodeName.split(SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR)[0];
                    String complexTypeElement = nodeNS + SOAPToRESTConstants.COMPLEX_TYPE_NODE_NAME;
                    NodeList nodeList = schemaElement.getElementsByTagName(complexTypeElement);
                    elemList.addAll(SOAPOperationBindingUtils.list(nodeList));
                }
            }
            elemList = reformComplexTypes(elemList);
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

    @Override public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
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

                List<WSDLOperationParam> inputParameters = getSoapInputParameters(bindingOperation);
                wsdlOperation.setParameters(inputParameters);
                List<WSDLOperationParam> outputParameters = getSoapOutputParameters(bindingOperation);
                wsdlOperation.setOutputParams(outputParameters);
            }
        }
        return wsdlOperation;
    }

    /**
     * Returns input parameters, given soap binding operation
     *
     * @param bindingOperation {@link BindingOperation} object
     * @return input parameters, given soap binding operation
     */
    private List<WSDLOperationParam> getSoapInputParameters(BindingOperation bindingOperation)
            throws APIMgtWSDLException {
        List<WSDLOperationParam> params = new ArrayList<>();
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
                        String partElement = part.getElementName().getLocalPart();
                        this.getParameters(partElement, params);
                    }
                }
            }
        }
        return params;
    }

    /**
     * Returns output parameters, given soap binding operation
     *
     * @param bindingOperation {@link BindingOperation} object
     * @return output parameters, given soap binding operation
     */
    private List<WSDLOperationParam> getSoapOutputParameters(BindingOperation bindingOperation)
            throws APIMgtWSDLException {
        List<WSDLOperationParam> params = new ArrayList<>();
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
                        String partElement = part.getElementName().getLocalPart();
                        this.getParameters(partElement, params);
                    }
                }
            }
        }
        return params;
    }

    /**
     * Gets parameters definitions for the given input/output soap operation
     *
     * @param partElement parameter element name without namespace
     * @param params      reference parameter list to populate from the parameter nodes
     * @throws APIMgtWSDLException
     */
    private void getParameters(String partElement, List<WSDLOperationParam> params) throws APIMgtWSDLException {
        if (typeList != null) {
            Map<String, WSDLComplexType> typeMap = this.getComplexTypeMap(elemList);
            if (log.isDebugEnabled()) {
                log.debug("Number of complex types of the WSDL: " + typeMap.size());
            }
            for (Node element : elemList) {
                Node parentElement = element.getParentNode();
                if (!WSDL_ELEMENT_NODE.equals(parentElement.getLocalName())) {
                    parentElement = element;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Parent element of the complex type element: " + element.getNodeName() + " is "
                            + parentElement.getNodeName());
                }
                if (parentElement.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue()
                        .equals(partElement)) {
                    NodeList childNodes = element.getChildNodes().item(1).getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j).getLocalName() != null && WSDL_ELEMENT_NODE
                                .equals(childNodes.item(j).getLocalName())) {
                            WSDLOperationParam param = new WSDLOperationParam();
                            NamedNodeMap attributes = childNodes.item(j).getAttributes();
                            param.setName(attributes.getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue());
                            String dataType = attributes.getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE).getNodeValue()
                                    .split(SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR)[1];
                            Node maxOccursNode = attributes.getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE);
                            if (maxOccursNode != null && (
                                    maxOccursNode.getNodeValue().equals(SOAPToRESTConstants.UNBOUNDED) || !maxOccursNode
                                            .getNodeValue().equals("1"))) {
                                param.setArray(true);
                            }
                            if (!primitiveTypeList.contains(dataType)) {
                                WSDLComplexType complexType = typeMap.get(dataType);
                                if (complexType != null) {
                                    param.setWsdlComplexType(complexType);
                                }
                                param.setComplexType(true);
                            }
                            param.setDataType(
                                    attributes.getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE).getNodeValue());
                            params.add(param);
                        }
                    }
                }
            }
        } else {
            throw new APIMgtWSDLException("Cannot find any types from the given wsdl.");
        }
    }

    /**
     * gets complex types of the wsdl definition
     *
     * @param elemList complex type elements list from the schema
     * @return map of the complex types
     */
    private Map<String, WSDLComplexType> getComplexTypeMap(List<Node> elemList) {
        Map<String, WSDLComplexType> typeMap = new HashMap<>();

        if (elemList.get(0).getAttributes() != null
                && elemList.get(0).getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
            for (Node element : elemList) {
                if (element.getAttributes() != null
                        && element.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                    List<Node> childNodes = SOAPOperationBindingUtils
                            .list(element.getChildNodes().item(1).getChildNodes());
                    WSDLComplexType complexType = new WSDLComplexType();
                    for (Node childNode : childNodes) {
                        if (childNode.getLocalName() != null && WSDL_ELEMENT_NODE.equals(childNode.getLocalName())) {
                            WSDLOperationParam param = new WSDLOperationParam();
                            NamedNodeMap attributes = childNode.getAttributes();
                            param.setName(attributes.getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue());
                            String dataType = attributes.getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE).getNodeValue()
                                    .split(SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR)[1];
                            Node maxOccursNode = attributes.getNamedItem(SOAPToRESTConstants.MAX_OCCURS_ATTRIBUTE);
                            if (maxOccursNode != null && (
                                    maxOccursNode.getNodeValue().equals(SOAPToRESTConstants.UNBOUNDED) || !maxOccursNode
                                            .getNodeValue().equals("1"))) {
                                param.setArray(true);
                            }
                            if (primitiveTypeList.contains(dataType)) {
                                param.setDataType(dataType);
                            } else {
                                WSDLComplexType nestedComplexType = typeMap.get(dataType);
                                param.setWsdlComplexType(nestedComplexType);
                            }
                            complexType.getParamList().add(param);
                        }
                    }
                    typeMap.put(element.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue(),
                            complexType);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Number of complex types of the WSDL: " + typeMap.size());
        }
        return typeMap;
    }

    /**
     * reforms the types in the type reference order
     *
     * @param elemList complex elements list
     * @return ordered node list
     */
    private List<Node> reformComplexTypes(List<Node> elemList) {
        List<Node> clonedList = new ArrayList<>(elemList);
        List<Node> reformedList = new ArrayList<>();
        List<String> addedTypes = new ArrayList<>();
        List<Node> namedElements = new ArrayList<>();
        for (Node element : elemList) {
            if (element.getAttributes() != null
                    && element.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE) != null) {
                List<Node> childNodes = SOAPOperationBindingUtils.list(element.getChildNodes().item(1).getChildNodes());
                boolean isPrimitive = false;
                boolean isComplex = false;
                for (Node childNode : childNodes) {
                    if (childNode.getLocalName() != null && WSDL_ELEMENT_NODE.equals(childNode.getLocalName())) {
                        String dataType = childNode.getAttributes().getNamedItem(SOAPToRESTConstants.TYPE_ATTRIBUTE)
                                .getNodeValue().split(SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR)[1];
                        if (primitiveTypeList.contains(dataType)) {
                            isPrimitive = true;
                        } else {
                            isComplex = true;
                            addedTypes.add(dataType);
                        }
                    }
                }
                if (isPrimitive && !isComplex) {
                    clonedList.remove(element);
                    reformedList.add(element);
                }

                if (isComplex) {
                    namedElements.add(element);
                }
            }
        }

        for (Node node : namedElements) {
            String nodeVal = node.getAttributes().getNamedItem(SOAPToRESTConstants.NAME_ATTRIBUTE).getNodeValue();
            if (addedTypes.contains(nodeVal)) {
                clonedList.remove(node);
                reformedList.add(node);
            }
        }
        reformedList.addAll(clonedList);
        return reformedList;
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
