/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.owasp.encoder.Encode;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class EncodeHostObject extends ScriptableObject {
	private static final Log log = LogFactory.getLog(EncodeHostObject.class);

    @Override
    public String getClassName() {
        return "Encode";
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        EncodeHostObject Obj = new EncodeHostObject();
        return Obj;
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static String validateInput(Object[] args) throws APIManagementException{
        if ( args == null || args.length != 1 ) {
            handleException("Invalid input parameters to the encode method");
        }
        return String.valueOf(args[0]);
    }

    public static String jsFunction_forCDATA(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        return Encode.forCDATA(validateInput(args));
    }

    public static String jsFunction_forCssString(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        return Encode.forCssString(validateInput(args));
    }

    public static String jsFunction_forCssUrl(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        return Encode.forCssUrl(validateInput(args));
    }

    public static String jsFunction_forHtml(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forHtml(validateInput(args));
    }

    public static String jsFunction_forHtmlAttribute(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forHtmlAttribute(validateInput(args));
    }

    public static String jsFunction_forHtmlContent(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forHtmlContent(validateInput(args));
    }

    public static String jsFunction_forHtmlUnquotedAttribute(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forHtmlUnquotedAttribute(validateInput(args));
    }

    public static String jsFunction_forJava(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forJava(validateInput(args));
    }

    public static String jsFunction_forJavaScript(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forJavaScript(validateInput(args));
    }

    public static String jsFunction_forJavaScriptAttribute(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forJavaScriptAttribute(validateInput(args));
    }

    public static String jsFunction_forJavaScriptBlock(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forJavaScriptBlock(validateInput(args));
    }

    public static String jsFunction_forJavaScriptSource(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forJavaScriptSource(validateInput(args));
    }

    public static String jsFunction_forUri(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forUri(validateInput(args));
    }

    public static String jsFunction_forUriComponent(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forUriComponent(validateInput(args));
    }

    public static String jsFunction_forXml(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forXml(validateInput(args));
    }

    public static String jsFunction_forXmlAttribute(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forXmlAttribute(validateInput(args));
    }

    public static String jsFunction_forXmlComment(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forXmlComment(validateInput(args));
    }

    public static String jsFunction_forXmlContent(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException{
        return Encode.forXmlContent(validateInput(args)) ;
    }

}
