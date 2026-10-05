/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

/**
 * Unchecked wrapper for a GENUINE (non-policy) failure to resolve or fetch a nested WSDL 1.1 schema
 * reference from within {@link AccessControlledWSDLLocator} — e.g. a remote fetch transport error or a
 * missing local file that nonetheless resolved safely inside the archive root.
 * <p>
 * {@link javax.wsdl.xml.WSDLLocator#getImportInputSource(String, String)} declares no checked exceptions, so
 * such a failure cannot be thrown as-is. WSDL4J's {@code parseSchema} rethrows a {@link RuntimeException}
 * escaping the locator <em>unwrapped</em> (its exception table does NOT convert it to a
 * {@link javax.wsdl.WSDLException}), so this dedicated type lets {@link WSDL11ProcessorImpl}'s init methods
 * catch precisely this failure — and only this one — and map it to {@code CANNOT_PROCESS_WSDL_CONTENT},
 * without broadly swallowing every {@link RuntimeException}.
 */
class SchemaResolutionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    SchemaResolutionRuntimeException(Throwable cause) {
        super(cause);
    }
}
