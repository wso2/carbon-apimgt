//package org.wso2.carbon.apimgt.impl.wsdl;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.wso2.carbon.apimgt.impl.wsdl.SOAPOperationParser.BindingType;
//import org.wso2.carbon.apimgt.impl.wsdl.SOAPOperationParser.SOAPApiOperation;
//import org.wso2.carbon.apimgt.impl.wsdl.SOAPOperationParser.MessageStructure;
//import org.wso2.carbon.apimgt.impl.wsdl.SOAPOperationParser.Parameter;
//
//import java.io.InputStream;
//import java.util.List;
//
///**
// * Test cases for SOAPOperationParser
// */
//public class SOAPApiOperationsTestCase {
//
//    @Test
//    public void testGetAllOperations() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        Assert.assertNotNull("Operations should not be null", operations);
//        Assert.assertTrue("Should have at least one operation", operations.size() > 0);
//    }
//
//    @Test
//    public void testSOAPOperationsIdentified() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        boolean foundCheckPhoneNumber = false;
//        boolean foundCheckPhoneNumbers = false;
//
//        for (SOAPApiOperation operation : operations) {
//            // Verify binding type is SOAP
//            Assert.assertTrue("Operation should be SOAP 1.1 or 1.2",
//                    operation.getBindingType() == BindingType.SOAP11 ||
//                            operation.getBindingType() == BindingType.SOAP12);
//
//            if ("CheckPhoneNumber".equals(operation.getName())) {
//                foundCheckPhoneNumber = true;
//            }
//            if ("CheckPhoneNumbers".equals(operation.getName())) {
//                foundCheckPhoneNumbers = true;
//            }
//        }
//
//        Assert.assertTrue("Should find CheckPhoneNumber operation", foundCheckPhoneNumber);
//        Assert.assertTrue("Should find CheckPhoneNumbers operation", foundCheckPhoneNumbers);
//    }
//
//    @Test
//    public void testSOAPActionParsed() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            Assert.assertNotNull("SOAP action should not be null", operation.getSoapAction());
//
//            if ("CheckPhoneNumber".equals(operation.getName())) {
//                Assert.assertTrue("SOAP action should contain operation name",
//                        operation.getSoapAction().contains("CheckPhoneNumber"));
//            }
//        }
//    }
//
//    @Test
//    public void testInputEnvelopeGenerated() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            String inputEnvelope = operation.getInputEnvelope();
//
//            Assert.assertNotNull("Input envelope should not be null", inputEnvelope);
//            Assert.assertTrue("Input envelope should contain Envelope tag",
//                    inputEnvelope.contains("<soapenv:Envelope"));
//            Assert.assertTrue("Input envelope should contain Header tag",
//                    inputEnvelope.contains("<soapenv:Header"));
//            Assert.assertTrue("Input envelope should contain Body tag",
//                    inputEnvelope.contains("<soapenv:Body"));
//            Assert.assertTrue("Input envelope should contain operation name",
//                    inputEnvelope.contains(operation.getName()));
//        }
//    }
//
//    @Test
//    public void testOutputEnvelopeGenerated() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            String outputEnvelope = operation.getOutputEnvelope();
//
//            Assert.assertNotNull("Output envelope should not be null", outputEnvelope);
//            Assert.assertTrue("Output envelope should contain Envelope tag",
//                    outputEnvelope.contains("<soapenv:Envelope"));
//            Assert.assertTrue("Output envelope should contain Body tag",
//                    outputEnvelope.contains("<soapenv:Body"));
//        }
//    }
//
////    @Test
////    public void testFaultEnvelopeGenerated() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            String faultEnvelope = operation.getFaultEnvelope();
////
////            Assert.assertNotNull("Fault envelope should not be null", faultEnvelope);
////            Assert.assertTrue("Fault envelope should contain Fault tag",
////                    faultEnvelope.contains("<soapenv:Fault"));
////        }
////    }
//
////    @Test
////    public void testSOAP11FaultStructure() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            if (operation.getBindingType() == BindingType.SOAP11) {
////                String faultEnvelope = operation.getFaultEnvelope();
////
////                Assert.assertTrue("SOAP 1.1 fault should contain faultcode",
////                        faultEnvelope.contains("<faultcode>"));
////                Assert.assertTrue("SOAP 1.1 fault should contain faultstring",
////                        faultEnvelope.contains("<faultstring>"));
////            }
////        }
////    }
////
////    @Test
////    public void testSOAP12FaultStructure() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/soap12-service.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            if (operation.getBindingType() == BindingType.SOAP12) {
////                String faultEnvelope = operation.getFaultEnvelope();
////
////                Assert.assertTrue("SOAP 1.2 fault should contain Code",
////                        faultEnvelope.contains("<soapenv:Code>"));
////                Assert.assertTrue("SOAP 1.2 fault should contain Reason",
////                        faultEnvelope.contains("<soapenv:Reason>"));
////                Assert.assertTrue("SOAP 1.2 envelope should use SOAP 1.2 namespace",
////                        faultEnvelope.contains("http://www.w3.org/2003/05/soap-envelope"));
////            }
////        }
////    }
//
////    @Test
////    public void testInputParametersParsed() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            MessageStructure inputMessage = operation.getInputMessage();
////
////            Assert.assertNotNull("Input message should not be null", inputMessage);
////            Assert.assertTrue("Input message should have parameters",
////                    inputMessage.getParameters().size() > 0);
////
////            if ("CheckPhoneNumber".equals(operation.getName())) {
////                boolean foundPhoneNumber = false;
////                boolean foundLicenseKey = false;
////
////                for (Parameter param : inputMessage.getParameters()) {
////                    if ("PhoneNumber".equals(param.getName())) {
////                        foundPhoneNumber = true;
////                        Assert.assertEquals("PhoneNumber should be string type", "string", param.getType());
////                    }
////                    if ("LicenseKey".equals(param.getName())) {
////                        foundLicenseKey = true;
////                        Assert.assertEquals("LicenseKey should be string type", "string", param.getType());
////                    }
////                }
////
////                Assert.assertTrue("Should find PhoneNumber parameter", foundPhoneNumber);
////                Assert.assertTrue("Should find LicenseKey parameter", foundLicenseKey);
////            }
////        }
////    }
////
////    @Test
////    public void testOutputParametersParsed() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            MessageStructure outputMessage = operation.getOutputMessage();
////
////            Assert.assertNotNull("Output message should not be null", outputMessage);
////            Assert.assertTrue("Output message should have parameters",
////                    outputMessage.getParameters().size() > 0);
////        }
////    }
////
//    @Test
//    public void testComplexTypesResolved() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            if ("CheckPhoneNumber".equals(operation.getName())) {
//                MessageStructure outputMessage = operation.getOutputMessage();
//
//                // CheckPhoneNumberResult should be a complex type
//                for (Parameter param : outputMessage.getParameters()) {
//                    if ("CheckPhoneNumberResult".equals(param.getType()) ||
//                            param.getNestedParameters() != null) {
//
//                        List<Parameter> nested = param.getNestedParameters();
//                        Assert.assertNotNull("Complex type should have nested parameters", nested);
//                        Assert.assertTrue("Complex type should have fields", nested.size() > 0);
//
//                        // Verify some known fields in PhoneReturn type
//                        boolean foundCompany = false;
//                        boolean foundValid = false;
//
//                        for (Parameter nestedParam : nested) {
//                            if ("Company".equals(nestedParam.getName())) {
//                                foundCompany = true;
//                            }
//                            if ("Valid".equals(nestedParam.getName())) {
//                                foundValid = true;
//                            }
//                        }
//
//                        Assert.assertTrue("Should find Company field", foundCompany);
//                        Assert.assertTrue("Should find Valid field", foundValid);
//                    }
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testArrayTypesResolved() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            if ("CheckPhoneNumbers".equals(operation.getName())) {
//                MessageStructure inputMessage = operation.getInputMessage();
//                String inputEnvelope = operation.getInputEnvelope();
//
//                // Check for PhoneNumbers array parameter
//                boolean foundPhoneNumbersArray = false;
//
//                for (Parameter param : inputMessage.getParameters()) {
//                    if ("PhoneNumbers".equals(param.getName())) {
//                        // Check if it has nested parameters (ArrayOfString)
//                        if (param.getNestedParameters() != null) {
//                            for (Parameter nested : param.getNestedParameters()) {
//                                if ("string".equals(nested.getName())) {
//                                    // Check maxOccurs to verify it's an array
//                                    if ("unbounded".equals(nested.getMaxOccurs())) {
//                                        foundPhoneNumbersArray = true;
//
//                                        // Verify input envelope has multiple string elements
//                                        int stringCount = countOccurrences(inputEnvelope, "<string>");
//                                        Assert.assertTrue("Array should generate multiple elements",
//                                                stringCount >= 2);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                Assert.assertTrue("Should find PhoneNumbers array", foundPhoneNumbersArray);
//            }
//        }
//    }
//
//    @Test
//    public void testSOAPHeadersParsed() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/service-with-headers.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            List<SOAPOperationParser.SOAPHeader> inputHeaders = operation.getInputHeaders();
//
//            if (inputHeaders != null && !inputHeaders.isEmpty()) {
//                Assert.assertTrue("Should have at least one header", inputHeaders.size() > 0);
//
//                String inputEnvelope = operation.getInputEnvelope();
//
//                // Verify header is in envelope
//                Assert.assertTrue("Envelope should contain header wrapper element",
//                        inputEnvelope.contains("<tns:"));
//
//                for (SOAPOperationParser.SOAPHeader header : inputHeaders) {
//                    Assert.assertNotNull("Header name should not be null", header.getName());
//                    Assert.assertNotNull("Header parameters should not be null", header.getParameters());
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testNestedComplexTypes() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/sample-service.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        boolean foundNestedType = false;
//
//        for (SOAPApiOperation operation : operations) {
//            MessageStructure inputMessage = operation.getInputMessage();
//
//            for (Parameter param : inputMessage.getParameters()) {
//                if (param.getNestedParameters() != null) {
//                    for (Parameter nested : param.getNestedParameters()) {
//                        if (nested.getNestedParameters() != null &&
//                                nested.getNestedParameters().size() > 0) {
//                            foundNestedType = true;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        Assert.assertTrue("Should find nested complex types", foundNestedType);
//    }
//
////    @Test
////    public void testMultipleBindings() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        // Check if same operation appears with different bindings
////        int soap11Count = 0;
////        int soap12Count = 0;
////
////        for (SOAPApiOperation operation : operations) {
////            if (operation.getBindingType() == BindingType.SOAP11) {
////                soap11Count++;
////            } else if (operation.getBindingType() == BindingType.SOAP12) {
////                soap12Count++;
////            }
////        }
////
////        Assert.assertTrue("Should have SOAP operations",
////                soap11Count > 0 || soap12Count > 0);
////    }
//
//    @Test
//    public void testGetSOAPOperationsOnly() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> soapOperations = parser.getSOAPOperations();
//
//        Assert.assertNotNull("SOAP operations should not be null", soapOperations);
//
//        for (SOAPApiOperation operation : soapOperations) {
//            Assert.assertTrue("Should only return SOAP operations",
//                    operation.getBindingType() == BindingType.SOAP11 ||
//                            operation.getBindingType() == BindingType.SOAP12);
//        }
//    }
//
//    @Test
//    public void testImportedSchemas() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/import-schemas/sampleservice.wsdl");
//
//        // This test requires basePath for imports
//        String basePath = Thread.currentThread().getContextClassLoader()
//                .getResource("wsdls/import-schemas/").getPath();
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream, basePath);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        Assert.assertNotNull("Should parse WSDL with imports", operations);
//        Assert.assertTrue("Should have operations", operations.size() > 0);
//    }
//
//    @Test
//    public void testEnvelopeNamespaces() throws Exception {
//        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("wsdls/phoneverify.wsdl");
//
//        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
//        List<SOAPApiOperation> operations = parser.getAllOperations();
//
//        for (SOAPApiOperation operation : operations) {
//            String inputEnvelope = operation.getInputEnvelope();
//
//            // Verify namespace declarations
//            Assert.assertTrue("Should have soapenv namespace",
//                    inputEnvelope.contains("xmlns:soapenv="));
//            Assert.assertTrue("Should have target namespace",
//                    inputEnvelope.contains("xmlns:tns="));
//
//            // Verify namespace usage
//            Assert.assertTrue("Should use tns prefix for operation",
//                    inputEnvelope.contains("<tns:" + operation.getName()));
//        }
//    }
//
////    @Test
////    public void testMinMaxOccurs() throws Exception {
////        InputStream wsdlStream = Thread.currentThread().getContextClassLoader()
////                .getResourceAsStream("wsdls/phoneverify.wsdl");
////
////        SOAPOperationParser parser = new SOAPOperationParser(wsdlStream);
////        List<SOAPApiOperation> operations = parser.getAllOperations();
////
////        for (SOAPApiOperation operation : operations) {
////            MessageStructure inputMessage = operation.getInputMessage();
////
////            for (Parameter param : inputMessage.getParameters()) {
////                // minOccurs and maxOccurs should be captured
////                if (param.getMinOccurs() != null || param.getMaxOccurs() != null) {
////                    // At least one cardinality should be set
////                    Assert.assertTrue("Cardinality should be captured", true);
////                }
////            }
////        }
////    }
//
//    // Helper method
//    private int countOccurrences(String str, String findStr) {
//        int lastIndex = 0;
//        int count = 0;
//
//        while (lastIndex != -1) {
//            lastIndex = str.indexOf(findStr, lastIndex);
//            if (lastIndex != -1) {
//                count++;
//                lastIndex += findStr.length();
//            }
//        }
//        return count;
//    }
//}