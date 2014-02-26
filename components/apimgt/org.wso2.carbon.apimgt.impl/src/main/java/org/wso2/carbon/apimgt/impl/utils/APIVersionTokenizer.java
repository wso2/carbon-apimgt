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

/**
 * Tokenizer (parser) for API version strings. Based on a program by Markus Jarderot
 * which was shared at <a href="http://stackoverflow.com/a/10034633">SO</a>.
 */
public class APIVersionTokenizer {

    private final String versionString;
    private final int length;

    private int position;
    private int number;
    private String suffix;

    public APIVersionTokenizer(String versionString) {
        this.versionString = versionString;
        this.length = versionString.length();
    }

    public int getNumber() {
        return number;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean next() {
        number = 0;
        suffix = null;

        if (position >= length) {
            return false;
        }

        while (position < length) {
            char c = versionString.charAt(position);
            if (c < '0' || c > '9') {
                break;
            }
            number = number * 10 + Character.getNumericValue(c);
            position++;
        }

        int suffixStart = position;
        while (position < length) {
            char c = versionString.charAt(position);
            if (c == '.') {
                break;
            }
            position++;
        }

        if (suffixStart < position) {
            suffix = versionString.substring(suffixStart, position);
        }
        if (position < length) {
            position++;
        }
        return true;
    }
}
