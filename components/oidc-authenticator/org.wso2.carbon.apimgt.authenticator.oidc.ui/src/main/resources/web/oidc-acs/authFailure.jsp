<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<link href="css/authFailures.css" type="text/css" rel="stylesheet" />
<fmt:bundle basename="org.wso2.carbon.apimgt.authenticator.oidc.ui.i18n.Resources">
    <div id="middle">
        <div id="workArea">
            <p></p>

            <div class="authFailuresMsg">
                <h2><fmt:message key='auth.failure'/></h2>
                <p><fmt:message key='auth.failure.reason'/></p>
            </div>

            <ul class="authFailures">
                <li><p><fmt:message key='auth.failure.reason.1'/></p></li>
                <li><p><fmt:message key='auth.failure.reason.2'/></p></li>
                <li><p><fmt:message key='auth.failure.reason.3'/></p></li>
            </ul>
            <div class="authFailuresTryAgain">Please <a href="../admin/login.jsp">Try Again.</a></div>
        </div>
    </div>
</fmt:bundle>