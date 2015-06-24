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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.db.AbstractDBMediator;
import org.apache.synapse.mediators.db.Statement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.sql.Types;

/**
 * Base class for serializers for database related mediators.
 * 
 * <dbreport | dblookup | .. etc>
 *   <connection>
 *     <pool>
 *     (
 *       <driver/>
 *       <url/>
 *       <user/>
 *       <password/>
 *     |
 *       <dsName/>
 *     |
 *       <dsName/>
 *       <icClass/>
 *       <url/>
 *       <user/>
 *       <password/>
 *     )
 *       <property name="name" value="value"/>*
 *     </pool>
 *   </connection>
 *   <statement>
 *     <sql>insert into table values (?, ?, ..) OR select target from destinations where src = ?</sql>
 *     <parameter (value="const" | expression="xpath") type="INTEGER|VARCHAR|..."/>*
 *     <result name="propName" column="target | number"/>*
 *   </statement>+
 * </dbreport | dblookup | .. etc>
 *
 * Supported properties
 * autocommit = true | false
 * isolation = Connection.TRANSACTION_NONE
 *           | Connection.TRANSACTION_READ_COMMITTED
 *           | Connection.TRANSACTION_READ_UNCOMMITTED
 *           | Connection.TRANSACTION_REPEATABLE_READ
 *           | Connection.TRANSACTION_SERIALIZABLE
 * initialsize = int
 * maxactive = int
 * maxidle = int
 * maxopenstatements = int
 * maxwait = long
 * minidle = int
 * poolstatements = true | false
 * testonborrow = true | false
 * testonreturn = true | false
 * testwhileidle = true | false
 * validationquery = String
 */
public abstract class AbstractDBMediatorSerializer extends AbstractMediatorSerializer {

    protected void serializeDBInformation(AbstractDBMediator mediator, OMElement dbParent) {

        OMElement connElt = fac.createOMElement("connection", synNS);
        connElt.addChild(createPoolElement(mediator));
        dbParent.addChild(connElt);

        // process statements
        for (Statement statement : mediator.getStatementList()) {
            dbParent.addChild(createStatementElement(statement));
        }
    }

    private OMNode createPoolElement(AbstractDBMediator mediator) {
        OMElement poolElt = fac.createOMElement("pool", synNS);
        for (Object o : mediator.getDataSourceProps().keySet()) {

            String value = mediator.getDataSourceProps().get(o);

            if (o instanceof QName) {
                QName name = (QName) o;
                OMElement elt = fac.createOMElement(name.getLocalPart(), synNS);
                elt.setText(value);
                poolElt.addChild(elt);

            } else if (o instanceof String) {
                OMElement elt = fac.createOMElement(
                    AbstractDBMediatorFactory.PROP_Q.getLocalPart(), synNS);
                elt.addAttribute(fac.createOMAttribute("name", nullNS, (String) o));
                elt.addAttribute(fac.createOMAttribute("value", nullNS, value));
                poolElt.addChild(elt);
             }
        }
        return poolElt;
    }

    private OMNode createStatementElement(Statement statement) {
        
        OMElement stmntElt = fac.createOMElement(
            AbstractDBMediatorFactory.STMNT_Q.getLocalPart(), synNS);

        OMElement sqlElt = fac.createOMElement(
            AbstractDBMediatorFactory.SQL_Q.getLocalPart(), synNS);
        OMText sqlText = fac.createOMText(statement.getRawStatement(), XMLStreamConstants.CDATA);
        sqlElt.addChild(sqlText);
        stmntElt.addChild(sqlElt);

        // serialize parameters of the statement
        for (Statement.Parameter param : statement.getParameters()) {
            OMElement paramElt = createStatementParamElement(param);
            stmntElt.addChild(paramElt);
        }

        // serialize any optional results of the statement
        for (String name : statement.getResultsMap().keySet()) {

            String columnStr = statement.getResultsMap().get(name);

            OMElement resultElt = fac.createOMElement(
                AbstractDBMediatorFactory.RESULT_Q.getLocalPart(), synNS);

            resultElt.addAttribute(
                fac.createOMAttribute("name", nullNS, name));
            resultElt.addAttribute(
                fac.createOMAttribute("column", nullNS, columnStr));

            stmntElt.addChild(resultElt);
        }

        return stmntElt;
    }

    private OMElement createStatementParamElement(Statement.Parameter param) {
        OMElement paramElt = fac.createOMElement(
            AbstractDBMediatorFactory.PARAM_Q.getLocalPart(), synNS);

        if (param.getPropertyName() != null) {
            paramElt.addAttribute(
                fac.createOMAttribute("value", nullNS, param.getPropertyName()));
        }
        if (param.getXpath() != null) {
            SynapseXPathSerializer.serializeXPath(param.getXpath(), paramElt, "expression");
        }

        switch (param.getType()) {
            case Types.CHAR: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "CHAR"));
                break;
            }
            case Types.VARCHAR: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "VARCHAR"));
                break;
            }
            case Types.LONGVARCHAR: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "LONGVARCHAR"));
                break;
            }
            case Types.NUMERIC: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "NUMERIC"));
                break;
            }
            case Types.DECIMAL: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DECIMAL"));
                break;
            }
            case Types.BIT: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "BIT"));
                break;
            }
            case Types.TINYINT: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TINYINT"));
                break;
            }
            case Types.SMALLINT: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "SMALLINT"));
                break;
            }
            case Types.INTEGER: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "INTEGER"));
                break;
            }
            case Types.BIGINT: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "BIGINT"));
                break;
            }
            case Types.REAL: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "REAL"));
                break;
            }
            case Types.FLOAT: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "FLOAT"));
                break;
            }
            case Types.DOUBLE: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DOUBLE"));
                break;
            }
            case Types.DATE: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "DATE"));
                break;
            }
            case Types.TIME: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TIME"));
                break;
            }
            case Types.TIMESTAMP: {
                paramElt.addAttribute(fac.createOMAttribute("type", nullNS, "TIMESTAMP"));
                break;
            }
            default: {
                throw new SynapseException("Unknown or unsupported JDBC type : " +
                    param.getType());                            
            }
        }
        return paramElt;
    }
}
