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

package org.apache.synapse.mediators.filters.router;

import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.mediators.eip.Target;

/**
 * 
 */
public class ConditionalRoute {

    private Evaluator evaluator;

    private Target target;

    private boolean breakRoute = true;

    private boolean breakRouteExplicitlySet;

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public boolean isBreakRoute() {
        return breakRoute;
    }

    public void setBreakRoute(boolean breakRoute) {
        this.breakRouteExplicitlySet = true;
        this.breakRoute = breakRoute;
    }

    public boolean isBreakRouteExplicitlySet() {
        return breakRouteExplicitlySet;
    }
}
