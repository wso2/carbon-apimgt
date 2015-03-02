/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.format.hessian;

/**
 * Constants related to Hessian message builder and formatter pair
 */
public final class HessianConstants {

    /** Hessian element local name inside the SOAP payload */
    public static final String HESSIAN_ELEMENT_LOCAL_NAME = "hessianDataSource";

    /** Hessian element namespace inside the SOAP payload */
    public static final String HESSIAN_NAMESPACE_URI = "http://ws.apache.org/ns/axis2/hessian";

    /** Hessian namespace prefix to be used */
    public static final String HESSIAN_NS_PREFIX = "hs";

    /** DataSource name for HessianDataSource */
    public static final String HESSIAN_DATA_SOURCE_NAME = "HessianDataSource";

    /** Hessian content type */
    public static final String HESSIAN_CONTENT_TYPE = "x-application/hessian";
    
    /** Hessian fault marker for protocol version 1.0 */
    public static final char HESSIAN_V1_FAULT_IDENTIFIER = 'f';
    
    /** Hessian fault marker for protocol version 2.0 */
    public static final char HESSIAN_V2_FAULT_IDENTIFIER = 'F';
}
