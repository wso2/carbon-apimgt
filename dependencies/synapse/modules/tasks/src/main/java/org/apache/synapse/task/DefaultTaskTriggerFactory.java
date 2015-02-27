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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.Random;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Default TriggerFactory ship with synapse utils
 */
public class DefaultTaskTriggerFactory implements TaskTriggerFactory {

    private static final Log log = LogFactory.getLog(DefaultTaskTriggerFactory.class);

    private final static Random RANDOM = new Random();

    /**
     * @see TaskTriggerFactory
     */
    public Trigger createTrigger(TaskDescription taskDescription) {

        String name = taskDescription.getName();
        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Name of the Task cannot be null", log);
        }

        String cron = taskDescription.getCron();
        int repeatCount = taskDescription.getCount();
        long repeatInterval = taskDescription.getInterval();
        Date startTime = taskDescription.getStartTime();
        Date endTime = taskDescription.getEndTime();
        String group = taskDescription.getGroup();

        if (group == null || "".equals(group)) {
            group = TaskDescription.DEFAULT_GROUP;
        }

        Trigger trigger;

        TriggerBuilder<Trigger> triggerBuilder = newTrigger()
                .withIdentity(name + "-trigger-" + String.valueOf(RANDOM.nextLong()), group);

        if (startTime != null) {
            triggerBuilder.startAt(startTime);
        } else {
            triggerBuilder.startNow();
        }

        if (endTime != null) {
            triggerBuilder.endAt(endTime);
        }
        
        if (cron == null || "".equals(cron)) {
            if (repeatCount >= 0) {
                trigger = triggerBuilder.withSchedule(simpleSchedule()
                .withIntervalInMilliseconds(repeatInterval)
                .withRepeatCount(repeatCount - 1))
                .build();
            } else {
                trigger = triggerBuilder.withSchedule(simpleSchedule()
                .withIntervalInMilliseconds(repeatInterval)
                .repeatForever())
                .build();
            }

        } else {
            trigger = triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        }

        if (trigger == null) {
            throw new SynapseTaskException("Trigger is null for the Task description : " +
                    taskDescription, log);
        }

        return trigger;
    }
}
