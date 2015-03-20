package org.apache.synapse.commons.builders;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.axis2.util.MultipleEntryHashMap;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.regex.Pattern;

/**
 * Synapse specific message builder for "application/x-www-form-urlencoded" content type. This
 * builder extends the functionality provided by the Axis2 builder.
 */
public class XFormURLEncodedBuilder implements Builder {


    private static final QName XFORM_FIRST_ELEMENT = new QName("xformValues");

    public OMElement processDocument(InputStream inputStream, String s,
                                     MessageContext messageContext) throws AxisFault {
    	// first process the input stream
    	 SOAPEnvelope soapEnv = (SOAPEnvelope) processDocumentWrapper(inputStream, s, messageContext);

        // when this is a POST request, if the body of the soap envelope is empty and the parameter
        // map is there, build a dummy soap body which contains all the parameters coming in.
        SOAPBody body = soapEnv.getBody();
        String httpMethod = (String) messageContext.getProperty(HTTPConstants.HTTP_METHOD);
        if (body.getFirstElement() == null && HTTPConstants.HTTP_METHOD_POST.equals(httpMethod) &&
                messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP) != null) {
            MultipleEntryHashMap map = (MultipleEntryHashMap) messageContext
                    .getProperty(Constants.REQUEST_PARAMETER_MAP);
            SOAPFactory soapFactory = getSOAPFactory(messageContext);
            OMElement bodyFirstChild = soapFactory
                    .createOMElement(XFORM_FIRST_ELEMENT, body);
            createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild, map);
        }
        return soapEnv;
    }

    /**
     * @return Returns the document element.
     */
    private OMElement processDocumentWrapper(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {

        MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();
        SOAPFactory soapFactory;
        AxisBindingOperation axisBindingOperation =
                (AxisBindingOperation) messageContext.getProperty(
                        Constants.AXIS_BINDING_OPERATION);
        String queryParameterSeparator = null;
        String templatedPath = null;
        if (axisBindingOperation != null) {
            queryParameterSeparator = (String) axisBindingOperation
                    .getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
            templatedPath =
                    (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
        }
        if (queryParameterSeparator == null) {
            queryParameterSeparator =
                    WSDL20DefaultValueHolder.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT;
        }

        AxisEndpoint axisEndpoint =
                (AxisEndpoint) messageContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (axisEndpoint != null) {
            AxisBinding axisBinding = axisEndpoint.getBinding();
            String soapVersion =
                    (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            soapFactory = getSOAPFactory(soapVersion);
        } else {
            soapFactory = getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }
        EndpointReference endpointReference = messageContext.getTo();
        if (endpointReference == null) {
            throw new AxisFault("Cannot create DocumentElement without destination EPR");
        }

        String requestURL = endpointReference.getAddress();
        try {
            requestURL = extractParametersUsingHttpLocation(templatedPath, parameterMap,
                                                            requestURL,
                                                            queryParameterSeparator);
        } catch (UnsupportedEncodingException e) {
            throw AxisFault.makeFault(e);
        }

        String query = requestURL;
        int index;
        if ((index = requestURL.indexOf("?")) > -1) {
            query = requestURL.substring(index + 1);
        }

        extractParametersFromRequest(parameterMap, query, queryParameterSeparator,
                                     (String) messageContext.getProperty(
                                             Constants.Configuration.CHARACTER_SET_ENCODING),
                                     inputStream);

        messageContext.setProperty(Constants.REQUEST_PARAMETER_MAP, parameterMap);
        return BuilderUtil.buildsoapMessage(messageContext, parameterMap,
                                            soapFactory);
    }

    private static void createSOAPMessageWithoutSchema(SOAPFactory soapFactory,
                                                      OMElement bodyFirstChild,
                                                      MultipleEntryHashMap requestParameterMap) {

        // first add the parameters in the URL
        if (requestParameterMap != null) {
            for (Object o : requestParameterMap.keySet()) {
                String key = (String) o;
                Object value;
                while ((value = requestParameterMap.get(key)) != null) {
                    addRequestParameter(soapFactory, bodyFirstChild, null, key, value);
                }
            }
        }
    }

    private static void addRequestParameter(SOAPFactory soapFactory,
                                            OMElement bodyFirstChild,
                                            OMNamespace ns,
                                            String key,
                                            Object parameter) {
        if (parameter instanceof DataHandler) {
            DataHandler dataHandler = (DataHandler)parameter;
            OMText dataText = bodyFirstChild.getOMFactory().createOMText(
                    dataHandler, true);
            soapFactory.createOMElement(key, ns, bodyFirstChild).addChild(
                    dataText);
        } else {
            String textValue = parameter.toString();
            soapFactory.createOMElement(key, ns, bodyFirstChild).setText(
                    textValue);
        }
    }

    private void extractParametersFromRequest(MultipleEntryHashMap parameterMap,
                                                String query,
                                                String queryParamSeparator,
                                                final String charsetEncoding,
                                                final InputStream inputStream)
            throws AxisFault {

        if (query != null && !"".equals(query)) {

            String parts[] = query.split(queryParamSeparator);
            for (String part : parts) {
                int separator = part.indexOf("=");
                if (separator > 0) {
                    String value = part.substring(separator + 1);
                    try {
                        value = URIEncoderDecoder.decode(value);
                    } catch (UnsupportedEncodingException e) {
                        throw AxisFault.makeFault(e);
                    }

                    parameterMap.put(replaceInvalidCharacters(part.substring(0, separator)),
                                     value);
                }
            }

        }

        if (inputStream != null) {
            try {
                InputStreamReader inputStreamReader;
                try {
                    inputStreamReader = (InputStreamReader) AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                                public Object run() throws UnsupportedEncodingException {
                                    return new InputStreamReader(inputStream, charsetEncoding);
                                }
                            }
                    );
                } catch (PrivilegedActionException e) {
                    throw (UnsupportedEncodingException) e.getException();
                }
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        String parts[] = line.split(
                                WSDL20DefaultValueHolder.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT);

                        for (String part : parts) {
                            int separator = part.indexOf("=");
                            String value = part.substring(separator + 1);
                            parameterMap.put(replaceInvalidCharacters(part.substring(0, separator)),
                                             URIEncoderDecoder.decode(value));
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }

    //    "2a" replace with "_JsonReader_PD_2a"
    //    "$a" replace with "_JsonReader_PS_a"
    private String replaceInvalidCharacters(String keyString){
        if(Pattern.compile("^[0-9]").matcher(keyString).find()) {
            return "_JsonReader_PD_" + keyString;
        } else if (keyString.startsWith("$")){
            return "_JsonReader_PS_" + keyString.substring(1);
        } else
            return keyString;
    }
    
    private SOAPFactory getSOAPFactory(MessageContext messageContext) throws AxisFault {
        SOAPFactory soapFactory;
        AxisEndpoint axisEndpoint = (AxisEndpoint) messageContext
                .getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (axisEndpoint != null) {
            AxisBinding axisBinding = axisEndpoint.getBinding();
            String soapVersion =
                    (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            soapFactory = getSOAPFactory(soapVersion);
        } else {
            soapFactory = getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }
        return soapFactory;
    }

    private SOAPFactory getSOAPFactory(String nsURI) throws AxisFault {
        if (nsURI == null) {
            return OMAbstractFactory.getSOAP12Factory();
        }
        else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }
    
    private String extractParametersUsingHttpLocation(String templatedPath,
                                                      MultipleEntryHashMap parameterMap,
                                                      String requestURL,
                                                      String queryParameterSeparator)
          throws AxisFault, UnsupportedEncodingException {


      if (templatedPath != null && !"".equals(templatedPath) && templatedPath.contains("{")) {
          StringBuilder pathTemplate = new StringBuilder(templatedPath);

          // this will hold the index, from which we need to process the request URI
          int startIndex = 0;
          int templateStartIndex = 0;
          int templateEndIndex = 0;
          int indexOfNextConstant = 0;

          StringBuilder requestURIBuffer = new StringBuilder(requestURL);

          while (startIndex < requestURIBuffer.length()) {
              // this will always hold the starting index of a template parameter
              templateStartIndex = pathTemplate.indexOf("{", templateStartIndex);

              if (templateStartIndex > 0) {
                  // get the preceding constant part from the template
                  String constantPart =
                          pathTemplate.substring(templateEndIndex + 1, templateStartIndex);
                  constantPart = constantPart.replaceAll("\\{\\{","{");
                  constantPart = constantPart.replaceAll("}}","}");

                  // get the index of the end of this template param
                  templateEndIndex = pathTemplate.indexOf("}", templateStartIndex);
                  if ((pathTemplate.length() -1) > templateEndIndex && pathTemplate.charAt(templateEndIndex +1) == '}') {
                      templateEndIndex = pathTemplate.indexOf("}", templateEndIndex +2);
                  }

                  String parameterName =
                          pathTemplate.substring(templateStartIndex + 1, templateEndIndex);
                  // next try to find the next constant
                  templateStartIndex = pathTemplate.indexOf("{", templateEndIndex);
                  if (pathTemplate.charAt(templateStartIndex +1) == '{') {
                      templateStartIndex = pathTemplate.indexOf("{", templateStartIndex +2);
                  }

                  int endIndexOfConstant = requestURIBuffer
                                                   .indexOf(constantPart, indexOfNextConstant) + constantPart.length();

                  if (templateStartIndex == -1) {
                      if (templateEndIndex == pathTemplate.length() - 1) {

                          // We may have occations where we have templates of the form foo/{name}.
                          // In this case the next connstant will be ? and not the
                          // queryParameterSeparator
                          indexOfNextConstant =
                                  requestURIBuffer
                                          .indexOf("?", endIndexOfConstant);
                          if (indexOfNextConstant == -1) {
                              indexOfNextConstant =
                                      requestURIBuffer
                                              .indexOf(queryParameterSeparator,
                                                       endIndexOfConstant);
                          }
                          if (indexOfNextConstant > 0) {
                              addParameterToMap(parameterMap, parameterName,
                                                requestURIBuffer.substring(endIndexOfConstant,
                                                                           indexOfNextConstant));
                              return requestURL.substring(indexOfNextConstant);
                          } else {

                              addParameterToMap(parameterMap, parameterName,
                                                requestURIBuffer.substring(
                                                        endIndexOfConstant));
                              return "";
                          }

                      } else {

                          constantPart =
                                  pathTemplate.substring(templateEndIndex + 1,
                                                         pathTemplate.length());
                          constantPart = constantPart.replaceAll("\\{\\{","{");
                          constantPart = constantPart.replaceAll("}}","}");
                          indexOfNextConstant =
                                  requestURIBuffer.indexOf(constantPart, endIndexOfConstant);

                          addParameterToMap(parameterMap, parameterName,
                                            requestURIBuffer.substring(
                                                    endIndexOfConstant, indexOfNextConstant));

                          if (requestURIBuffer.length() > indexOfNextConstant + 1) {
                              return requestURIBuffer.substring(indexOfNextConstant + 1);
                          }
                          return "";
                      }
                  } else {

                      // this is the next constant from the template
                      constantPart =
                              pathTemplate
                                      .substring(templateEndIndex + 1, templateStartIndex);
                      constantPart = constantPart.replaceAll("\\{\\{","{");
                      constantPart = constantPart.replaceAll("}}","}");

                      indexOfNextConstant =
                              requestURIBuffer.indexOf(constantPart, endIndexOfConstant);
                      addParameterToMap(parameterMap, parameterName, requestURIBuffer.substring(
                              endIndexOfConstant, indexOfNextConstant));
                      startIndex = indexOfNextConstant;
                  }
              }
          }
      }
      return requestURL;
  }

  private void addParameterToMap(MultipleEntryHashMap parameterMap, String paramName,
                                 String paramValue)
          throws UnsupportedEncodingException, AxisFault {
      try {
          paramValue = URIEncoderDecoder.decode(paramValue);
      } catch (UnsupportedEncodingException e) {
          throw AxisFault.makeFault(e);
      }
      if (paramName.startsWith(WSDL2Constants.TEMPLATE_ENCODE_ESCAPING_CHARACTER)) {
          parameterMap.put(paramName.substring(1), paramValue);
      } else {
          parameterMap.put(paramName, paramValue);
      }
  }
}

