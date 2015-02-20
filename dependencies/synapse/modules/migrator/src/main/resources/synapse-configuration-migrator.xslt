<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  ~  Licensed to the Apache Software Foundation (ASF) under one
  ~  or more contributor license agreements.  See the NOTICE file
  ~  distributed with this work for additional information
  ~  regarding copyright ownership.  The ASF licenses this file
  ~  to you under the Apache License, Version 2.0 (the
  ~  "License"); you may not use this file except in compliance
  ~  with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  -->

<!--
This is the synapse migration xslt which will migrate the configuration from the 1.x
version to the 2.x compatible version
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:syn="http://ws.apache.org/ns/synapse"
                xmlns:spring="http://ws.apache.org/ns/synapse/spring"
                xmlns:synNew="http://ws.apache.org/ns/synapse"
                exclude-result-prefixes="syn">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="syn:* | spring:*" priority="0">
        <xsl:call-template name="convertNS"/>
    </xsl:template>

    <xsl:template match="syn:filter | synNew:filter" priority="0">
        <xsl:element name="{local-name()}" namespace="http://ws.apache.org/ns/synapse">
            <xsl:copy-of select="@*"/>
            <xsl:choose>
                <xsl:when test="local-name(child::*[position()=1])='then' or local-name(child::*[position()=1])='else'">
                    <xsl:if test="count(child::syn:then)>0 or count(child::synNew:then)>0">
                        <xsl:element name="then" namespace="http://ws.apache.org/ns/synapse">
                            <xsl:apply-templates select="child::syn:then/* | child::synNew:then/*"/>
                        </xsl:element>
                    </xsl:if>
                    <xsl:if test="count(child::syn:else)>0 or count(child::synNew:else)>0">
                        <xsl:element name="else" namespace="http://ws.apache.org/ns/synapse">
                            <xsl:apply-templates select="child::syn:else/* | child::synNew:else/*"/>
                        </xsl:element>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="then" namespace="http://ws.apache.org/ns/synapse">
                        <xsl:apply-templates/>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template match="syn:definitions/syn:sequence | syn:definitions/syn:localEntry | syn:definitions/syn:proxy | syn:definitions/syn:task | syn:definitions/syn:endpoint | syn:definitions/syn:eventSource | syn:definitions/syn:registry" priority="2">
        <xsl:call-template name="convertNS"/>
    </xsl:template>

    <xsl:template match="syn:definitions | synNew:definitions" priority="1">
        <xsl:element name="definitions" namespace="http://ws.apache.org/ns/synapse">
            <xsl:text>

</xsl:text>
            <xsl:for-each select="syn:* | synNew:* | spring:* | comment()">
                <xsl:if test="local-name()='sequence' or local-name()='localEntry' or local-name()='proxy' or local-name()='task' or local-name()='endpoint' or local-name()='eventSource' or local-name()='priorityExecutor' or local-name()='registry'">
                    <xsl:apply-templates select="."/>
                    <xsl:text>

</xsl:text>
                </xsl:if>
                <xsl:if test="self::comment() and (local-name(following-sibling::*[position()=1])='localEntry' or local-name(following-sibling::*[position()=1])='proxy' or local-name(following-sibling::*[position()=1])='task' or local-name(following-sibling::*[position()=1])='sequence' or local-name(following-sibling::*[position()=1])='endpoint' or local-name(following-sibling::*[position()=1])='eventSource' or local-name(following-sibling::*[position()=1])='priorityExecutor' or local-name(following-sibling::*[position()=1])='registry')">
                    <xsl:copy-of select="self::comment()" xml:space="preserve"/>
                    <xsl:text>
</xsl:text>
                </xsl:if>
            </xsl:for-each>
            <xsl:if test="not(syn:sequence[@name='main'] or synNew:sequence[@name='main']) and (count(syn:*[local-name()!='sequence' and local-name()!='localEntry' and local-name()!='proxy' and local-name()!='task' and local-name()!='endpoint' and local-name()!='eventSource' and local-name()!='registry']) + count(synNew:*[local-name()!='sequence' and local-name()!='localEntry' and local-name()!='proxy' and local-name()!='task' and local-name()!='endpoint' and local-name()!='eventSource' and local-name()!='priorityExecutor' and local-name()!='registry']))!=0">
                <xsl:element name="sequence" namespace="http://ws.apache.org/ns/synapse">
                    <xsl:attribute name="name">main</xsl:attribute>
                    <xsl:for-each select="syn:* | synNew:* | spring:* | comment()">
                        <xsl:if test="local-name()!='sequence' and local-name()!='localEntry' and local-name()!='proxy' and local-name()!='task' and local-name()!='endpoint' and local-name()!='eventSource' and local-name()!='priorityExecutor' and local-name()!='registry'">
                            <xsl:choose>
                                <xsl:when test="self::comment()">
                                    <xsl:if test="local-name(following-sibling::*[position()=1])!='localEntry' and local-name(following-sibling::*[position()=1])!='sequence' and local-name(following-sibling::*[position()=1])!='proxy' and local-name(following-sibling::*[position()=1])!='task' and local-name(following-sibling::*[position()=1])!='endpoint' and local-name(following-sibling::*[position()=1])!='eventSource' and local-name(following-sibling::*[position()=1])!='priorityExecutor' and local-name(following-sibling::*[position()=1])!='registry'">
                                        <xsl:copy-of select="self::comment()" xml:space="preserve"/>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:element>
                <xsl:text>

</xsl:text>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/ | @* | node() | text() | processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="comment()">
        <xsl:choose>
            <xsl:when test="local-name(following-sibling::*[position()=1])='definitions'" xml:space="preserve">
<xsl:copy-of select="." xml:space="preserve"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="convertNS">
        <xsl:element name="{local-name()}" namespace="http://ws.apache.org/ns/synapse">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
