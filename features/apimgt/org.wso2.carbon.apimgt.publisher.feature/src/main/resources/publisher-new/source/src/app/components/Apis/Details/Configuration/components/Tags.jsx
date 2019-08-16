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

import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import ChipInput from 'material-ui-chip-input';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Tags(props) {
    const { api, configDispatcher } = props;
    return (
        <Grid container spacing={1} alignItems='flex-start'>
            <Grid item>
                <Typography variant='subtitle1'>Tags</Typography>
            </Grid>
            <Grid item xs={4}>
                <ChipInput
                    value={api.tags}
                    onAdd={(tag) => {
                        configDispatcher({ action: 'tags', value: [...api.tags, tag] });
                    }}
                    onDelete={(tag) => {
                        configDispatcher({ action: 'tags', value: api.tags.filter(oldTag => oldTag !== tag) });
                    }}
                />
            </Grid>
        </Grid>
    );
}

Tags.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
