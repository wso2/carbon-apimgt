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

package org.apache.synapse.commons.evaluators.config;

import java.util.Map;
import java.util.HashMap;

/**
 * {@link EvaluatorSerializer}s for serializing Evaluators are found using the evaluator name.
 * This class stores information about Evaluator Serializers and their corresponding names. This
 * is a Singleton.
 */
public class EvaluatorSerializerFinder {
    private static final EvaluatorSerializerFinder finder = new EvaluatorSerializerFinder();

    private Map<String, EvaluatorSerializer> serializerMap =
            new HashMap<String, EvaluatorSerializer>();

    private EvaluatorSerializerFinder() {
        serializerMap.put("and", new AndSerializer());
        serializerMap.put("or", new OrSerializer());
        serializerMap.put("not", new NotSerializer());
        serializerMap.put("match", new MatchSerializer());
        serializerMap.put("equal", new EqualSerializer());
    }

    public static EvaluatorSerializerFinder getInstance() {
        return finder;
    }

    public EvaluatorSerializer getSerializer(String name) {
        return serializerMap.get(name);
    }
}
