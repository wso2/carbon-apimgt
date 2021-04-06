/* eslint-disable react/prop-types */
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

import React, { useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import View from 'AppComponents/Apis/Details/Documents/View';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles((theme) => ({
    fullView: {
        cursor: 'pointer',
        position: 'absolute',
        right: 5,
        top: 5,
    },
    paper: {
        padding: theme.spacing(2),
        color: theme.palette.text.secondary,
        minHeight: 400,
        position: 'relative',
        background: theme.custom.apiDetailPages.documentBackground,
    },
    popupHeader: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        position: 'fixed',
        width: '100%',
    },
    viewWrapper: {
        padding: theme.spacing(2),
        marginTop: 50,
    },
}));
/**
 * Switch routes for documents.
 * @param {JSON} props The props passed down from parents.
 * @returns {JSX} Returning JSX to render.
 */
export default function Details(props) {
    // const restApi = new API();
    const { apiId, selectedDoc } = props;
    const classes = useStyles();
    const [open, setOpen] = useState(false);
    // const [doc, setDoc] = useState(null);
    const toggleOpen = () => {
        setOpen(!open);
    };
    return (
        <>
            <div className={classes.paper}>
                {(selectedDoc.sourceType === 'MARKDOWN' || selectedDoc.sourceType === 'INLINE') && (
                    <IconButton
                        onClick={toggleOpen}
                        aria-label={'View ' + selectedDoc.name + ' document in full screen'}
                        className={classes.fullView}
                    >
                        <Icon>
                            launch
                        </Icon>
                    </IconButton>
                )}
                <View doc={selectedDoc} apiId={apiId} fullScreen={open} />
            </div>
            <Dialog fullScreen open={open} onClose={toggleOpen}>
                <div square className={classes.popupHeader}>
                    <IconButton color='inherit' onClick={toggleOpen} aria-label='Close full screen view'>
                        <Icon>close</Icon>
                    </IconButton>
                    <Typography variant='h4'>{selectedDoc.name}</Typography>
                </div>
                <div className={classes.viewWrapper}>
                    <View doc={selectedDoc} apiId={apiId} fullScreen={open} />
                </div>
            </Dialog>
        </>
    );
}
