/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.hostobjects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;


public class TenantManagerHostObject extends ScriptableObject {
	private static final Log log = LogFactory.getLog(TenantManagerHostObject.class);


    //using a set for file extensions white list since it will be faster to search
    private static final Set<String> EXTENTION_WHITELIST = new HashSet<String>(Arrays.asList(
            new String[]{"css", "jpg", "png", "gif", "svg", "ttf", "html", "js", "json"}
    ));

    public static String getStoreTenantThemesPath() {
        return "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "jaggeryapps"
                + File.separator + "devportal" + File.separator + "site" + File.separator + "public"
                + File.separator + "tenant_themes" + File.separator;
    }

    @Override
    public String getClassName() {
        return "APIManager";
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        return new TenantManagerHostObject();
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static boolean jsFunction_addTenantTheme(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        if ( args == null || args.length != 2) {
            handleException("Invalid input parameters for addTenantTheme");
        }

        FileHostObject uploadFile = null;
        String tenant = null;
        try{
            uploadFile = (FileHostObject) args[0];
            tenant = (String) args[1];
        } catch (ClassCastException ce) {
            handleException("Invalid input parameters for addTenantTheme");
        }

        //extract the zip file to store directory
        return deployTenantTheme(uploadFile, tenant);
    }

    //would be nice to have zip4j
    private static boolean deployTenantTheme(FileHostObject themeFile, String tenant)throws APIManagementException{
        ZipInputStream zis=null;
        byte[] buffer = new byte[1024];
        boolean success = true;

        String outputFolder = TenantManagerHostObject.getStoreTenantThemesPath()+tenant;

        InputStream zipInputStream = null;
        try{
            zipInputStream = themeFile.getInputStream();
        } catch(ScriptException e) {
             handleException("Error occurred while deploying tenant theme file" , e);
        }

        try{

            //create output directory if it is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                if(!folder.mkdirs()){
                    handleException("Unable to create tenant theme directory");
                }
            } else {
                //remove existing files inside the directory
                FileUtils.cleanDirectory(folder);
            }

            //get the zip file content
            zis = new ZipInputStream(zipInputStream);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String ext = null;

            while(ze!=null){

                String fileName = ze.getName();
                APIUtil.validateFileName(fileName);
                File newFile = new File(outputFolder + File.separator + fileName);
                String canonicalizedNewFilePath = newFile.getCanonicalPath();
                String canonicalizedDestinationPath = new File(outputFolder).getCanonicalPath();
                if (!canonicalizedNewFilePath.startsWith(canonicalizedDestinationPath)) {
                    handleException("Attempt to upload invalid zip archive with file at " + fileName + ". File path is " +
                            "outside target directory");
                }

                if (ze.isDirectory()) {
                    if(!newFile.exists()){
                         boolean status = newFile.mkdir();
                         if(status){
                            //todo handle exception
                         }
                    }
                } else {
                    ext = FilenameUtils.getExtension(ze.getName());
                    if (TenantManagerHostObject.EXTENTION_WHITELIST.contains(ext)) {
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                    } else {
                        log.warn("Unsupported file is uploaded with tenant theme by " + tenant + " : file name : "
                                + ze.getName());
                        success = false;
                    }

                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        }catch(IOException ex){
            handleException("Failed to deploy tenant theme",ex);
            //todo remove if the tenant theme directory is created.
        }
        finally {
            IOUtils.closeQuietly(zis);
            IOUtils.closeQuietly(zipInputStream);
        }
        return success;
    }

}
