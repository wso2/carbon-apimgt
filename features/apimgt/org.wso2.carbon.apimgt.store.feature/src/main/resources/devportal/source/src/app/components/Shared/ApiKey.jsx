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
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Validation from 'AppData/Validation';

// Styles for Grid and Paper elements
const styles = theme => ({
    FormControl: {
        'margin-bottom': '8px',
        width: '100%',
        padding: '0px 10px',
    },
});

/**
 * Used to display generate api key in UI
 */
const tokens = (props) => {
    const [infiniteValidity, setInfiniteValidity] = useState(true);
    const [invalidTimeout, setInvaildTimeout] = useState(false);

    /**
    * This method is used to handle the updating of create api key
    * request object.
    * @param {*} field
    * @param {*} event event fired
    */
    const handleChange = (field, event) => {
        const { accessTokenRequest, updateAccessTokenRequest } = props;
        const newRequest = { ...accessTokenRequest };

        const { target: currentTarget } = event;

        switch (field) {
            case 'infiniteValidity':
                setInfiniteValidity(currentTarget.checked);
                if (currentTarget.checked) {
                    newRequest.timeout = -1;
                } else {
                    newRequest.timeout = null;
                }
                break;
            case 'timeout':
                if (Validation.number.validate(currentTarget.value).error === undefined) {
                    newRequest.timeout = currentTarget.value;
                    setInvaildTimeout(false);
                } else {
                    newRequest.timeout = null;
                    setInvaildTimeout(true);
                }
                break;
            default:
                break;
        }
        updateAccessTokenRequest(newRequest);
    };
    const { classes, intl, accessTokenRequest } = props;

    return (
        <React.Fragment>
            <FormControl margin='normal' className={classes.FormControl}>
                <FormControlLabel
                    control={<Checkbox
                        checked={infiniteValidity}
                        onChange={e => handleChange('infiniteValidity', e)}
                        value={accessTokenRequest.timeout}
                        color='primary'
                    />}
                    label='API Key with infinite validity period'
                />
                {!infiniteValidity && <TextField
                    required
                    label={intl.formatMessage({
                        defaultMessage: 'API Key validity period',
                        id: 'Shared.AppsAndKeys.Tokens.apikey',
                    })}
                    InputLabelProps={{
                        shrink: true,
                    }}
                    helperText={
                        invalidTimeout ? (
                            intl.formatMessage({
                                defaultMessage: 'Please use a valid number for API Key expiry time',
                                id: 'Shared.AppsAndKeys.Tokens.apikey.set.validity.error',
                            })
                        ) : (
                            intl.formatMessage({
                                defaultMessage: 'You can set an expiration period to determine the validity period of '
                                + 'the token after generation. Set this as -1 to ensure that the '
                                + 'apikey never expires.',
                                id: 'Shared.AppsAndKeys.Tokens.apikey.set.validity.help',
                            })
                        )
                    }
                    fullWidth
                    name='timeout'
                    onChange={e => handleChange('timeout', e)}
                    placeholder={intl.formatMessage({
                        defaultMessage: 'Enter time in seconds',
                        id: 'Shared.AppsAndKeys.Tokens.apikey.enter.time',
                    })}
                    value={accessTokenRequest.timeout}
                    autoFocus
                    className={classes.inputText}
                    error={invalidTimeout}
                />
                }
            </FormControl>
        </React.Fragment>
    );
};
tokens.contextTypes = {
    intl: PropTypes.shape({}).isRequired,
};
export default injectIntl(withStyles(styles)(tokens));
