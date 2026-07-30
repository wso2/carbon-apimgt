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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.utils;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.internal.service.model.RevokedJWTEventData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class BlockConditionDBUtilTest {

    @Test
    public void testGetRevokedJWTEventsWithOwnership() throws Exception {

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement jwtStatement = Mockito.mock(PreparedStatement.class);
        ResultSet jwtResultSet = Mockito.mock(ResultSet.class);
        PreparedStatement consumerKeyStatement = Mockito.mock(PreparedStatement.class);
        ResultSet consumerKeyResultSet = Mockito.mock(ResultSet.class);
        PreparedStatement subjectEntityStatement = Mockito.mock(PreparedStatement.class);
        ResultSet subjectEntityResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.contains("AM_REVOKED_JWT"))).thenReturn(jwtStatement);
        Mockito.when(connection.prepareStatement(Mockito.contains("AM_APP_REVOKED_EVENT")))
                .thenReturn(consumerKeyStatement);
        Mockito.when(connection.prepareStatement(Mockito.contains("AM_SUBJECT_ENTITY_REVOKED_EVENT")))
                .thenReturn(subjectEntityStatement);
        Mockito.when(jwtStatement.executeQuery()).thenReturn(jwtResultSet);
        Mockito.when(consumerKeyStatement.executeQuery()).thenReturn(consumerKeyResultSet);
        Mockito.when(subjectEntityStatement.executeQuery()).thenReturn(subjectEntityResultSet);

        Mockito.when(jwtResultSet.next()).thenReturn(true, false);
        Mockito.when(jwtResultSet.getString("SIGNATURE")).thenReturn("signature");
        Mockito.when(jwtResultSet.getLong("EXPIRY_TIMESTAMP")).thenReturn(1000L);
        Mockito.when(jwtResultSet.getInt("TENANT_ID")).thenReturn(10);

        Mockito.when(consumerKeyResultSet.next()).thenReturn(true, false);
        Mockito.when(consumerKeyResultSet.getString("CONSUMER_KEY")).thenReturn("consumer-key");
        Mockito.when(consumerKeyResultSet.getTimestamp(Mockito.eq("TIME_REVOKED"), Mockito.any(Calendar.class)))
                .thenReturn(new Timestamp(2000L));
        Mockito.when(consumerKeyResultSet.getString("ORGANIZATION")).thenReturn("tenant-a.com");

        Mockito.when(subjectEntityResultSet.next()).thenReturn(true, false);
        Mockito.when(subjectEntityResultSet.getString("ENTITY_ID")).thenReturn("subject");
        Mockito.when(subjectEntityResultSet.getString("ENTITY_TYPE")).thenReturn("USER");
        Mockito.when(subjectEntityResultSet.getTimestamp(Mockito.eq("TIME_REVOKED"), Mockito.any(Calendar.class)))
                .thenReturn(new Timestamp(3000L));
        Mockito.when(subjectEntityResultSet.getString("ORGANIZATION")).thenReturn("tenant-a.com");

        RevokedJWTEventData result = BlockConditionDBUtil.getRevokedJWTEventsWithOwnership(connection);

        assertEquals(1, result.getRevokedJWTList().size());
        assertEquals("signature", result.getRevokedJWTList().get(0).getJwtSignature());
        assertEquals(Long.valueOf(1000L), result.getRevokedJWTList().get(0).getExpiryTime());
        assertEquals(10, result.getRevokedJWTList().get(0).getTenantId());
        assertEquals(1, result.getRevokedJWTConsumerKeyList().size());
        assertEquals("consumer-key", result.getRevokedJWTConsumerKeyList().get(0).getConsumerKey());
        assertEquals(Long.valueOf(2000L), result.getRevokedJWTConsumerKeyList().get(0).getRevocationTime());
        assertEquals("tenant-a.com", result.getRevokedJWTConsumerKeyList().get(0).getOrganization());
        assertEquals(1, result.getRevokedJWTSubjectEntityList().size());
        assertEquals("subject", result.getRevokedJWTSubjectEntityList().get(0).getEntityId());
        assertEquals("USER", result.getRevokedJWTSubjectEntityList().get(0).getEntityType());
        assertEquals(Long.valueOf(3000L), result.getRevokedJWTSubjectEntityList().get(0).getRevocationTime());
        assertEquals("tenant-a.com", result.getRevokedJWTSubjectEntityList().get(0).getOrganization());
    }

    @Test(expected = SQLException.class)
    public void testGetRevokedJWTEventsWithOwnershipPropagatesRetrievalFailure() throws Exception {

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement jwtStatement = Mockito.mock(PreparedStatement.class);
        ResultSet jwtResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.contains("AM_REVOKED_JWT"))).thenReturn(jwtStatement);
        Mockito.when(jwtStatement.executeQuery()).thenReturn(jwtResultSet);
        Mockito.when(jwtResultSet.next()).thenReturn(false);
        Mockito.when(connection.prepareStatement(Mockito.contains("AM_APP_REVOKED_EVENT")))
                .thenThrow(new SQLException("synthetic failure"));

        BlockConditionDBUtil.getRevokedJWTEventsWithOwnership(connection);
    }
}
