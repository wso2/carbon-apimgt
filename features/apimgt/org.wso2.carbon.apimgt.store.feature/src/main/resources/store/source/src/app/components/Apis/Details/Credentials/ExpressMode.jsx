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

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import ArrowForward from '@material-ui/icons/ArrowForward';
import InputBase from '@material-ui/core/InputBase';
import Select from '@material-ui/core/Select';
import Input from '@material-ui/core/Input';
import MenuItem from '@material-ui/core/MenuItem';
import RadioGroup from '@material-ui/core/RadioGroup';
import Radio from '@material-ui/core/Radio';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Button from  '@material-ui/core/Button';
import Alert from '../../../Shared/Alert';

// import ApiContext from '../../ApiContext'
import API from '../../../../data/api';
import Application from '../../../../data/Application';
import ApplicationCreate from '../../../Shared/AppsAndKeys/ApplicationCreate';
import SubscribeToApi from '../../../Shared/AppsAndKeys/SubscribeToApi';
import Keys from '../../../Shared/AppsAndKeys/KeyConfiguration';
import Tokens from '../../../Shared/AppsAndKeys/Tokens';
import ViewToken from '../../../Shared/AppsAndKeys/ViewToken';
import { ApiContext } from '../ApiContext';

const styles = theme => ({
    root: {
        paddingLeft: 40,
        maxWidth: theme.custom.contentAreaWidth,
    },
    arrowTextContainer: {
        display: 'flex',
        alignItems: 'center',
    },
    margin: {
        margin: 0,
    },
    tokenType: {
        paddingLeft: theme.spacing.unit,
    },
    tableClass: {
        '& td': {
            verticalAlign: 'middle',
        },
    },
    tableClassOther: {
        '& td': {
            verticalAlign: 'top',
        },
    },
    descCol: {
        verticalAlign: 'top !important',
        paddingTop: 10,
    },
    group: {
        flexDirection: 'row',
    },
});
const restApi = new API();

class ExpressMode extends Component {
    constructor(props) {
        super(props);
        this.state = {
            newApp: null,


            tiers: null,
            quota: 'Unlimited',
            appName: null,
            appDescription: 'Auto generated application',
            apiPolicyName: null,
            tokenType: "OAUTH",
            callBackURL: "http://localhost",
            supportedGrantTypes: ['client_credentials'],
            restApplication: null,
        };
    }

    /**
     *
     *
     * @memberof ExpressMode
     */
    componentDidMount() {
        // Get all the tires to populate the drop down.
        const promised_tiers = restApi.getAllTiers('application');
        promised_tiers
            .then((response) => {
                const tierResponseObj = response.body;
                const tiers = [];
                tierResponseObj.list.map(item => tiers.push(item.name));
                this.setState({ tiers });

                if (tiers.length > 0) {
                    this.setState({ quota: tiers[0] });
                }
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
        this.setState({
            appName: Math.random()
                .toString(36)
                .substr(2, 5),
        });
    }

    /**
     *
     *
     * @memberof ApplicationCreate
     */
    handleInputChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };
    generateCredentials = () => {
        let { api } = this.props;
        let { name, quota, description, supportedGrantTypes, callbackUrl, tokenType } = this.state;
        const application_data = {
            name: name,
            throttlingTier: quota,
            description: description,
        };
        restApi.createApplication(application_data).then((response) => {
            const appCreated = JSON.parse(response.data);
            // Once application loading fixed this need to pass application ID and load app
            console.log('Application created successfully.', appCreated, api);
            const restApplication = Application.get(appCreated.applicationId);

            
            const promiseKey = restApplication.generateKeys("SANDBOX", supportedGrantTypes, callbackUrl, tokenType);
            promiseKey.then( (x) => {
                console.info(x);
            }).catch( (error) => {
                console.info(error);
            } );

    
        })
        .catch((error_response) => {
            Alert.error('Application already exists.');
            console.log('Error while creating the application');
        });
    }
    render() {
        const { classes, api } = this.props;
        let {
            newApp, tiers, appName, appDescription, quota, apiPolicyName, tokenType, callBackURL, 
        } = this.state;
        const apiTiersList = [];
        if (api && api.policies) {
            for (let i = 0; i < api.policies.length; i++) {
                const tierName = api.policies[i];
                apiTiersList.push({ value: tierName, label: tierName });
            }
            if (apiTiersList.length > 0 && !apiPolicyName) {
                apiPolicyName = apiTiersList[0].value;
            }
        }
        let supportedGrantTypes=  ['client_credentials'];
        return (
            <ApiContext.Consumer>
                {({ api, applicationsAvailable }) => (
                <Grid container spacing={24} className={classes.root}>
                    <Grid item xs={12}>
                        <Typography variant='body1'>
                            Express mode take you through application creation, key generation, and subscription process
                            with following settings in one go. Click the values to change them to new values.
                        </Typography>
                        <Button variant="contained" color="primary" className={classes.button} onClick={this.generateCredentials}>
                            Generate Credentials for 
                        </Button>
                    </Grid>
                    <Grid item xs={3}>
                        <div className={classes.arrowTextContainer}>
                            <Typography variant='body1'>New Application</Typography>
                            <ArrowForward />
                        </div>
                    </Grid>
                    <Grid item xs={3}>
                        <div className={classes.arrowTextContainer}>
                            <Typography variant='body1'>
                                Subscribe <strong>{api.name}</strong> to Application <strong>{appName}</strong>
                            </Typography>
                            <ArrowForward />
                        </div>
                    </Grid>
                    <Grid item xs={6}>
                        <div className={classes.arrowTextContainer}>
                            <Typography variant='body1'>Generate Keys</Typography>
                        </div>
                    </Grid>
                    <Grid item xs={3}>
                        <ApplicationCreate innerRef={node => (this.applicationCreate = node)} />
                    </Grid>
                    <Grid item xs={3}>
                        
                        <SubscribeToApi innerRef={node => (this.subscribeToApi = node)} newApp={newApp} api={api} applicationsAvailable={applicationsAvailable} />;
                        
                    </Grid>
                    <Grid item xs={6}>
                        <Keys innerRef={node => (this.keys = node)} selectedApp={newApp} keyType='SANDBOX' />
                        
                    </Grid>
                </Grid>
                )}
                </ApiContext.Consumer>
        );
    }
}

ExpressMode.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ExpressMode);
