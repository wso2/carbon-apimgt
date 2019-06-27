/*
 *
 *  * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.kerberos.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

/**
 * Kerberos delegator.
 */
public class KerberosDelegator {

    private static final Log log = LogFactory.getLog(KerberosDelegator.class);
    private final Oid krb5PrincipalNameType;
    private final Oid spnegoOid;
    private final Subject selfSubject;

    public KerberosDelegator(Subject selfSubject) {
        this.selfSubject = selfSubject;
        this.krb5PrincipalNameType = getOid("1.2.840.113554.1.2.2.1"); // http://oid-info.com/get/1.2.840.113554.1.2.2.1
        this.spnegoOid = getOid("1.3.6.1.5.5.2"); // http://oid-info.com/get/1.3.6.1.5.5.2
    }

    public byte[] delegate(byte[] clientKerberosTicket, String targetSPN) throws Exception {
        GSSCredential delegationCredential = null;
        GSSContext context = null;
        try {
            delegationCredential = getDelegationCredential(clientKerberosTicket);
            context = startAsClient(targetSPN, delegationCredential);
            return context.initSecContext(new byte[0], 0, 0);
        } finally {
            if (context != null) {
                try {
                    context.dispose();
                } catch (GSSException e) {
                    log.error("Error while disposing GSS Context",e);
                }
            }
            if (delegationCredential != null) {
                try {
                    delegationCredential.dispose();
                } catch (GSSException e) {
                    log.error("Error while disposing GSS credential", e);
                    throw e;
                }
            }
        }
    }

    private GSSCredential getDelegationCredential(final byte[] clientKerberosTicket) throws Exception {
        PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() {
            @Override
            public GSSCredential run() throws Exception {
                GSSContext context = null;
                try {
                    GSSManager manager = GSSManager.getInstance();
                    context = manager.createContext((GSSCredential) null);
                    while (!context.isEstablished()) {
                        // ignoring returning response
                        context.acceptSecContext(clientKerberosTicket, 0, clientKerberosTicket.length);
                    }
                    if (context.getCredDelegState()) {
                        return context.getDelegCred();
                    } else {
                        throw new Exception("Credential delegation is not configured properly.");
                    }
                } finally {
                    if (context != null) {
                        try {
                            context.dispose();
                        } catch (GSSException e) {
                            log.error("Error,failed to dispose GSS credential ", e);
                            throw e;

                        }
                    }
                }
            }
        };
        try {
            return Subject.doAs(selfSubject, action);
        } catch (PrivilegedActionException e) {
            throw new Exception("Cannot create GSS credential from client's Kerberos ticket.", e.getException());
        }
    }

    private GSSContext startAsClient(final String targetSPN, final GSSCredential delegationCredential) throws Exception {
        PrivilegedExceptionAction<GSSContext> action = new PrivilegedExceptionAction<GSSContext>() {
            @Override
            public GSSContext run() throws Exception {
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName(targetSPN, krb5PrincipalNameType);
                GSSContext gssContext = manager
                        .createContext(serverName.canonicalize(spnegoOid), spnegoOid, delegationCredential,
                                GSSContext.DEFAULT_LIFETIME);
                //gssContext.requestMutualAuth(true);
                //gssContext.requestCredDeleg(true);
                return gssContext;
            }
        };
        try {
            return Subject.doAs(selfSubject, action);
        } catch (PrivilegedActionException e) {
            throw new Exception(
                    "Cannot create GSS context for '" + targetSPN + "' SPN using '" + delegationCredential + "'.",
                    e.getException());
        }
    }

    /**
     * Returns an Universal Object Identifier (Oid) for the given string.
     *
     * @param oid the dot separated string representation of the oid
     * @return Oid or {@code null} if the passed string is invalid
     */
    private static Oid getOid(String oid) {
        try {
            return new Oid(oid);
        } catch (GSSException e) {
            log.error(e);
            // Won't happen as only valid strings are passed.
            return null;
        }
    }
}
