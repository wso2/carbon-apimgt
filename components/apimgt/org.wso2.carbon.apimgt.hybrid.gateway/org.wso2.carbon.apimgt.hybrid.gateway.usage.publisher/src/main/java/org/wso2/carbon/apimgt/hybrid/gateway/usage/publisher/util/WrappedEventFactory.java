/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util;

import com.lmax.disruptor.EventFactory;
import org.wso2.carbon.databridge.commons.Event;

/**
 * WrappedEventFactory
 */
public class WrappedEventFactory implements EventFactory<WrappedEventFactory.WrappedEvent> {

    public WrappedEvent newInstance() {
        return new WrappedEvent();
    }

    /**
     * WrappedEvent
     */
    public static class WrappedEvent {

        private Event event;

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return "WrappedEvent{" +
                    "event=" + event +
                    "}";
        }
    }

}
