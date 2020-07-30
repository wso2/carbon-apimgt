/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import YAML from 'js-yaml';

export default function (editor) {
    editor.on('paste', (e) => {
        const originalStr = e.text;
        if (!isJSON(originalStr)) {
            return;
        }

        let yamlString;
        try {
            yamlString = YAML.safeDump(YAML.safeLoad(originalStr), {
                lineWidth: -1, // don't generate line folds
            });
        } catch (e) {
            return;
        }

        if (!confirm('Would you like to convert your JSON into YAML?')) {
            return;
        }

        // using SelectionRange instead of CursorPosition, because:
        // SR.start|end === CP when there's no selection
        // and it catches indentation edge cases when there is one
        const padding = makePadding(editor.getSelectionRange().start.column);

        // update the pasted content
        e.text = yamlString
            .split('\n')
            .map((line, i) => (i == 0 ? line : padding + line)) // don't pad first line, it's already indented
            .join('\n')
            .replace(/\t/g, '  '); // tabs -> spaces, just to be sure
    });
}

function isJSON(str) {
    // basic test: "does this look like JSON?"
    const regex = /^[ \r\n\t]*[{\[]/;

    return regex.test(str);
}

function makePadding(len) {
    let str = '';

    while (str.length < len) {
        str += ' ';
    }

    return str;
}
