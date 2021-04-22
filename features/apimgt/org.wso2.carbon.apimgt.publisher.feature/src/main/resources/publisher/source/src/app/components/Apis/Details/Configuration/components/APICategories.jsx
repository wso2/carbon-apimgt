/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';
import Box from '@material-ui/core/Box';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { makeStyles } from '@material-ui/core/styles';
import API from 'AppData/api';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { isRestricted } from 'AppData/AuthManager';

const useStyles = makeStyles((theme) => ({
    tooltip: {
        position: 'absolute',
        right: theme.spacing(-4),
        top: theme.spacing(1),
    },
    listItemText: {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
}));
/**
 * Render the categories drop down.
 * @param {JSON} props props passed from it's parents.
 * @returns {JSX} Render the categories drop down.
 */
function APICategories(props) {
    const [categories, setCategories] = useState({});
    const { api, configDispatcher } = props;
    const classes = useStyles();
    const [apiFromContext] = useAPI();

    useEffect(() => {
        API.apiCategories().then((response) => setCategories(response.body));
    }, []);

    if (!categories.list) {
        return null;
    } else if (categories.list.length === 0) {
        return (
            <Box style={{ position: 'relative', marginTop: 10 }}>
                <TextField
                    fullWidth
                    select
                    label={(
                        <>
                            <FormattedMessage
                                id='Apis.Details.Configurations.api.categories'
                                defaultMessage='API Categories'
                            />
                        </>
                    )}
                    margin='normal'
                    variant='outlined'
                    disabled
                    value='emptyMessage'
                >
                    <MenuItem
                        dense
                        disableGutters
                        value='emptyMessage'
                    >
                        <ListItemText primary={(
                            <FormattedMessage
                                id='Apis.Details.Configurations.api.categories.empty'
                                defaultMessage='No API Categories defined.'
                            />
                        )}
                        />
                    </MenuItem>
                </TextField>
            </Box>
        );
    } else {
        return (
            <Box style={{ position: 'relative', marginTop: 10 }}>
                <TextField
                    fullWidth
                    select
                    label={(
                        <>
                            <FormattedMessage
                                id='Apis.Details.Configurations.api.categories'
                                defaultMessage='API Categories'
                            />
                        </>
                    )}
                    name='categories'
                    margin='normal'
                    variant='outlined'
                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                    value={api.categories}
                    SelectProps={{
                        multiple: true,
                        renderValue: (selected) => (Array.isArray(selected) ? selected.join(', ') : selected),
                        MenuProps: {
                            anchorOrigin: {
                                vertical: 'bottom',
                                horizontal: 'left',
                            },
                            getContentAnchorEl: null,
                            keepMounted: true,
                            PaperProps: {
                                style: {
                                    maxHeight: 300,
                                    maxWidth: 300,
                                },
                            },
                        },
                    }}
                    onChange={(e) => configDispatcher({ action: 'categories', value: e.target.value })}
                    InputProps={{
                        id: 'itest-id-categories-input',
                    }}
                    helperText='Select API Categories for the API'
                >
                    { categories.list.map((category) => (
                        <MenuItem
                            dense
                            disableGutters
                            id={category.id}
                            key={category.name}
                            value={category.name}
                        >
                            <Checkbox color='primary' checked={api.categories.includes(category.name)} />
                            <ListItemText
                                primary={category.name}
                                secondary={category.description}
                                classes={{ primary: classes.listItemText }}
                            />
                        </MenuItem>
                    ))}
                </TextField>
                <Tooltip
                    title={(
                        <>
                            <p>
                                <FormattedMessage
                                    id='Api.category.dropdown.tooltip'
                                    defaultMessage={'Allow to group APIs that have similar attributes.'
                                        + ' There has to be pre-defined API categories in the'
                                        + ' environment in order to be attached to an API.'}
                                />
                            </p>
                        </>
                    )}
                    aria-label='API Categories'
                    placement='right-end'
                    interactive
                    className={classes.tooltip}
                >
                    <HelpOutline />
                </Tooltip>
            </Box>
        );
    }
}

APICategories.defaultProps = {
    categories: [],
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};

export default APICategories;
