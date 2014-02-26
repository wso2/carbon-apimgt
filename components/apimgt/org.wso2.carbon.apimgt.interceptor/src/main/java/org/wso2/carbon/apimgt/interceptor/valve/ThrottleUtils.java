package org.wso2.carbon.apimgt.interceptor.valve;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.interceptor.valve.internal.DataHolder;
import org.wso2.carbon.apimgt.core.APIManagerConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

public class ThrottleUtils {
	
	private static final Log log = LogFactory.getLog(ThrottleUtils.class);
	
	public static OMNode lookup(String key){
        try {
            Resource resource = getResource(key);

            if (resource instanceof Collection || resource == null) {
                return null;
            }

            ByteArrayInputStream inputStream = null;
            Object content = resource.getContent();
            if (content instanceof String) {
                inputStream = new ByteArrayInputStream(content.toString().getBytes());
            } else if (content instanceof byte[]) {
                inputStream = new ByteArrayInputStream((byte[]) content);
            }

            OMNode result = null;
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance().
                        createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                result = builder.getDocumentElement();

            } catch (OMException ignored) {
               // result = readNonXML(resource);

            } catch (XMLStreamException ignored) {
                //result = readNonXML(resource);

            } catch (Exception e) {
                // a more general exception(e.g. a Runtime exception if the XML doc has an
                // external DTD deceleration and if not connected to internet) which in case
                // just log for debugging
                log.error("Error while reading the resource '" + key + "'", e);
            } finally {
                try {
                    resource.discard();
                    if (result != null && result.getParent() != null) {
                        result.detach();
                        OMDocumentImpl parent = new OMDocumentImpl(OMAbstractFactory.getOMFactory());
                        parent.addChild(result);
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error while closing the input stream", e);
                }
            }
            return result;

        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + key, e);
        }

        return null;
    }
	
	 private static void handleException(String string, RegistryException e) {
		// TODO Auto-generated method stub
		
	}

	private static Resource getResource(String path) {

	        RegistryService registryService = DataHolder.getRegistryService();

	        Registry registry = null;
	        try {
	            registry = registryService.getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
	        } catch (RegistryException e) {
	            log.error("Error while fetching Governance Registry of Super Tenant");
	            return null;
	        }

	        String key = resolvePath(path);

	        try {
	            if (registry.resourceExists(key)) {
	                return registry.get(key);
	            }
	        } catch (RegistryException e) {
	            handleException("Error while fetching the resource " + path, e);
	        }
	        return null;
	    }
	 
	 private static String resolvePath(String path) {
	        if (path == null || "".equals(path)) {
	            path = RegistryConstants.ROOT_PATH;
	        }

	        path = path.substring(APIManagerConstants.GOVERNANCE_REGISTRY_PREFIX.length());

	        if (path.startsWith(RegistryConstants.PATH_SEPARATOR)) {
	                path = path.substring(1);
	        }
	        path = RegistryConstants.ROOT_PATH + path;
	        return path;
	    }
}
