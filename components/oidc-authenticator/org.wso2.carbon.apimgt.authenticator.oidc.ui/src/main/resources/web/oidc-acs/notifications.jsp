<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->

<%@ page import="org.wso2.carbon.apimgt.authenticator.oidc.ui.common.OIDCConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
    String statusMessage = (String)session.getAttribute(
            OIDCConstants.NOTIFICATIONS_ERROR_MSG);
    session.invalidate();
%>
<style>
.info-box{
background-color:#EEF3F6;
border:1px solid #ABA7A7;
font-size:13px;
font-weight:bold;
margin-bottom:10px;
padding:10px;
}
</style>
<fmt:bundle basename="org.wso2.carbon.apimgt.authenticator.oidc.ui.i18n.Resources">
    <div id="middle">
        <div id="workArea">
            <table class="styledLeft">
            	<tbody>
            		<tr>
                    	<td><fmt:message key='<%= statusMessage %>'/></td>
                	</tr>
            	</tbody>
            </table>
        </div>
    </div>
</fmt:bundle>