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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.healthcheck.api.core.HealthChecker;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Registers {@link TMHealthChecker} as a carbon health check {@link HealthChecker} service, its
 * {@link ReceiverThreadPoolMonitor} as a JMX MBean, and the marker event stream its test events are published to.
 * All of these are removed on deactivation, since the component can reactivate without the bundle stopping.
 */
@Component(
        name = "org.wso2.carbon.apimgt.healthcheck.internal.TMHealthCheckerServiceComponent",
        immediate = true)
public class TMHealthCheckerServiceComponent {

    private static final Log log = LogFactory.getLog(TMHealthCheckerServiceComponent.class);

    private ServiceRegistration<?> healthCheckerRegistration;

    /**
     * Catches {@link Throwable} so a failure does not make the framework retry activation, as carbon health check
     * does; the marker stream is registered when the optional {@link EventStreamService} binds instead.
     */
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
            log.error("Failed to register " + TMHealthChecker.CHECKER_NAME, e);
        }
        registerThreadPoolMBean(threadPoolMonitor);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        unregisterHealthChecker();
        unregisterThreadPoolMBean();
        unregisterMarkerStream(TMHealthCheckerDataHolder.getEventStreamService());
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
            log.error("Failed to unregister " + TMHealthChecker.CHECKER_NAME, e);
        } finally {
            healthCheckerRegistration = null;
        }
    }

    /**
     * Registers the marker stream in a super-tenant flow; a failure here never stops the checker itself.
     */
    private void registerMarkerStream() { 

        EventStreamService eventStreamService = TMHealthCheckerDataHolder.getEventStreamService();
        if (eventStreamService == null) {
            log.error("Failed to register the " + TMHealthChecker.MARKER_STREAM_NAME
                    + " event stream: EventStreamService is not available");
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
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
            log.error("Failed to register the " + TMHealthChecker.MARKER_STREAM_NAME + " event stream", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void unregisterMarkerStream(EventStreamService eventStreamService) {

        if (eventStreamService == null) {
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            eventStreamService.removeEventStreamDefinition(TMHealthChecker.MARKER_STREAM_NAME,
                    TMHealthChecker.MARKER_STREAM_VERSION);
        } catch (Throwable e) {
            log.error("Failed to remove the " + TMHealthChecker.MARKER_STREAM_NAME + " event stream", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Replaces any MBean left behind by an earlier activation; a failure here never affects the checker itself.
     */
    private void registerThreadPoolMBean(ReceiverThreadPoolMonitor threadPoolMonitor) {

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(ReceiverThreadPoolMonitor.OBJECT_NAME);
            if (mBeanServer.isRegistered(objectName)) {
                mBeanServer.unregisterMBean(objectName);
            }
            mBeanServer.registerMBean(threadPoolMonitor, objectName);
            if (log.isDebugEnabled()) {
                log.debug("Registered the " + ReceiverThreadPoolMonitor.OBJECT_NAME + " MBean");
            }
        } catch (Throwable e) {
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

    /**
     * Optional, since it is only used for the marker stream and must never take the checker down.
     */
    @Reference(
            name = "tm.healthchecker.event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService service) {

        TMHealthCheckerDataHolder.setEventStreamService(service);
        registerMarkerStream();
    }

    /**
     * Removes the marker stream only if {@code service} is still the one held, since a dynamic reference binds its
     * replacement before unbinding the old one.
     */
    protected void unsetEventStreamService(EventStreamService service) {

        if (TMHealthCheckerDataHolder.getEventStreamService() == service) {
            unregisterMarkerStream(service);
        }
        TMHealthCheckerDataHolder.unsetEventStreamService(service);
    }

    /**
     * Optional: without it the receiver's thread pools are reported as {@code UNKNOWN}.
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

        TMHealthCheckerDataHolder.unsetDataBridgeReceiverService(service);
    }
}
