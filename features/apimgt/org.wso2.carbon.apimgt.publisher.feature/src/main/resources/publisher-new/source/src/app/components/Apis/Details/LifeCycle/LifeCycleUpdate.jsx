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
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import Alert from 'AppComponents/Shared/Alert';
import LifeCycleImage from './LifeCycleImage';
import { FormattedMessage } from 'react-intl';

const styles = theme => ({
    buttonsWrapper: {
        marginTop: 40,
    },
    stateButton: {
        marginRight: theme.spacing.unit,
    },
});

/**
 *
 *
 * @class LifeCycleUpdate
 * @extends {Component}
 */
class LifeCycleUpdate extends Component {
    constructor(props) {
        super(props);
        this.updateLifeCycleState = this.updateLifeCycleState.bind(this);
        this.api = new API();
        this.state = {
            newState: null,
        };
    }

    /**
     *
     *
     * @param {*} apiUUID
     * @param {*} action
     * @memberof LifeCycleUpdate
     */
    updateLCStateOfAPI(apiUUID, action) {
        let promisedUpdate;
        const lifecycleChecklist = this.props.checkList.map(item => item.value + ':' + item.checked);
        if (lifecycleChecklist.length > 0) {
            promisedUpdate = this.api.updateLcState(apiUUID, action, lifecycleChecklist);
        } else {
            promisedUpdate = this.api.updateLcState(apiUUID, action);
        }
        promisedUpdate
            .then(response => {
                /* TODO: Handle IO erros ~tmkb */
                this.props.handleUpdate(true);
                let newState = response.body.lifecycleState.state;
                this.setState({ newState });

                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.LifeCycle.LifeCycleUpdate.success',
                    defaultMessage: 'Lifecycle state updated successfully',
                }));
                /* TODO: add i18n ~tmkb */
            })
            .catch(error_response => {
                console.log(error_response);
                Alert.error(JSON.stringify(error_response));
            });
    }

    /**
     *
     *
     * @param {*} event
     * @memberof LifeCycleUpdate
     */
    updateLifeCycleState(event) {
        event.preventDefault();
        let action = event.currentTarget.getAttribute('data-value');
        const apiUUID = this.props.api.id;
        const { privateJetModeEnabled } = this.props;
        this.updateLCStateOfAPI(apiUUID, action);
    }

    render() {
        const { api, lcState, classes, theme, handleChangeCheckList, checkList } = this.props;
        const { newState } = this.state;
        const is_workflow_pending = api.workflowStatus && api.workflowStatus.toLowerCase() === 'pending';
        return (
            <Grid container>
                {is_workflow_pending ? (
                    <Grid item xs={12}>
                        <Typography variant="h5">
                            <FormattedMessage id='Apis.Details.LifeCycle.LifeCycleUpdate.pending' defaultMessage='Pending lifecycle state change.' />
                        </Typography>
                        <Typography>
                            <FormattedMessage id='Apis.Details.LifeCycle.LifeCycleUpdate.adjective' defaultMessage='adjective' />
                        </Typography>
                    </Grid>
                ) : (
                        <Grid item xs={12}>
                            {theme.custom.lifeCycleImage ? (
                                <img src={theme.custom.lifeCycleImage} alt="Lifecycle image" />
                            ) : (
                                    <LifeCycleImage lifeCycleStatus={newState || api.lifeCycleStatus} />
                                )}
                        </Grid>
                    )}
                <Grid item xs={12}>
                    {!is_workflow_pending && (
                        <FormGroup row>
                            {checkList.map((checkItem, index) => (
                                <FormControlLabel
                                    key={index}
                                    control={
                                        <Checkbox
                                            checked={checkList[index].checked}
                                            onChange={handleChangeCheckList(index)}
                                            value={checkList[index].value}
                                        />
                                    }
                                    label={checkList[index].label}
                                />
                            ))}
                        </FormGroup>
                    )}
                    <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                        <div className={classes.buttonsWrapper}>
                            {is_workflow_pending ? (
                                <div className="btn-group" role="group">
                                    <input
                                        type="button"
                                        className="btn btn-primary wf-cleanup-btn"
                                        defaultValue="Delete pending lifecycle state change request"
                                    />
                                </div>
                            ) : (
                                    lcState.availableTransitions.map(
                                        transition_state =>
                                            lcState.state !== transition_state.targetState && (
                                                <Button
                                                    variant="outlined"
                                                    className={classes.stateButton}
                                                    key={transition_state.event}
                                                    data-value={transition_state.event}
                                                    onClick={this.updateLifeCycleState}
                                                >
                                                    {transition_state.event}
                                                </Button>
                                            ),
                                    ) /* Skip when transitions available for current state , this occurs in states where have allowed re-publishing in prototype and published sates */
                                )}
                        </div>
                    </ScopeValidation>
                </Grid>
            </Grid>
        );
    }
}

LifeCycleUpdate.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(LifeCycleUpdate);
