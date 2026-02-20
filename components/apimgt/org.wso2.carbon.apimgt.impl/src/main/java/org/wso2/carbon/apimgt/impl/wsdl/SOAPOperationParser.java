package org.wso2.carbon.apimgt.impl.wsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Improved WSDL Parser to extract all operations (SOAP, HTTP, etc.) and generate envelopes
 */
public class SOAPOperationParser {

    private static final Log log = LogFactory.getLog(SOAPOperationParser.class);
    private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    private static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    private static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final String SOAP12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";

    private Document wsdlDocument;
    private XPath xpath;
    private Map<String, String> namespaces;
    private Map<String, Element> schemaTypes;
    private Map<String, Element> schemaElements;
    private String targetNamespace;
    private String basePath;
    private Map<String, Document> importedDocuments;
    private String schemaPrefix;

    public SOAPOperationParser(InputStream wsdlInputStream) throws Exception {
        this(wsdlInputStream, null);
    }

    public SOAPOperationParser(String wsdlContent) throws Exception {
        this(new java.io.ByteArrayInputStream(wsdlContent.getBytes()), null);
    }

    public SOAPOperationParser(InputStream wsdlInputStream, String basePath) throws Exception {
        this.basePath = basePath;
        this.importedDocuments = new HashMap<>();
        this.namespaces = new HashMap<>();
        this.schemaTypes = new HashMap<>();
        this.schemaElements = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.wsdlDocument = builder.parse(wsdlInputStream);

        initializeParser();
    }

    private void initializeParser() throws Exception {
        extractNamespaces();
        findSchemaPrefix();

        Element root = wsdlDocument.getDocumentElement();
        this.targetNamespace = root.getAttribute("targetNamespace");

        XPathFactory xpathFactory = XPathFactory.newInstance();
        this.xpath = xpathFactory.newXPath();
        this.xpath.setNamespaceContext(new WSDLNamespaceContext(namespaces));

        if (basePath != null) {
            processImports();
        }

        indexSchemaTypes();
    }

    private void extractNamespaces() {
        Element root = wsdlDocument.getDocumentElement();
        NamedNodeMap attributes = root.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String nodeName = attr.getNodeName();
            if (nodeName.startsWith("xmlns:")) {
                String prefix = nodeName.substring(6);
                namespaces.put(prefix, attr.getNodeValue());
            } else if (nodeName.equals("xmlns")) {
                namespaces.put("", attr.getNodeValue());
            }
        }

        if (!namespaces.containsKey("wsdl")) {
            namespaces.put("wsdl", WSDL_NAMESPACE);
        }
        if (!namespaces.containsKey("soap")) {
            namespaces.put("soap", SOAP_NAMESPACE);
        }
        if (!namespaces.containsKey("soap12")) {
            namespaces.put("soap12", SOAP12_NAMESPACE);
        }
    }

    private void findSchemaPrefix() {
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            if (XSD_NAMESPACE.equals(entry.getValue())) {
                schemaPrefix = entry.getKey();
                return;
            }
        }
        schemaPrefix = "s";
        log.warn("Schema prefix not found, defaulting to 's'");
    }

    private void processImports() throws Exception {
        NodeList wsdlImports = wsdlDocument.getElementsByTagNameNS(WSDL_NAMESPACE, "import");
        for (int i = 0; i < wsdlImports.getLength(); i++) {
            Element importElement = (Element) wsdlImports.item(i);
            String location = importElement.getAttribute("location");
            if (location != null && !location.isEmpty()) {
                loadImportedDocument(location);
            }
        }

        NodeList xsdImports = wsdlDocument.getElementsByTagNameNS(XSD_NAMESPACE, "import");
        NodeList xsdIncludes = wsdlDocument.getElementsByTagNameNS(XSD_NAMESPACE, "include");
        processSchemaImports(xsdImports);
        processSchemaImports(xsdIncludes);
    }

    private void processSchemaImports(NodeList imports) {
        for (int i = 0; i < imports.getLength(); i++) {
            Element importElement = (Element) imports.item(i);
            String schemaLocation = importElement.getAttribute("schemaLocation");
            if (schemaLocation != null && !schemaLocation.isEmpty()) {
                loadImportedDocument(schemaLocation);
            }
        }
    }

    private void loadImportedDocument(String location) {
        FileInputStream fis = null;
        try {
            if (importedDocuments.containsKey(location)) {
                return;
            }

            File importFile = new File(basePath, location);
            if (!importFile.exists()) {
                log.warn("Imported file not found: " + location);
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            fis = new FileInputStream(importFile);
            Document importedDoc = builder.parse(fis);

            importedDocuments.put(location, importedDoc);
            indexSchemaTypesFromDocument(importedDoc);

        } catch (Exception e) {
            log.warn("Failed to load imported document: " + location, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    log.warn("Error closing file stream", e);
                }
            }
        }
    }

    private void indexSchemaTypes() throws Exception {
        indexSchemaTypesFromDocument(wsdlDocument);
    }

    private void indexSchemaTypesFromDocument(Document document) throws Exception {
        NodeList complexTypes = document.getElementsByTagNameNS(XSD_NAMESPACE, "complexType");
        NodeList simpleTypes = document.getElementsByTagNameNS(XSD_NAMESPACE, "simpleType");
        NodeList elements = document.getElementsByTagNameNS(XSD_NAMESPACE, "element");

        for (int i = 0; i < complexTypes.getLength(); i++) {
            Element element = (Element) complexTypes.item(i);
            String name = element.getAttribute("name");
            if (name != null && !name.isEmpty() && !schemaTypes.containsKey(name)) {
                schemaTypes.put(name, element);
            }
        }

        for (int i = 0; i < simpleTypes.getLength(); i++) {
            Element element = (Element) simpleTypes.item(i);
            String name = element.getAttribute("name");
            if (name != null && !name.isEmpty() && !schemaTypes.containsKey(name)) {
                schemaTypes.put(name, element);
            }
        }

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String name = element.getAttribute("name");
            if (name != null && !name.isEmpty() && !schemaElements.containsKey(name)) {
                schemaElements.put(name, element);
            }
        }
    }

    /**
     * Get all operations from the WSDL (all binding types)
     */
    public List<SOAPApiOperation> getAllOperations() throws Exception {
        List<SOAPApiOperation> operations = new ArrayList<>();

        // Get all bindings
        NodeList bindings = wsdlDocument.getElementsByTagNameNS(WSDL_NAMESPACE, "binding");

        for (int i = 0; i < bindings.getLength(); i++) {
            Element binding = (Element) bindings.item(i);
            String bindingName = binding.getAttribute("name");
            String portTypeName = getLocalName(binding.getAttribute("type"));

            // Determine binding type
            BindingType bindingType = determineBindingType(binding);

            // Skip non-SOAP bindings
            if (bindingType == null) {
                continue;
            }

            // Get the portType
            Element portType = findPortType(portTypeName);
            if (portType == null) {
                log.warn("PortType not found: " + portTypeName);
                continue;
            }

            // Get all operations in this binding
            NodeList bindingOps = binding.getElementsByTagNameNS(WSDL_NAMESPACE, "operation");

            for (int j = 0; j < bindingOps.getLength(); j++) {
                Element bindingOp = (Element) bindingOps.item(j);
                String opName = bindingOp.getAttribute("name");

                // Get operation details based on binding type
                SOAPApiOperation operation = parseOperationForBinding(
                        opName, bindingOp, portType, bindingName, bindingType
                );

                if (operation != null) {
                    operations.add(operation);
                }
            }
        }

        return operations;
    }

    /**
     * Get only SOAP operations (backward compatibility)
     */
    public List<SOAPApiOperation> getSOAPOperations() throws Exception {
        List<SOAPApiOperation> allOps = getAllOperations();
        List<SOAPApiOperation> soapOps = new ArrayList<>();

        for (SOAPApiOperation op : allOps) {
            if (op.getBindingType() == BindingType.SOAP11 ||
                    op.getBindingType() == BindingType.SOAP12) {
                soapOps.add(op);
            }
        }

        return soapOps;
    }

    /**
     * Determine the binding type
     */
    private BindingType determineBindingType(Element binding) {
        // Check for SOAP 1.1
        NodeList soap11 = binding.getElementsByTagNameNS(SOAP_NAMESPACE, "binding");
        if (soap11.getLength() > 0) {
            return BindingType.SOAP11;
        }

        // Check for SOAP 1.2
        NodeList soap12 = binding.getElementsByTagNameNS(SOAP12_NAMESPACE, "binding");
        if (soap12.getLength() > 0) {
            return BindingType.SOAP12;
        }
        return null;
    }

    private Element findPortType(String portTypeName) {
        NodeList portTypes = wsdlDocument.getElementsByTagNameNS(WSDL_NAMESPACE, "portType");
        for (int i = 0; i < portTypes.getLength(); i++) {
            Element pt = (Element) portTypes.item(i);
            if (portTypeName.equals(pt.getAttribute("name"))) {
                return pt;
            }
        }
        return null;
    }

    /**
     * Parse SOAP headers from binding operation
     */
    private List<SOAPHeader> parseSOAPHeaders(Element bindingOperation, boolean isInput) {
        List<SOAPHeader> headers = new ArrayList<>();

        // Get input or output element
        String elementName = isInput ? "input" : "output";
        NodeList ioElements = bindingOperation.getElementsByTagNameNS(WSDL_NAMESPACE, elementName);

        if (ioElements.getLength() == 0) {
            return headers;
        }

        Element ioElement = (Element) ioElements.item(0);

        // Check for SOAP 1.1 headers
        NodeList soap11Headers = ioElement.getElementsByTagNameNS(SOAP_NAMESPACE, "header");
        parseHeadersList(soap11Headers, headers);

        // Check for SOAP 1.2 headers
        NodeList soap12Headers = ioElement.getElementsByTagNameNS(SOAP12_NAMESPACE, "header");
        parseHeadersList(soap12Headers, headers);

        return headers;
    }

    /**
     * Parse a list of header elements
     */
    private void parseHeadersList(NodeList headerElements, List<SOAPHeader> headers) {
        for (int i = 0; i < headerElements.getLength(); i++) {
            Element headerElement = (Element) headerElements.item(i);

            SOAPHeader header = new SOAPHeader();

            String messageName = getLocalName(headerElement.getAttribute("message"));
            String partName = headerElement.getAttribute("part");
            String required = headerElement.getAttribute("required");

            header.setMessageName(messageName);
            header.setPartName(partName);
            header.setRequired("true".equalsIgnoreCase(required) || "1".equals(required));

            // Parse the header message to get its structure
            try {
                // Find the message element
                NodeList messages = wsdlDocument.getElementsByTagNameNS(WSDL_NAMESPACE, "message");
                Element msgElement = null;

                for (int j = 0; j < messages.getLength(); j++) {
                    Element msg = (Element) messages.item(j);
                    if (messageName.equals(msg.getAttribute("name"))) {
                        msgElement = msg;
                        break;
                    }
                }

                if (msgElement == null) {
                    log.warn("Header message not found: " + messageName);
                    continue;
                }

                // Get the part
                NodeList parts = msgElement.getElementsByTagNameNS(WSDL_NAMESPACE, "part");
                if (parts.getLength() == 0) {
                    continue;
                }

                Element part = (Element) parts.item(0);
                String elementRef = part.getAttribute("element");

                if (elementRef != null && !elementRef.isEmpty()) {
                    String elementLocalName = getLocalName(elementRef);
                    Element schemaElement = schemaElements.get(elementLocalName);

                    if (schemaElement != null) {
                        // Create a wrapper parameter for the header element
                        Parameter headerParam = new Parameter();
                        headerParam.setName(elementLocalName);

                        // Parse the nested structure
                        List<Parameter> nestedParams = parseElement(schemaElement);
                        headerParam.setNestedParameters(nestedParams);

                        // Set the header name from the element name
                        header.setName(elementLocalName);

                        // Add the wrapper parameter
                        List<Parameter> headerParams = new ArrayList<>();
                        headerParams.add(headerParam);
                        header.setParameters(headerParams);
                    } else {
                        log.warn("Schema element not found for header: " + elementLocalName);
                    }
                }

            } catch (Exception e) {
                log.warn("Failed to parse header message: " + messageName, e);
            }

            headers.add(header);
        }
    }

    /**
     * Generate header parameter XML with proper namespace
     */
    private void generateHeaderXML(StringBuilder sb, Parameter param, String indent) {
        if (param.getNestedParameters() != null && !param.getNestedParameters().isEmpty()) {
            // Header wrapper element (e.g., AuthHeader)
            sb.append(indent).append("<tns:").append(param.getName()).append(">\n");

            for (Parameter nested : param.getNestedParameters()) {
                generateHeaderFieldXML(sb, nested, indent + "  ");
            }

            sb.append(indent).append("</tns:").append(param.getName()).append(">\n");
        } else {
            // Simple header field
            String sampleValue = param.getType();
            sb.append(indent).append("<tns:").append(param.getName()).append(">");
            sb.append(sampleValue);
            sb.append("</tns:").append(param.getName()).append(">\n");
        }
    }

    /**
     * Generate header field XML (nested elements within header)
     */
    private void generateHeaderFieldXML(StringBuilder sb, Parameter param, String indent) {
        String maxOccurs = param.getMaxOccurs();
        boolean isRepeating = maxOccurs != null &&
                (maxOccurs.equals("unbounded") ||
                        (isNumeric(maxOccurs) && Integer.parseInt(maxOccurs) > 1));

        if (param.getNestedParameters() != null && !param.getNestedParameters().isEmpty()) {
            if (isRepeating) {
                for (int i = 0; i < 2; i++) {
                    sb.append(indent).append("<").append(param.getName()).append(">\n");
                    for (Parameter nested : param.getNestedParameters()) {
                        generateHeaderFieldXML(sb, nested, indent + "  ");
                    }
                    sb.append(indent).append("</").append(param.getName()).append(">\n");
                }
            } else {
                sb.append(indent).append("<").append(param.getName()).append(">\n");
                for (Parameter nested : param.getNestedParameters()) {
                    generateHeaderFieldXML(sb, nested, indent + "  ");
                }
                sb.append(indent).append("</").append(param.getName()).append(">\n");
            }
        } else {
            String sampleValue = param.getType();

            if (isRepeating) {
                for (int i = 0; i < 2; i++) {
                    sb.append(indent).append("<").append(param.getName()).append(">");
                    sb.append(sampleValue);
                    sb.append("</").append(param.getName()).append(">\n");
                }
            } else {
                sb.append(indent).append("<").append(param.getName()).append(">");
                sb.append(sampleValue);
                sb.append("</").append(param.getName()).append(">\n");
            }
        }
    }

    private SOAPApiOperation parseOperationForBinding(String operationName, Element bindingOp,
            Element portType, String bindingName,
            BindingType bindingType) throws Exception {
        SOAPApiOperation operation = new SOAPApiOperation();
        operation.setName(operationName);
        operation.setBinding(bindingName);
        operation.setBindingType(bindingType);

        // Get binding-specific details
        switch (bindingType) {
        case SOAP11:
        case SOAP12:
            String soapAction = getSoapAction(bindingOp);
            operation.setSoapAction(soapAction);
            operation.setHttpMethod("POST"); // SOAP is always POST
            break;
        }

        // Get operation from portType
        Element portTypeOp = findOperationInPortType(portType, operationName);
        if (portTypeOp == null) {
            return null;
        }

        // Parse input headers
        List<SOAPHeader> inputHeaders = parseSOAPHeaders(bindingOp, true);
        operation.setInputHeaders(inputHeaders);

        // Parse output headers
        List<SOAPHeader> outputHeaders = parseSOAPHeaders(bindingOp, false);
        operation.setOutputHeaders(outputHeaders);

        // Get input message
        NodeList inputs = portTypeOp.getElementsByTagNameNS(WSDL_NAMESPACE, "input");
        if (inputs.getLength() > 0) {
            Element input = (Element) inputs.item(0);
            String inputMsgName = getLocalName(input.getAttribute("message"));
            MessageStructure inputStruct = parseMessage(inputMsgName, true);
            operation.setInputMessage(inputStruct);

            operation.setInputEnvelope(generateSOAPEnvelope(inputStruct, operationName, true, bindingType, inputHeaders));

        }

        // Get output message
        NodeList outputs = portTypeOp.getElementsByTagNameNS(WSDL_NAMESPACE, "output");
        if (outputs.getLength() > 0) {
            Element output = (Element) outputs.item(0);
            String outputMsgName = getLocalName(output.getAttribute("message"));
            MessageStructure outputStruct = parseMessage(outputMsgName, false);
            operation.setOutputMessage(outputStruct);

            operation.setOutputEnvelope(generateSOAPEnvelope(outputStruct, operationName + "Response", false, bindingType,
                        outputHeaders));
        }

        return operation;
    }

    private Element findOperationInPortType(Element portType, String operationName) {
        NodeList operations = portType.getElementsByTagNameNS(WSDL_NAMESPACE, "operation");
        for (int i = 0; i < operations.getLength(); i++) {
            Element op = (Element) operations.item(i);
            if (operationName.equals(op.getAttribute("name"))) {
                return op;
            }
        }
        return null;
    }

    private String getSoapAction(Element bindingOperation) {
        // Try SOAP 1.1
        NodeList soapOps = bindingOperation.getElementsByTagNameNS(SOAP_NAMESPACE, "operation");
        if (soapOps.getLength() > 0) {
            return ((Element) soapOps.item(0)).getAttribute("soapAction");
        }

        // Try SOAP 1.2
        soapOps = bindingOperation.getElementsByTagNameNS(SOAP12_NAMESPACE, "operation");
        if (soapOps.getLength() > 0) {
            return ((Element) soapOps.item(0)).getAttribute("soapAction");
        }

        return "";
    }

    private MessageStructure parseMessage(String messageName, boolean isInput) throws Exception {
        MessageStructure message = new MessageStructure();
        message.setName(messageName);

        NodeList messages = wsdlDocument.getElementsByTagNameNS(WSDL_NAMESPACE, "message");
        Element msgElement = null;

        for (int i = 0; i < messages.getLength(); i++) {
            Element msg = (Element) messages.item(i);
            if (messageName.equals(msg.getAttribute("name"))) {
                msgElement = msg;
                break;
            }
        }

        if (msgElement == null) {
            log.warn("Message not found: " + messageName);
            return message;
        }

        NodeList parts = msgElement.getElementsByTagNameNS(WSDL_NAMESPACE, "part");

        for (int i = 0; i < parts.getLength(); i++) {
            Element part = (Element) parts.item(i);
            String partName = part.getAttribute("name");
            String elementRef = part.getAttribute("element");
            String typeRef = part.getAttribute("type");

            if (elementRef != null && !elementRef.isEmpty()) {
                String elementName = getLocalName(elementRef);
                Element schemaElement = schemaElements.get(elementName);

                if (schemaElement != null) {
                    List<Parameter> params = parseElement(schemaElement);
                    message.getParameters().addAll(params);
                } else {
                    log.warn("Schema element not found: " + elementName);
                }
            } else if (typeRef != null && !typeRef.isEmpty()) {
                String typeName = getLocalName(typeRef);
                Parameter param = new Parameter();
                param.setName(partName);
                param.setType(typeName);

                if (schemaTypes.containsKey(typeName)) {
                    Element typeElement = schemaTypes.get(typeName);
                    List<Parameter> nestedParams = parseComplexType(typeElement);
                    param.setNestedParameters(nestedParams);
                }

                message.getParameters().add(param);
            }
        }

        return message;
    }

    private List<Parameter> parseElement(Element element) throws Exception {
        List<Parameter> parameters = new ArrayList<>();

        String typeRef = element.getAttribute("type");
        if (typeRef != null && !typeRef.isEmpty()) {
            String typeName = getLocalName(typeRef);
            if (schemaTypes.containsKey(typeName)) {
                Element typeElement = schemaTypes.get(typeName);
                return parseComplexType(typeElement);
            }
        }

        NodeList complexTypes = element.getElementsByTagNameNS(XSD_NAMESPACE, "complexType");
        if (complexTypes.getLength() > 0) {
            Element complexType = (Element) complexTypes.item(0);
            return parseComplexType(complexType);
        }

        return parameters;
    }

    private List<Parameter> parseComplexType(Element complexType) throws Exception {
        List<Parameter> parameters = new ArrayList<>();

        NodeList sequences = complexType.getElementsByTagNameNS(XSD_NAMESPACE, "sequence");
        if (sequences.getLength() > 0) {
            parameters.addAll(parseSequence((Element) sequences.item(0)));
        }

        NodeList alls = complexType.getElementsByTagNameNS(XSD_NAMESPACE, "all");
        if (alls.getLength() > 0) {
            parameters.addAll(parseSequence((Element) alls.item(0)));
        }

        NodeList choices = complexType.getElementsByTagNameNS(XSD_NAMESPACE, "choice");
        if (choices.getLength() > 0) {
            parameters.addAll(parseSequence((Element) choices.item(0)));
        }

        NodeList complexContents = complexType.getElementsByTagNameNS(XSD_NAMESPACE, "complexContent");
        if (complexContents.getLength() > 0) {
            parameters.addAll(parseComplexContent((Element) complexContents.item(0)));
        }

        return parameters;
    }

    private List<Parameter> parseSequence(Element sequence) throws Exception {
        List<Parameter> parameters = new ArrayList<>();
        NodeList elements = sequence.getElementsByTagNameNS(XSD_NAMESPACE, "element");

        for (int i = 0; i < elements.getLength(); i++) {
            Element elem = (Element) elements.item(i);

            if (!sequence.equals(elem.getParentNode())) {
                continue;
            }

            Parameter param = new Parameter();
            param.setName(elem.getAttribute("name"));

            String type = elem.getAttribute("type");
            String minOccurs = elem.getAttribute("minOccurs");
            String maxOccurs = elem.getAttribute("maxOccurs");

            param.setMinOccurs(minOccurs);
            param.setMaxOccurs(maxOccurs);

            if (type != null && !type.isEmpty()) {
                String typeName = getLocalName(type);
                param.setType(typeName);

                if (schemaTypes.containsKey(typeName)) {
                    Element complexTypeElement = schemaTypes.get(typeName);
                    List<Parameter> nestedParams = parseComplexType(complexTypeElement);
                    param.setNestedParameters(nestedParams);
                }
            } else {
                NodeList inlineComplexTypes = elem.getElementsByTagNameNS(XSD_NAMESPACE, "complexType");
                if (inlineComplexTypes.getLength() > 0) {
                    Element inlineType = (Element) inlineComplexTypes.item(0);
                    List<Parameter> nestedParams = parseComplexType(inlineType);
                    param.setNestedParameters(nestedParams);
                    param.setType("complexType");
                } else {
                    param.setType("string");
                }
            }

            parameters.add(param);
        }

        return parameters;
    }

    private List<Parameter> parseComplexContent(Element complexContent) throws Exception {
        List<Parameter> parameters = new ArrayList<>();

        NodeList extensions = complexContent.getElementsByTagNameNS(XSD_NAMESPACE, "extension");
        if (extensions.getLength() > 0) {
            Element extension = (Element) extensions.item(0);

            String base = extension.getAttribute("base");
            if (base != null && !base.isEmpty()) {
                String baseName = getLocalName(base);
                if (schemaTypes.containsKey(baseName)) {
                    parameters.addAll(parseComplexType(schemaTypes.get(baseName)));
                }
            }

            NodeList sequences = extension.getElementsByTagNameNS(XSD_NAMESPACE, "sequence");
            if (sequences.getLength() > 0) {
                parameters.addAll(parseSequence((Element) sequences.item(0)));
            }
        }

        NodeList restrictions = complexContent.getElementsByTagNameNS(XSD_NAMESPACE, "restriction");
        if (restrictions.getLength() > 0) {
            Element restriction = (Element) restrictions.item(0);
            NodeList sequences = restriction.getElementsByTagNameNS(XSD_NAMESPACE, "sequence");
            if (sequences.getLength() > 0) {
                parameters.addAll(parseSequence((Element) sequences.item(0)));
            }
        }

        return parameters;
    }

    private String generateSOAPEnvelope(MessageStructure message, String operationName,
            boolean isInput, BindingType bindingType, List<SOAPHeader> headers) {
        StringBuilder envelope = new StringBuilder();

        envelope.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        if (bindingType == BindingType.SOAP12) {
            envelope.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"");
        } else {
            envelope.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"");
        }

        envelope.append(" xmlns:tns=\"").append(targetNamespace).append("\">\n");
        // Generate Header section
        if (headers != null && !headers.isEmpty()) {
            envelope.append("  <soapenv:Header>\n");

            for (SOAPHeader header : headers) {
                if (header.getParameters() != null && !header.getParameters().isEmpty()) {
                    for (Parameter param : header.getParameters()) {
//                        generateParameterXML(envelope, param, "    ");
                        generateHeaderXML(envelope, param, "    ");
                    }
                }
            }

            envelope.append("  </soapenv:Header>\n");
        } else {
            envelope.append("  <soapenv:Header/>\n");
        }
        envelope.append("  <soapenv:Body>\n");

        if (!message.getParameters().isEmpty()) {
            envelope.append("    <tns:").append(operationName).append(">\n");

            for (Parameter param : message.getParameters()) {
                generateParameterXML(envelope, param, "      ");
            }

            envelope.append("    </tns:").append(operationName).append(">\n");
        }

        envelope.append("  </soapenv:Body>\n");
        envelope.append("</soapenv:Envelope>");

        return envelope.toString();
    }

    private String generateHTTPGetRequest(MessageStructure message, String location) {
        StringBuilder request = new StringBuilder();
        request.append("GET ").append(location);

        if (!message.getParameters().isEmpty()) {
            request.append("?");
            boolean first = true;
            for (Parameter param : message.getParameters()) {
                if (!first) request.append("&");
                request.append(param.getName()).append("={").append(param.getName()).append("}");
                first = false;
            }
        }

        request.append(" HTTP/1.1\n");
        request.append("Host: {host}\n");
        request.append("Accept: text/xml");

        return request.toString();
    }

    private String generateHTTPPostRequest(MessageStructure message, String location) {
        StringBuilder request = new StringBuilder();
        request.append("POST ").append(location).append(" HTTP/1.1\n");
        request.append("Host: {host}\n");
        request.append("Content-Type: application/x-www-form-urlencoded\n\n");

        if (!message.getParameters().isEmpty()) {
            boolean first = true;
            for (Parameter param : message.getParameters()) {
                if (!first) request.append("&");
                request.append(param.getName()).append("={").append(param.getName()).append("}");
                first = false;
            }
        }

        return request.toString();
    }

    private String generateXMLResponse(MessageStructure message) {
        StringBuilder response = new StringBuilder();
        response.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        response.append("<response>\n");

        for (Parameter param : message.getParameters()) {
            generateParameterXML(response, param, "  ");
        }

        response.append("</response>");
        return response.toString();
    }

    private void generateParameterXML(StringBuilder sb, Parameter param, String indent) {
        String maxOccurs = param.getMaxOccurs();
        boolean isRepeating = maxOccurs != null &&
                (maxOccurs.equals("unbounded") ||
                        (isNumeric(maxOccurs) && Integer.parseInt(maxOccurs) > 1));

        if (param.getNestedParameters() != null && !param.getNestedParameters().isEmpty()) {
            if (isRepeating) {
                for (int i = 0; i < 2; i++) {
                    sb.append(indent).append("<tns:").append(param.getName()).append(">\n");
                    for (Parameter nested : param.getNestedParameters()) {
                        generateParameterXML(sb, nested, indent + "  ");
                    }
                    sb.append(indent).append("</tns:").append(param.getName()).append(">\n");
                }
            } else {
                sb.append(indent).append("<tns:").append(param.getName()).append(">\n");
                for (Parameter nested : param.getNestedParameters()) {
                    generateParameterXML(sb, nested, indent + "  ");
                }
                sb.append(indent).append("</tns:").append(param.getName()).append(">\n");
            }
        } else {
            String sampleValue = param.getType();

            if (isRepeating) {
                for (int i = 0; i < 2; i++) {
                    sb.append(indent).append("<").append(param.getName()).append(">");
                    sb.append(sampleValue);
                    sb.append("</").append(param.getName()).append(">\n");
                }
            } else {
                sb.append(indent).append("<").append(param.getName()).append(">");
                sb.append(sampleValue);
                sb.append("</").append(param.getName()).append(">\n");
            }
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getLocalName(String qName) {
        if (qName == null || qName.isEmpty()) {
            return qName;
        }
        int colonIndex = qName.indexOf(':');
        if (colonIndex != -1) {
            return qName.substring(colonIndex + 1);
        }
        return qName;
    }

    /**
     * Binding Type Enum
     */
    public enum BindingType {
        SOAP11("SOAP 1.1"),
        SOAP12("SOAP 1.2"),
        UNKNOWN("Unknown");

        private final String displayName;

        BindingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * WSDL Operation class (replaces SOAPOperation)
     */
    public static class SOAPApiOperation {
        private String name;
        private String binding;
        private BindingType bindingType;
        private String soapAction;
        private String httpMethod;
        private String httpLocation;
        private MessageStructure inputMessage;
        private MessageStructure outputMessage;
        private String inputEnvelope;
        private String outputEnvelope;
        private List<SOAPHeader> inputHeaders;
        private List<SOAPHeader> outputHeaders;

        public MessageStructure getInputMessage() {
            return inputMessage;
        }

        public void setInputMessage(MessageStructure inputMessage) {
            this.inputMessage = inputMessage;
        }

        public MessageStructure getOutputMessage() {
            return outputMessage;
        }

        public void setOutputMessage(MessageStructure outputMessage) {
            this.outputMessage = outputMessage;
        }

        public List<SOAPHeader> getInputHeaders() {
            return inputHeaders;
        }

        public void setInputHeaders(List<SOAPHeader> inputHeaders) {
            this.inputHeaders = inputHeaders;
        }

        public List<SOAPHeader> getOutputHeaders() {
            return outputHeaders;
        }

        public void setOutputHeaders(List<SOAPHeader> outputHeaders) {
            this.outputHeaders = outputHeaders;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getBinding() { return binding; }
        public void setBinding(String binding) { this.binding = binding; }

        public BindingType getBindingType() { return bindingType; }
        public void setBindingType(BindingType bindingType) { this.bindingType = bindingType; }

        public String getSoapAction() { return soapAction; }
        public void setSoapAction(String soapAction) { this.soapAction = soapAction; }

        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

        public String getHttpLocation() { return httpLocation; }
        public void setHttpLocation(String httpLocation) { this.httpLocation = httpLocation; }

        public String getInputEnvelope() { return inputEnvelope; }
        public void setInputEnvelope(String inputEnvelope) { this.inputEnvelope = inputEnvelope; }

        public String getOutputEnvelope() { return outputEnvelope; }
        public void setOutputEnvelope(String outputEnvelope) { this.outputEnvelope = outputEnvelope; }

        @Override
        public String toString() {
            return "WSDLOperation{" +
                    "name='" + name + '\'' +
                    ", bindingType=" + bindingType +
                    ", binding='" + binding + '\'' +
                    ", soapAction='" + soapAction + '\'' +
                    ", httpMethod='" + httpMethod + '\'' +
                    '}';
        }
    }

    public static class MessageStructure {
        private String name;
        private List<Parameter> parameters = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<Parameter> getParameters() { return parameters; }
        public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }
    }

    public static class Parameter {
        private String name;
        private String type;
        private String minOccurs;
        private String maxOccurs;
        private List<Parameter> nestedParameters;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMinOccurs() { return minOccurs; }
        public void setMinOccurs(String minOccurs) { this.minOccurs = minOccurs; }

        public String getMaxOccurs() { return maxOccurs; }
        public void setMaxOccurs(String maxOccurs) { this.maxOccurs = maxOccurs; }

        public List<Parameter> getNestedParameters() { return nestedParameters; }
        public void setNestedParameters(List<Parameter> nestedParameters) {
            this.nestedParameters = nestedParameters;
        }

        @Override
        public String toString() {
            return "Parameter{name='" + name + "', type='" + type + "'}";
        }
    }

    /**
     * SOAP Header structure
     */
    public static class SOAPHeader {
        private String name;
        private String messageName;
        private String partName;
        private boolean required;
        private List<Parameter> parameters;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getMessageName() { return messageName; }
        public void setMessageName(String messageName) { this.messageName = messageName; }

        public String getPartName() { return partName; }
        public void setPartName(String partName) { this.partName = partName; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public List<Parameter> getParameters() { return parameters; }
        public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }
    }

    private static class WSDLNamespaceContext implements NamespaceContext {
        private Map<String, String> namespaces;

        public WSDLNamespaceContext(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaces.getOrDefault(prefix, "");
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    prefixes.add(entry.getKey());
                }
            }
            return prefixes.iterator();
        }
    }
}