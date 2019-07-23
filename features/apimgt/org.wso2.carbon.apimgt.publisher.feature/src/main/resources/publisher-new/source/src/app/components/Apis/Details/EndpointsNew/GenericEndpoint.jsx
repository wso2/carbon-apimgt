/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useState } from 'react';
import { Divider, Icon, IconButton, InputAdornment, TextField, withStyles } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

const styles = theme => ({
    endpointInputWrapper: {
        width: '100%',
        display: 'flex',
        justifyContent: 'space-between',
    },
    textField: {
        width: '100%',
    },
    input: {
        marginLeft: theme.spacing.unit,
        flex: 1,
    },
    iconButton: {
        padding: theme.spacing.unit,
    },
    divider: {
        width: 1,
        height: 28,
        margin: 4,
    },
});
function GenericEndpoint(props) {
    const {
        category,
        endpointURL,
        editEndpoint,
        classes,
        type,
        setAdvancedConfigOpen,
        deleteEndpoint,
        index,
    } = props;
    const [serviceUrl, setServiceUrl] = useState(endpointURL);

    useEffect(() => {
        setServiceUrl(endpointURL);
    }, [props]);
    return (
        <div className={classes.endpointInputWrapper}>
            <TextField
                id='endpoint-input'
                label={<FormattedMessage
                    id='Apis.Details.EndpointsNew.GenericEndpoint.service.url.input'
                    defaultMessage='Service URL'
                />}
                className={classes.textField}
                value={serviceUrl}
                placeholder={!serviceUrl ? 'http://appserver/resource' : ''}
                onChange={event => setServiceUrl(event.target.value)}
                onBlur={() => editEndpoint(index, category, serviceUrl)}
                variant='outlined'
                margin='normal'
                required
                InputProps={{
                    endAdornment: (
                        <InputAdornment position='end'>
                            <IconButton
                                className={classes.iconButton}
                                aria-label='Settings'
                                onClick={() => setAdvancedConfigOpen(index, type, category)}
                            >
                                <Icon>
                                    settings
                                </Icon>
                            </IconButton>
                            {(index > 0) ? <Divider className={classes.divider} /> : <div />}
                            {(type === 'load_balance' || type === 'failover') ? (
                                <IconButton
                                    className={classes.iconButton}
                                    aria-label='Delete'
                                    onClick={() => deleteEndpoint(index, type, category)}
                                >
                                    <Icon>
                                        delete
                                    </Icon>
                                </IconButton>
                            ) : (<div />)}
                        </InputAdornment>
                    ),
                }}
            />
        </div>);
}

GenericEndpoint.propTypes = {
    endpointURL: PropTypes.string.isRequired,
    deleteEndpoint: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
    setAdvancedConfigOpen: PropTypes.func.isRequired,
    index: PropTypes.number.isRequired,
    editEndpoint: PropTypes.func.isRequired,
    category: PropTypes.string.isRequired,
};

export default withStyles(styles)(GenericEndpoint);
