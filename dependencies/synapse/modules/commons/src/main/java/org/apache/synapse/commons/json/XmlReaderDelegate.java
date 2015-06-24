/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.commons.json;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * Detaches the special prefix applied by the JsonReaderDelegate.<br/>
 * Hence this returns the original key value that was read in.
 */
final class XmlReaderDelegate extends StreamReaderDelegate {
    private static final Log logger = LogFactory.getLog(XmlReaderDelegate.class.getName());

    private boolean processNCNames;

    public XmlReaderDelegate(XMLStreamReader reader, boolean processNCNames) {
        super(reader);
        /** Possible reader implementations include;
            com.ctc.wstx.sr.ValidatingStreamReader
            de.odysseus.staxon.json.JsonXMLStreamReader
            com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl
         */
        if (logger.isDebugEnabled()) {
            logger.debug("#XmlReaderDelegate. Setting XMLStreamReader: " + reader.getClass().getName());
        }
        this.processNCNames = processNCNames;
    }

    public String getLocalName() {
        String localName = super.getLocalName();
        String newName = localName;
        if (localName == null || "".equals(localName)) {
            return localName;
        }
        boolean checked = false;
        String subStr;
        if (localName.charAt(0) == Constants.C_USOCRE) {
            if (localName.startsWith(Constants.PRECEDING_DIGIT)) {
                subStr = localName.substring(Constants.PRECEDING_DIGIT.length(),
                        localName.length());
                if (processNCNames) {
                    newName = toOrigJsonKey(subStr);
                    checked = true;
                } else {
                    newName = subStr;
                }
            } else if (localName.startsWith(Constants.PRECEDING_DOLLOR)) {
                subStr = localName.substring(Constants.PRECEDING_DOLLOR.length(),
                        localName.length());
                if (processNCNames) {
                    newName = (char) Constants.C_DOLLOR + toOrigJsonKey(subStr);
                    checked = true;
                } else {
                    newName = (char) Constants.C_DOLLOR + subStr;
                }
            }
        }
        if (!checked && processNCNames) {
            newName = toOrigJsonKey(newName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#getLocalName. old=" + localName + ", new=" + newName);
        }
        return newName;
    }

    public QName getName() {
        QName qName = super.getName();
        String localName = qName.getLocalPart();
        QName newName = qName;
        if (localName == null || "".equals(localName)) {
            return qName;
        }
        boolean checked = false;
        String subStr;
        if (localName.charAt(0) == Constants.C_USOCRE) {
            if (localName.startsWith(Constants.PRECEDING_DIGIT)) {
                subStr = localName.substring(Constants.PRECEDING_DIGIT.length(),
                        localName.length());
                if (processNCNames) {
                    localName =  toOrigJsonKey(subStr);
                    checked = true;
                } else {
                    localName = subStr;
                }
                newName = new QName(qName.getNamespaceURI(), localName, qName.getPrefix());
            } else if (localName.startsWith(Constants.PRECEDING_DOLLOR)) {
                subStr = localName.substring(Constants.PRECEDING_DOLLOR.length(),
                        localName.length());
                if (processNCNames) {
                    localName =  (char) Constants.C_DOLLOR + toOrigJsonKey(subStr);
                    checked = true;
                } else {
                    localName = (char) Constants.C_DOLLOR + subStr;
                }
                newName = new QName(qName.getNamespaceURI(), localName, qName.getPrefix());
            }
        }
        if (!checked && processNCNames) {
            String newNameP = toOrigJsonKey(localName);
            if (!localName.equals(newNameP)) {
                newName = new QName(qName.getNamespaceURI(), newNameP, qName.getPrefix());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#getName. old=" + localName + ", new=" + newName.getLocalPart());
        }
        return newName;
    }

    private String toOrigJsonKey(String src) {
        int indexO, indexN;
        indexO = indexN = 0;
        int length = Constants.ID_KEY.length();
        StringBuilder newStr = new StringBuilder(src.length());
        int[] index = new int[1];
        while (indexN < src.length()) {
            indexN = src.indexOf(Constants.ID_KEY, indexO);
            if (indexN == -1) {
                if (indexO == 0) {
                    return src;
                }
                copyChars(src, indexO, src.length(), newStr);
                break;
            }
            copyChars(src, indexO, indexN, newStr);
            indexO = indexN + length;
            index[0] = indexO;
            int character = readInt(src, index);
            if (character != -1) {
                newStr.append((char) character);
                indexO = index[0] + 1;
            } else {
                copyChars(src, indexO - length, indexO, newStr);
            }
        }
        return newStr.toString();
    }

    private static void copyChars(String src, int low, int high, StringBuilder newStr) {
        for (int i = low; i < high; ++i) {
            newStr.append(src.charAt(i));
        }
    }

    private static int readInt(String s, int[] index) {
        int value = 0;
        int l = s.length();
        while (index[0] < l && index[0] != -1 && s.charAt(index[0]) != '_') {
            int n = s.charAt(index[0]++) - '0';
            n = (n >= 0 && n <= 9) ? n : -1;
            if (n == -1) {
                return -1;
            }
            value = value * 10 + n;
        }
        if (value <= 0) {
            return -1;
        }
        return value;
    }
}
