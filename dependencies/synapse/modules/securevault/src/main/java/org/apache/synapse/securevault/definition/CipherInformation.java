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
package org.apache.synapse.securevault.definition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.CipherOperationMode;
import org.apache.synapse.securevault.EncodingType;

/**
 * Encapsulates the cipher related information
 */
public class CipherInformation {

    /**
     * Default cipher algorithm
     */
    public static final String DEFAULT_ALGORITHM = "RSA";

    private static final Log log = LogFactory.getLog(CipherInformation.class);

    /* Cipher algorithm */
    private String algorithm = DEFAULT_ALGORITHM;

    /* Cipher operation mode - ENCRYPT or DECRYPT */
    private CipherOperationMode cipherOperationMode;

    /* Mode of operation - ECB,CCB,etc*/
    private String mode;

    /* Type of the input to the cipher */
    private EncodingType inType;

    /* Type of the output from the cipher*/
    private EncodingType outType;

    /* Ciphering type - asymmetric , symmetric*/
    private String type;

    private String provider;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        if (algorithm == null || "".equals(algorithm)) {
            log.info("Given algorithm is null, using a default one : RSA");
        }
        this.algorithm = algorithm;
    }

    public CipherOperationMode getCipherOperationMode() {
        return cipherOperationMode;
    }

    public void setCipherOperationMode(CipherOperationMode operationMode) {
        this.cipherOperationMode = operationMode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EncodingType getInType() {
        return inType;
    }

    public void setInType(EncodingType inType) {
        this.inType = inType;
    }

    public EncodingType getOutType() {
        return outType;
    }

    public void setOutType(EncodingType outType) {
        this.outType = outType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
