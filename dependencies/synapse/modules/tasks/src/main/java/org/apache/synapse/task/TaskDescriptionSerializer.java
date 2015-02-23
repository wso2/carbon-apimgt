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
package org.apache.synapse.task;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Serialize a TaskDescription into a OMElement
 */
public class TaskDescriptionSerializer {

    private static final Log log = LogFactory.getLog(TaskDescriptionSerializer.class);
    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();

    private static final String NULL_NAMESPACE = "";
    private static final OMNamespace NULL_OMNS
        = omFactory.createOMNamespace(NULL_NAMESPACE, "");

    public static OMElement serializeTaskDescription(OMNamespace targetNamespace,
                                                     TaskDescription taskDescription) {

        if (taskDescription == null) {
            throw new SynapseTaskException("TaskDescription can not be null", log);
        }

        OMElement task = omFactory.createOMElement("task", targetNamespace);
        task.addAttribute("name", taskDescription.getName(), NULL_OMNS);

        String taskClass = taskDescription.getTaskClass();
        if (taskClass != null && !"".equals(taskClass)) {
            task.addAttribute("class", taskDescription.getTaskClass(), NULL_OMNS);
        }

        String group = taskDescription.getGroup();
        if (group != null && !"".equals(group)) {
            task.addAttribute("group", group, NULL_OMNS);
        }

        List<String> pinnedServers = taskDescription.getPinnedServers();
        if (pinnedServers != null && !pinnedServers.isEmpty()) {
            StringBuffer pinnedServersStr = new StringBuffer(pinnedServers.get(0));
            for (int i = 1; i < pinnedServers.size(); i++) {
                pinnedServersStr.append(" ").append(pinnedServers.get(i));
            }
            task.addAttribute(omFactory.createOMAttribute("pinnedServers",
                    NULL_OMNS, pinnedServersStr.toString()));
        }

        if (taskDescription.getDescription() != null) {
            OMElement descElem = omFactory.createOMElement("description", targetNamespace, task);
            descElem.setText(taskDescription.getDescription());
        }

        OMElement el = omFactory.createOMElement("trigger", targetNamespace, task);
        if (taskDescription.getInterval() == 1 && taskDescription.getCount() == 1) {
            el.addAttribute("once", "true", NULL_OMNS);
        } else if (taskDescription.getCron() != null) {
            el.addAttribute("cron", taskDescription.getCron(), NULL_OMNS);
        } else {
            if (taskDescription.getCount() != -1) {
                el.addAttribute("count", Integer.toString(taskDescription.getCount()), NULL_OMNS);
            }

            if (taskDescription.getInterval() != 0) {
                long interval = taskDescription.getInterval() / 1000;
                el.addAttribute("interval", Long.toString(interval), NULL_OMNS);
            }
        }

        for (Object o : taskDescription.getProperties()) {
            OMElement prop = (OMElement) o;
            if (prop != null) {
                prop.setNamespace(targetNamespace);
                task.addChild(prop.cloneOMElement());
            }
        }

        return task;
    }

}
