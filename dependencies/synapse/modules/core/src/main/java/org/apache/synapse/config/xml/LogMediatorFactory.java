/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.builtin.LogMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Created a Log mediator that logs messages using commons-logging.
 *
 * <pre>
 * &lt;log [level="simple|headers|full|custom"]&gt;
 *      &lt;property&gt; *
 * &lt;/log&gt;
 * </pre>
 */
public class LogMediatorFactory extends AbstractMediatorFactory  {

    private static final QName LOG_Q    = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "log");
    private static final String SIMPLE  = "simple";
    private static final String HEADERS = "headers";
    private static final String FULL    = "full";
    private static final String CUSTOM  = "custom";
    public static final String CAT_INFO = "INFO";
    public static final String CAT_TRACE = "TRACE";
    public static final String CAT_DEBUG = "DEBUG";
    public static final String CAT_WARN = "WARN";
    public static final String CAT_ERROR = "ERROR";
    public static final String CAT_FATAL = "FATAL";
    private static final QName ATT_LEVEL = new QName("level");
    private static final QName ATT_SEPERATOR = new QName("separator");
    private static final QName ATT_CATEGORY = new QName("category");

    public QName getTagQName() {
        return LOG_Q;
    }

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        LogMediator logMediator = new LogMediator();

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(logMediator,elem);
        
        // Set the high level set of properties to be logged (i.e. log level)
        OMAttribute level = elem.getAttribute(ATT_LEVEL);
        if (level != null) {
            String levelstr = level.getAttributeValue();
            if (SIMPLE.equals(levelstr)) {
                logMediator.setLogLevel(LogMediator.SIMPLE);
            } else if (HEADERS.equals(levelstr)) {
                logMediator.setLogLevel(LogMediator.HEADERS);
            } else if (FULL.equals(levelstr)) {
                logMediator.setLogLevel(LogMediator.FULL);
            } else if (CUSTOM.equals(levelstr)) {
                logMediator.setLogLevel(LogMediator.CUSTOM);
            }
        }

        // Set the log statement category (i.e. INFO, DEBUG, etc..)
        OMAttribute category = elem.getAttribute(ATT_CATEGORY);
        if (category != null) {
            String catstr = category.getAttributeValue().trim().toUpperCase();
            if (CAT_INFO.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_INFO);
            } else if (CAT_TRACE.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_TRACE);
            } else if (CAT_DEBUG.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_DEBUG);
            } else if (CAT_WARN.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_WARN);
            } else if (CAT_ERROR.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_ERROR);
            } else if (CAT_FATAL.equals(catstr)) {
                logMediator.setCategory(LogMediator.CATEGORY_FATAL);
            } else {
                handleException("Invalid log category. Category has to be one of " +
                        "the following : INFO, TRACE, DEBUG, WARN, ERROR, FATAL");
            }
        }

        // check if a custom separator has been supplied, if so use it
        OMAttribute separator = elem.getAttribute(ATT_SEPERATOR);
        if (separator != null) {
            logMediator.setSeparator(separator.getAttributeValue());
        }

        logMediator.addAllProperties(MediatorPropertyFactory.getMediatorProperties(elem));

        return logMediator;
    }
}
