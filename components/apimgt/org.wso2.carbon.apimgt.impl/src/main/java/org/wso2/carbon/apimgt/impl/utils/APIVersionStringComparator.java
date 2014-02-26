/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import java.util.Comparator;

/**
 * <p>Compares API version strings. This comparator supports following version string
 * format.</p>
 * <ul>
 *     <li>VersionString := VersionToken+</li>
 *     <li>VersionToken  := VersionNumber | VersionSuffix | VersionNumber VersionSuffix</li>
 *     <li>VersionNumber := [0-9]+</li>
 *     <li>VersionSuffix := ~(0-9) AnyChar*</li>
 * </ul>
 * <p>Some example version strings supported by the comparator are given below.</p>
 * <ul>
 *     <li>1.5</li>
 *     <li>2.1.1</li>
 *     <li>2.1.2b</li>
 *     <li>1.3-SNAPSHOT</li>
 *     <li>2.0.0.wso2v4</li>
 * </ul>
 * <p>Version matching is carried out by comparing the version strings token by token. Version
 * numbers are compared in the conventional manner and the suffixes are compared
 * lexicographically.</p>
 */
public class APIVersionStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        APIVersionTokenizer tokenizer1 = new APIVersionTokenizer(o1);
        APIVersionTokenizer tokenizer2 = new APIVersionTokenizer(o2);

        int number1, number2;
        String suffix1, suffix2;

        while (tokenizer1.next()) {
            if (!tokenizer2.next()) {
                do {
                    number1 = tokenizer1.getNumber();
                    suffix1 = tokenizer1.getSuffix();
                    if (number1 != 0 || suffix1 != null) {
                        // Version one is longer than number two, and non-zero
                        return 1;
                    }
                } while (tokenizer1.next());

                // Version one is longer than version two, but zero
                return 0;
            }

            number1 = tokenizer1.getNumber();
            suffix1 = tokenizer1.getSuffix();
            number2 = tokenizer2.getNumber();
            suffix2 = tokenizer2.getSuffix();

            if (number1 < number2) {
                // Number one is less than number two
                return -1;
            }
            if (number1 > number2) {
                // Number one is greater than number two
                return 1;
            }

            if (suffix1 == null && suffix2 == null) {
                continue; // No suffixes
            } else if (suffix1 == null) {
                return 1; // First suffix is empty (2.0 > 2.0-SNAPSHOT)
            } else if (suffix2 == null) {
                return -1; // Second suffix is empty (2.0-alpha < 2.0)
            } else {
                // Lexical comparison of suffixes - (2.0-wso2v4 < 2.0-wso2v5)
                int result = suffix1.compareTo(suffix2);
                if (result != 0) {
                    return result;
                }
            }
        }

        if (tokenizer2.next()) {
            do {
                number2 = tokenizer2.getNumber();
                suffix2 = tokenizer2.getSuffix();
                if (number2 != 0 || suffix2 != null) {
                    // Version two is longer than version one, and non-zero
                    return -1;
                }
            } while (tokenizer2.next());

            // Version two is longer than version one, but zero
            return 0;
        }
        return 0;
    }
}
