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

package org.apache.synapse.maven.xar;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ServiceLocator implements ClassVisitor {
    private final String serviceClassName;
    private Class<?> serviceClass;
    private List<String> implementations = new LinkedList<String>();

    public ServiceLocator(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public List<String> getImplementations() {
        return implementations;
    }

    public void init(ClassLoader classLoader) throws ClassScannerException {
        try {
            serviceClass = classLoader.loadClass(serviceClassName);
        } catch (ClassNotFoundException e) {
            throw new ClassScannerException("Class " + serviceClassName + " not found");
        }
    }

    public void visit(Class<?> clazz) throws ClassScannerException {
        if (serviceClass.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
            implementations.add(clazz.getName());
        }
    }
}
