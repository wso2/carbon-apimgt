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

package org.apache.synapse.mediators.db;

import org.apache.synapse.SynapseException;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates an SQL statement, one or more parameters for it and optionally some information
 * about results that one would like to read.
 */
public class Statement {

    private String rawStatement = null;
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    private final Map<String, String> resultsMap = new HashMap<String, String>();

    public Statement(String rawStatement) {
        this.rawStatement = rawStatement;
    }

    public String getRawStatement() {
        return rawStatement;
    }

    public void addParameter(String propertyName, SynapseXPath xpath, String type){
        parameters.add(new Parameter(propertyName, xpath, type));
    }

    public void addResult(String propertyName, String column) {
        resultsMap.put(propertyName, column);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Map<String, String> getResultsMap() {
        return resultsMap;
    }

    public static class Parameter {
        String propertyName = null;
        SynapseXPath xpath = null;
        int type = 0;

        Parameter(String value, SynapseXPath xpath, String type) {

            this.propertyName = value;
            this.xpath = xpath; 
            if ("CHAR".equals(type)) {
                this.type = Types.CHAR;
            } else if ("VARCHAR".equals(type)) {
                this.type = Types.VARCHAR;
            } else if ("LONGVARCHAR".equals(type)) {
                this.type = Types.LONGVARCHAR;
            } else if ("NUMERIC".equals(type)) {
                this.type = Types.NUMERIC;
            } else if ("DECIMAL".equals(type)) {
                this.type = Types.DECIMAL;
            } else if ("BIT".equals(type)) {
                this.type = Types.BIT;
            } else if ("TINYINT".equals(type)) {
                this.type = Types.TINYINT;
            } else if ("SMALLINT".equals(type)) {
                this.type = Types.SMALLINT;
            } else if ("INTEGER".equals(type)) {
                this.type = Types.INTEGER;
            } else if ("BIGINT".equals(type)) {
                this.type = Types.BIGINT;
            } else if ("REAL".equals(type)) {
                this.type = Types.REAL;
            } else if ("FLOAT".equals(type)) {
                this.type = Types.FLOAT;
            } else if ("DOUBLE".equals(type)) {
                this.type = Types.DOUBLE;
            } else if ("DATE".equals(type)) {
                this.type = Types.DATE;
            } else if ("TIME".equals(type)) {
                this.type = Types.TIME;
             } else if ("TIMESTAMP".equals(type)) {
                this.type = Types.TIMESTAMP;
            } else {
                throw new SynapseException("Unknown or unsupported JDBC type : " + type);
            }
        }

        public String getPropertyName() {
            return propertyName;
        }

        public SynapseXPath getXpath() {
            return xpath;
        }

        public int getType() {
            return type;
        }
    }
}

