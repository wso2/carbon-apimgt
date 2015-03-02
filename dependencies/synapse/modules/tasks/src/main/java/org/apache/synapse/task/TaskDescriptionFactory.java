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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.PropertyHelper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Create TaskDescription based on OMElement
 */
public class TaskDescriptionFactory {

    private static final Log log = LogFactory.getLog(TaskDescriptionFactory.class);
    private static final String NULL_NAMESPACE = "";
    private final static String TASK = "task";
    private final static String TRIGGER = "trigger";
    private final static String PROPERTY = "property";
    private static final String DESCRIPTION = "description";

    public static TaskDescription createTaskDescription(OMElement el, OMNamespace tagetNamespace) {

        if (log.isDebugEnabled()) {
            log.debug("Creating SimpleQuartz Task");
        }
        QName task = createQName(TASK, tagetNamespace);
        if (task.equals(el.getQName())) {

            TaskDescription taskDescription = new TaskDescription();

            String name = el.getAttributeValue(
                    new QName(NULL_NAMESPACE, "name"));
            if (name != null) {
                taskDescription.setName(name);
            } else {
                handleException("Name for a task is required, missing name in the task");
            }

            String group = el.getAttributeValue(
                    new QName(NULL_NAMESPACE, "group"));
            if (group != null) {
                taskDescription.setGroup(group);
            }

            // set the task class
            OMAttribute classAttr = el.getAttribute(new QName("class"));
            if (classAttr != null && classAttr.getAttributeValue() != null) {
                String classname = classAttr.getAttributeValue();
                try {
                    Class.forName(classname).newInstance();
                } catch (Exception e) {
                    handleException("Failed to load task class " + classname, e);
                }
                taskDescription.setTaskClass(classname);
            } else {
                log.warn("TaskClass cannot be found." +
                        "Task implementation may need a task class if there is no default one");
            }
            
            OMElement descElem = el.getFirstChildWithName(createQName(DESCRIPTION, tagetNamespace));
            if (descElem != null) {
                taskDescription.setDescription(descElem.getText());
            }

            // set pinned server list
            OMAttribute pinnedServers = el.getAttribute(new QName(NULL_NAMESPACE, "pinnedServers"));
            if (pinnedServers != null) {
                String pinnedServersValue = pinnedServers.getAttributeValue();
                if (pinnedServersValue == null) {
                    // default to all servers
                } else {
                    StringTokenizer st = new StringTokenizer(pinnedServersValue, " ,");
                    List<String> pinnedServersList = new ArrayList<String>();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.length() != 0) {
                            pinnedServersList.add(token);
                        }
                    }
                    taskDescription.setPinnedServers(pinnedServersList);
                }
            }

            // next sort out the property children

            Iterator it = el.getChildrenWithName(createQName(PROPERTY, tagetNamespace));
            while (it.hasNext()) {
                OMElement prop = (OMElement) it.next();
                if (PropertyHelper.isStaticProperty(prop)) {
                    taskDescription.addProperty(prop);
                } else {
                    handleException("Tasks does not support dynamic properties");
                }
            }

            // setting the trigger to the task
            OMElement trigger = el.getFirstChildWithName(createQName(TRIGGER, tagetNamespace));
            if (trigger != null) {

                OMAttribute count = trigger.getAttribute(new QName("count"));
                if (count != null) {
                    try {
                        taskDescription.setCount(Integer.parseInt(count.getAttributeValue()));
                    } catch (Exception e) {
                        handleException("Failed to parse trigger count as an integer", e);
                    }
                }

                OMAttribute once = trigger.getAttribute(new QName("once"));
                if (once != null && Boolean.TRUE.toString().equals(once.getAttributeValue())) {
                    taskDescription.setCount(1);
                    taskDescription.setInterval(1);
                }

                OMAttribute repeatInterval = trigger.getAttribute(new QName("interval"));
                if (repeatInterval == null && taskDescription.getCount() > 1) {
                    handleException("Trigger seems to be " +
                            "a simple trigger, but no interval specified");
                } else if (repeatInterval != null && repeatInterval.getAttributeValue() != null) {
                    try {
                        long repeatIntervalInSeconds = Long.parseLong(
                                repeatInterval.getAttributeValue());
                        long repeatIntervalInMillis = repeatIntervalInSeconds * 1000;
                        taskDescription.setInterval(repeatIntervalInMillis);
                    } catch (Exception e) {
                        handleException("Failed to parse trigger interval as a long value", e);
                    }
                }

                OMAttribute expr = trigger.getAttribute(new QName("cron"));
                if (expr == null && taskDescription.getInterval() == 0) {
                    taskDescription.setCount(1);
                    taskDescription.setInterval(1);
                } else if (expr != null && taskDescription.getInterval() > 0) {
                    handleException("Trigger syntax error : " +
                            "both cron and simple trigger attributes are present");
                } else if (expr != null && expr.getAttributeValue() != null) {
                    taskDescription.setCron(expr.getAttributeValue());
                }

            } else {
                taskDescription.setCount(1);
                taskDescription.setInterval(1);
            }

            return taskDescription;
        } else {
            handleException("Syntax error in the task : wrong QName for the task");
            return null;
        }
    }

    private static QName createQName(String localName, OMNamespace omNamespace) {
        return new QName(omNamespace.getNamespaceURI(), localName, omNamespace.getPrefix());
    }

    private static void handleException(String message, Exception e) {
        log.error(message);
        throw new SynapseTaskException(message, e);
    }

    private static void handleException(String message) {
        log.error(message);
        throw new SynapseTaskException(message);
    }

}
