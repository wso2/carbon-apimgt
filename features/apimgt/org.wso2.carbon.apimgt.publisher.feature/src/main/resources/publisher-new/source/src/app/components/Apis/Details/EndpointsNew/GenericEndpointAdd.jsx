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
import { TextField, Button, FormControl, InputAdornment, Divider, InputLabel, withStyles, Input, Icon, InputBase, IconButton, Paper } from '@material-ui/core';
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
    endpointEditBtnWrapper: {
        display: 'flex',
    },
    root: {
        width: '80%',
        margin: '2px 0 2px 0',
    },
    input: {
        marginLeft: 8,
        flex: 1,
    },
    iconButton: {
        padding: 10,
    },
    divider: {
        width: 1,
        height: 28,
        margin: 4,
    },
});
function GenericEndpointAdd(props) {
    const {
        classes,
        addEndpoint,
    } = props;
    const [serviceUrl, setServiceUrl] = useState('');
    const [isError, setError] = useState(false);

    const onAddEndpoint = () => {
        setServiceUrl('');
        addEndpoint(serviceUrl);
    };
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
                onChange={event => setServiceUrl(event.target.value)}
                onBlur={event => setError(event.target.value === '')}
                variant='outlined'
                margin='normal'
                placeholder='Add new endpoint'
                error={isError}
                required
                InputProps={{
                    endAdornment: (
                        <InputAdornment position='end'>
                            <IconButton
                                onClick={onAddEndpoint}
                                color='green'
                                className={classes.iconButton}
                                aria-label='Search'
                                disabled={serviceUrl === ''}
                            >
                                <Icon>
                                    add
                                </Icon>
                            </IconButton>
                        </InputAdornment>
                    ),
                }}
            />
        </div>);
}

GenericEndpointAdd.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(GenericEndpointAdd);
