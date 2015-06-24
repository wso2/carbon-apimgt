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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SwitchCase define a case element of Switch Mediator and It has a list mediator and
 * a regex that is matched by its owning SwitchMediator for selection.
 * If any SwitchCase has selected ,Then the list mediator of it, will responsible
 * for message mediation
 */

public class SwitchCase implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SwitchCase.class);

    /**
     * The regular expression pattern to be checked
     */
    private Pattern regex = null;
    
    /**
     * The list mediator which is responsible on message mediation of the case
     */
    private AnonymousListMediator caseMediator;

    public void init(SynapseEnvironment se) {
        caseMediator.init(se);
    }

    public void destroy() {
        caseMediator.destroy();
    }

    /**
     * To delegate message mediation to list mediator
     *
     * @param synCtx message context to be mediated
     * @return boolean value
     */
    public boolean mediate(MessageContext synCtx) {
        return caseMediator == null || caseMediator.mediate(synCtx);
    }

    /**
     * To get list mediator of this case element
     *
     * @return List mediator of  switch case
     */
    public AnonymousListMediator getCaseMediator() {
        return caseMediator;
    }

    /**
     * To set the set of case mediators
     *
     * @param caseMediator anonymous sequence to be used for the case mediation
     */
    public void setCaseMediator(AnonymousListMediator caseMediator) {
        this.caseMediator = caseMediator;
    }

    /**
     * To get the regular expression pattern
     *
     * @return Pattern
     */
    public Pattern getRegex() {
        return regex;
    }

    /**
     * To set the regular expression pattern
     *
     * @param regex Regular Expression to be matched
     */
    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    /**
     * To evaluate regular expression pattern to a get switch case
     *
     * @param value value to be tested over the regular expression of match
     * @return boolean value
     */
    public boolean matches(String value) {

        if (value == null) {

            log.warn("Provided character sequence for switch case condition is 'null'." +
                    " Switch case will not be executed.");

            return false;
        }

        Matcher matcher = regex.matcher(value);
        if (matcher == null) {

            log.warn("Matcher for the provided character sequence and the pattern ' "
                    + regex + " '" + " cannot be found. Switch case will not be executed.");

            return false;
        }

        return matcher.matches();
    }
}
