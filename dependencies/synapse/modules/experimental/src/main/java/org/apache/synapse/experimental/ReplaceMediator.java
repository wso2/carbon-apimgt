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

package org.apache.synapse.experimental;

import org.apache.axiom.om.OMNode;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SourceXPathSupport;
import org.apache.synapse.util.xpath.SynapseXPath;

public class ReplaceMediator extends AbstractMediator {
    private final SourceXPathSupport target = new SourceXPathSupport();
    private String property;

    public boolean mediate(MessageContext synCtx) {
        SynapseLog synLog = getLog(synCtx);
        OMNode replacement = (OMNode)synCtx.getProperty(property);
        OMNode node = target.selectOMNode(synCtx, synLog);
        node.insertSiblingAfter(replacement);
        node.detach();
        synCtx.setProperty(property, null);
        return true;
    }
    
    public SynapseXPath getTarget() {
        return target.getXPath();
    }

    public void setTarget(SynapseXPath source) {
        this.target.setXPath(source);
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
