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

final class Constants {
    private Constants() {}

    public static final String JSON_STRING = "JSON_STRING";

    /** The JSON Key for wrapper type JSON Object */
    public static final String K_OBJECT = "\"jsonObject\"";
    /** The JSON Key for wrapper type anonymous JSON array */
    public static final String K_ARRAY = "\"jsonArray\"";
    /** The JSON Key for wrapper type anonymous JSON array elements */
    public static final String K_ARRAY_ELEM = "\"jsonElement\"";

    public static final String ID = "_JsonReader";
    /** Used when the local name starts with a digit character. */
    public static final String PRECEDING_DIGIT_S = "_PD_";
    /** Final prefix for local names that have preceding digits */
    public static final String PRECEDING_DIGIT = ID + PRECEDING_DIGIT_S;
    /** Used when the local name starts with the $ character. */
    public static final String PRECEDING_DOLLOR_S = "_PS_";
    public static final String PRECEDING_DOLLOR = ID + PRECEDING_DOLLOR_S;
    /** The Dollar character */
    public static final int C_DOLLOR = '$';
    /** The underscore character */
    public static final int C_USOCRE = '_';

    public static final String ID_KEY = ID + "_";

}
