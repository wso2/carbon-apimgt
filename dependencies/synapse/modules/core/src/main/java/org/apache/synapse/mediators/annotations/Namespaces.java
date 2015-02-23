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

package org.apache.synapse.mediators.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to declare namespaces available to be used in XPATH expressions
 * 
 * To support setting multiple namespaces with arbitrary prefixes the namespace and 
 * namespace prefix are held in as a single string value seperated by a colon. For
 * example, "ns:http://tempuri.org" represents the namespace uri http://tempuri.org
 * and the namespace prefix "ns", and the complete Namespaces annotation for that
 * example would be @Namespaces({"ns:http://tempuri.org"})
 */
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface Namespaces {

    String[] value();

}
