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

import React, { useState, useReducer, useEffect } from 'react';
import PropTypes from 'prop-types';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import Badge from '@material-ui/core/Badge';
import ExpansionPanelActions from '@material-ui/core/ExpansionPanelActions';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { makeStyles } from '@material-ui/core/styles';
import LinearProgress from '@material-ui/core/LinearProgress';
import cloneDeep from 'lodash.clonedeep';
import Alert from 'AppComponents/Shared/Alert';
import Utils from 'AppData/Utils';
import DeleteIcon from '@material-ui/icons/Delete';
import IconButton from '@material-ui/core/IconButton';

// splitted operation components

import DescriptionAndSummary from './operationComponents/DescriptionAndSummary';
import OperationGovernance from './operationComponents/OperationGovernance';
import Parameters from './operationComponents/Parameters';

/**
 *
 * Handle the operation UI
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function Operation(props) {
    const {
        operation: initOperation, updateOpenAPI, highlight, operationRateLimits, api,
    } = props;
    const [isSaving, setIsSaving] = useState(false); // Use to show the loader and disable button
    const [isDeleting, setIsDeleting] = useState(false); // Use to disable the expansion panel
    // TODO: could combine above state into `isDoingSomething` like state,
    // because both can't happen simultaneously ~tmkb
    const [isNotSaved, setIsNotSaved] = useState(false);
    // Use to show a badge if there are/is unsaved changes in the operation

    /**
     * Reduce the operation related actions to state
     * @param {Object} operationState current Operation object (Combined object
     *  from API operations and OpenAPI response)
     * @param {Object} operationAction Action object containing the action name and event
     * @returns {Object} next state
     */
    function operationReducer(operationState, operationAction) {
        const { action, event } = operationAction;
        const nextState = cloneDeep(operationState);
        if (action !== 'update') {
            setIsNotSaved(true);
        }
        switch (action) {
            case 'update':
                return event.value;
            case 'summary':
            case 'description':
                nextState.spec[action] = event.value;
                return nextState;
            case 'authType':
                nextState[action] = event.value ? 'Any' : 'None';
                nextState.spec['x-auth-type'] = event.value ? 'Any' : 'None';
                return nextState;
            case 'throttlingPolicy':
            case 'scopes':
                nextState[action] = event.value;
                return nextState;

            default:
                break;
        }
        return operationState;
    }
    const [operation, operationActionsDispatcher] = useReducer(operationReducer, cloneDeep(initOperation));
    useEffect(() => {
        if (!isNotSaved) {
            // Accept update iff there are no unsaved changes,
            // Otherwise outside updates will override the current operations unsaved changes
            operationActionsDispatcher({ action: 'update', event: { value: initOperation } });
        }
    }, [initOperation, isNotSaved]);

    const [isCollapsed, setIsCollapsed] = useState(false);

    // Create styles dynamically using the mapped color for given verb
    const useStyles = makeStyles((theme) => {
        const backgroundColor = theme.custom.resourceChipColors[operation.verb.toLowerCase()];
        return {
            customButton: {
                '&:hover': { backgroundColor },
                backgroundColor,
                width: theme.spacing(12),
            },
            paperStyles: {
                border: `1px solid ${backgroundColor}`,
                borderBottom: isSaving ? '0px' : '',
            },
            customDivider: {
                backgroundColor,
            },
            linearProgress: {
                height: '2px',
            },
            highlightSelected: {
                backgroundColor: Utils.hexToRGBA(backgroundColor, 0.1),
            },
            contentNoMargin: {
                margin: '0px',
            },
        };
    });

    /**
     * Handle the Save button event,
     *
     */
    function saveChanges() {
        setIsSaving(true);
        updateOpenAPI('operation', operation)
            .then(() => setIsNotSaved(false))
            .finally(() => setIsSaving(false));
    }

    /**
     *
     *
     * @param {*} params
     */
    function deleteOperation(event) {
        event.stopPropagation();
        setIsDeleting(true);
        updateOpenAPI('delete', operation).then(() => Alert.info('Operation deleted successfully'));
    }
    const classes = useStyles();
    const closedWithUnsavedChanges = !isCollapsed && isNotSaved;

    /**
     *
     *
     * @param {*} event
     * @param {*} expanded
     */
    function handleCollapse(event, expanded) {
        if (!expanded && isNotSaved) {
            Alert.warning(`Unsaved changes detected in ${operation.target} ${operation.verb}`);
        }
        setIsCollapsed(expanded);
    }
    return (
        <ExpansionPanel disabled={isDeleting} onChange={handleCollapse} className={classes.paperStyles}>
            <ExpansionPanelSummary
                className={highlight ? classes.highlightSelected : ''}
                disableRipple
                disableTouchRipple
                expandIcon={<ExpandMoreIcon />}
                aria-controls='panel2a-content'
                id='panel2a-header'
                classes={{ content: classes.contentNoMargin }}
            >
                <Grid container direction='row' justify='space-between' alignItems='center' spacing={0}>
                    <Grid item md={10}>
                        {closedWithUnsavedChanges ? (
                            <Badge color='primary' variant='dot'>
                                <Button
                                    disableFocusRipple
                                    variant='contained'
                                    size='small'
                                    className={classes.customButton}
                                >
                                    {operation.verb}
                                </Button>
                            </Badge>
                        ) : (
                            <Button
                                disableFocusRipple
                                variant='contained'
                                size='small'
                                className={classes.customButton}
                            >
                                {operation.verb}
                            </Button>
                        )}

                        <Typography display='inline' style={{ margin: '0px 30px' }} variant='h6' gutterBottom>
                            {operation.target}

                            <Typography display='inline' style={{ margin: '0px 30px' }} variant='caption' gutterBottom>
                                {operation.spec.summary}
                            </Typography>
                        </Typography>
                    </Grid>

                    <Grid item md={1}>
                        <IconButton onClick={deleteOperation} aria-label='delete'>
                            <DeleteIcon fontSize='small' />
                            {isDeleting && <CircularProgress size={24} />}
                        </IconButton>
                    </Grid>
                </Grid>
            </ExpansionPanelSummary>
            <Divider light className={classes.customDivider} />
            <ExpansionPanelDetails>
                <Grid spacing={2} container direction='row' justify='flex-start' alignItems='flex-start'>
                    <DescriptionAndSummary
                        operation={operation}
                        operationActionsDispatcher={operationActionsDispatcher}
                    />
                    <OperationGovernance
                        operation={operation}
                        operationActionsDispatcher={operationActionsDispatcher}
                        operationRateLimits={operationRateLimits}
                        api={api}
                    />
                    <Parameters
                        operation={operation}
                        operationActionsDispatcher={operationActionsDispatcher}
                        operationRateLimits={operationRateLimits}
                        api={api}
                    />
                </Grid>
            </ExpansionPanelDetails>
            <Divider className={classes.customDivider} />
            <ExpansionPanelActions style={{ justifyContent: 'flex-start' }}>
                <Button disabled={isSaving} onClick={saveChanges} variant='outlined' size='small' color='primary'>
                    Save
                    {isSaving && <CircularProgress size={24} />}
                </Button>
                <Button
                    size='small'
                    onClick={() => {
                        operationActionsDispatcher({ action: 'update', event: { value: initOperation } });
                        setIsNotSaved(false);
                    }}
                >
                    Reset
                </Button>
            </ExpansionPanelActions>
            {isSaving && <LinearProgress classes={{ root: classes.linearProgress }} />}
        </ExpansionPanel>
    );
}
Operation.defaultProps = {
    highlight: false,
    operationRateLimits: [], // Response body.list from apis policies for `api` throttling policies type
};
Operation.propTypes = {
    api: PropTypes.shape({ scopes: PropTypes.arrayOf(PropTypes.shape({})) }).isRequired,
    updateOpenAPI: PropTypes.func.isRequired,
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
        spec: PropTypes.shape({}).isRequired,
    }).isRequired,
    highlight: PropTypes.bool,
    operationRateLimits: PropTypes.arrayOf(PropTypes.shape({})),
};
