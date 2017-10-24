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
import org.wso2.carbon.sequences.stub.types.SequenceAdminServiceStub;

import static junit.framework.Assert.fail;

/**
 * Test class for SequenceAdminServiceClient
 */
public class SequenceAdminServiceClientTestCase {
    OMElement omElement = Mockito.mock(OMElement.class);

    @Test
    public void testAddSequenceAxisFault() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            sequenceAdminServiceClient.addSequence(omElement);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceClient.addSequenceForTenant(omElement, "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testDeleteSequenceAxisFault() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            sequenceAdminServiceClient.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceClient.deleteSequenceForTenant("xyz", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testGetSequenceAxisFault() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            sequenceAdminServiceClient.getSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceClient.getSequenceForTenant("xyz", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testIsExsistingSequenceAxisFault() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            sequenceAdminServiceClient.isExistingSequence("xyz");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }

        try {
            sequenceAdminServiceClient.isExistingSequenceForTenant("xyz", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }


    @Test
    public void testAddSequence() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
            SequenceAdminServiceStub sequenceAdminServiceStub = Mockito.mock(SequenceAdminServiceStub.class);
            Mockito.doNothing().when(sequenceAdminServiceStub).addSequence(omElement);
            sequenceAdminServiceClient.setSequenceAdminStub(sequenceAdminServiceStub);
        } catch (Exception e) {
            fail("Exception while testing addSequence");
        }
        try {
            sequenceAdminServiceClient.addSequence(omElement);
        } catch (AxisFault e) {
            fail("AxisFault while testing addSequence");
        }

        try {
            sequenceAdminServiceClient.addSequenceForTenant(omElement, "abc.com");
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing addSequenceForTenant");
        }
    }

    @Test
    public void testDeleteSequence() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
            SequenceAdminServiceStub sequenceAdminServiceStub = Mockito.mock(SequenceAdminServiceStub.class);
            Mockito.doNothing().when(sequenceAdminServiceStub).deleteSequence("xyz");
            sequenceAdminServiceClient.setSequenceAdminStub(sequenceAdminServiceStub);
        } catch (Exception e) {
            fail("Exception while testing deleteSequence");
        }
        try {
            sequenceAdminServiceClient.deleteSequence("xyz");
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing deleteSequence");
        }

        try {
            sequenceAdminServiceClient.deleteSequenceForTenant("xyz", "abc.com");
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing deleteSequenceForTenant");
        }
    }

    @Test
    public void testGetSequence() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
            SequenceAdminServiceStub sequenceAdminServiceStub = Mockito.mock(SequenceAdminServiceStub.class);
            OMElement omElement = Mockito.mock(OMElement.class);
            Mockito.when(sequenceAdminServiceStub.getSequence("xyz")).thenReturn(omElement);
            Mockito.when(sequenceAdminServiceStub.getSequenceForTenant("xyz", "abc.com"))
                    .thenReturn(omElement);
            sequenceAdminServiceClient.setSequenceAdminStub(sequenceAdminServiceStub);
        } catch (Exception e) {
            fail("Exception while testing getSequence");
        }
        try {
            Assert.assertNotNull(sequenceAdminServiceClient.getSequence("xyz"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequence");
        }

        try {
            Assert.assertNotNull(sequenceAdminServiceClient.getSequenceForTenant("xyz",
                    "abc.com"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequenceForTenant");
        }
    }

    @Test
    public void testIsExsistingSequence() {
        SequenceAdminServiceClient sequenceAdminServiceClient = null;
        try {
            sequenceAdminServiceClient = new SequenceAdminServiceClient();
            SequenceAdminServiceStub sequenceAdminServiceStub = Mockito.mock(SequenceAdminServiceStub.class);
            Mockito.when(sequenceAdminServiceStub.isExistingSequence("xyz")).thenReturn(true);
            Mockito.when(sequenceAdminServiceStub.isExistingSequenceForTenant("xyz", "abc.com"))
                    .thenReturn(true);
            sequenceAdminServiceClient.setSequenceAdminStub(sequenceAdminServiceStub);
        } catch (Exception axisFault) {
            // test for axisFault
        }
        try {
            Assert.assertTrue(sequenceAdminServiceClient.isExistingSequence("xyz"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequence");
        }

        try {
            Assert.assertTrue(sequenceAdminServiceClient.isExistingSequenceForTenant("xyz", "abc.com"));
        } catch (AxisFault axisFault) {
            fail("AxisFault while testing getSequenceForTenant");
        }
    }
}
