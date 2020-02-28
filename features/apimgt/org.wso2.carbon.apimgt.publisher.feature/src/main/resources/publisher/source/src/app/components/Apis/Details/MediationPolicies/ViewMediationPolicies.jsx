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

import React, { useState, useEffect, useContext } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import {
    Dialog, CardActions,
} from '@material-ui/core';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import ArrowRightAltIcon from '@material-ui/icons/ArrowRightAlt';
import Typography from '@material-ui/core/Typography';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Button from '@material-ui/core/Button';
import API from 'AppData/api.js';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import VisibilityIcon from '@material-ui/icons/Visibility';
import Tooltip from '@material-ui/core/Tooltip';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import FileCopyIcon from '@material-ui/icons/FileCopy';
import CloseIcon from '@material-ui/icons/Close';
import MessageTrace from './MessageTrace';

const useStyles = makeStyles((theme) => ({
    container: {
        border: '1px solid black',
        borderRadius: '20px',
        backgroundColor: theme.palette.black,
    },
    diagramContainer: {
        border: '1px dashed black',
        borderRadius: '20px',
        position: 'relative',
        display: 'flex',
        overflowX: 'auto',
        overflowY: 'hidden',
        padding: '10px 30px',
    },
    imgContainer: {
        borderRadius: '100%',
        height: '100%',
        width: '100px',
        overflow: 'hidden',
        margin: '10px',
    },
    img: {
        width: '100%',
        height: '100%',
    },
    mediator: {
        display: 'flex',
        marginInlineEnd: '100px',
    },
    cardContainer: {
        display: 'flex',
        alignItems: 'center',
    },
    name: {
        textAlign: 'center',
        width: '100px',
    },
    card: {
        minHeight: '200px',
    },
    customWidth: {
        maxWidth: '200px',
        fontSize: '12px',
    },
    diffView: {
        width: '80px',
        fontSize: '10px',
    },
    appBar: {
        position: 'relative',
        backgroundColor: theme.palette.background.appBar,
    },
    closeBtn: {
        backgroundColor: theme.palette.getContrastText(theme.palette.background.appBar),
        justifyContent: 'flex-end',
        '&:hover': {
            backgroundColor: theme.palette.getContrastText(theme.palette.background.appBar),
        },
    },
    titleRoot: {
        padding: 0,
    },
    dialogTitle: {
        flex: 1,
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
    traceView: {
        overflowY: 'auto',
    },
    arrow: {
        paddingRight: 10,
        fontSize: 100,
        top: '80px',
        width: '100px',
        position: 'absolute',
    },
    iconButtonPlaser: {
        position: 'absolute',
        bottom: theme.spacing(6),
        marginLeft: 27,
        fontsize: '10px',
    },
}));

function ViewMediationPolicies(props) {
    const {
        intl, selectedMediationPolicy,
    } = props;

    const classes = useStyles();
    const [addedMediators, setAddedMediators] = useState([]);
    const [eventAddedArray, setEventAddedArray] = useState([]);
    const [open, setOpen] = useState(false);
    const [traceViewing, setTraceViewing] = useState(false);
    const [name, setName] = useState(null);
    const [componentName, setComponentName] = useState(null);
    const [diffViewActive, setDiffViewActive] = useState(false);

    const { api } = useContext(ApiContext);
    const { id: apiId } = api;
    // eslint-disable-next-line global-require
    const convertToJson = require('xml-js');

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
        setTraceViewing(false);
        setDiffViewActive(false);
    };

    const startTraceViewing = (mediatorName, medaitorComponentName) => {
        setName(mediatorName);
        setComponentName(medaitorComponentName);
        setTraceViewing(true);
        setDiffViewActive(false);
    };

    const startDiffViewing = (mediatorName, medaitorComponentName) => {
        setName(mediatorName);
        setComponentName(medaitorComponentName);
        setDiffViewActive(true);
    };

    /**
 * Generates Id to a mediator.
 * @returns {string} The generated Id.
 */

    function generateId() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            const r = Math.random() * (16 || 0); const
                v = c === 'x' ? r : (r && (0x3 || 0x8));
            return v.toString(16);
        });
    }

    function getJSONObject(xmlValue) {
        const xmlText = xmlValue;
        const jsonObject = convertToJson.xml2json(xmlText, { compact: false, spaces: 4 });
        console.log(jsonObject);
        return jsonObject;
    }

    function getTracingMesssageEvents(messageTraceId) {
        const promisedGetContent = API.getTracingMessagesEvents(apiId, messageTraceId);
        promisedGetContent
            .then((response) => {
                const arr = response.body;
                const newEventAddedArray = [];
                arr.forEach((element) => {
                    if (element.componentType === 'MEDIATOR') {
                        newEventAddedArray.push(element);
                    }
                });
                newEventAddedArray.splice((newEventAddedArray.length) - 2, 2);
                setEventAddedArray(newEventAddedArray);
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.MediationPolicies.ViewMediationPolicies.error.while.retrieving.tracing.events',
                    defaultMessage: 'Error occurred while retrieving message tracing events',
                }));
            });
    }

    function getTracingMesssageID() {
        const promisedGetContent = API.getTracingMessagesIds(apiId);
        promisedGetContent
            .then((response) => {
                const traceId = response.body;
                getTracingMesssageEvents(traceId);
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.MediationPolicies.ViewMediationPolicies.error.while.retrieving.tracing.list',
                    defaultMessage: 'Error occurred while retrieving message tracing list',
                }));
            });
    }

    function getCustomSequenceContent() {
        const promisedGetContent = API.getMediationPolicyContent(selectedMediationPolicy.id, apiId);
        promisedGetContent
            .then((response) => {
                const jsonText = JSON.parse(getJSONObject(response.body));
                const mediatorIcons = [
                    {
                        key: 'LogMediator',
                        name: 'log',
                        src: 'site/public/images/mediatorIconsSVG/log-mediator.svg',
                    },
                    {
                        key: 'PropertyMediator',
                        name: 'property',
                        src: 'site/public/images/mediatorIconsSVG/property-mediator.svg',
                    },
                    {
                        key: 'DropMediator',
                        name: 'drop',
                        src: 'site/public/images/mediatorIconsSVG/drop-mediator.svg',
                    },
                    {
                        key: 'PropertyGroupMediator',
                        name: 'propertyGroup',
                        src: 'site/public/images/mediatorIconsSVG/propertyGroup-mediator.svg',
                    },
                    {
                        key: 'ScriptMediator',
                        name: 'script',
                        src: 'site/public/images/mediatorIconsSVG/script-mediator.svg',
                    },
                    {
                        key: 'ClassMediator',
                        name: 'class',
                        src: 'site/public/images/mediatorIconsSVG/class-mediator.svg',
                    },
                    {
                        key: 'PayloadFactoryMediator',
                        name: 'payloadFactory',
                        src: 'site/public/images/mediatorIconsSVG/payloadFactory-mediator.svg',
                    },
                    {
                        key: 'HeaderMediator',
                        name: 'header',
                        src: 'site/public/images/mediatorIconsSVG/header-mediator.svg',
                    },
                ];
                getTracingMesssageID();
                const newAddedMediators = [];
                jsonText.elements[0].elements.forEach((mediators) => {
                    // eslint-disable-next-line array-callback-return
                    mediatorIcons.filter((icons) => {
                        if (icons.name === mediators.name) {
                            newAddedMediators.push({
                                uuid: generateId(),
                                ...icons,
                                name: icons.name,
                                attribute_name: mediators.attributes.name,
                                componentName: icons.key + ':' + mediators.attributes.name,
                            });
                        }
                    });
                });
                setAddedMediators(newAddedMediators);
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.MediationPolicies.ViewMediationPolicies.error.while.retrieving.mediation.policy',
                    defaultMessage: 'Error occurred while retrieving mediation policy',
                }));
            });
    }

    useEffect(() => {
        getCustomSequenceContent();
    }, [selectedMediationPolicy]);

    return (
        <>
            <Button
                className={classes.viewIcon}
                size='small'
                onClick={handleOpen}
            >
                <VisibilityIcon />
            </Button>
            <Dialog
                className={classes.container}
                open={open}
                disableBackdropClick
                disableEscapeKeyDown
                fullScreen
                onClose={handleClose}
                aria-labelledby='view-title'
                onEnter={getCustomSequenceContent}
            >
                <DialogTitle id='view-title' className={classes.titleRoot}>
                    <AppBar className={classes.appBar}>
                        <Toolbar className={classes.toolbar}>
                            <Typography variant='h5' className={classes.dialogTitle}>
                                <FormattedMessage
                                    id='Apis.Details.MediationPolicies.ViewMediationPolicies.view'
                                    defaultMessage='Mediation Policy'
                                />
                            </Typography>
                            <IconButton
                                edge='end'
                                className={classes.closeBtn}
                                onClick={handleClose}
                                aria-label='close'
                            >
                                <CloseIcon />
                            </IconButton>
                        </Toolbar>
                    </AppBar>
                </DialogTitle>
                <DialogContent>
                    <Grid className={classes.diagramContainer}>
                        {addedMediators.map((mediator) => {
                            return (
                                <div className={classes.mediator} key={mediator.uuid}>
                                    <div className={classes.cardContainer}>
                                        <Tooltip
                                            title={(
                                                <p className={classes.p}>
                                                    Component Name :
                                                    {' '}
                                                    { mediator.name }
                                                    {' '}
                                                    <br />
                                                    Name :
                                                    {' '}
                                                    {mediator.attribute_name}
                                                    {' '}
                                                    <br />
                                                    Flow :
                                                    {' '}
                                                    {selectedMediationPolicy.type}
                                                </p>
                                            )}
                                            classes={{ tooltip: classes.customWidth }}
                                            placement='right-start'
                                            arrow
                                        >
                                            <Button
                                                onClick={() => startTraceViewing(mediator.name, mediator.componentName)}
                                            >
                                                <Card className={classes.card}>
                                                    <CardContent>
                                                        <Typography
                                                            className={classes.name}
                                                            noWrap
                                                            title={`${mediator.name}Mediator`}
                                                        >
                                                            {mediator.name}
                                                    Mediator
                                                        </Typography>
                                                    </CardContent>
                                                    <CardMedia
                                                        className={classes.imgContainer}
                                                        key={mediator.uuid}
                                                    >
                                                        <img
                                                            src={mediator.src}
                                                            alt='{madiator.name}'
                                                            className={classes.img}
                                                        />
                                                    </CardMedia>
                                                    <CardActions>
                                                        <Typography
                                                            className={classes.name}
                                                            noWrap
                                                            title={mediator.attribute_name}
                                                        >
                                                            {mediator.attribute_name}
                                                        </Typography>
                                                    </CardActions>
                                                </Card>
                                            </Button>
                                        </Tooltip>
                                    </div>
                                    <div>
                                        <Tooltip
                                            title={(
                                                <p>Click to View Diff</p>
                                            )}
                                            classes={{ tooltip: classes.diffView }}
                                            placement='bottom'
                                        >
                                            <IconButton
                                                className={classes.iconButtonPlaser}
                                                onClick={() => startDiffViewing(mediator.name, mediator.componentName)}
                                            >
                                                <FileCopyIcon />
                                                {/* <FormattedMessage
                                                id='Apis.Details.MediationPolicies.ViewMediationPolicies.diff'
                                                defaultMessage='DIFF'
                                            /> */}
                                            </IconButton>
                                        </Tooltip>
                                        <div>
                                            <ArrowRightAltIcon className={classes.arrow} />
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </Grid>
                    <Grid className={classes.traceView}>
                        {traceViewing && (
                            <MessageTrace
                                name={name}
                                eventAddedArray={eventAddedArray}
                                componentName={componentName}
                                diffViewActive={diffViewActive}
                            />
                        )}
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={handleClose}
                        color='primary'
                        variant='contained'
                    >
                        <FormattedMessage
                            id='Apis.Details.MediationPolicies.ViewMediationPolicies.view.done.btn'
                            defaultMessage='Done'
                        />
                    </Button>
                </DialogActions>
            </Dialog>

        </>
    );
}

ViewMediationPolicies.propTypes = {
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};
export default ViewMediationPolicies;
