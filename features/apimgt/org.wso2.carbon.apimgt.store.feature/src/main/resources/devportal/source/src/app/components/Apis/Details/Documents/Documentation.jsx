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
import { FormattedMessage } from 'react-intl';
import Alert from '../../../Shared/Alert';
import API from '../../../../data/api';
import CustomIcon from '../../../Shared/CustomIcon';
import View from './View';

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
});
window.requestAnimFrame = (function () {
    return (
        window.requestAnimationFrame
        || window.webkitRequestAnimationFrame
        || window.mozRequestAnimationFrame
        || function (callback) {
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
    const { classes } = props;
    const [selectedIndexA, changeSelectedIndexA] = useState(0);
    const [selectedIndexB, changeSelectedIndexB] = useState(0);
    const [documentList, changeDocumentList] = useState(null);
    const [selectedDoc, changeSelectedDoc] = useState(null);
    const [open, setOpen] = useState(false);
    const handleListItemClick = (event, newIndexA, newIndexB, doc) => {
        changeSelectedIndexA(newIndexA);
        changeSelectedIndexB(newIndexB);
        changeSelectedDoc(doc);
        scrollToY(0, 1500, 'easeInOutQuint');
    };
    const apiId = props.match.params.api_uuid;

    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(apiId);
        promisedApi
            .then((response) => {
                const types = [];
                if (response.obj.list.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    const allDocs = response.obj.list;
                    for (let i = 0; i < allDocs.length; i++) {
                        const selectedType = allDocs[i].type;
                        let hasType = false;
                        for (let j = 0; j < types.length; j++) {
                            if (selectedType === types[j].docType) {
                                types[j].docs.push(allDocs[i]);
                                hasType = true;
                            }
                        }
                        if (!hasType) {
                            // Adding a new type entry
                            types.push({
                                docType: selectedType,
                                docs: [allDocs[i]],
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
    /*
     *
     *
     * @param {*} summary
     * @returns
     * @memberof Documentation
     */
    const truncateSummary = (summary) => {
        let newSummery = summary;
        const maxCount = 100;
        if (summary.length > maxCount && summary.length > maxCount + 5) {
            newSummery = summary.substring(1, 100) + ' ... ';
        }
        return newSummery;
    };
    const toggleOpen = () => {
        setOpen(!open);
    };
    return (
        <div className={classes.contentWrapper}>
            <Typography variant='h4'>
                <FormattedMessage id='Apis.Details.Documents.Documentation.title' defaultMessage='Documentation' />
            </Typography>
            {documentList && (
                <Grid container spacing={3} className={classes.docContent}>
                    <Grid item xs={12} sm={3}>
                        <Paper className={classes.paperMenu}>
                            <List component='nav' className={classes.listRoot}>
                                {documentList.map((type, indexA) => (
                                    <React.Fragment>
                                        <ListItem className={classes.parentListItem}>
                                            <ListItemIcon>
                                                <CustomIcon strokeColor='#444' width={24} height={24} icon='docs' />
                                            </ListItemIcon>
                                            <ListItemText primary={type.docType} />
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
                                                            selectedIndexA === indexA && selectedIndexB === indexB
                                                        }
                                                        onClick={event => handleListItemClick(event, indexA, indexB, doc)
                                                        }
                                                    >
                                                        <ListItemIcon>
                                                            {doc.sourceType === 'MARKDOWN' && <Icon>code</Icon>}
                                                            {doc.sourceType === 'INLINE' && <Icon>description</Icon>}
                                                            {doc.sourceType === 'URL' && <Icon>open_in_new</Icon>}
                                                            {doc.sourceType === 'FILE' && <Icon>arrow_downward</Icon>}
                                                        </ListItemIcon>
                                                        <ListItemText
                                                            inset
                                                            primary={doc.name}
                                                            secondary={truncateSummary(doc.summary)}
                                                        />
                                                    </ListItem>
                                                ))}
                                            </List>
                                        )}
                                    </React.Fragment>
                                ))}
                            </List>
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={9}>
                        {selectedDoc && (
                            <React.Fragment>
                                <Paper className={classes.paper}>
                                    {(selectedDoc.sourceType === 'MARKDOWN' || selectedDoc.sourceType === 'INLINE') && (
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
                    </Grid>
                </Grid>
            )}
        </div>
    );
}

FullWidthGrid.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(FullWidthGrid);
