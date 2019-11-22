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

import React, {useState, useEffect} from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import Utils from 'AppData/Utils';

function Download(props) {
    const { intl } = props;

    const { docId, apiId } = props;
    const [isFileAvailable, setIsFileAvailable] = useState(false);
    const [isSuccessful, setIsSuccessful] = useState(false);

    useEffect(() => {
        const api = new Api();
        const promised_get_content = api.getFileForDocument(apiId, docId);
        promised_get_content
            .then((done) => {
                setIsSuccessful(true);
                setIsFileAvailable(true);
            })
            .catch((error) => {
                setIsSuccessful(true);
                setIsFileAvailable(false);
            });
    }, []);
    const handleDownload = () => {
        const api = new Api();
        const promised_get_content = api.getFileForDocument(apiId, docId);
        promised_get_content
            .then((response) => {
                Utils.forceDownload(response);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(intl.formatMessage({
                        id:'Apis.Details.Documents.Download.documents.markdown.editor.download.error',
                        defaultMessage: 'Error downloading the file',
                    }));
                }
            });
    };

    return (
        isSuccessful &&
        <Button onClick={handleDownload} disabled={!isFileAvailable}>
            <Icon>arrow_downward</Icon>
            <FormattedMessage
                id='Apis.Details.Documents.Download.documents.listing.download'
                defaultMessage='Download'
            />
        </Button>
    );
}
Download.propTypes = {
    apiId: PropTypes.shape({}).isRequired,
    docId: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(Download);
