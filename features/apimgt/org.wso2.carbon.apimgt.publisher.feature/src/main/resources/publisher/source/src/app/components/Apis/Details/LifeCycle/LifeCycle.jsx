/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';

import LifeCycleUpdate from './LifeCycleUpdate';
import LifeCycleHistory from './LifeCycleHistory';

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
        maxWidth: theme.custom.contentAreaWidth,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    historyHead: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
});
/**
 *
 *
 * @class LifeCycle
 * @extends {Component}
 */
class LifeCycle extends Component {
    /**
     * Creates an instance of LifeCycle.
     * @param {Object} props
     * @memberof LifeCycle
     */
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {
            lcHistory: null,
            checkList: [],
        };
        this.updateData = this.updateData.bind(this);
        this.handleChangeCheckList = this.handleChangeCheckList.bind(this);
    }

    /**
     *
     * @inheritdoc
     * @memberof LifeCycle
     */
    componentDidMount() {
        this.updateData();
    }

    handleChangeCheckList = (index) => (event, checked) => {
        const { checkList } = this.state;
        checkList[index].checked = checked;
        this.setState({ checkList });
    };

    /**
     *
     *
     * @memberof LifeCycle
     */
    updateData() {
        const { api: { id } } = this.props;
        const promisedAPI = Api.get(id);
        // const promised_tiers = Api.policies('api');
        const promisedLcState = this.api.getLcState(id);
        const privateJetModeEnabled = false;

        const promisedLcHistory = this.api.getLcHistory(id);
        // const promised_labels = this.api.labels();
        Promise.all([promisedAPI, promisedLcState, promisedLcHistory])
            .then((response) => {
                const api = response[0];
                const lcState = response[1].body;
                const lcHistory = response[2].body.list;

                if (privateJetModeEnabled) {
                    if (!api.hasOwnGateway) {
                        const transitions = lcState.availableTransitionBeanList;
                        const PUBLISHED = 'Published';

                        for (const transition of transitions) {
                            if (transition.targetState === PUBLISHED && lcState.state !== PUBLISHED) {
                                const publishInPrivateJetMode = {
                                    event: 'Publish In Private Jet Mode',
                                    targetState: 'Published In Private Jet Mode',
                                };
                                lcState.availableTransitionBeanList.push(publishInPrivateJetMode);
                            }
                        }
                    }
                }
                // Creating checklist
                const checkList = [];
                let index = 0;
                for (const item of lcState.checkItems) {
                    checkList.push({
                        index,
                        label: item.name,
                        value: item.name,
                        checked: false,
                    });
                    index++;
                }
                this.setState({
                    api,
                    lcState,
                    lcHistory,
                    privateJetModeEnabled,
                    checkList,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof LifeCycle
     */
    render() {
        const { classes } = this.props;
        const {
            api, lcState, privateJetModeEnabled, checkList, lcHistory,
        } = this.state;
        const apiFromContext = this.context.api;
        if (apiFromContext && isRestricted(['apim:api_publish'], apiFromContext)) {
            return (
                <Grid container direction='row' alignItems='center' spacing={4} style={{ marginTop: 20 }}>
                    <Grid item>
                        <Typography variant='body2' color='primary'>
                            <FormattedMessage
                                id='Apis.Details.LifeCycle.LifeCycle.change.not.allowed'
                                defaultMessage={
                                    '* You are not authorized to change the life cycle state of the API'
                                    + ' due to insufficient permissions'
                                }
                            />
                        </Typography>
                    </Grid>
                </Grid>
            );
        }

        if (!lcState) {
            return <Progress />;
        }
        return (
            <>
                <Typography variant='h4' gutterBottom>
                    <FormattedMessage id='Apis.Details.LifeCycle.LifeCycle.lifecycle' defaultMessage='Lifecycle' />
                </Typography>
                <div className={classes.contentWrapper}>
                    <Grid container>
                        <Grid item xs={12}>
                            <LifeCycleUpdate
                                handleUpdate={this.updateData}
                                lcState={lcState}
                                checkList={checkList}
                                handleChangeCheckList={this.handleChangeCheckList}
                                api={api}
                                privateJetModeEnabled={privateJetModeEnabled}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            {lcHistory.length > 1 && (
                                <div>
                                    <Typography variant='h6' gutterBottom className={classes.historyHead}>
                                        <FormattedMessage
                                            id='Apis.Details.LifeCycle.LifeCycle.history'
                                            defaultMessage='History'
                                        />
                                    </Typography>
                                    <LifeCycleHistory lcHistory={lcHistory} />
                                </div>
                            )}
                        </Grid>
                    </Grid>
                </div>
            </>
        );
    }
}

LifeCycle.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

LifeCycle.contextType = ApiContext;

export default withStyles(styles)(LifeCycle);
