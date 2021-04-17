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

import React, { useContext, useState } from 'react';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage, useIntl } from 'react-intl';
import { useHistory } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import { isRestricted } from 'AppData/AuthManager';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import Popover from '@material-ui/core/Popover';
import moment from 'moment';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableContainer from '@material-ui/core/TableContainer';
import clsx from 'clsx';
import TableRow from '@material-ui/core/TableRow';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Chip from '@material-ui/core/Chip';
import { makeStyles } from '@material-ui/core/styles';
import HelpOutlineIcon from '@material-ui/icons/HelpOutline';
import Configurations from 'Config';
import Card from '@material-ui/core/Card';
import AddIcon from '@material-ui/icons/Add';
import CardContent from '@material-ui/core/CardContent';
import IconButton from '@material-ui/core/IconButton';
import APIProduct from 'AppData/APIProduct';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import RestoreIcon from '@material-ui/icons/Restore';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import MenuItem from '@material-ui/core/MenuItem';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import CardHeader from '@material-ui/core/CardHeader';
import Checkbox from '@material-ui/core/Checkbox';
import RadioButtonUncheckedIcon from '@material-ui/icons/RadioButtonUnchecked';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import API from 'AppData/api';
import { ConfirmDialog } from 'AppComponents/Shared/index';
import { useRevisionContext } from 'AppComponents/Shared/RevisionContext';
import Utils from 'AppData/Utils';
import DisplayDevportal from './DisplayDevportal';
import DeploymentOnbording from './DeploymentOnbording';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    saveButton: {
        marginTop: theme.spacing(3),
    },
    shapeRec: {
        backgroundColor: 'black',
        alignSelf: 'center',
        width: 120,
        height: 3,
    },
    shapeCircaleBack: {
        backgroundColor: '#E2E2E2',
        width: 63,
        height: 63,
    },
    shapeInnerComplete: {
        backgroundColor: '#095677',
        width: 50,
        height: 50,
        marginTop: 6,
        marginLeft: 6.5,
        placeSelf: 'middle',
    },
    shapeInnerInactive: {
        backgroundColor: '#BFBFBF',
        width: 50,
        height: 50,
        marginTop: 6,
        marginLeft: 6,
        placeSelf: 'middle',
    },
    shapeDottedEnd: {
        backgroundColor: '#BFBFBF',
        border: '1px dashed #707070',
        width: 47,
        height: 47,
        marginTop: 7,
        marginLeft: 7,
        placeSelf: 'middle',
    },
    shapeDottedStart: {
        backgroundColor: '#1CB1BF',
        border: '2px solid #ffffff',
        width: 47,
        height: 47,
        marginTop: 6,
        marginLeft: 6,
        placeSelf: 'middle',
    },
    plusIconStyle: {
        marginTop: 8,
        marginLeft: 8,
    },
    shapeDottedStart1: {
        backgroundColor: '#1CB1BF',
        width: 50,
        height: 50,
        marginTop: 6,
        marginLeft: 6.5,
        placeSelf: 'middle',
    },
    textShape: {
        marginTop: 5.5,
        marginLeft: 6.5,
    },
    textShape2: {
        marginTop: 8,
        marginLeft: 115,
        height: '18px',
        fontFamily: 'sans-serif',
    },
    textPadding: {
        height: '25px',
        paddingBottom: '2px',
    },
    textDelete: {
        marginTop: 8,
        marginLeft: 120,
        fontFamily: 'sans-serif',
        fontSize: 'small',
    },
    textShape3: {
        color: '#38536c',
        marginLeft: 70,
    },
    textShape7: {
        color: '#38536c',
    },
    primaryEndpoint: {
        color: '#006E9C',
    },
    secondaryEndpoint: {
        color: '#415A85',
    },
    textShape4: {
        marginTop: 55,
    },
    textShape8: {
        marginTop: 80,
    },
    textShape5: {
        marginTop: 10,
        marginLeft: 85,
        marginBottom: 10,
    },
    textShape6: {
        color: '#1B3A57',
    },
    button1: {
        color: '#1B3A57',
        marginLeft: 7,
    },
    button2: {
        color: '#1B3A57',
        marginLeft: 7,
        marginTop: 10,
    },
    shapeRecBack: {
        backgroundColor: 'black',
        alignSelf: 'center',
        width: 40,
        height: 3,
    },
    shapeCircle: {
        borderRadius: '50%',
    },
    shapeCircleBlack: {
        backgroundColor: '#000000',
        alignSelf: 'center',
        paddingLeft: '15px',
        width: 15,
        height: 15,
    },
    changeCard: {
        boxShadow: 15,
        borderRadius: '10px',
        backgroundColor: theme.palette.secondary.highlight,
    },
    noChangeCard: {
        boxShadow: 15,
        borderRadius: '10px',
    },
    cardHeight: {
        boxShadow: 1,
        height: '90%',
    },
    cardContentHeight: {
        boxShadow: 1,
        height: '50%',
    },
    cardActionHeight: {
        boxShadow: 1,
        height: '25%',
    },
    dialogContent: {
        overflow: 'auto',
        height: '90%',
    },
    textOverlay: {
        overflow: 'hidden',
        maxHeight: '100%',
        maxWidth: '100%',
        cursor: 'pointer',
        '&:hover': { overflow: 'visible' },
    },
    gridOverflow: {
        overflow: 'scroll',
        width: '100%',
    },
    formControl: {
        margin: theme.spacing(1),
        minWidth: 130,
    },
    dialogPaper: {
        width: '800px',
        height: '500px',
    },
    createRevisionDialogStyle: {
        width: '800px',
    },
    sectionTitle: {
        marginBottom: theme.spacing(2),
    },
    deployNewRevButtonStyle: {
        marginRight: theme.spacing(3),
        marginBottom: theme.spacing(3),
        marginTop: theme.spacing(3),
    },
    popover: {
        pointerEvents: 'none',
    },
    paper: {
        padding: theme.spacing(1),
        maxWidth: '300px',
    },
    timePaddingStyle: {
        marginTop: theme.spacing(1),
    },
    labelSpace: {
        paddingLeft: theme.spacing(1),
    },
    labelSpacingDown: {
        marginBottom: theme.spacing(2),
    },
    warningText: {
        color: '#ff0000',
    },
    tableCellVhostSelect: {
        paddingTop: theme.spacing(0),
        paddingBottom: theme.spacing(0),
    },
    vhostSelect: {
        marginTop: theme.spacing(3),
    },
    textCount: {
        marginTop: theme.spacing(-2.5),
    },
    containerInline: {
        display: 'inline-flex',
    },
    containerOverflow: {
        display: 'grid',
        gridGap: '16px',
        paddingLeft: '48px',
        gridTemplateColumns: 'repeat(auto-fill,minmax(160px,1fr))',
        gridAutoFlow: 'column',
        gridAutoColumns: 'minmax(160px,1fr)',
        overflowX: 'auto',
    },
}));

/**
 * Renders an Environments list
 * @class Environments
 * @extends {React.Component}
 */
export default function Environments() {
    const classes = useStyles();
    const maxCommentLength = '255';
    const intl = useIntl();
    const { api } = useContext(APIContext);
    const history = useHistory();
    const { settings } = useAppContext();
    const {
        allRevisions, getRevision, allEnvRevision, getDeployedEnv,
    } = useRevisionContext();
    let revisionCount;
    if (Configurations.app.revisionCount) {
        revisionCount = Configurations.app.revisionCount;
    } else {
        revisionCount = 5;
    }
    const restApi = new API();
    const restProductApi = new APIProduct();
    const [selectedRevision, setRevision] = useState([]);
    const defaultVhosts = settings.environment.map(
        (e) => (e.vhosts && e.vhosts.length > 0 ? { env: e.name, vhost: e.vhosts[0].host } : undefined),
    );
    const [selectedVhosts, setVhosts] = useState(defaultVhosts);
    const [selectedVhostDeploy, setVhostsDeploy] = useState(defaultVhosts);
    const [extraRevisionToDelete, setExtraRevisionToDelete] = useState(null);
    const [description, setDescription] = useState('');
    const [SelectedEnvironment, setSelectedEnvironment] = useState([]);
    const [open, setOpen] = useState(false);
    const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
    const [revisionToDelete, setRevisionToDelete] = useState([]);
    const [confirmRestoreOpen, setConfirmRestoreOpen] = useState(false);
    const [revisionToRestore, setRevisionToRestore] = useState([]);
    const [currentLength, setCurrentLength] = useState(0);
    const [openDeployPopup, setOpenDeployPopup] = useState(history.location.state === 'deploy');

    // allEnvDeployments represents all deployments of the API with mapping
    // environment -> {revision deployed to env, vhost deployed to env with revision}
    const allEnvDeployments = Utils.getAllEnvironmentDeployments(settings.environment, allEnvRevision);

    const toggleOpenConfirmDelete = (revisionName, revisionId) => {
        setRevisionToDelete([revisionName, revisionId]);
        setConfirmDeleteOpen(!confirmDeleteOpen);
    };

    const toggleOpenConfirmRestore = (revisionName, revisionId) => {
        setRevisionToRestore([revisionName, revisionId]);
        setConfirmRestoreOpen(!confirmRestoreOpen);
    };

    const toggleDeployRevisionPopup = () => {
        setOpenDeployPopup(!openDeployPopup);
    };

    const handleCloseDeployPopup = () => {
        history.replace();
        setOpenDeployPopup(false);
        setExtraRevisionToDelete(null);
    };

    const handleClickOpen = () => {
        if (!isRestricted(['apim:api_create', 'apim:api_publish'], api)) {
            setOpen(true);
        }
    };

    const handleDeleteSelect = (event) => {
        setExtraRevisionToDelete([event.target.value, event.target.name]);
    };

    const handleSelect = (event) => {
        const revisions = selectedRevision.filter((r) => r.env !== event.target.name);
        const oldRevision = selectedRevision.find((r) => r.env === event.target.name);
        let displayOnDevPortal = true;
        if (oldRevision) {
            displayOnDevPortal = oldRevision.displayOnDevPortal;
        }
        revisions.push({ env: event.target.name, revision: event.target.value, displayOnDevPortal });
        setRevision(revisions);
    };

    const handleVhostSelect = (event) => {
        const vhosts = selectedVhosts.filter((v) => v.env !== event.target.name);
        vhosts.push({ env: event.target.name, vhost: event.target.value });
        setVhosts(vhosts);
    };

    const handleVhostDeploySelect = (event) => {
        const vhosts = selectedVhostDeploy.filter((v) => v.env !== event.target.name);
        vhosts.push({ env: event.target.name, vhost: event.target.value });
        setVhostsDeploy(vhosts);
    };

    const handleClose = () => {
        setOpen(false);
        setExtraRevisionToDelete(null);
    };

    const handleChange = (event) => {
        if (event.target.checked) {
            setSelectedEnvironment([...SelectedEnvironment, event.target.value]);
        } else {
            setSelectedEnvironment(
                SelectedEnvironment.filter((env) => env !== event.target.value),
            );
        }
        if (event.target.name === 'description') {
            setDescription(event.target.value);
            setCurrentLength(event.target.value.length);
        }
    };

    /**
     * Handles deleting a revision
     * @param {Object} revisionId the revision Id
     * @returns {Object} promised delete
     */
    function deleteRevision(revisionId) {
        let promiseDelete;
        if (api.apiType === API.CONSTS.APIProduct) {
            promiseDelete = restProductApi.deleteProductRevision(api.id, revisionId)
                .then(() => {
                    Alert.info(intl.formatMessage({
                        defaultMessage: 'Revision Deleted Successfully',
                        id: 'Apis.Details.Environments.Environments.revision.delete.success',
                    }));
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error(intl.formatMessage({
                            defaultMessage: 'Something went wrong while deleting the revision',
                            id: 'Apis.Details.Environments.Environments.revision.delete.error',
                        }));
                    }
                }).finally(() => {
                    history.replace();
                    getRevision();
                });
        } else {
            promiseDelete = restApi.deleteRevision(api.id, revisionId)
                .then(() => {
                    Alert.info(intl.formatMessage({
                        defaultMessage: 'Revision Deleted Successfully',
                        id: 'Apis.Details.Environments.Environments.revision.delete.success',
                    }));
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error(intl.formatMessage({
                            defaultMessage: 'Something went wrong while deleting the revision',
                            id: 'Apis.Details.Environments.Environments.revision.delete.error',
                        }));
                    }
                }).finally(() => {
                    history.replace();
                    getRevision();
                });
        }
        return promiseDelete;
    }

    /**
     * Handles creating a new revision
     * @param {Object} body the request body
     * @returns {Object} promised create
     */
    function createRevision(body) {
        if (api.apiType === API.CONSTS.APIProduct) {
            restProductApi.createProductRevision(api.id, body)
                .then(() => {
                    Alert.info('Revision Created Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while creating the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                });
        } else {
            api.createRevision(api.id, body)
                .then(() => {
                    Alert.info('Revision Created Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while creating the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                });
        }
    }

    /**
      * Handles adding a new revision
      * @memberof Revisions
      */
    function handleClickAddRevision() {
        const body = {
            description,
        };
        if (extraRevisionToDelete) {
            deleteRevision(extraRevisionToDelete[0])
                .then(() => {
                    createRevision(body);
                }).finally(() => setExtraRevisionToDelete(null));
        } else {
            createRevision(body);
        }
        setOpen(false);
        setDescription('');
        setExtraRevisionToDelete(null);
    }

    const runActionDelete = (confirm, revisionId) => {
        if (confirm) {
            deleteRevision(revisionId);
        }
        setConfirmDeleteOpen(!confirmDeleteOpen);
        setRevisionToDelete([]);
    };

    /**
      * Handles restore revision
      * @memberof Revisions
      */
    function restoreRevision(revisionId) {
        if (api.apiType !== API.CONSTS.APIProduct) {
            restApi.restoreRevision(api.id, revisionId)
                .then(() => {
                    Alert.info('Revision Restored Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while restoring the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        } else {
            restProductApi.restoreProductRevision(api.id, revisionId)
                .then(() => {
                    Alert.info('Revision Restored Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while restoring the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        }
    }

    const runActionRestore = (confirm, revisionId) => {
        if (confirm) {
            restoreRevision(revisionId);
        }
        setConfirmRestoreOpen(!confirmRestoreOpen);
        setRevisionToRestore([]);
    };

    /**
      * Handles undeploy a revision
      * @memberof Revisions
      */
    function undeployRevision(revisionId, envName) {
        const body = [{
            name: envName,
            displayOnDevportal: false,
        }];
        if (api.apiType !== API.CONSTS.APIProduct) {
            restApi.undeployRevision(api.id, revisionId, body)
                .then(() => {
                    Alert.info('Revision Undeployed Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while undeploying the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        } else {
            restProductApi.undeployProductRevision(api.id, revisionId, body)
                .then(() => {
                    Alert.info('Revision Undeployed Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while undeploying the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        }
    }

    /**
      * Handles deploy a revision
      * @memberof Revisions
      */
    function deployRevision(revisionId, envName, vhost, displayOnDevportal) {
        const body = [{
            name: envName,
            displayOnDevportal,
            vhost,
        }];
        if (api.apiType !== API.CONSTS.APIProduct) {
            restApi.deployRevision(api.id, revisionId, body)
                .then(() => {
                    Alert.info('Deploy revision Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while deploy the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        } else {
            restProductApi.deployProductRevision(api.id, revisionId, body)
                .then(() => {
                    Alert.info('Deploy revision Successfully');
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while deploy the revision');
                    }
                    console.error(error);
                }).finally(() => {
                    getRevision();
                    getDeployedEnv();
                });
        }
    }

    /**
      * Handles adding a new revision and deploy
      * @memberof Revisions
      */
    function createDeployRevision(envList, vhostList) {
        const body = {
            description,
        };
        if (api.apiType !== API.CONSTS.APIProduct) {
            restApi.createRevision(api.id, body)
                .then((response) => {
                    Alert.info('Revision Created Successfully');
                    const body1 = [];
                    for (let i = 0; i < envList.length; i++) {
                        body1.push({
                            name: envList[i],
                            vhost: vhostList.find((v) => v.env === envList[i]).vhost,
                            displayOnDevportal: true,
                        });
                    }
                    restApi.deployRevision(api.id, response.body.id, body1)
                        .then(() => {
                            Alert.info('Revision Deployed Successfully');
                        })
                        .catch((error) => {
                            if (error.response) {
                                Alert.error(error.response.body.description);
                            } else {
                                Alert.error('Something went wrong while deploying the revision');
                            }
                            console.error(error);
                        }).finally(() => {
                            history.replace();
                            getRevision();
                            getDeployedEnv();
                        });
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while creating the revision');
                    }
                    console.error(error);
                });
            setOpenDeployPopup(false);
        } else {
            restProductApi.createProductRevision(api.id, body)
                .then((response) => {
                    Alert.info('Revision Created Successfully');
                    const body1 = [];
                    for (let i = 0; i < envList.length; i++) {
                        body1.push({
                            name: envList[i],
                            vhost: vhostList.find((v) => v.env === envList[i]).vhost,
                            displayOnDevportal: true,
                        });
                    }
                    restProductApi.deployProductRevision(api.id, response.body.id, body1)
                        .then(() => {
                            Alert.info('Revision Deployed Successfully');
                        })
                        .catch((error) => {
                            if (error.response) {
                                Alert.error(error.response.body.description);
                            } else {
                                Alert.error('Something went wrong while deploying the revision');
                            }
                            console.error(error);
                        }).finally(() => {
                            history.replace();
                            getRevision();
                            getDeployedEnv();
                        });
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while creating the revision');
                    }
                    console.error(error);
                });
            setOpenDeployPopup(false);
        }
    }

    /**
     * Handles creating and deploying a new revision
     * @param {Object} envList the environment list
     * @param {Array} vhostList the vhost list
     * @param {Object} length the length of the list
     */
    function handleCreateAndDeployRevision(envList, vhostList) {
        if (extraRevisionToDelete) {
            deleteRevision(extraRevisionToDelete[0])
                .then(() => {
                    createDeployRevision(envList, vhostList);
                }).finally(() => {
                    setExtraRevisionToDelete(null);
                });
        } else {
            createDeployRevision(envList, vhostList);
        }
    }

    const confirmDeleteDialog = (
        <ConfirmDialog
            key='key-dialog'
            labelCancel={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.delete.cancel'
                    defaultMessage='Cancel'
                />
            )}
            title={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.delete.confirm.title'
                    defaultMessage='Confirm Delete'
                />
            )}
            message={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.delete.confirm.message'
                    defaultMessage='Are you sure you want to delete {revision} ?'
                    values={{ revision: revisionToDelete[0] }}
                />
            )}
            labelOk={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.delete.confirm.ok'
                    defaultMessage='Yes'
                />
            )}
            callback={(e) => runActionDelete(e, revisionToDelete[1])}
            open={confirmDeleteOpen}
        />
    );

    const confirmRestoreDialog = (
        <ConfirmDialog
            key='key-dialog-restore'
            labelCancel={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.restore.cancel'
                    defaultMessage='Cancel'
                />
            )}
            title={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.restore.confirm.title'
                    defaultMessage='Confirm Restore'
                />
            )}
            message={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.restore.confirm.message'
                    defaultMessage='Are you sure you want to restore {revision} (To Current API)?'
                    values={{ revision: revisionToRestore[0] }}
                />
            )}
            labelOk={(
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.revision.restore.confirm.ok'
                    defaultMessage='Yes'
                />
            )}
            callback={(e) => runActionRestore(e, revisionToRestore[1])}
            open={confirmRestoreOpen}
        />
    );

    let item1;
    /**
     * Returns modified item1
     * @param {*} revDescription The description of the revision
     * @returns {Object} Returns the item1
     */
    function ReturnItem1({ revDescription, revName, revCreatedTime }) {
        const [anchorEl, setAnchorEl] = useState(null);

        const handlePopoverOpen = (event) => {
            setAnchorEl(event.currentTarget);
        };

        const handlePopoverClose = () => {
            setAnchorEl(null);
        };

        const openPopover = Boolean(anchorEl);
        item1 = (
            <Grid
                className={classes.containerInline}
            >
                <Grid item className={classes.shapeRec} />
                <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                    <Grid
                        className={clsx(classes.shapeInnerComplete, classes.shapeCircle)}
                        onMouseEnter={handlePopoverOpen}
                        onMouseLeave={handlePopoverClose}
                    />
                    <Popover
                        id='mouse-over-popover'
                        className={classes.popover}
                        classes={{
                            paper: classes.paper,

                        }}
                        open={openPopover}
                        anchorEl={anchorEl}
                        anchorOrigin={{
                            vertical: 'top',
                            horizontal: 'right',
                        }}
                        transformOrigin={{
                            vertical: 'bottom',
                            horizontal: 'left',
                        }}
                        onClose={handlePopoverClose}
                        disableRestoreFocus
                    >
                        <div>
                            <Typography variant='body1'>
                                <b>{revName}</b>
                            </Typography>
                            <Typography variant='body2'>
                                {revDescription}
                            </Typography>
                            <div className={classes.timePaddingStyle}>
                                <Typography variant='caption'>
                                    <span>{moment(revCreatedTime).fromNow()}</span>
                                </Typography>
                            </div>
                        </div>
                    </Popover>
                </Grid>
                <Grid item className={classes.shapeRecBack} />
            </Grid>
        );
        return item1;
    }
    const item2 = (
        <Grid
            className={classes.containerInline}
        >
            <Grid item className={classes.shapeRec} />
            <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                <Grid className={clsx(classes.shapeInnerInactive, classes.shapeCircle)} />
            </Grid>
            <Grid item className={classes.shapeRecBack} />
        </Grid>
    );
    const item3 = (
        <Grid
            className={classes.containerInline}
        >
            <Grid item className={classes.shapeRec} />
            <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                <Grid className={clsx(classes.shapeDottedEnd, classes.shapeCircle)} />
            </Grid>
        </Grid>
    );
    const item4 = (
        <Grid
            className={classes.containerInline}
        >
            <Grid item className={classes.shapeRec} />
            <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                <Grid
                    onClick={handleClickOpen}
                    className={clsx(classes.shapeDottedStart, classes.shapeCircle)}
                    style={{ cursor: 'pointer' }}
                >
                    <AddIcon style={{ fontSize: 30 }} className={classes.plusIconStyle} />
                </Grid>
            </Grid>
            <Grid item className={classes.shapeRecBack} />
        </Grid>
    );
    const item5 = (
        <Grid
            className={classes.containerInline}
        >
            <Grid item className={classes.shapeRec} />
            <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                <Grid
                    onClick={handleClickOpen}
                    className={clsx(classes.shapeDottedStart, classes.shapeCircle)}
                    style={{ cursor: 'pointer' }}
                >
                    <AddIcon style={{ fontSize: 30 }} className={classes.plusIconStyle} />
                </Grid>
            </Grid>
        </Grid>
    );
    let item6;
    /**
     * Returns modified item6
     * @param {*} revDescription The description of the revision
     * @returns {Object} Returns the item6
     */
    function ReturnItem6({ revDescription, revName, revCreatedTime }) {
        const [anchorEl1, setAnchorEl1] = useState(null);

        const handlePopoverOpen = (event) => {
            setAnchorEl1(event.currentTarget);
        };

        const handlePopoverClose = () => {
            setAnchorEl1(null);
        };

        const openPopover = Boolean(anchorEl1);
        item6 = (
            <Grid
                className={classes.containerInline}
            >
                <Grid item className={classes.shapeRec} />
                <Grid item className={clsx(classes.shapeCircaleBack, classes.shapeCircle)}>
                    <Grid
                        className={clsx(classes.shapeDottedStart1, classes.shapeCircle)}
                        onMouseEnter={handlePopoverOpen}
                        onMouseLeave={handlePopoverClose}
                    />
                    <Popover
                        id='mouse-over-popover'
                        className={classes.popover}
                        classes={{
                            paper: classes.paper,
                        }}
                        open={openPopover}
                        anchorEl={anchorEl1}
                        anchorOrigin={{
                            vertical: 'bottom',
                            horizontal: 'right',
                        }}
                        transformOrigin={{
                            vertical: 'top',
                            horizontal: 'left',
                        }}
                        onClose={handlePopoverClose}
                        disableRestoreFocus
                    >
                        <div>
                            <Typography variant='body1'>
                                <b>{revName}</b>
                            </Typography>
                            <Typography variant='body2'>
                                {revDescription}
                            </Typography>
                            <div className={classes.timePaddingStyle}>
                                <Typography variant='caption'>
                                    <span>{moment(revCreatedTime).fromNow()}</span>
                                </Typography>
                            </div>
                        </div>
                    </Popover>
                </Grid>
                <Grid item className={classes.shapeRecBack} />
            </Grid>
        );
        return item6;
    }

    const items = [];
    if (!api.isRevision) {
        if (allRevisions && allRevisions.length !== 0) {
            items.push(
                <Grid item className={clsx(classes.shapeCircleBlack, classes.shapeCircle)} />,
            );
            for (let revision = 0; revision < (allRevisions.length); revision++) {
                if (revision % 2 === 0) {
                    items.push(
                        <Grid item>
                            <Grid className={classes.textShape4} />
                            <ReturnItem1
                                revDescription={allRevisions[revision].description}
                                revName={allRevisions[revision].displayName}
                                revCreatedTime={allRevisions[revision].createdTime}
                            />
                            {item1}
                            <Grid className={classes.textShape2}>
                                {allRevisions[revision].displayName}
                            </Grid>
                            <Grid>
                                <Button
                                    className={classes.textShape3}
                                    onClick={() => toggleOpenConfirmRestore(
                                        allRevisions[revision].displayName, allRevisions[revision].id,
                                    )}
                                    size='small'
                                    type='submit'
                                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                    startIcon={<RestoreIcon />}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.restore'
                                        defaultMessage='Restore'
                                    />
                                </Button>
                                <Button
                                    className={classes.textShape7}
                                    onClick={() => toggleOpenConfirmDelete(
                                        allRevisions[revision].displayName, allRevisions[revision].id,
                                    )}
                                    disabled={(allEnvRevision && allEnvRevision.filter(
                                        (o1) => o1.id === allRevisions[revision].id,
                                    ).length !== 0) || isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                    size='small'
                                    color='#38536c'
                                    startIcon={<DeleteForeverIcon />}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.delete'
                                        defaultMessage='Delete'
                                    />
                                </Button>
                            </Grid>
                        </Grid>,
                    );
                } else {
                    items.push(
                        <Grid item>
                            <Grid className={classes.textShape5} />
                            <Grid className={classes.textShape2}>
                                {allRevisions[revision].displayName}
                            </Grid>
                            <Grid className={classes.textPadding}>
                                <Button
                                    className={classes.textShape3}
                                    onClick={() => toggleOpenConfirmRestore(
                                        allRevisions[revision].displayName, allRevisions[revision].id,
                                    )}
                                    size='small'
                                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                    type='submit'
                                    startIcon={<RestoreIcon />}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.restore'
                                        defaultMessage='Restore'
                                    />
                                </Button>
                                <Button
                                    className={classes.textShape7}
                                    onClick={() => toggleOpenConfirmDelete(
                                        allRevisions[revision].displayName, allRevisions[revision].id,
                                    )}
                                    disabled={(allEnvRevision && allEnvRevision.filter(
                                        (o1) => o1.id === allRevisions[revision].id,
                                    ).length !== 0) || isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                    size='small'
                                    color='#38536c'
                                    startIcon={<DeleteForeverIcon />}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.delete'
                                        defaultMessage='Delete'
                                    />
                                </Button>
                            </Grid>
                            <ReturnItem6
                                revDescription={allRevisions[revision].description}
                                revName={allRevisions[revision].displayName}
                                revCreatedTime={allRevisions[revision].createdTime}
                            />
                            {item6}
                        </Grid>,
                    );
                }
            }
            if (allRevisions.length !== revisionCount) {
                items.push(
                    <Grid item>
                        <Grid className={classes.textShape4}>
                            {item4}
                        </Grid>
                    </Grid>,
                );
            }
            if (allRevisions.length === revisionCount) {
                items.push(
                    <Grid item>
                        <Grid className={classes.textShape4}>
                            {item5}
                        </Grid>
                    </Grid>,
                );
            }
            for (let unassignRevision = 0; unassignRevision
                < (revisionCount - (allRevisions.length + 1)); unassignRevision++) {
                items.push(
                    <Grid item>
                        <Grid className={classes.textShape4} />
                        {item2}
                    </Grid>,
                );
            }
            if (revisionCount !== allRevisions.length) {
                items.push(
                    <Grid item>
                        <Grid className={classes.textShape4} />
                        {item3}
                    </Grid>,
                );
            }
        }

        if (allRevisions && allRevisions.length === 0) {
            items.push(
                <div>
                    <Grid className={classes.textShape8} />
                    <Grid item className={clsx(classes.shapeCircleBlack, classes.shapeCircle)} />
                </div>,
            );
            items.push(
                <Grid item>
                    <Grid className={classes.textShape4}>
                        {item4}
                    </Grid>
                </Grid>,
            );
            for (let revision = 0; revision < (revisionCount - (allRevisions.length + 1)); revision++) {
                items.push(
                    <Grid item>
                        <Grid className={classes.textShape4} />
                        {item2}
                    </Grid>,
                );
            }
            items.push(
                <Grid item>
                    <Grid className={classes.textShape4} />
                    {item3}
                </Grid>,
            );
        }
    }

    /**
     * Get gateway access URL from vhost
     * @param vhost VHost object
     * @param type URL type WS or HTTP
     * @returns {{secondary: string, primary: string}}
     */
    function getGatewayAccessUrl(vhost, type) {
        const endpoints = { primary: '', secondary: '', combined: '' };
        if (!vhost) {
            return endpoints;
        }

        if (type === 'WS') {
            endpoints.primary = 'ws://' + vhost.host + ':' + vhost.wsPort;
            endpoints.secondary = 'wss://' + vhost.host + ':' + vhost.wssPort;
            endpoints.combined = endpoints.secondary + ' ' + endpoints.primary;
            return endpoints;
        }

        const httpContext = vhost.httpContext ? '/' + vhost.httpContext.replace(/^\//g, '') : '';
        endpoints.primary = 'http://' + vhost.host
            + (vhost.httpPort === 80 ? '' : ':' + vhost.httpPort) + httpContext;
        endpoints.secondary = 'https://' + vhost.host
            + (vhost.httpsPort === 443 ? '' : ':' + vhost.httpsPort) + httpContext;
        endpoints.combined = endpoints.secondary + ' ' + endpoints.primary;
        return endpoints;
    }

    function getVhostHelperText(env, selectionList, shorten, maxTextLen) {
        const selected = selectionList && selectionList.find((v) => v.env === env);
        if (selected) {
            const vhost = settings.environment.find((e) => e.name === env).vhosts.find(
                (v) => v.host === selected.vhost,
            );

            const maxtLen = maxTextLen || 30;
            const gatewayUrls = getGatewayAccessUrl(vhost, api.isWebSocket() ? 'WS' : 'HTTP');
            if (shorten) {
                const helperText = getGatewayAccessUrl(vhost, api.isWebSocket() ? 'WS' : 'HTTP').secondary;
                return helperText.length > maxtLen ? helperText.substring(0, maxtLen) + '...' : helperText;
            }
            return gatewayUrls.combined;
        }
        return '';
    }

    return (
        <>
            {allRevisions && allRevisions.length === 0 && (
                <DeploymentOnbording
                    classes={classes}
                    getVhostHelperText={getVhostHelperText}
                    createDeployRevision={createDeployRevision}
                    description
                    setDescription={setDescription}
                />
            )}
            {allRevisions && allRevisions.length !== 0 && (
                <Grid md={12}>
                    <Typography id='itest-api-details-deployments-head' variant='h5' gutterBottom>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.deployments.heading'
                            defaultMessage='Deployments'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.deployments.sub.heading'
                            defaultMessage='Create revisions and deploy in Gateway Environments'
                        />
                    </Typography>
                </Grid>
            )}
            {!api.isRevision && allRevisions && allRevisions.length !== 0
            && (
                <Grid container>
                    <Button
                        onClick={toggleDeployRevisionPopup}
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                        variant='contained'
                        color='primary'
                        size='large'
                        className={classes.deployNewRevButtonStyle}
                    >
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.deploy.new.revision'
                            defaultMessage='Deploy New Revision'
                        />
                    </Button>
                </Grid>
            )}
            <Grid container>
                <Dialog
                    open={openDeployPopup}
                    onClose={handleCloseDeployPopup}
                    aria-labelledby='form-dialog-title'
                    classes={{ paper: classes.dialogPaper }}
                >
                    <DialogTitle id='form-dialog-title' variant='h2'>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.deploy.new.revision.heading'
                            defaultMessage='Deploy New Revision'
                        />
                    </DialogTitle>
                    <DialogContent className={classes.dialogContent}>
                        { allRevisions && allRevisions.length === revisionCount && (
                            <Typography variant='body' align='left' className={classes.warningText}>
                                <FormattedMessage
                                    id='Apis.Details.Environments.Environments.select.rev.warning'
                                    defaultMessage={'Please delete a revision as '
                                    + 'the number of revisions have reached a maximum of {count}'}
                                    values={{ count: revisionCount }}
                                />
                            </Typography>
                        )}
                        { allRevisions && allRevisions.length === revisionCount && (
                            <Box mb={3}>
                                <TextField
                                    fullWidth
                                    id='revision-to-delete-selector'
                                    required
                                    select
                                    label={(
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.select.rev.delete'
                                            defaultMessage='Revision to delete'
                                        />
                                    )}
                                    SelectProps={{
                                        MenuProps: {
                                            anchorOrigin: {
                                                vertical: 'bottom',
                                                horizontal: 'left',
                                            },
                                            getContentAnchorEl: null,
                                        },
                                    }}
                                    name='extraRevisionToDelete'
                                    onChange={handleDeleteSelect}
                                    helperText={allRevisions && allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).length === 0
                                        ? (
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.select.rev.helper1'
                                                defaultMessage={'Please undeploy and delete a revision as '
                                                + 'the number of revisions have reached a maximum of {count}'}
                                                values={{ count: revisionCount }}
                                            />
                                        ) : (
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.select.rev.helper'
                                                defaultMessage={'Please select a revision to delete as '
                                                + 'the number of revisions have reached a maximum of {count}'}
                                                values={{ count: revisionCount }}
                                            />
                                        )}
                                    margin='normal'
                                    variant='outlined'
                                    disabled={api.isRevision || allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).length === 0}
                                >
                                    {allRevisions && allRevisions.length !== 0 && allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).map(
                                        (revision) => (
                                            <MenuItem value={revision.id} name={revision.displayName}>
                                                {revision.displayName}
                                            </MenuItem>
                                        ),
                                    )}
                                </TextField>
                            </Box>
                        )}
                        <Box mb={3}>
                            <TextField
                                autoFocus
                                name='description'
                                margin='dense'
                                variant='outlined'
                                label='Description'
                                inputProps={{ maxLength: maxCommentLength }}
                                helperText={(
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.description.deploy'
                                        defaultMessage='Brief description of the new revision'
                                    />
                                )}
                                fullWidth
                                multiline
                                rows={3}
                                rowsMax={4}
                                defaultValue={description}
                                onBlur={handleChange}
                            />
                            <Typography className={classes.textCount} align='right'>
                                {currentLength + '/' + maxCommentLength}
                            </Typography>
                        </Box>
                        <Box mt={2}>
                            <Typography variant='h6' align='left' className={classes.sectionTitle}>
                                <FormattedMessage
                                    id='Apis.Details.Environments.Environments.api.gateways.heading'
                                    defaultMessage='API Gateways'
                                />
                            </Typography>
                            <Grid
                                container
                                spacing={3}
                            >
                                {settings.environment.map((row) => (
                                    <Grid item xs={4}>
                                        <Card
                                            className={clsx(SelectedEnvironment
                                                && SelectedEnvironment.includes(row.name)
                                                ? (classes.changeCard) : (classes.noChangeCard), classes.cardHeight)}
                                            variant='outlined'
                                        >
                                            <Box height='100%'>
                                                <CardHeader
                                                    action={(
                                                        <Checkbox
                                                            id={row.name.split(' ').join('')}
                                                            value={row.name}
                                                            checked={SelectedEnvironment.includes(row.name)}
                                                            onChange={handleChange}
                                                            color='primary'
                                                            icon={<RadioButtonUncheckedIcon />}
                                                            checkedIcon={<CheckCircleIcon color='primary' />}
                                                            inputProps={{ 'aria-label': 'secondary checkbox' }}
                                                        />
                                                    )}
                                                    title={(
                                                        <Typography variant='subtitle2'>
                                                            {row.displayName}
                                                        </Typography>
                                                    )}
                                                    subheader={(
                                                        <Typography
                                                            variant='body2'
                                                            color='textSecondary'
                                                            gutterBottom
                                                        >
                                                            {row.type}
                                                        </Typography>
                                                    )}
                                                />
                                                <CardContent className={classes.cardContentHeight}>
                                                    <Grid
                                                        container
                                                        direction='column'
                                                        spacing={2}
                                                    >
                                                        <Grid item xs={12}>
                                                            <Tooltip
                                                                title={getVhostHelperText(row.name,
                                                                    selectedVhostDeploy)}
                                                                placement='bottom'
                                                            >
                                                                <TextField
                                                                    id='vhost-selector'
                                                                    select
                                                                    label={(
                                                                        <FormattedMessage
                                                                            id='Apis.Details.Environments.deploy.vhost'
                                                                            defaultMessage='VHost'
                                                                        />
                                                                    )}
                                                                    SelectProps={{
                                                                        MenuProps: {
                                                                            anchorOrigin: {
                                                                                vertical: 'bottom',
                                                                                horizontal: 'left',
                                                                            },
                                                                            getContentAnchorEl: null,
                                                                        },
                                                                    }}
                                                                    name={row.name}
                                                                    value={selectedVhostDeploy.find(
                                                                        (v) => v.env === row.name,
                                                                    ).vhost}
                                                                    onChange={handleVhostDeploySelect}
                                                                    margin='dense'
                                                                    variant='outlined'
                                                                    fullWidth
                                                                    helperText={getVhostHelperText(row.name,
                                                                        selectedVhostDeploy, true)}
                                                                >
                                                                    {row.vhosts.map(
                                                                        (vhost) => (
                                                                            <MenuItem value={vhost.host}>
                                                                                {vhost.host}
                                                                            </MenuItem>
                                                                        ),
                                                                    )}
                                                                </TextField>
                                                            </Tooltip>
                                                        </Grid>
                                                        <Grid item>
                                                            {allEnvRevision
                                                                && allEnvRevision.filter(
                                                                    (o1) => {
                                                                        if (o1.deploymentInfo.filter(
                                                                            (o2) => o2.name === row.name,
                                                                        ).length > 0) {
                                                                            return o1;
                                                                        }
                                                                        return null;
                                                                    },
                                                                ).length !== 0 ? (
                                                                    allEnvRevision && allEnvRevision.filter(
                                                                        (o1) => {
                                                                            if (o1.deploymentInfo.filter(
                                                                                (o2) => o2.name === row.name,
                                                                            ).length > 0) {
                                                                                return o1;
                                                                            }
                                                                            return null;
                                                                        },
                                                                    ).map((o3) => (
                                                                        <div>
                                                                            <Chip
                                                                                label={o3.displayName}
                                                                                style={{ backgroundColor: '#15B8CF' }}
                                                                            />
                                                                        </div>
                                                                    ))) : (
                                                                // eslint-disable-next-line react/jsx-indent
                                                                    <div />
                                                                )}
                                                        </Grid>
                                                        <Grid item />
                                                    </Grid>
                                                </CardContent>
                                            </Box>
                                        </Card>
                                    </Grid>
                                ))}
                            </Grid>
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDeployPopup}>
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.deploy.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                        <Button
                            type='submit'
                            variant='contained'
                            onClick={
                                () => handleCreateAndDeployRevision(SelectedEnvironment, selectedVhostDeploy)
                            }
                            color='primary'
                            disabled={SelectedEnvironment.length === 0
                                || (allRevisions && allRevisions.length === revisionCount && !extraRevisionToDelete)
                                || isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                        >
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.deploy.deploy'
                                defaultMessage='Deploy'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </Grid>
            {allRevisions && allRevisions.length !== 0 && (
                <>
                    <Grid
                        container
                        direction='row'
                        alignItems='flex-start'
                        spacing={1}
                    >
                        <Grid item>
                            <Typography variant='h6' gutterBottom>
                                <FormattedMessage
                                    id='Apis.Details.Environments.Environments.Deployments'
                                    defaultMessage='Revisions'
                                />
                            </Typography>
                        </Grid>
                        <Grid item>
                            <Tooltip
                                title={(
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.Create.Revision.Deploy'
                                        defaultMessage='Create new revision and deploy'
                                    />
                                )}
                                placement='top-end'
                                aria-label='New Deployment'
                            >
                                <IconButton size='small' aria-label='delete'>
                                    <HelpOutlineIcon fontSize='small' />
                                </IconButton>
                            </Tooltip>
                        </Grid>

                    </Grid>
                    <Box className={classes.containerOverflow}>
                        <Grid
                            xs={12}
                            className={classes.containerInline}
                        >
                            {items}
                            {confirmDeleteDialog}
                            {confirmRestoreDialog}
                        </Grid>
                    </Box>
                </>
            )}
            <Grid container>
                <Dialog
                    open={open}
                    onClose={handleClose}
                    aria-labelledby='form-dialog-title'
                    classes={{ paper: classes.createRevisionDialogStyle }}
                >
                    <DialogTitle id='form-dialog-title' variant='h2'>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.revision.create.heading'
                            defaultMessage='Create New Revision (From Current API)'
                        />
                    </DialogTitle>
                    <DialogContent className={classes.dialogContent}>
                        { allRevisions && allRevisions.length === revisionCount && (
                            <Typography variant='body' align='left' className={classes.warningText}>
                                <FormattedMessage
                                    id='Apis.Details.Environments.Environments.select.rev.warning'
                                    defaultMessage={'Please delete a revision as '
                                    + 'the number of revisions have reached a maximum of {count}'}
                                    values={{ count: revisionCount }}
                                />
                            </Typography>
                        )}
                        { allRevisions && allRevisions.length === revisionCount && (
                            <Box mb={3}>
                                <TextField
                                    fullWidth
                                    id='revision-to-delete-selector'
                                    required
                                    select
                                    label={(
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.select.rev.delete'
                                            defaultMessage='Revision to delete'
                                        />
                                    )}
                                    SelectProps={{
                                        MenuProps: {
                                            anchorOrigin: {
                                                vertical: 'bottom',
                                                horizontal: 'left',
                                            },
                                            getContentAnchorEl: null,
                                        },
                                    }}
                                    name='extraRevisionToDelete'
                                    onChange={handleDeleteSelect}
                                    helperText={allRevisions && allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).length === 0
                                        ? (
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.select.rev.helper1'
                                                defaultMessage={'Please undeploy and delete a revision as '
                                                + 'the number of revisions have reached a maximum of {count}'}
                                                values={{ count: revisionCount }}
                                            />
                                        ) : (
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.select.rev.helper'
                                                defaultMessage={'Please select a revision to delete as '
                                                + 'the number of revisions have reached a maximum of {count}'}
                                                values={{ count: revisionCount }}
                                            />
                                        )}
                                    margin='normal'
                                    variant='outlined'
                                    disabled={api.isRevision || allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).length === 0}
                                >
                                    {allRevisions && allRevisions.length !== 0 && allRevisions.filter(
                                        (o1) => o1.deploymentInfo.length === 0,
                                    ).map(
                                        (revision) => (
                                            <MenuItem value={revision.id} name={revision.displayName}>
                                                {revision.displayName}
                                            </MenuItem>
                                        ),
                                    )}
                                </TextField>
                            </Box>
                        )}
                        <Box mb={3}>
                            <TextField
                                autoFocus
                                name='description'
                                margin='dense'
                                variant='outlined'
                                label='Description'
                                inputProps={{ maxLength: maxCommentLength }}
                                helperText={(
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.revision.description.create'
                                        defaultMessage='Brief description of the new revision'
                                    />
                                )}
                                fullWidth
                                multiline
                                rows={3}
                                rowsMax={4}
                                defaultValue={description}
                                onBlur={handleChange}
                            />
                            <Typography className={classes.textCount} align='right'>
                                {currentLength + '/' + maxCommentLength}
                            </Typography>
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleClose}>
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.create.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                        <Button
                            onClick={handleClickAddRevision}
                            type='submit'
                            variant='contained'
                            color='primary'
                            disabled={api.isRevision
                                || (allRevisions && allRevisions.length === revisionCount && !extraRevisionToDelete)}
                        >
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.create.create'
                                defaultMessage='Create'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </Grid>
            {allRevisions && allRevisions.length !== 0 && (
                <Box mx='auto' mt={5}>
                    <Typography variant='h6' className={classes.sectionTitle}>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.APIGateways'
                            defaultMessage='API Gateways'
                        />
                    </Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell align='left'>
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.api.gateways.name'
                                            defaultMessage='Name'
                                        />
                                    </TableCell>
                                    <TableCell align='left'>
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.gateway.accessUrl'
                                            defaultMessage='Gateway Access URL'
                                        />
                                    </TableCell>
                                    {api && api.isDefaultVersion !== true
                                        ? (
                                            <TableCell align='left'>
                                                <FormattedMessage
                                                    id='Apis.Details.Environments.Environments.gateway
                                                    .deployed.revision'
                                                    defaultMessage='Deployed Revision'
                                                />
                                            </TableCell>
                                        )
                                        : (
                                            <TableCell align='left'>
                                                <FormattedMessage
                                                    id='Apis.Details.Environments.Environments.gateway.action'
                                                    defaultMessage='Action'
                                                />
                                            </TableCell>
                                        )}
                                    <TableCell align='left'>
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.visibility.in.devportal'
                                            defaultMessage='Gateway URL Visibility'
                                        />
                                        <Tooltip
                                            title={(
                                                <FormattedMessage
                                                    id='Apis.Details.Environments.Environments.display.devportal'
                                                    defaultMessage='Display Gateway Access URLs in developer portal.'
                                                />
                                            )}
                                            placement='top-end'
                                            aria-label='New Deployment'
                                        >
                                            <IconButton size='small' aria-label='delete'>
                                                <HelpOutlineIcon fontSize='small' />
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {settings.environment.map((row) => (
                                    <TableRow key={row.name}>
                                        <TableCell component='th' scope='row'>
                                            {row.displayName}
                                        </TableCell>
                                        {allEnvDeployments[row.name].revision != null ? (
                                            <>
                                                <TableCell align='left'>
                                                    <div className={classes.primaryEndpoint}>
                                                        {api.isWebSocket()
                                                            ? getGatewayAccessUrl(allEnvDeployments[row.name]
                                                                .vhost, 'WS')
                                                                .primary : getGatewayAccessUrl(
                                                                allEnvDeployments[row.name].vhost, 'HTTP',
                                                            ).primary}
                                                    </div>
                                                    <div className={classes.secondaryEndpoint}>
                                                        {api.isWebSocket()
                                                            ? getGatewayAccessUrl(allEnvDeployments[row.name]
                                                                .vhost, 'WS')
                                                                .secondary : getGatewayAccessUrl(
                                                                allEnvDeployments[row.name].vhost, 'HTTP',
                                                            ).secondary}
                                                    </div>
                                                </TableCell>
                                            </>
                                        ) : (
                                            <>
                                                <TableCell align='left' className={classes.tableCellVhostSelect}>
                                                    <Tooltip
                                                        title={getVhostHelperText(row.name, selectedVhosts)}
                                                        placement='bottom'
                                                    >
                                                        <TextField
                                                            id='vhost-selector'
                                                            select
                                                            label={(
                                                                <FormattedMessage
                                                                    id='Apis.Details.Environments.Environments
                                                                    .select.vhost'
                                                                    defaultMessage='Select Access URL'
                                                                />
                                                            )}
                                                            SelectProps={{
                                                                MenuProps: {
                                                                    anchorOrigin: {
                                                                        vertical: 'bottom',
                                                                        horizontal: 'left',
                                                                    },
                                                                    getContentAnchorEl: null,
                                                                },
                                                            }}
                                                            name={row.name}
                                                            value={selectedVhosts.find((v) => v.env === row.name).vhost}
                                                            onChange={handleVhostSelect}
                                                            margin='dense'
                                                            variant='outlined'
                                                            className={classes.vhostSelect}
                                                            fullWidth
                                                            disabled={api.isRevision
                                                            || !allRevisions || allRevisions.length === 0}
                                                            helperText={getVhostHelperText(row.name, selectedVhosts,
                                                                true, 100)}
                                                        >
                                                            {row.vhosts.map(
                                                                (vhost) => (
                                                                    <MenuItem value={vhost.host}>
                                                                        {vhost.host}
                                                                    </MenuItem>
                                                                ),
                                                            )}
                                                        </TextField>
                                                    </Tooltip>
                                                </TableCell>
                                            </>
                                        )}
                                        <TableCell align='left' style={{ width: '300px' }}>
                                            {allEnvDeployments[row.name].revision != null
                                                ? (
                                                    <div>
                                                        <Chip
                                                            label={allEnvDeployments[row.name].revision.displayName}
                                                            style={{ backgroundColor: '#15B8CF' }}
                                                        />
                                                        <Button
                                                            className={classes.button1}
                                                            variant='outlined'
                                                            disabled={api.isRevision
                                                                || isRestricted(['apim:api_create',
                                                                    'apim:api_publish'], api)}
                                                            onClick={() => undeployRevision(
                                                                allEnvDeployments[row.name].revision.id, row.name,
                                                            )}
                                                            size='small'
                                                        >
                                                            <FormattedMessage
                                                                id='Apis.Details.Environments.Environments.undeploy.btn'
                                                                defaultMessage='Undeploy'
                                                            />
                                                        </Button>
                                                    </div>
                                                ) : (
                                                    <div>
                                                        <TextField
                                                            id='revision-selector'
                                                            select
                                                            label={(
                                                                <FormattedMessage
                                                                    id='Apis.Details.Environments.Environments
                                                                    .select.table'
                                                                    defaultMessage='Select Revision'
                                                                />
                                                            )}
                                                            SelectProps={{
                                                                MenuProps: {
                                                                    anchorOrigin: {
                                                                        vertical: 'bottom',
                                                                        horizontal: 'left',
                                                                    },
                                                                    getContentAnchorEl: null,
                                                                },
                                                            }}
                                                            name={row.name}
                                                            onChange={handleSelect}
                                                            margin='dense'
                                                            variant='outlined'
                                                            style={{ width: '50%' }}
                                                            disabled={api.isRevision
                                                                || !allRevisions || allRevisions.length === 0}
                                                        >
                                                            {allRevisions && allRevisions.length !== 0
                                                            && allRevisions.map(
                                                                (number) => (
                                                                    <MenuItem value={number.id}>
                                                                        {number.displayName}
                                                                    </MenuItem>
                                                                ),
                                                            )}
                                                        </TextField>
                                                        <Button
                                                            className={classes.button2}
                                                            disabled={api.isRevision || !selectedRevision.some(
                                                                (r) => r.env === row.name && r.revision,
                                                            ) || !selectedVhosts.some(
                                                                (v) => v.env === row.name && v.vhost,
                                                            )}
                                                            variant='outlined'
                                                            onClick={() => deployRevision(selectedRevision.find(
                                                                (r) => r.env === row.name,
                                                            ).revision, row.name, selectedVhosts.find(
                                                                (v) => v.env === row.name,
                                                            ).vhost, selectedRevision.find(
                                                                (r) => r.env === row.name,
                                                            ).displayOnDevPortal)}

                                                        >
                                                            <FormattedMessage
                                                                id='Apis.Details.Environments.Environments
                                                                .deploy.button'
                                                                defaultMessage='Deploy'
                                                            />
                                                        </Button>
                                                    </div>
                                                )}
                                        </TableCell>
                                        <TableCell align='left'>
                                            <DisplayDevportal
                                                name={row.name}
                                                api={api}
                                                EnvDeployments={allEnvDeployments[row.name]}
                                            />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Box>
            )}
        </>
    );
}
