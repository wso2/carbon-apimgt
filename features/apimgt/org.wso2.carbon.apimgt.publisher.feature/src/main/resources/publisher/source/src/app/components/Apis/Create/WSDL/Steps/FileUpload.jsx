/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import { FormControl } from 'material-ui';
import Dropzone from 'react-dropzone';
import PropTypes from 'prop-types';

import React from 'react';

const FileUploader = (props) => {
    const { onDropHandler, currentFiles } = props;
    return (
        <FormControl className='horizontal dropzone-wrapper'>
            <div className='dropzone'>
                <Dropzone onDrop={onDropHandler} multiple={false}>
                    <p>Try dropping some files here, or click to select files to upload.</p>
                </Dropzone>
            </div>
            <aside>
                <h2>Uploaded files</h2>
                <ul>
                    {currentFiles &&
                        currentFiles.map(file => (
                            <li key={file.name}>
                                {file.name} - {file.size} bytes
                            </li>
                        ))}
                </ul>
            </aside>
        </FormControl>
    );
};

FileUploader.propTypes = {
    onDropHandler: PropTypes.func.isRequired,
    currentFiles: PropTypes.arrayOf(Object),
};
FileUploader.defaultProps = {
    currentFiles: [],
};
export default FileUploader;
