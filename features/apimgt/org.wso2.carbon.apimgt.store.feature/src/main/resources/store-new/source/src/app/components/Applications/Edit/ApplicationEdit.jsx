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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import ArrowBack from '@material-ui/icons/ArrowBack';
import TextField from '@material-ui/core/TextField';
import Input, { InputLabel } from '@material-ui/core/Input';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import { MenuItem } from '@material-ui/core/Menu';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import API from '../../../data/api';
import Alert from '../../Shared/Alert';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginLeft: 20,
    },
    buttonsWrapper: {
        marginTop: 40,
    },
    inputText: {
        marginTop: 20,
    },
    buttonAlignment: {
        marginLeft: 20,
    },
    buttonRight: {
        textDecoration: 'none',
    },
});
/**
 *
 *
 * @class ApplicationEdit
 * @extends {Component}
 */
class ApplicationEdit extends Component {
    constructor(props) {
        super(props);
        this.state = {
            name: null,
            quota: 'Unlimited',
            description: null,
            id: null,
            tiers: [],
            notFound: false,
            lifeCycleStatus: null,
        };
    }

    /**
     *
     *
     * @memberof ApplicationEdit
     */
    componentDidMount() {
        const api = new API();
        const promised_application = Application.get(this.props.match.params.application_id);
        const promised_tiers = api.getAllTiers('application');
        Promise.all([promised_application, promised_tiers])
            .then((response) => {
                const [application, tierResponse] = response;
                this.setState({
                    quota: application.throttlingTier,
                    name: application.name,
                    description: application.description,
                    id: application.id,
                    lifeCycleStatus: application.lifeCycleStatus,
                });
                const tiers = [];
                tierResponse.body.list.map(item => tiers.push(item.name));
                this.setState({ tiers });
            })
            .catch((error) => {
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
                console.error(error);
            });
    }

    /**
     *
     *
     * @memberof ApplicationEdit
     */
    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    /**
     *
     *
     * @memberof ApplicationEdit
     */
    handleSubmit = (event) => {
        event.preventDefault();
        if (!this.state.name) {
            Alert.error('Application name is required');
        } else {
            const updated_application = {
                id: this.state.id,
                name: this.state.name,
                throttlingTier: this.state.quota,
                description: this.state.description,
                lifeCycleStatus: this.state.lifeCycleStatus,
            };
            const api = new API();
            const promised_update = api.updateApplication(updated_application, null);
            promised_update
                .then((response) => {
                    const appId = response.body.applicationId;
                    const redirectUrl = '/applications/' + appId;
                    this.props.history.push(redirectUrl);
                    console.log('Application updated successfully.');
                })
                .catch((error) => {
                    Alert.error('Error while updating application');
                    console.log('Error while updating application ' + error);
                });
        }
    };

    /**
     *
     *
     * @returns
     * @memberof ApplicationEdit
     */
    render() {
        const { classes } = this.props;
        const {
            name, tiers, notFound, id, quota, description,
        } = this.state;
        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <Grid container spacing={0} justify='flex-start'>
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10} className={classes.titleBar}>
                    <div className={classes.buttonLeft}>
                        <Link to={'/applications/' + id}>
                            <Button variant='raised' size='small' className={classes.buttonBack} color='default'>
                                <ArrowBack />
                            </Button>
                        </Link>
                        <div className={classes.title}>
                            <Typography variant='display1'>Go Back</Typography>
                        </div>
                    </div>
                </Grid>
                <Grid item xs={12} lg={6} xl={4}>
                    <form className={classes.container} noValidate autoComplete='off'>
                        <TextField
                            required
                            label='Application Name'
                            value={name}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText='Enter a name to identify the Application. You will be able to pick this application when subscribing to APIs '
                            fullWidth
                            name='name'
                            onChange={this.handleChange('name')}
                            placeholder='My Mobile Application'
                            autoFocus
                            className={classes.inputText}
                        />
                        {tiers && (
                            <FormControl margin='normal'>
                                <InputLabel htmlFor='quota-helper'>Per Token Quota</InputLabel>
                                <Select value={quota} onChange={this.handleChange('quota')} input={<Input name='quota' id='quota-helper' />}>
                                    {tiers.map(tier => (
                                        <MenuItem key={tier} value={tier}>
                                            {tier}
                                        </MenuItem>
                                    ))}
                                </Select>
                                <div>Assign API request quota per access token. Allocated quota will be shared among all the subscribed APIs of the application.</div>
                            </FormControl>
                        )}
                        <TextField
                            label='Application Description'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            value={description}
                            helperText='Describe the application'
                            fullWidth
                            multiline
                            rowsMax='4'
                            name='description'
                            onChange={this.handleChange('description')}
                            placeholder='This application is grouping apis for my mobile application'
                            className={classes.inputText}
                        />
                        <div className={classes.buttonsWrapper}>
                            <Button variant='raised' color='primary' onClick={this.handleSubmit}>
                                Update
                            </Button>
                            <Link to='/applications' className={classes.buttonRight}>
                                <Button variant='raised' className={classes.buttonAlignment}>
                                    Cancel
                                </Button>
                            </Link>
                        </div>
                    </form>
                </Grid>
            </Grid>
        );
    }
}
ApplicationEdit.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ApplicationEdit);
