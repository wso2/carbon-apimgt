/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.internal;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIManagerComponent.class, ServiceReferenceHolder.class, FileUtil.class })
public class APIManagerComponentAddRxtConfigsTest {
    @Rule
    private TemporaryFolder folder = new TemporaryFolder();
    private ServiceReferenceHolder serviceReferenceHolder;
    private RegistryService registryService;
    private UserRegistry userRegistry;
    private Resource resource;
    private ComponentContext componentContext;

    @Before
    public void setupClass() {
        System.setProperty("carbon.home", "");
    }

    @Before
    public void setup() throws IOException {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(FileUtil.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        componentContext = Mockito.mock(ComponentContext.class);
        registryService = Mockito.mock(RegistryService.class);
        userRegistry = Mockito.mock(UserRegistry.class);
        resource = Mockito.mock(Resource.class);

        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    @Test
    public void testShouldAddRxtConfigs() throws Exception {
        folder.newFile("abc.rxt");
        folder.newFile("abcd.rxt");
        Mockito.when(registryService.getRegistry(Mockito.anyString())).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(Mockito.anyString())).thenReturn(true).thenReturn(false);
        Mockito.when(userRegistry.newResource()).thenReturn(resource);
        Mockito.when(userRegistry.put(Mockito.anyString(), Mockito.any(Resource.class)))
                .thenThrow(IndexOutOfBoundsException.class);
        PowerMockito.when(FileUtil.readFileToString(Mockito.anyString())).thenReturn("TEXT");
        PowerMockito.whenNew(File.class).withArguments(Mockito.anyString()).thenReturn(folder.getRoot());
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            // Intentionally throwing an exception after the method to stop further execution of activate();
            apiManagerComponent.activate(componentContext);
            Assert.fail("IndexOutOfBoundsException is expected");
        } catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void testShouldThrowExceptionWhenFailToGetRegistry() throws Exception {
        folder.newFile("abc.rxt");
        Mockito.when(registryService.getRegistry(Mockito.anyString())).thenThrow(RegistryException.class);
        PowerMockito.whenNew(File.class).withArguments(Mockito.anyString()).thenReturn(folder.getRoot());
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            // Test should complete silently
            apiManagerComponent.activate(componentContext);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail("IndexOutOfBoundsException is not expected here");
        } catch (Exception e) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void testShouldThrowExceptionWhenFailToSaveResource() throws Exception {
        folder.newFile("abc.rxt");
        Mockito.when(registryService.getRegistry(Mockito.anyString())).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(Mockito.anyString())).thenReturn(false);
        Mockito.when(userRegistry.newResource()).thenThrow(RegistryException.class);
        PowerMockito.when(FileUtil.readFileToString(Mockito.anyString())).thenThrow(IOException.class).thenReturn("A");
        PowerMockito.whenNew(File.class).withArguments(Mockito.anyString()).thenReturn(folder.getRoot());
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        // IOException
        try {
            // Test should complete silently
            apiManagerComponent.activate(componentContext);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail("IndexOutOfBoundsException is not expected here");
        } catch (Exception e) {
            Assert.fail("Unexpected exception was thrown");
        }

        // Registry exception
        try {
            // Test should complete silently
            apiManagerComponent.activate(componentContext);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail("IndexOutOfBoundsException is not expected here");
        } catch (Exception e) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @AfterClass
    public static void destroyClass() {
        System.clearProperty("carbon.home");
    }
}
