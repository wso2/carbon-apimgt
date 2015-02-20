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

package org.apache.synapse.migrator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 */
public class ConfigurationMigrator {

    private static final String MIGRATOR_XSLT_PATH
            = "modules/migrator/src/main/resources/synapse-configuration-migrator.xslt";

    public static void doTransform(String xmlFile, String xslFile, String outFile)
            throws TransformerException, IOException {

        FileReader xslFileReader = new FileReader(xslFile);
        StreamSource xslStreamSource = new StreamSource(xslFileReader);

        FileReader xmlFileReader = new FileReader(xmlFile);
        StreamSource xmlStreamSource = new StreamSource(xmlFileReader);

        FileWriter outFileWriter = new FileWriter(outFile);
        StreamResult outStreamResult = new StreamResult(outFileWriter);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslStreamSource);

        transformer.transform(xmlStreamSource, outStreamResult);
        outFileWriter.flush();
    }

    public static void main(String[] arguments) {

        System.out.println("\n\t#######################################################");
        System.out.println("\t#      Apache Synapse - Configuration Migration       #");
        System.out.println("\t#######################################################");

        System.out.println("\n[INFO] Migration STARTED");

        try {
            if (arguments.length == 2) {
                System.out.println("[INFO] Migrating the synapse 1.x configuration '"
                        + arguments[0] + "' into a new 2.x configuration at '" + arguments[1] + "'");
                doTransform(arguments[0], MIGRATOR_XSLT_PATH, arguments[1]);
            } else if (arguments.length == 3) {
                System.out.println("[INFO] Migrating the synapse 1.x configuration '"
                        + arguments[0] + "' into a new 2.x configuration at '"
                        + arguments[1] + "' using the XSLT '" + arguments[2] + "'");
                doTransform(arguments[0], arguments[2], arguments[1]);
            } else {
                System.out.println("[ERROR] Invalid arguments provided for migration");
            }
            System.out.println("[INFO] Migration SUCCESSFULLY COMPLETED");
            System.out.println("[INFO] Migrated 2.x configuration is available at '"
                    + arguments[1] + "'");
        } catch (TransformerException e) {
            handleException("Migration FAILED\n\t" + e.toString());
        } catch (IOException e) {
            handleException("Migration FAILED\n\t" + e.toString());
        }

    }

    private static void handleException(String message) {
        System.out.println("[ERROR] " + message);
    }
}
