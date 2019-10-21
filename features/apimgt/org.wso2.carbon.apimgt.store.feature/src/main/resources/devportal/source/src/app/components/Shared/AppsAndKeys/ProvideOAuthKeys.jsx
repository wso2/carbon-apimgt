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
import { withStyles } from '@material-ui/core/styles';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    textField: {
        marginTop: 0,
    },
});

/**
 * Provide Consumer Key and Secret of existing Auth apps
 *
 * @param props
 * @returns {*}
 * @constructor
 */
function ProvideOAuthKeys(props) {
    const {
        classes, consumerKey, consumerSecret, intl, onChange, isUserOwner,
    } = props;

    /**
     * Handle onChange of provided consumer key and secret
     *
     * @param event
     */
    function handleChange(event) {
        if (onChange) {
            onChange(event);
        }
    }

    return (
        <div>
            <Grid container spacing={3} direction='column'>
                <Grid item xs={6}>
                    <TextField
                        id='provided-consumer-key'
                        name='providedConsumerKey'
                        className={classes.textField}
                        label={intl.formatMessage({
                            defaultMessage: 'Consumer Key',
                            id: 'Shared.AppsAndKeys.ProvideOAuthKeys.consumer.key',
                        })}
                        value={consumerKey}
                        onChange={e => handleChange(e)}
                        margin='normal'
                        fullWidth
                        disabled={!isUserOwner}
                        variant='outlined'
                    />
                    <FormControl>
                        <FormHelperText id='consumer-key-helper-text'>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.ProvideOAuthKeys.consumer.key.title'
                                defaultMessage='Consumer Key of the OAuth application'
                            />
                        </FormHelperText>
                    </FormControl>
                </Grid>
                <Grid item xs={6}>
                    <TextField
                        id='provided-consumer-secret'
                        name='providedConsumerSecret'
                        label={intl.formatMessage({
                            defaultMessage: 'Consumer Secret',
                            id: 'Shared.AppsAndKeys.ProvideOAuthKeys.consumer.secret',
                        })}
                        className={classes.textField}
                        value={consumerSecret}
                        onChange={e => handleChange(e)}
                        margin='normal'
                        fullWidth
                        disabled={!isUserOwner}
                        variant='outlined'
                    />
                    <FormControl>
                        <FormHelperText id='consumer-secret-helper-text'>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.ProvideOAuthKeys.consumer.secret.of.application'
                                defaultMessage='Consumer Secret of the OAuth application'
                            />
                        </FormHelperText>
                    </FormControl>
                </Grid>
            </Grid>
        </div>
    );
}

ProvideOAuthKeys.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    onChange: PropTypes.func.isRequired,
    consumerKey: PropTypes.string,
    consumerSecret: PropTypes.string,
    isUserOwner: PropTypes.string,
};

ProvideOAuthKeys.defaultProps = {
    consumerKey: '',
    consumerSecret: '',
    isUserOwner: false,
};

export default injectIntl(withStyles(styles)(ProvideOAuthKeys));
