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
package org.apache.synapse.libraries.imports;

import org.apache.synapse.libraries.util.LibDeployerUtils;

public class SynapseImport {

    private String importedLibName;
    private String importedLibPackage;
    private String fileName;
    private boolean status = false;

    public String getLibName() {
	return importedLibName;
    }

    public void setLibName(String name) {
	this.importedLibName = name;
    }

    public String getLibPackage() {
	return importedLibPackage;
    }

    public void setLibPackage(String version) {
	this.importedLibPackage = version;
    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getName() {
	return LibDeployerUtils.getQualifiedName(this);
    }

    public boolean isStatus() {
	return status;
    }

    public void setStatus(boolean status) {
	this.status = status;
    }

}
