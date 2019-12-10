/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.sequences.services.SequenceAdmin;

import static org.junit.Assert.fail;

/**
 * Test class for SequenceAdminServiceProxy
 */
public class SequenceAdminServiceProxyTestCase {
    OMElement omElement = Mockito.mock(OMElement.class);

    @Test
    public void testAddSequenceAxisFault() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
        try {
            sequenceAdminServiceProxy.addSequence(omElement);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceProxy.addSequence(omElement);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testDeleteSequenceAxisFault() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
        try {
            sequenceAdminServiceProxy.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceProxy.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testGetSequenceAxisFault() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
        try {
            sequenceAdminServiceProxy.getSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceProxy.getSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testIsExsistingSequenceAxisFault() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
        try {
            sequenceAdminServiceProxy.isExistingSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceProxy.isExistingSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }


    @Test
    public void testAddSequence() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        try {
            sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
            SequenceAdmin sequenceAdmin = Mockito.mock(SequenceAdmin.class);
            Mockito.doNothing().when(sequenceAdmin).addSequence(omElement);
            sequenceAdminServiceProxy.setSequenceAdmin(sequenceAdmin);
        } catch (Exception e) {
            fail("Exception while testing addSequence");
        }
        try {
            sequenceAdminServiceProxy.addSequence(omElement);
        } catch (AxisFault e) {
            fail("AxisFault while testing addSequence");
        }

        try {
            sequenceAdminServiceProxy.addSequence(omElement);
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing addSequenceForTenant");
        }
    }

    @Test
    public void testDeleteSequence() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        try {
            sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
            SequenceAdmin sequenceAdmin = Mockito.mock(SequenceAdmin.class);
            Mockito.doNothing().when(sequenceAdmin).deleteSequence("xyz");
            sequenceAdminServiceProxy.setSequenceAdmin(sequenceAdmin);
        } catch (Exception e) {
            fail("Exception while testing deleteSequence");
        }
        try {
            sequenceAdminServiceProxy.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing deleteSequence");
        }

        try {
            sequenceAdminServiceProxy.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing deleteSequenceForTenant");
        }
    }

    @Test
    public void testGetSequence() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        try {
            sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
            SequenceAdmin sequenceAdmin = Mockito.mock(SequenceAdmin.class);
            OMElement omElement = Mockito.mock(OMElement.class);
            Mockito.when(sequenceAdmin.getSequenceForTenant("xyz", "abc.com")).thenReturn(omElement);
            sequenceAdminServiceProxy.setSequenceAdmin(sequenceAdmin);
        } catch (Exception e) {
            fail("Exception while testing getSequence");
        }
        try {
            Assert.assertNotNull(sequenceAdminServiceProxy.getSequence("xyz"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequence");
        }

    }

    @Test
    public void testIsExsistingSequence() {
        SequenceAdminServiceProxy sequenceAdminServiceProxy = null;
        try {
            sequenceAdminServiceProxy = new SequenceAdminServiceProxy("abc.com");
            SequenceAdmin sequenceAdmin = Mockito.mock(SequenceAdmin.class);
            Mockito.when(sequenceAdmin.isExistingSequence("xyz")).thenReturn(true);
            Mockito.when(sequenceAdmin.isExistingSequenceForTenant("xyz", "abc.com"))
                    .thenReturn(true);
            sequenceAdminServiceProxy.setSequenceAdmin(sequenceAdmin);
        } catch (Exception axisFault) {
            // test for axisFault
        }
        try {
            Assert.assertTrue(sequenceAdminServiceProxy.isExistingSequence("xyz"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequence");
        }

        try {
            Assert.assertTrue(sequenceAdminServiceProxy.isExistingSequence("xyz"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequenceForTenant");
        }
    }
}
