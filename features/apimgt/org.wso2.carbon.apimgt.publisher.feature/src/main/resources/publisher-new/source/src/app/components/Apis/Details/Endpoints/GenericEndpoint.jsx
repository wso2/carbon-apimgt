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
import {
    Divider,
    Icon,
    IconButton,
    InputAdornment,
    TextField,
    withStyles,
} from '@material-ui/core';
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
    } = props;
    const [serviceUrl, setServiceUrl] = useState(endpointURL);

    useEffect(() => {
        setServiceUrl(endpointURL);
    }, [endpointURL]);
    return (
        <React.Fragment className={classes.endpointInputWrapper}>
            <TextField
                label={<FormattedMessage
                    id='Apis.Details.Endpoints.GenericEndpoint.service.url.input'
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
                    readOnly,
                    endAdornment: (
                        <InputAdornment position='end'>
                            {type === 'prototyped' ?
                                <div /> :
                                <IconButton
                                    className={classes.iconButton}
                                    aria-label='Settings'
                                    onClick={() => setAdvancedConfigOpen(index, type, category)}
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
        </React.Fragment>);
}

GenericEndpoint.defaultProps = {
    readOnly: false,
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
};

export default withStyles(styles)(GenericEndpoint);
