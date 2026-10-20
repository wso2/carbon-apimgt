/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.healthcheck.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.healthcheck.probe.ReceiverThreadPoolMonitor;
import org.wso2.carbon.apimgt.healthcheck.probe.TMHealthChecker;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.healthcheck.api.core.HealthChecker;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Connects the Traffic Manager health checker to carbon health check, the same way carbon health check's own
 * {@code HealthMonitorServiceComponent} connects {@code DataSourceHealthChecker}: on activation it registers a new
 * checker instance as a {@link HealthChecker} OSGi service, and unregisters it on deactivation - this component
 * can deactivate and reactivate without the bundle itself stopping (e.g. if a mandatory reference is temporarily
 * unavailable), so skipping the unregister would leave a stale registration alongside the new one. Carbon health
 * check finds the registered checker, calls {@code init()} with its {@code <HealthChecker>} configuration, and
 * calls {@code checkHealth()} on every {@code GET /health} request.
 *
 * <p>It also owns the lifecycle of the marker event stream the checker's test event is published to: registered
 * here on activation and removed on deactivation, instead of a static {@code eventstreams} resource file, so the
 * stream only exists for as long as this bundle is actually running.</p>
 *
 * <p>It also registers the checker's {@link ReceiverThreadPoolMonitor} as a JMX MBean
 * ({@value ReceiverThreadPoolMonitor#OBJECT_NAME}), so the receiver's remaining threads can be read with any JMX
 * client as well as through {@code GET /health}; it is removed on deactivation.</p>
 */
@Component(
        name = "org.wso2.carbon.apimgt.healthcheck.internal.TMHealthCheckerServiceComponent",
        immediate = true)
public class TMHealthCheckerServiceComponent {

    private static final Log log = LogFactory.getLog(TMHealthCheckerServiceComponent.class);

    private ServiceRegistration<?> healthCheckerRegistration;

    @Activate
    protected void activate(ComponentContext ctxt) {

        ReceiverThreadPoolMonitor threadPoolMonitor =
                new ReceiverThreadPoolMonitor(TMHealthCheckerDataHolder::getReceiverThreadPoolSize);
        try {
            healthCheckerRegistration = ctxt.getBundleContext().registerService(HealthChecker.class.getName(),
                    new TMHealthChecker(threadPoolMonitor), null);
            if (log.isDebugEnabled()) {
                log.debug("Registered " + TMHealthChecker.CHECKER_NAME + " as a HealthChecker");
            }
        } catch (Throwable e) {
            // Catching throwable to avoid retrying to initiate component, as carbon health check does.
            log.error("Failed to register " + TMHealthChecker.CHECKER_NAME, e);
        }
        registerMarkerStream();
        registerThreadPoolMBean(threadPoolMonitor);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        unregisterHealthChecker();
        unregisterThreadPoolMBean();
        unregisterMarkerStream();
        if (log.isDebugEnabled()) {
            log.debug(TMHealthChecker.CHECKER_NAME + " component is deactivated");
        }
    }

    private void unregisterHealthChecker() {

        if (healthCheckerRegistration == null) {
            return;
        }
        try {
            healthCheckerRegistration.unregister();
        } catch (Throwable e) {
            // Already unregistered, e.g. because the bundle itself is stopping.
            log.debug("Failed to unregister " + TMHealthChecker.CHECKER_NAME, e);
        } finally {
            healthCheckerRegistration = null;
        }
    }

    private void registerMarkerStream() {

        EventStreamService eventStreamService = TMHealthCheckerDataHolder.getEventStreamService();
        if (eventStreamService == null) {
            log.error("Failed to register the " + TMHealthChecker.MARKER_STREAM_NAME
                    + " event stream: EventStreamService is not available");
            return;
        }
        try {
            StreamDefinition streamDefinition =
                    new StreamDefinition(TMHealthChecker.MARKER_STREAM_NAME, TMHealthChecker.MARKER_STREAM_VERSION);
            streamDefinition.setDescription("Test events sent by the Traffic Manager throttle receiver health "
                    + "check. Nothing reads this stream.");
            streamDefinition.addPayloadData("probeTime", AttributeType.LONG);
            eventStreamService.addEventStreamDefinition(streamDefinition);
            if (log.isDebugEnabled()) {
                log.debug("Registered the " + TMHealthChecker.MARKER_STREAM_NAME + " event stream");
            }
        } catch (Throwable e) {
            // Catching throwable so a stream registration failure cannot stop the checker itself from registering.
            log.error("Failed to register the " + TMHealthChecker.MARKER_STREAM_NAME + " event stream", e);
        }
    }

    private void unregisterMarkerStream() {

        EventStreamService eventStreamService = TMHealthCheckerDataHolder.getEventStreamService();
        if (eventStreamService == null) {
            return;
        }
        try {
            eventStreamService.removeEventStreamDefinition(TMHealthChecker.MARKER_STREAM_NAME,
                    TMHealthChecker.MARKER_STREAM_VERSION);
        } catch (Throwable e) {
            log.error("Failed to remove the " + TMHealthChecker.MARKER_STREAM_NAME + " event stream", e);
        }
    }

    private void registerThreadPoolMBean(ReceiverThreadPoolMonitor threadPoolMonitor) {

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(ReceiverThreadPoolMonitor.OBJECT_NAME);
            if (mBeanServer.isRegistered(objectName)) {
                // Left behind by an earlier activation of this bundle that did not deactivate cleanly.
                mBeanServer.unregisterMBean(objectName);
            }
            mBeanServer.registerMBean(threadPoolMonitor, objectName);
            if (log.isDebugEnabled()) {
                log.debug("Registered the " + ReceiverThreadPoolMonitor.OBJECT_NAME + " MBean");
            }
        } catch (Throwable e) {
            // Catching throwable so an MBean failure cannot affect the health checker itself.
            log.error("Failed to register the " + ReceiverThreadPoolMonitor.OBJECT_NAME + " MBean", e);
        }
    }

    private void unregisterThreadPoolMBean() {

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(ReceiverThreadPoolMonitor.OBJECT_NAME);
            if (mBeanServer.isRegistered(objectName)) {
                mBeanServer.unregisterMBean(objectName);
            }
        } catch (Throwable e) {
            log.error("Failed to remove the " + ReceiverThreadPoolMonitor.OBJECT_NAME + " MBean", e);
        }
    }

    @Reference(
            name = "tm.healthchecker.api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {

        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(service);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {

        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(null);
    }

    @Reference(
            name = "tm.healthchecker.event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService service) {

        TMHealthCheckerDataHolder.setEventStreamService(service);
    }

    protected void unsetEventStreamService(EventStreamService service) {

        TMHealthCheckerDataHolder.setEventStreamService(null);
    }

    /**
     * Optional: without it the checker still runs, and only reports the receiver's thread pools as
     * {@code UNKNOWN}.
     */
    @Reference(
            name = "tm.healthchecker.databridge.receiver.service",
            service = DataBridgeReceiverService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataBridgeReceiverService")
    protected void setDataBridgeReceiverService(DataBridgeReceiverService service) {

        TMHealthCheckerDataHolder.setDataBridgeReceiverService(service);
    }

    protected void unsetDataBridgeReceiverService(DataBridgeReceiverService service) {

        TMHealthCheckerDataHolder.setDataBridgeReceiverService(null);
    }
}
