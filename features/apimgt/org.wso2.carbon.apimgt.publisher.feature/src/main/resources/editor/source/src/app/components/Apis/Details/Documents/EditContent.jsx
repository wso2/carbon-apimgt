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

import React, { useEffect, useState, Suspense, lazy } from 'react';
import { withRouter } from 'react-router-dom';
import isEmpty from 'lodash.isempty';
import MarkdownEditor from './MarkdownEditor';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared';

const TextEditor = lazy(() => import('./TextEditor' /* webpackChunkName: "EditContentTextEditor" */));

function EditContent(props) {
    const [doc, setDoc] = useState(null);
    const {
        history: {
            location: { state, pathname },
            replace,
        },
    } = props;
    useEffect(() => {
        // Check if we want to show the add content screen in the case of coming from GoToEdit.jsx with history -> state props

        if (!isEmpty(state)) {
            const { doc } = state;
            if (doc) {
                setDoc(doc);
            }
        }
    }, []);
    return (
        <React.Fragment>
            {doc && doc.sourceType === 'MARKDOWN' && (
                <MarkdownEditor docName={doc.name} docId={doc.documentId} showAtOnce />
            )}
            {doc && doc.sourceType === 'INLINE' && (
                <Suspense
                    fallback={<Progress />}
                >
                    <TextEditor docName={doc.name} docId={doc.documentId} showAtOnce />
                </Suspense>
            )}
        </React.Fragment>
    );
}
export default withRouter(EditContent);
