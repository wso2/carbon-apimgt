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

import React, { useEffect, useState, useContext } from 'react';
import {
    Divider,
    Icon,
    IconButton,
    InputAdornment,
    TextField,
    withStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

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
        marginLeft: theme.spacing(),
        flex: 1,
    },
    iconButton: {
        padding: theme.spacing(),
    },
    divider: {
        width: 1,
        height: 28,
        margin: 4,
    },
});
/**
 * This component represents a single endpoint view of the endpoints listing. This component holds the actions that
 * affect the endpont. Eg: Delete, advance configuration.
 *
 * @param {any} props The input props
 * @returns {any} The HTML representation of the component.
 * */
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
        readOnly,
        autoFocus,
        name,
    } = props;
    const [serviceUrl, setServiceUrl] = useState(endpointURL);
    const { api } = useContext(APIContext);

    useEffect(() => {
        setServiceUrl(endpointURL);
    }, [endpointURL]);

    return (
        <div className={classes.endpointInputWrapper}>
            <TextField
                disabled={isRestricted(['apim:api_create'], api)}
                label={name}
                className={classes.textField}
                value={serviceUrl}
                placeholder={!serviceUrl ? 'http://appserver/resource' : ''}
                onChange={event => setServiceUrl(event.target.value)}
                onBlur={() => editEndpoint(index, category, serviceUrl)}
                variant='outlined'
                margin='normal'
                required
                InputProps={{
                    readOnly,
                    autoFocus,
                    endAdornment: (
                        <InputAdornment position='end'>
                            <IconButton
                                className={classes.iconButton}
                                aria-label='Settings'
                                // onClick={() => testEndpoint(index, type, category)}
                                disabled={(isRestricted(['apim:api_create'], api))}
                            >
                                <Icon>
                                    check_circle
                                </Icon>
                            </IconButton>
                            {type === 'prototyped' ?
                                <div /> :
                                <IconButton
                                    className={classes.iconButton}
                                    aria-label='Settings'
                                    onClick={() => setAdvancedConfigOpen(index, type, category)}
                                    disabled={(isRestricted(['apim:api_create'], api))}
                                >
                                    <Icon>
                                        settings
                                    </Icon>
                                </IconButton>
                            }
                            {(index > 0) ? <Divider className={classes.divider} /> : <div />}
                            {(type === 'load_balance' || type === 'failover') ? (
                                <IconButton
                                    className={classes.iconButton}
                                    aria-label='Delete'
                                    color='secondary'
                                    onClick={() => deleteEndpoint(index, type, category)}
                                    disabled={(isRestricted(['apim:api_create'], api))}
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

GenericEndpoint.defaultProps = {
    readOnly: false,
    autoFocus: false,
    name: 'Service URL',
};

GenericEndpoint.propTypes = {
    endpointURL: PropTypes.string.isRequired,
    deleteEndpoint: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
    setAdvancedConfigOpen: PropTypes.func.isRequired,
    index: PropTypes.number.isRequired,
    editEndpoint: PropTypes.func.isRequired,
    category: PropTypes.string.isRequired,
    readOnly: PropTypes.bool,
    autoFocus: PropTypes.bool,
    name: PropTypes.string,
};

export default withStyles(styles)(GenericEndpoint);
