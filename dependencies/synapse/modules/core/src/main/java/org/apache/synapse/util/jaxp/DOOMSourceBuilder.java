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

package org.apache.synapse.util.jaxp;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.ElementHelper;
import org.w3c.dom.Element;

/**
 * {@link SourceBuilder} implementation that transforms the AXIOM tree
 * to DOOM and produces a {@link DOMSource}.
 */
public class DOOMSourceBuilder implements SourceBuilder {
    public Source getSource(OMElement node) {
        return new DOMSource(
                ((Element) ElementHelper.importOMElement(node,
                DOOMAbstractFactory.getOMFactory())).getOwnerDocument());
    }

    public void release() {
    }
}
