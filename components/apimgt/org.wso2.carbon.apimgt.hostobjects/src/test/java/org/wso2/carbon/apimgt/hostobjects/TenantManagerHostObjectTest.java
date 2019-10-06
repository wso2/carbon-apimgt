package org.wso2.carbon.apimgt.hostobjects;

import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TenantManagerHostObject.class})
public class TenantManagerHostObjectTest {
    private TenantManagerHostObject tmhostObject = new TenantManagerHostObject();

    @Test
    public void testGetStoreTenantThemesPath() throws Exception {
        Assert.assertEquals(TenantManagerHostObject.getStoreTenantThemesPath(), "repository"+File.separator
                +"deployment" +File.separator+"server"+File.separator+"jaggeryapps"+File.separator+"devportal"
                +File.separator+"site" +File.separator+"public"+File.separator+ "tenant_themes"+File.separator+"");
    }

    @Test
    public void testGetClassName() throws Exception {
        Assert.assertEquals(tmhostObject.getClassName(), "APIManager");
    }

    @Test
    public void testJsFunction_addTenantTheme() throws Exception {
        FileHostObject fileHostObject = Mockito.mock(FileHostObject.class);
        FileInputStream inputStream = Mockito.mock(FileInputStream.class);
        ZipInputStream zipInputStream = Mockito.mock(ZipInputStream.class);
        Object args[] = {fileHostObject, "b"};
        Mockito.when(fileHostObject.getInputStream()).thenReturn(inputStream);
        PowerMockito.whenNew(ZipInputStream.class).withAnyArguments().thenReturn(zipInputStream);

        Assert.assertTrue(tmhostObject.jsFunction_addTenantTheme(null, null, args, null));
    }

    @Test
    public void testJsFunction_addTenantThemeForClassCasting() throws Exception {
        Object args[] = {"test", "b"};
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, args, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }

    @Test
    public void testJsFunction_addTenantThemeWhenArgsAreNull() throws Exception {
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, null, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }

    @Test
    public void testJsFunction_addTenantThemeWhenArgsNotNull() throws Exception {
        Object args[] = {"test"};
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, args, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }
}