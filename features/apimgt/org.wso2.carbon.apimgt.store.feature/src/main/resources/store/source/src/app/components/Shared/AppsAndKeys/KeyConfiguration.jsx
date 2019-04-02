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

import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import { FormattedMessage } from 'react-intl';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    FormControl: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    quotaHelp: {
        position: 'relative',
    },
    checkboxWrapper: {
        display: 'flex',
    },
    checkboxWrapperColumn: {
        display: 'flex',
        flexDirection: 'row',
    },
    group: {
        flexDirection: 'row',
    },
});
/**
 *
 *
 * @class KeyConfiguration
 * @extends {React.Component}
 */
class KeyConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            application: null,
            tokenType: 'OAUTH',
        };
        this.appId = this.props.selectedApp.appId || this.props.selectedApp.value;
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleTokenTypeChange = this.handleTokenTypeChange.bind(this);
    }

    /**
     *
     *
     * @param {*} event
     * @memberof KeyConfiguration
     */
    handleTextChange(event) {
        const { application } = this.state;
        const { currentTarget } = event;
        const { keyType } = this.props;
        const keys = application.keys.get(keyType) || {
            supportedGrantTypes: ['client_credentials'],
            keyType,
            tokenType: this.state.tokenType,
        };
        keys.callbackUrl = currentTarget.value;
        application.keys.set(keyType, keys);
        this.setState({ application });
    }

    /**
     *
     *
     * @param {*} event
     * @memberof KeyConfiguration
     */
    handleCheckboxChange(event) {
        const { application } = this.state;
        const { keyType } = this.props;
        const { currentTarget } = event;
        const keys = application.keys.get(keyType) || {
            supportedGrantTypes: ['client_credentials'],
            keyType,
            tokenType: this.state.tokenType,
        };
        let index;

        if (currentTarget.checked) {
            keys.supportedGrantTypes.push(currentTarget.id);
        } else {
            index = keys.supportedGrantTypes.indexOf(currentTarget.id);
            keys.supportedGrantTypes.splice(index, 1);
        }
        application.keys.set(keyType, keys);
        // update the state with the new array of options
        this.setState({ application });
    }

    /**
     *
     *
     * @param {*} event
     * @memberof KeyConfiguration
     */
    handleTokenTypeChange(event) {
        const { application } = this.state;
        const { keyType } = this.props;
        const keys = application.keys.get(keyType) || {
            supportedGrantTypes: ['client_credentials'],
            keyType: keyType,
            tokenType: this.state.tokenType,
        };
        keys.tokenType = event.target.value;
        application.keys.set(keyType, keys);
        // update the state with the new array of options
        this.setState({ application, tokenType: event.target.value });
    }

    /**
     *
     *
     * @returns
     * @memberof KeyConfiguration
     */
    getKeyStatus() {
        return this.hasKeys;
    }

    /**
     * We have to wrap the two update and generate methods in a single mehtod.
     *
     * @returns
     * @memberof KeyConfiguration
     */
    keygenWrapper() {
        if (this.hasKeys) {
            return this.updateKeys();
        } else {
            return this.generateKeys();
        }
    }

    /**
     * Fetch Application object by ID coming from URL path params and fetch related keys to display
     *
     * @returns
     * @memberof KeyConfiguration
     */
    generateKeys() {
        const { application } = this.state;
        const { keyType } = this.props;
        const keys = application.keys.get(keyType) || {
            supportedGrantTypes: ['client_credentials'],
        };
        if (!keys.callbackUrl) {
            keys.callbackUrl = 'https://wso2.am.com';
        }
        const keyPromiss = application.generateKeys(keyType, keys.supportedGrantTypes, keys.callbackUrl, keys.tokenType);
        return keyPromiss;
    }

    /**
     *
     *
     * @returns
     * @memberof KeyConfiguration
     */
    updateKeys() {
        const { application } = this.state;
        const { keyType } = this.props;
        const keys = application.keys.get(keyType);
        const updatePromiss = application.updateKeys(keys.tokenType, keyType, keys.supportedGrantTypes, keys.callbackUrl, keys.consumerKey, keys.consumerSecret);
        return updatePromiss;
    }

    /**
     *
     *
     * @memberof KeyConfiguration
     */
    componentDidMount() {
        const promised_app = Application.get(this.appId);
        promised_app
            .then((application) => {
                application.getKeys().then(() => this.setState({ application }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof KeyConfiguration
     */
    render() {
        const { notFound } = this.state;
        const { keyType } = this.props;
        if (notFound) {
            return <ResourceNotFound />;
        }
        if (!this.state.application) {
            return <Loading />;
        }
        const { classes } = this.props;
        const cs_ck_keys = this.state.application.keys.get(keyType);
        const consumerKey = cs_ck_keys && cs_ck_keys.consumerKey;
        const consumerSecret = cs_ck_keys && cs_ck_keys.consumerSecret;
        let supportedGrantTypes = cs_ck_keys && cs_ck_keys.supportedGrantTypes;
        const callbackUrl = cs_ck_keys && cs_ck_keys.callbackUrl;
        supportedGrantTypes = supportedGrantTypes || false;
        if (consumerKey) {
            this.hasKeys = true;
        } else {
            this.hasKeys = false;
        }
        return (
            <React.Fragment>
                <FormControl className={classes.FormControl} component='fieldset'>
                    <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                        <FormattedMessage id='token.type' defaultMessage='Token Type' />
                    </InputLabel>
                    <RadioGroup aria-label='Token Type' name='tokenType' className={classes.group} value={this.state.tokenType} onChange={this.handleTokenTypeChange}>
                        <FormControlLabel value='OAUTH' control={<Radio />} label='OAUTH' />
                        <FormControlLabel value='JWT' control={<Radio />} label='JWT' />
                    </RadioGroup>
                </FormControl>
                <FormControl className={classes.FormControl} component='fieldset'>
                    <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                        <FormattedMessage id='grant.types' defaultMessage='Grant Types' />
                    </InputLabel>
                    <div className={classes.checkboxWrapper}>
                        <div className={classes.checkboxWrapperColumn}>
                            <FormControlLabel control={<Checkbox id='refresh_token' checked={supportedGrantTypes && supportedGrantTypes.includes('refresh_token')} onChange={this.handleCheckboxChange} value='refresh_token' />} label='Refresh Token' />
                            <FormControlLabel control={<Checkbox id='password' checked={supportedGrantTypes && supportedGrantTypes.includes('password')} value='password' onChange={this.handleCheckboxChange} />} label='Password' />
                            <FormControlLabel control={<Checkbox id='implicit' checked={supportedGrantTypes && supportedGrantTypes.includes('implicit')} value='implicit' onChange={this.handleCheckboxChange} />} label='Implicit' />
                        </div>
                        <div className={classes.checkboxWrapperColumn}>
                            <FormControlLabel control={<Checkbox id='code' checked={supportedGrantTypes && supportedGrantTypes.includes('code')} value='code' onChange={this.handleCheckboxChange} />} label='Code' />
                            <FormControlLabel control={<Checkbox id='client_credentials' checked disabled value='client_credentials' />} label='Client Credential' />
                        </div>
                    </div>
                    <FormHelperText>The application can use the following grant types to generate Access Tokens. Based on the application requirement, you can enable or disable grant types for this application.</FormHelperText>
                </FormControl>

                {
                    // (supportedGrantTypes && (supportedGrantTypes.includes("implicit") || supportedGrantTypes.includes("code")))  &&
                    <FormControl className={classes.FormControlOdd}>
                        <TextField id='callbackURL' fullWidth onChange={this.handleTextChange} label='Callback URL' placeholder='http://url-to-webapp' className={classes.textField} margin='normal' value={callbackUrl} />
                        <FormHelperText>Callback URL is a redirection URI in the client application which is used by the authorization server to send the client's user-agent (usually web browser) back after granting access.</FormHelperText>
                    </FormControl>
                }
            </React.Fragment>
        );
    }
}

export default withStyles(styles)(KeyConfiguration);
