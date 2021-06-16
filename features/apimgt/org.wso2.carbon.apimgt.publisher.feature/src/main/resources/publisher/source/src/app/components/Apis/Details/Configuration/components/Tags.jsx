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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import ChipInput from 'material-ui-chip-input';
import { FormattedMessage } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Chip from '@material-ui/core/Chip';
import { red } from '@material-ui/core/colors/';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Tags(props) {
    const { api, configDispatcher } = props;
    const [apiFromContext] = useAPI();
    const [isTagValid, setIsTagValid] = useState(true);
    const [invalidTags, setInvalidTags] = useState([]);
    const [isTagWithinLimit, setIsTagWithinLimit] = useState(true);
    const regexPattern = /([~!@#;%^&*+=|\\<>"'/,])/;
    const helperText = () => {
        if (isTagValid && isTagWithinLimit) {
            return (
                <FormattedMessage
                    id='Apis.Details.Configuration.components.Tags.helper'
                    defaultMessage='Press `Enter` after typing the tag name to add a new tag'
                />
            );
        } else if (!isTagValid) {
            return (
                <FormattedMessage
                    id='Apis.Details.Configuration.components.Tags.error'
                    defaultMessage={
                        'The tag contains one or more illegal characters '
                        + '( ~ ! @ # ; % ^ & * + = { } | < > , \' " \\\\ / ) .'
                    }
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='Apis.Details.Configuration.components.Tags.limit.error'
                    defaultMessage='The tag exceeds the maximum length of 30 characters'
                />
            );
        }
    };

    return (
        <React.Fragment style={{ marginTop: 10 }}>
            <ChipInput
                fullWidth
                variant='outlined'
                label={(
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Tags.title'
                        defaultMessage='Tags'
                    />
                )}
                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                value={api.tags}
                error={!(isTagValid && isTagWithinLimit)}
                helperText={helperText()}
                onAdd={(tag) => {
                    if (regexPattern.test(tag) || tag.length > 30) {
                        if (regexPattern.test(tag)) {
                            setIsTagValid(false);
                        } else {
                            setIsTagWithinLimit(false);
                        }
                        setInvalidTags([...invalidTags, tag]);
                    }
                    configDispatcher({ action: 'tags', value: [...api.tags, tag] });
                }}
                chipRenderer={({ value }, key) => (
                    <Chip
                        key={key}
                        size='small'
                        label={value}
                        onDelete={() => {
                            if (invalidTags.includes(value)) {
                                const currentInvalidTags = invalidTags.filter((existingTag) => existingTag !== value);
                                setInvalidTags(currentInvalidTags);
                                if (currentInvalidTags.length === 0) {
                                    setIsTagValid(true);
                                    setIsTagWithinLimit(true);
                                }
                            }
                            configDispatcher({ action: 'tags', value: api.tags.filter((oldTag) => oldTag !== value) });
                        }}
                        style={{
                            backgroundColor: (regexPattern.test(value) || value.length > 30) ? red[300] : null,
                            margin: '0 8px 12px 0',
                            float: 'left',
                        }}
                    />
                )}
                style={{ display: 'flex' }}
            />
        </React.Fragment>
    );
}

Tags.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
