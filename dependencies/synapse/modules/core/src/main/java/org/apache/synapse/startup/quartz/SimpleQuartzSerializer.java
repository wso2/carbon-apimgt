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

package org.apache.synapse.startup.quartz;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskDescriptionSerializer;
import org.apache.synapse.config.xml.StartupSerializer;

public class SimpleQuartzSerializer implements StartupSerializer {

    public OMElement serializeStartup(OMElement parent, Startup s) {

        if (!(s instanceof SimpleQuartz)) {
            throw new SynapseException("called TaskSerializer on some other " +
                    "kind of startup" + s.getClass().getName());
        }

        SimpleQuartz sq = (SimpleQuartz) s;
        
        TaskDescription taskDescription = sq.getTaskDescription();

        if (taskDescription != null) {
            OMElement task = TaskDescriptionSerializer.serializeTaskDescription(
                    SynapseConstants.SYNAPSE_OMNAMESPACE, taskDescription);
            if (task == null) {
                throw new SynapseException("Task Element can not be null.");
            }
            if (parent != null) {
                parent.addChild(task);
            }
            return task;
        } else {
            throw new SynapseException("Task Description is null");
        }
    }

}
