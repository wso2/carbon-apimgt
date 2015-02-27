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
package org.apache.synapse.transport.nhttp.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.transport.nhttp.HttpCoreNIOMultiSSLListener;
import org.apache.synapse.transport.nhttp.config.ServerConnFactoryBuilder;

public class MultiSSLProfileReloader implements MultiSSLProfileReloaderMBean {

    HttpCoreNIOMultiSSLListener httpCoreNIOMultiSSLListener;
    TransportInDescription transportInDescription;

    public MultiSSLProfileReloader(HttpCoreNIOMultiSSLListener multiSSLListener, TransportInDescription inDescription) {
        this.httpCoreNIOMultiSSLListener = multiSSLListener;
        this.transportInDescription = inDescription;
        MBeanRegistrar.getInstance().registerMBean(this, "MultiSSLProfileReload", "reload");
    }

    public String reloadSSLProfileConfig() throws AxisFault {
        Parameter oldParameter = transportInDescription.getParameter("SSLProfiles");
        Parameter profilePathParam = transportInDescription.getParameter("SSLProfilesConfigPath");
        if(oldParameter!=null && profilePathParam!=null) {
            transportInDescription.removeParameter(oldParameter);
            ServerConnFactoryBuilder builder = new ServerConnFactoryBuilder(transportInDescription, null);
            TransportInDescription loadedTransportIn = builder.loadMultiProfileSSLConfig();
            if (loadedTransportIn != null) {
                transportInDescription=loadedTransportIn;
                httpCoreNIOMultiSSLListener.reload(transportInDescription);
                return "SSLProfiles reloaded Successfully";
            }
            //add old value back
            transportInDescription.addParameter(oldParameter);
        }
        return "Failed to reload SSLProfiles";
    }
}
