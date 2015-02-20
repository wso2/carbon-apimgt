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

package org.apache.synapse.commons.jmx;

import org.wso2.securevault.secret.SecretInformation;

public class JmxInformation {

    private SecretInformation secretInformation;
    
    /** JNDI port used for the local RMI registry. */
    private int jndiPort;

    /** Optional RMI port to avoid usage of dynamic RMI ports which hinder firewall usage. */
    private int rmiPort;

    /** Hostname to be used to bind the RMI registry to. */
    private String hostName;
    
    /** Use authentication? */
    private boolean authenticate;

    /** Location of the JMX remote access file. */
    private String remoteAccessFile;
    
    /** Location of the JMX remote password file. */
    private String remotePasswordFile;
    
    /** Use remote SSL? */
    private boolean remoteSSL;
    
    /**
     * The jmxUrl to connect to.
     */
    private String jmxUrl;
    
    public SecretInformation getSecretInformation() {
        return secretInformation;
    }

    public void setSecretInformation(SecretInformation secretInformation) {
        this.secretInformation = secretInformation;
    }
    
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String host) {
        this.hostName = host;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public int getJndiPort() {
        return jndiPort;
    }

    public void setJndiPort(int jndiPort) {
        this.jndiPort = jndiPort;
    }
    
    public boolean isAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public String getRemoteAccessFile() {
        return remoteAccessFile;
    }

    public void setRemoteAccessFile(String remoteAccessFile) {
        this.remoteAccessFile = remoteAccessFile;
    }

    public String getRemotePasswordFile() {
        return remotePasswordFile;
    }

    public void setRemotePasswordFile(String remotePasswordFile) {
        this.remotePasswordFile = remotePasswordFile;
    }

    public boolean isRemoteSSL() {
        return remoteSSL;
    }

    public void setRemoteSSL(boolean remoteSSL) {
        this.remoteSSL = remoteSSL;
    }
    
    /**
     * Builds the JMX URL depending on the existence of RMI port.
     */
    public void updateJMXUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("service:jmx:rmi://");
        if (rmiPort > 0) {
            sb.append(hostName).append(":").append(rmiPort);
        }
        sb.append("/jndi/rmi://");
        sb.append(hostName).append(":").append(jndiPort);
        sb.append("/synapse");
        jmxUrl = sb.toString();
    }
    
    public String getJmxUrl() {
        return jmxUrl;
    }
}
