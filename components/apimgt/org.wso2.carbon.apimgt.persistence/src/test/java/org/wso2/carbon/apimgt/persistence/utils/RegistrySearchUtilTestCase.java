/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.persistence.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

public class RegistrySearchUtilTestCase {
    private Organization organization = new Organization("carbon.super");
    private String[] publisherRoles = { "internal/creator", "internal/publisher", "internal/everyone" };
    private String[] devPortalRoles = { "internal/subscriber", "internal/everyone" };
    private String[] anonymousRoles = { "system/wso2.anonymous.role" };

    @Test
    public void testAdminUserQueryInPublisher() throws APIPersistenceException {
        // Normal publisher api listing
        String inputQuery = "";
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("isAdmin", true);
        UserContext ctx = new UserContext("admin", organization, properties, new String[] { "admin" });

        String searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);

        String expected = "name=*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        Assert.assertEquals("Generated query mismatched. ", expected, searchQuery);
        
        // search for 'test' in description 
        inputQuery = "description:test";
        expected = "description=*test*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);
        Assert.assertEquals("Generated query mismatched for description search. ", expected, searchQuery);
        
        // search for provider 'pubuser'
        inputQuery = "provider:pubuser";
        expected = "provider=*pubuser*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);
        Assert.assertEquals("Generated query mismatched for provider search. ", expected, searchQuery);
        
        // search for propertyname 'test'
        inputQuery = "property_name:test";
        expected =   "api_meta.property_name=*test*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);
        Assert.assertEquals("Generated query mismatched for property search. ", expected, searchQuery);
    }

    @Test
    public void testPublisherUserQueryInPublisher() throws APIPersistenceException {
        // Normal publisher api listing
        String inputQuery = "";
        UserContext ctx = new UserContext("publisher", organization, null, publisherRoles);
        
        String searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);
        String expected = "publisher_roles=(null OR internal\\/creator OR internal\\/publisher OR internal\\/everyone)"
                + "&name=*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        Assert.assertEquals("Generated query mismatched. ", expected, searchQuery);
        
        // search for 'test' in description 
        inputQuery = "description:test";
        expected = "publisher_roles=(null OR internal\\/creator OR internal\\/publisher OR internal\\/everyone)"
                + "&description=*test*&type=(HTTP OR WS OR SOAPTOREST OR GRAPHQL OR SOAP OR SSE OR WEBSUB OR WEBHOOK)";
        searchQuery = RegistrySearchUtil.getPublisherSearchQuery(inputQuery, ctx);
        Assert.assertEquals("Generated query mismatched for description search. ", expected, searchQuery);
    }

    @Test
    public void testAdminUserQueryInDevPortal() throws APIPersistenceException {
        // Normal dev portal api listing
        String inputQuery = "";
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("isAdmin", true);
        UserContext ctx = new UserContext("admin", organization, properties, new String[] { "admin" });

        String searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);

        String expected = "name=*&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";
        Assert.assertEquals("Generated query mismatched. ", expected, searchQuery);
        
        // search for 'test' in description 
        inputQuery = "description:test";
        expected = "description=*test*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for description search. ", expected, searchQuery);
        
        // search for provider 'pubuser'
        inputQuery = "provider:pubuser";
        expected = "provider=*pubuser*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for provider search. ", expected, searchQuery);
    }

    @Test
    public void testAnonymousUserQueryInDevPortal() throws APIPersistenceException {
        // Normal dev portal api listing
        String inputQuery = "";
        UserContext ctx = new UserContext("wso2.anonymous.user", organization, null, anonymousRoles);
        String searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);

        String expected = "store_view_roles=(null OR system\\/wso2.anonymous.role)&name=*&enableStore=(true OR null)"
                + "&lcState=(PUBLISHED OR PROTOTYPED)";
        Assert.assertEquals("Generated query mismatched. ", expected, searchQuery);
        
        // search for 'test' in description 
        inputQuery = "description:test";
        expected = "store_view_roles=(null OR system\\/wso2.anonymous.role)&"
                + "description=*test*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for description search. ", expected, searchQuery);
        
        // search for provider 'pubuser'
        inputQuery = "provider:pubuser";
        expected = "store_view_roles=(null OR system\\/wso2.anonymous.role)&"
                + "provider=*pubuser*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for provider search. ", expected, searchQuery);
        
        // search for propertyname 'test'
        inputQuery = "property_name:test";
        expected =   "store_view_roles=(null OR system\\/wso2.anonymous.role)"
                + "&api_meta.property_name=*test*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for property search. ", expected, searchQuery);
        

    }

    @Test
    public void testDevPortalUserQueryInDevPortal() throws APIPersistenceException {
        // Normal dev portal api listing
        String inputQuery = "";
        UserContext ctx = new UserContext("devUser", organization, null, devPortalRoles);
        String searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);

        String expected = "store_view_roles=(null OR internal\\/subscriber OR internal\\/everyone)&name=*"
                + "&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";
        Assert.assertEquals("Generated query mismatched. ", expected, searchQuery);
        
        // search for 'test' in description 
        inputQuery = "description:test";
        expected = "store_view_roles=(null OR internal\\/subscriber OR internal\\/everyone)&"
                + "description=*test*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for description search. ", expected, searchQuery);
        
        // search for provider 'pubuser'
        inputQuery = "provider:pubuser";
        expected = "store_view_roles=(null OR internal\\/subscriber OR internal\\/everyone)&"
                + "provider=*pubuser*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for provider search. ", expected, searchQuery);
        
        // search for propertyname 'test'
        inputQuery = "property_name:test";
        expected =   "store_view_roles=(null OR internal\\/subscriber OR internal\\/everyone)"
                + "&api_meta.property_name=*test*&lcState=(PUBLISHED OR PROTOTYPED)";
        searchQuery = RegistrySearchUtil.getDevPortalSearchQuery(inputQuery, ctx, false);
        Assert.assertEquals("Generated query mismatched for property search. ", expected, searchQuery);
    }
}
