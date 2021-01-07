/* eslint-disable no-param-reassign */
/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
/* eslint-disable func-names */
const fs = require('fs');

module.exports = function (source, map) {
    const isWin = process.platform === 'win32';
    const pathSeperator = isWin ? '\\' : '/';
    let newSource = source;

    let headerPath = this.resourcePath;
    const rootContextSplits = this.rootContext.split(pathSeperator);
    let appContext = null;
    if (rootContextSplits.length > 0) {
        appContext = rootContextSplits[rootContextSplits.length - 1];
        if (this.resourcePath.indexOf(appContext + pathSeperator + 'override' + pathSeperator + 'src') === -1) {
            headerPath = this.resourcePath.replace(
                appContext + pathSeperator + 'source' + pathSeperator + 'src',
                appContext + pathSeperator + 'override' + pathSeperator + 'src',
            );
        }
    }

    if (appContext && fs.existsSync(headerPath)) {
        newSource = fs.readFileSync(headerPath, 'utf8');
        if (newSource.indexOf('AppOverride')) {
            const lines = newSource.split('\n');
            const formatedLines = [];

            // Check if there are import statements with 'AppOverride' prefix and re write the file path
            const regex = /import(?:["'\s]*([\w*{}\n\r\t, ]+)from\s*)?["'\s].*([@\w/_-]+)["'\s].*/;
            lines.forEach((line) => {
                if (regex.test(line) && line.indexOf('AppOverride')) {
                    line = line.replace(/AppOverride/g, this.rootContext + pathSeperator + 'override');
                    line = isWin ? line.replace(/\\/g, '/') : line;
                }
                formatedLines.push(line);
            });
            newSource = formatedLines.join('\n');
        }
        this.addDependency(headerPath);
    }
    this.callback(null, newSource, map);
};
