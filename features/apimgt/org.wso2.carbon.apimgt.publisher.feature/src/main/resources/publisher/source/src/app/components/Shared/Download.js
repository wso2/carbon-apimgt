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

/**
* Download a file
* @param {any} response Response of download file
*/
// eslint-disable-next-line func-names
const downloadFile = function (response) {
    let fileName = '';
    const contentDisposition = response.headers['content-disposition'];

    if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
        const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = fileNameReg.exec(contentDisposition);
        if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
    }
    const contentType = response.headers['content-type'];
    const blob = new Blob([response.data], {
        type: contentType,
    });
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
        window.navigator.msSaveBlob(blob, fileName);
    } else {
        const URL = window.URL || window.webkitURL;
        const downloadUrl = URL.createObjectURL(blob);

        if (fileName) {
            const aTag = document.createElement('a');
            if (typeof aTag.download === 'undefined') {
                window.location = downloadUrl;
            } else {
                aTag.href = downloadUrl;
                aTag.download = fileName;
                document.body.appendChild(aTag);
                aTag.click();
            }
        } else {
            window.location = downloadUrl;
        }

        setTimeout(() => {
            URL.revokeObjectURL(downloadUrl);
        }, 100);
    }
};
export default downloadFile;
