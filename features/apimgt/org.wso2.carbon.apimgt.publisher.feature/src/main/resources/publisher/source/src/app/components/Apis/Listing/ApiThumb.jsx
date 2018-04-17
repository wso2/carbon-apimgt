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

import React from 'react';

import { Redirect, Switch } from 'react-router-dom';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import { withStyles } from 'material-ui/styles';
import Delete from '@material-ui/icons/Delete';
import PropTypes from 'prop-types';

import MoreMenu from './MoreMenu';
import ImageGenerator from './ImageGenerator';
import API from '../../../data/api';
import { resourceMethod, resourcePath, ScopeValidation } from '../../../data/ScopeValidation';
import Utils from '../../../data/Utils';
import ConfirmDialog from '../../Shared/ConfirmDialog';
import Alert from '../../Shared/Alert';

const styles = theme => ({
    lifeCycleState: {
        width: '1.5em',
        height: '1.5em',
        borderRadius: '50%',
        marginRight: '0.5em',
    },
    lifeCycleDisplay: {
        width: 95,
        height: 30,
        marginTop: -15,
        marginLeft: 10,
        color: '#fff',
        textAlign: 'center',
        lineHeight: '30px',
        position: 'absolute',
    },
    lifeCycleState_Created: { backgroundColor: '#0000ff' },
    lifeCycleState_Prototyped: { backgroundColor: '#42dfff' },
    lifeCycleState_Published: { backgroundColor: '#41830A' },
    lifeCycleState_Maintenance: { backgroundColor: '#cecece' },
    lifeCycleState_Deprecated: { backgroundColor: '#D7C850' },
    lifeCycleState_Retired: { backgroundColor: '#000000' },
    thumbContent: {
        width: 250,
        backgroundColor: theme.palette.background.paper,
        padding: 10,
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1,
    },
    thumbRight: {
        alignSelf: 'flex-end',
    },
    thumbInfo: {
        display: 'flex',
    },
    thumbHeader: {
        width: 250,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
    },
    contextBox: {
        width: 140,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
        display: 'inline-block',
        lineHeight: '1em',
    },
    descriptionOverlay: {
        content: '',
        width: '100%',
        height: '100%',
        position: 'absolute',
        left: 0,
        top: 0,
        background: 'linear-gradient(transparent 25px, theme.palette.background.paper)',
    },
    descriptionWrapper: {
        color: theme.palette.text.secondary,
        position: 'relative',
        height: 50,
        overflow: 'hidden',
    },
    thumbDelete: {
        cursor: 'pointer',
        backgroundColor: '#ffffff9a',
        display: 'inline-block',
        position: 'absolute',
        top: 20,
        left: 224,
    },
    thumbWrapper: {
        position: 'relative',
        paddingTop: 20,
    },
    deleteIcon: {
        fill: 'red',
    },
});

/**
 * Create API Thumbnail card for environment diff and API listing page
 * @class ApiThumb
 * @extends {React.Component}
 */
class ApiThumb extends React.Component {
    /**
     * Util method to get confirmation dialuge
     * @static
     * @param {any} details
     * @returns {Object} Dialogue object
     * @memberof ApiThumb
     */
    static getRedirectConfirmDialogueDetails(details) {
        const {
            api, rootAPI, environmentName, currentEnvironmentName, isSameEnvironment,
        } = details;

        const title = `Switching API ${isSameEnvironment ? 'Versions' : 'Environments'}`;
        const messageCommon = `Are you sure you want to switch the "${api.name}" API`;
        const message = `${messageCommon} ${
            isSameEnvironment
                ? `version from “${rootAPI.version}” to “${api.version}”?`
                : `from version “${rootAPI.version}” in the ${currentEnvironmentName} environment to “${
                    api.version
                }” in the ${environmentName} environment?`
        }`;

        const labelCancel = 'Cancel';
        const labelOk = 'Switch';

        return {
            title,
            message,
            labelCancel,
            labelOk,
        };
    }

    /**
     * Creates an instance of ApiThumb.
     * @param {any} props @inheritDoc
     * @memberof ApiThumb
     */
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            overview_link: '',
            isRedirect: false,
            openDeleteConfirmDialog: false,
            openRedirectConfirmDialog: false,
            redirectConfirmDialogDetails: {},
        };

        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRedirectToAPIOverview = this.handleRedirectToAPIOverview.bind(this);
        this.deleteConfirmDialogCallback = this.deleteConfirmDialogCallback.bind(this);
        this.redirectConfirmDialogCallback = this.redirectConfirmDialogCallback.bind(this);
    }

    handleRequestClose = () => {
        this.setState({ openDeleteConfirmDialog: false });
    };

    openDeleteConfirmDialog = () => {
        this.setState({ openDeleteConfirmDialog: true });
    };

    /**
     * Handle delete button click event after the confirmation
     * @memberof ApiThumb
     */
    handleApiDelete() {
        const api = new API();
        const { id, name } = this.props.api;
        const promisedDelete = api.deleteAPI(id);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info('Something went wrong while deleting the ' + name + ' API!');
                this.setState({ openDeleteConfirmDialog: false });
                return;
            }
            Alert.info(' API deleted Successfully');
            this.props.updateApi(id);
            this.setState({ active: false });
        });
    }

    /**
     * Redirect to selected API overview page
     * @memberof ApiThumb
     */
    handleRedirectToAPIOverview() {
        const { api, environmentName, rootAPI } = this.props;
        const currentEnvironmentName = Utils.getCurrentEnvironment().label;
        // If environment name or version is not defined then consider as same environment or version.
        const isSameEnvironment = !environmentName || environmentName === currentEnvironmentName;
        const isSameVersion = !rootAPI || rootAPI.version === api.version;

        if (isSameEnvironment && isSameVersion) {
            this.setState({
                overview_link: `/apis/${api.id}`,
                isRedirect: true,
            });
        } else {
            // Ask for confirmation to switch environment or version of the API
            const redirectConfirmDialogDetails = ApiThumb.getRedirectConfirmDialogueDetails({
                api,
                rootAPI,
                environmentName,
                currentEnvironmentName,
                isSameEnvironment,
                isSameVersion,
            });

            this.setState({
                overview_link: `/apis/${api.id}/overview?environment=${environmentName}`,
                isRedirect: false,
                openRedirectConfirmDialog: true,
                redirectConfirmDialogDetails,
            });
        }
    }

    /**
     * Handle the delete confirmation action
     * @param {any} result Result
     * @memberof ApiThumb
     */
    deleteConfirmDialogCallback(result) {
        this.setState({
            openDeleteConfirmDialog: false,
        });
        if (result) this.handleApiDelete();
    }

    redirectConfirmDialogCallback(result) {
        this.setState({
            isRedirect: result,
            openRedirectConfirmDialog: false,
        });
    }

    /**
     * Render thumbnail
     * @returns {React.Component} Return component
     * @memberof ApiThumb
     */
    render() {
        const { api, environmentOverview, classes } = this.props;
        const gridItemSizes = environmentOverview
            ? {
                xs: 6,
                sm: 4,
                md: 3,
                lg: 2,
                xl: 2,
            }
            : {
                xs: 12,
                sm: 6,
                md: 4,
                lg: 3,
                xl: 2,
            };

        if (!this.state.active) {
            // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }

        if (this.state.isRedirect) {
            return (
                <Switch>
                    <Redirect to={this.state.overview_link} />
                </Switch>
            );
        }

        return (
            <Grid item {...gridItemSizes} className={classes.thumbWrapper}>
                {api && (
                    <div className={`${classes.lifeCycleDisplay} ${classes[`lifeCycleState_${api.lifeCycleStatus}`]}`}>
                        {api.lifeCycleStatus}
                    </div>
                )}
                {/* Do not render for environment overview page */}
                {!environmentOverview && (
                    <ScopeValidation resourcePath={resourcePath.SINGLE_API} resourceMethod={resourceMethod.DELETE}>
                        <a
                            className={classes.thumbDelete}
                            onKeyPress={this.openDeleteConfirmDialog}
                            onClick={this.openDeleteConfirmDialog}
                        >
                            <Delete className={classes.deleteIcon} />
                        </a>
                    </ScopeValidation>
                )}
                <ImageGenerator handleRedirectToAPIOverview={this.handleRedirectToAPIOverview} apiName={api.name} />
                <MoreMenu api_uuid={api.id} />

                <div className={classes.thumbContent}>
                    <Typography
                        className={classes.thumbHeader}
                        variant='display1'
                        gutterBottom
                        onClick={this.handleRedirectToAPIOverview}
                        title={api.name}
                    >
                        {environmentOverview ? <span>{api.version}</span> : <span>{api.name}</span>}
                    </Typography>
                    <Typography variant='caption' gutterBottom align='left'>
                        By: {api.provider}
                    </Typography>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant='subheading'>
                                {environmentOverview ? <span>{api.name}</span> : <span>{api.version}</span>}
                            </Typography>
                            <Typography variant='caption' gutterBottom align='left'>
                                {environmentOverview ? <span>Name</span> : <span>Version</span>}
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant='subheading' align='right' className={classes.contextBox}>
                                {api.context}
                            </Typography>
                            <Typography variant='caption' gutterBottom align='right'>
                                Context
                            </Typography>
                        </div>
                    </div>
                    <div className={classes.descriptionWrapper}>
                        {api.description}
                        <div className={classes.descriptionOverlay} />
                    </div>
                </div>

                <ConfirmDialog
                    title={`Delete API "${api.name} - ${api.version}"?`}
                    message='Are you sure you want to delete the API?'
                    labelOk='Delete'
                    callback={this.deleteConfirmDialogCallback}
                    open={this.state.openDeleteConfirmDialog}
                />
                <ConfirmDialog
                    {...this.state.redirectConfirmDialogDetails}
                    callback={this.redirectConfirmDialogCallback}
                    open={this.state.openRedirectConfirmDialog}
                />
            </Grid>
        );
    }
}

ApiThumb.defaultProps = {
    environmentName: undefined,
    rootAPI: undefined,
    environmentOverview: false,
};

ApiThumb.propTypes = {
    api: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
    }).isRequired,
    updateApi: PropTypes.func.isRequired,
    environmentName: PropTypes.string,
    rootAPI: PropTypes.shape({}),
    classes: PropTypes.shape({}).isRequired,
    environmentOverview: PropTypes.bool,
};

export default withStyles(styles)(ApiThumb);
