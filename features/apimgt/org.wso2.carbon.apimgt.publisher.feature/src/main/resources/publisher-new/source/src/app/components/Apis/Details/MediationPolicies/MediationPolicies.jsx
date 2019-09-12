/* eslint-disable require-jsdoc */
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
/* eslint no-param-reassign: ["error", { "props": false }] */
import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';

import API from 'AppData/api.js';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import MediationPolicyComponent from './MediationPolicyComponent';
import Create from './Create';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    paperRoot: {
        padding: 20,
        marginTop: 20,
    },
});

class MediationPolicies extends React.Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.api.id;
        this.api = props;
        const { mediationPolicies } = props.api;
        this.state = {

            // all mediation policies
            inSequences: [],
            outSequences: [],
            faultSequences: [],
            // api specific mediation policies
            inSeqCustom: [],
            outSeqCustom: [],
            faultSeqCustom: [],
            // selected mediation policies
            inMediationPolicy: mediationPolicies.filter(seq => seq.type === 'in'),
            outMediationPolicy: mediationPolicies.filter(seq => seq.type === 'out'),
            faultMediationPolicy: mediationPolicies.filter(seq => seq.type === 'fault'),
        };
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    componentDidMount() {
        this.globalInMediationPolicies = [];
        this.globalOutMediationPolicies = [];
        this.globalFaultMediationPolicies = [];

        this.inSequences = this.state;
        this.outSequences = this.state;
        this.faultSequences = this.state;
        this.inSeqCustom = this.state;
        this.outSeqCustom = this.state;
        this.faultSeqCustom = this.state;
        API.getGlobalMediationPolicies()
            .then((response) => {
                this.globalInMediationPolicies = response.obj.list.filter(seq => seq.type === 'IN');
                this.globalOutMediationPolicies = response.obj.list.filter(seq => seq.type === 'OUT');
                this.globalFaultMediationPolicies = response.obj.list.filter(seq => seq.type === 'FAULT');
                // this.setState();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
        API.getMediationPolicies(this.api_uuid)
            .then((response) => {
                this.inSeqCustom = response.obj.list.filter(seq => seq.type === 'IN');
                this.outSeqCustom = response.obj.list.filter(seq => seq.type === 'OUT');
                this.faultSeqCustom = response.obj.list.filter(seq => seq.type === 'FAULT');
                this.inSequences = [...this.globalInMediationPolicies, ...this.inSeqCustom];
                this.outSequences = [...this.globalOutMediationPolicies, ...this.outSeqCustom];
                this.faultSequences = [...this.globalFaultMediationPolicies, ...this.faultSeqCustom];
                this.setState({
                    inSequences: this.inSequences,
                    outSequences: this.outSequences,
                    faultSequences: this.faultSequences,
                    inSeqCustom: this.inSeqCustom,
                    outSeqCustom: this.outSeqCustom,
                    faultSeqCustom: this.faultSeqCustom,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }
    handleInputChange = (policy) => {
        if (policy.type === 'in') {
            this.setState({
                inMediationPolicy: policy,
            });
        }
    };
    handleSubmit(updateAPI) {
        const { inMediationPolicy, outMediationPolicy, faultMediationPolicy } = this.state;
        const mediationPolicies = { inMediationPolicy, outMediationPolicy, faultMediationPolicy };
        updateAPI(mediationPolicies);
    }
    render() {
        const { classes } = this.props;
        const isInFlowPolicyExists = this.state.inSeqCustom.length > 0;
        const isOutFlowPolicyExists = this.state.outSeqCustom.length > 0;
        const isFaultFlowPolicyExists = this.state.faultSeqCustom.length > 0;
        if (this.state.notFound) {
            return (<ResourceNotFound />);
        }
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.MediationPolicies.MediationPolicies'
                            defaultMessage='Message Mediation Policies'
                        />
                    </Typography>
                </div>
                <ApiContext.Consumer>
                    {({ api, updateAPI }) => (
                        <Grid container spacing={24} className={classes.FormControl}>
                            <Grid item xs={12}>
                                <Paper className={classes.paperRoot} elevation={1}>
                                    {
                                        isInFlowPolicyExists ? (
                                            <MediationPolicyComponent
                                                api={api}
                                                sequences={this.state.inSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                type='in'
                                                selectedMediationPolicy={this.state.inMediationPolicy}
                                            />
                                        ) : (
                                            <Create
                                                api={api}
                                                sequences={this.state.inSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                policyExists={isInFlowPolicyExists}
                                                apiId={api.id}
                                                type='in'
                                            />)}
                                    {
                                        isOutFlowPolicyExists ? (
                                            <MediationPolicyComponent
                                                api={api}
                                                sequences={this.state.outSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                type='out'
                                                selectedMediationPolicy={this.state.outMediationPolicy}
                                            />
                                        ) : (
                                            <Create
                                                api={api}
                                                sequences={this.state.outSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                policyExists={isOutFlowPolicyExists}
                                                apiId={api.id}
                                                type='out'
                                            />
                                        )}
                                    {
                                        isFaultFlowPolicyExists ? (
                                            <MediationPolicyComponent
                                                api={api}
                                                sequences={this.state.faultSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                type='fault'
                                                selectedMediationPolicy={this.state.faultMediationPolicy}
                                            />
                                        ) : (
                                            <Create
                                                api={api}
                                                sequences={this.state.faultSequences}
                                                handleInputChange={this.handleInputChange}
                                                classes={classes}
                                                policyExists={isFaultFlowPolicyExists}
                                                apiId={api.id}
                                                type='fault'
                                            />
                                        )}
                                </Paper>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={16}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    onClick={() => this.handleSubmit(updateAPI)}
                                                >
                                                    <FormattedMessage id='save' defaultMessage='Save' />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button>
                                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Grid>
                        </Grid>
                    )}
                </ApiContext.Consumer>
            </div>
        );
    }
}

MediationPolicies.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    inSequences: PropTypes.shape({}).isRequired,
    faultSequences: PropTypes.shape({}).isRequired,
    outSequences: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    inSeqCustom: PropTypes.shape({}).isRequired,
    outSeqCustom: PropTypes.shape({}).isRequired,
    faultSeqCustom: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(MediationPolicies);
