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
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import useWindowSize from 'AppComponents/Shared/UseWindowSize';
import View from 'AppComponents/Apis/Details/Documents/View';

const styles = theme => ({
    paper: {
        padding: theme.spacing.unit * 2,
        color: theme.palette.text.secondary,
        minHeight: 400,
        position: 'relative',
    },
    paperMenu: {
        color: theme.palette.text.secondary,
        minHeight: 400 + theme.spacing.unit * 4,
        height: '100%',
    },
    contentWrapper: {
        paddingLeft: theme.spacing.unit * 3,
        paddingRight: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 3,
    },
    docContent: {
        paddingTop: theme.spacing.unit,
    },
    parentListItem: {
        borderTop: 'solid 1px #ccc',
        borderBottom: 'solid 1px #ccc',
        color: theme.palette.grey[100],
        background: theme.palette.grey[100],
        cursor: 'default',
    },
    listRoot: {
        paddingTop: 0,
    },
    nested: {
        paddingLeft: theme.spacing.unit * 3,
        paddingTop: 3,
        paddingBottom: 3,
    },
    fullView: {
        cursor: 'pointer',
        position: 'absolute',
        right: 5,
        top: 5,
    },
    childList: {
        paddingTop: 0,
        marginTop: 0,
        paddingBottom: 0,
    },
    popupHeader: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        position: 'fixed',
        width: '100%',
    },
    viewWrapper: {
        padding: theme.spacing.unit * 2,
        marginTop: 50,
    },
    titleSub: {
        marginLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    generateCredentialWrapper: {
        marginLeft: 0,
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    genericMessageWrapper: {
        marginLeft: theme.spacing(2),
    },
    typeText: {
        color: '#000',
    },
    docLinkRoot: {
        paddingLeft: 0,
    },
    toggler: {
        height: '100%',
        paddingTop: 20,
        cursor: 'pointer',
        marginLeft: '-20px',
        display: 'block',
    },
    togglerTextParent: {
        writingMode: 'vertical-rl',
    },
    togglerText: {
        textOrientation: 'sideways',
    },
    toggleWrapper: {
        position: 'relative',
        background: '#fff9',
        paddingLeft: 20,
    },
    docContainer: {
        display: 'flex',
        marginLeft: 20,
        marginRight: 20,
        marginTop: 20,
    },
    docListWrapper: {
        width: 285,
    },
    docView: {
        flex: 1,
    },
    listItemRoot: {
        minWidth: 30, 
    },
});
window.requestAnimFrame = (function () {
    return (
        window.requestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        function (callback) {
            window.setTimeout(callback, 1000 / 60);
        }
    );
}());

// main function
function scrollToY(scrollTargetY, speed, easing) {
    // scrollTargetY: the target scrollY property of the window
    // speed: time in pixels per second
    // easing: easing equation to use

    var scrollY = window.scrollY || document.documentElement.scrollTop,
        scrollTargetY = scrollTargetY || 0,
        speed = speed || 2000,
        easing = easing || 'easeOutSine',
        currentTime = 0;

    // min time .1, max time .8 seconds
    const time = Math.max(0.1, Math.min(Math.abs(scrollY - scrollTargetY) / speed, 0.8));

    // easing equations from https://github.com/danro/easing-js/blob/master/easing.js
    const easingEquations = {
        easeOutSine(pos) {
            return Math.sin(pos * (Math.PI / 2));
        },
        easeInOutSine(pos) {
            return -0.5 * (Math.cos(Math.PI * pos) - 1);
        },
        easeInOutQuint(pos) {
            if ((pos /= 0.5) < 1) {
                return 0.5 * Math.pow(pos, 5);
            }
            return 0.5 * (Math.pow(pos - 2, 5) + 2);
        },
    };

    // add animation loop
    function tick() {
        currentTime += 1 / 60;

        const p = currentTime / time;
        const t = easingEquations[easing](p);

        if (p < 1) {
            requestAnimFrame(tick);

            window.scrollTo(0, scrollY + (scrollTargetY - scrollY) * t);
        } else {
            console.log('scroll done');
            window.scrollTo(0, scrollTargetY);
        }
    }

    // call it once to get started
    tick();
}
function FullWidthGrid(props) {
    const { classes, intl } = props;
    const [selectedIndexA, changeSelectedIndexA] = useState(0);
    const [selectedIndexB, changeSelectedIndexB] = useState(0);
    const [documentList, changeDocumentList] = useState(null);
    const [selectedDoc, changeSelectedDoc] = useState(null);
    const [open, setOpen] = useState(false);
    const [width, height] = useWindowSize();
    const [showDocList, setShowDocList] = useState(width < 1400 ? false: true);
    const toggleDocList = () => {
        setShowDocList(!showDocList);
    };
    const handleListItemClick = (event, newIndexA, newIndexB, doc) => {
        changeSelectedIndexA(newIndexA);
        changeSelectedIndexB(newIndexB);
        changeSelectedDoc(doc);
        scrollToY(0, 1500, 'easeInOutQuint');
    };
    const apiId = props.match.params.apiUuid;
    useEffect(() => {
         width < 1400 ? setShowDocList(false) : setShowDocList(true);
    }, [width]);
    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(apiId);
        promisedApi
            .then((response) => {
                const overviewDoc = response.body.list.filter(item => item.otherTypeName !== '_overview');
                const types = [];
                if (overviewDoc.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    for (let i = 0; i < overviewDoc.length; i++) {
                        const selectedType = overviewDoc[i].type;
                        let hasType = false;
                        for (let j = 0; j < types.length; j++) {
                            if (selectedType === types[j].docType) {
                                types[j].docs.push(overviewDoc[i]);
                                hasType = true;
                            }
                        }
                        if (!hasType) {
                            // Adding a new type entry
                            types.push({
                                docType: selectedType,
                                docs: [overviewDoc[i]],
                            });
                        }
                    }
                }
                changeDocumentList(types);
                if (types.length > 0) {
                    handleListItemClick(null, 0, 0, types[0].docs[0]);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    Alert.error('Error occured');
                }
            });
    }, []);
   
    const toggleOpen = () => {
        setOpen(!open);
    };
    return (
        <Grid container className={classes.contentWrapper}>
            <Grid item md={12} lg={12}>
                <Grid container spacing={5}>
                    <Grid item md={12}>
                        <Typography variant='h4' className={classes.titleSub}>
                            <FormattedMessage
                                id='Apis.Details.Documents.Documentation.title'
                                defaultMessage='Documentation'
                            />
                        </Typography>
                        {!documentList || (documentList && documentList.length === 0) ? (
                            <div className={classes.genericMessageWrapper}>
                                <GenericDisplayDialog
                                    classes={classes}
                                    heading={intl.formatMessage({
                                        defaultMessage: 'No Documents Yet',
                                        id: 'Apis.Details.Documents.Documentation.no.docs',
                                    })}
                                    caption={intl.formatMessage({
                                        defaultMessage: 'No documents available for this API yet',
                                        id: 'Apis.Details.Documents.Documentation.no.docs.content',
                                    })}
                                />
                            </div>
                        ) : (
                            <div className={classes.docContainer}>
                                {showDocList && (
                                    <div className={classes.docListWrapper}>
                                        <Paper className={classes.paperMenu}>
                                            <List component='nav' className={classes.listRoot}>
                                                {documentList.map((type, indexA) => (
                                                    <React.Fragment>
                                                        <ListItem className={classes.parentListItem}>
                                                            <ListItemIcon classes={{root: classes.listItemRoot}}>
                                                                <CustomIcon
                                                                    strokeColor='#444'
                                                                    width={24}
                                                                    height={24}
                                                                    icon='docs'
                                                                />
                                                            </ListItemIcon>
                                                            <ListItemText
                                                                primary={type.docType}
                                                                classes={{ root: classes.typeText }}
                                                            />
                                                        </ListItem>
                                                        {type.docs.length > 0 && (
                                                            <List component='div' className={classes.childList}>
                                                                {type.docs.map((doc, indexB) => (
                                                                    <ListItem
                                                                        button
                                                                        className={classes.nested}
                                                                        classes={{
                                                                            selected: classes.selected,
                                                                        }}
                                                                        selected={
                                                                            selectedIndexA === indexA &&
                                                                            selectedIndexB === indexB
                                                                        }
                                                                        onClick={event =>
                                                                            handleListItemClick(
                                                                                event,
                                                                                indexA,
                                                                                indexB,
                                                                                doc,
                                                                            )
                                                                        }
                                                                    >
                                                                        <ListItemIcon classes={{root: classes.listItemRoot}}>
                                                                            {doc.sourceType === 'MARKDOWN' && (
                                                                                <Icon>code</Icon>
                                                                            )}
                                                                            {doc.sourceType === 'INLINE' && (
                                                                                <Icon>description</Icon>
                                                                            )}
                                                                            {doc.sourceType === 'URL' && (
                                                                                <Icon>open_in_new</Icon>
                                                                            )}
                                                                            {doc.sourceType === 'FILE' && (
                                                                                <Icon>arrow_downward</Icon>
                                                                            )}
                                                                        </ListItemIcon>
                                                                        <ListItemText
                                                                            inset
                                                                            primary={doc.name}
                                                                            classes={{ root: classes.docLinkRoot }}
                                                                        />
                                                                    </ListItem>
                                                                ))}
                                                            </List>
                                                        )}
                                                    </React.Fragment>
                                                ))}
                                            </List>
                                        </Paper>
                                    </div>
                                )}
                                <div className={classes.toggleWrapper}>
                                    <a className={classes.toggler} onClick={toggleDocList}>
                                        <div className={classes.togglerTextParent}>
                                            <div className={classes.togglerText}>
                                                {showDocList ? (
                                                    <FormattedMessage
                                                        id='Apis.Details.Documents.Documentation.hide'
                                                        defaultMessage='HIDE'
                                                    />
                                                ) : (
                                                    <FormattedMessage
                                                        id='Apis.Details.Documents.Documentation.show'
                                                        defaultMessage='SHOW'
                                                    />
                                                )}
                                            </div>
                                        </div>
                                        {showDocList ? (
                                            <Icon>keyboard_arrow_left</Icon>
                                        ) : (
                                            <Icon>keyboard_arrow_right</Icon>
                                        )}
                                    </a>
                                </div>
                                <div className={classes.docView}>
                                    {selectedDoc && (
                                        <React.Fragment>
                                            <Paper className={classes.paper}>
                                                {(selectedDoc.sourceType === 'MARKDOWN' ||
                                                    selectedDoc.sourceType === 'INLINE') && (
                                                    <Icon className={classes.fullView} onClick={toggleOpen}>
                                                        launch
                                                    </Icon>
                                                )}
                                                <View doc={selectedDoc} apiId={apiId} fullScreen={open} />
                                            </Paper>
                                            <Dialog fullScreen open={open} onClose={toggleOpen}>
                                                <Paper square className={classes.popupHeader}>
                                                    <IconButton color='inherit' onClick={toggleOpen} aria-label='Close'>
                                                        <Icon>close</Icon>
                                                    </IconButton>
                                                    <Typography variant='h4' className={classes.docName}>
                                                        {selectedDoc.name}
                                                    </Typography>
                                                </Paper>
                                                <div className={classes.viewWrapper}>
                                                    <View doc={selectedDoc} apiId={apiId} fullScreen={open} />
                                                </div>
                                            </Dialog>
                                        </React.Fragment>
                                    )}
                                </div>
                            </div>
                        )}
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}

FullWidthGrid.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(FullWidthGrid));
