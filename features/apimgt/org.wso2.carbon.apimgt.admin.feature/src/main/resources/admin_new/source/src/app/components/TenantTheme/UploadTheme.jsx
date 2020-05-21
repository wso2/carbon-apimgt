/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import {
    List, Button, Box, Typography, Toolbar, Grid, Paper,
} from '@material-ui/core';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Dropzone from 'react-dropzone';
import Icon from '@material-ui/core/Icon';
import InsertDriveFileIcon from '@material-ui/icons/InsertDriveFile';

const dropzoneStyles = {
    border: '1px dashed ',
    borderRadius: '5px',
    cursor: 'pointer',
    height: 75,
    padding: '8px 0px',
    position: 'relative',
    textAlign: 'center',
    width: '95%',
    margin: '30px 25px',
};

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    addEditFormControl: {
        minHeight: theme.spacing(40),
        maxHeight: theme.spacing(40),
        minWidth: theme.spacing(55),
    },
    gridRoot: {
        paddingLeft: 0,
    },
    paper: {
        maxWidth: 936,
        margin: 'auto',
        overflow: 'hidden',
    },
    main: {
        flex: 1,
        padding: theme.spacing(6, 4),
        background: '#eaeff1',
    },
    paperUpload: {
        maxWidth: 936,
        margin: 'auto',
        overflow: 'hidden',
        marginTop: theme.spacing(5),
    },
    uploadButtonGrid: {
        display: 'grid',
        padding: '0px 400px 10px',
    },
}));

/**
 * Render a pop-up dialog to add/edit an Microgateway label
 * @param {JSON} props .
 * @returns {JSX}.
 */
function UploadTheme() {
    const classes = useStyles();
    const [themeFile, setThemeFile] = useState([]);
    const [isFileAccepted, setIsFileAccepted] = useState(false);


    const onDrop = (acceptedFile) => {
        setIsFileAccepted(true);
        setThemeFile(acceptedFile[0]);
    };

    const uploadThemeFile = () => {
        // Upload theme API call
    };

    return (
        <>
            <Toolbar className={classes.root}>
                <Grid container alignItems='center' spacing={1} classes={{ root: classes.gridRoot }}>
                    <Grid item xs>
                        <Typography color='inherit' variant='h5' component='h1'>
                            <FormattedMessage
                                id='TenantTheme.Upload.Theme.page.heading'
                                defaultMessage='Upload Tenant Theme'
                            />
                        </Typography>
                    </Grid>

                    <Grid item>
                        <HelpBase>
                            <List component='nav' aria-label='main mailbox folders'>
                                <ListItem button>
                                    <ListItemIcon>
                                        <DescriptionIcon />
                                    </ListItemIcon>
                                    <Link
                                        target='_blank'
                                        href={Configurations.app.docUrl
        + 'develop/customizations/customizing-the-developer-portal/overriding-developer-portal-theme/#tenant-theming'}
                                    >
                                        <ListItemText primary={(
                                            <FormattedMessage
                                                id='TenantTheme.Upload.Theme.help.link.one'
                                                defaultMessage='Tenant theming'
                                            />
                                        )}
                                        />

                                    </Link>
                                </ListItem>
                            </List>
                        </HelpBase>
                    </Grid>

                </Grid>
            </Toolbar>

            <main className={classes.main}>
                <Paper className={classes.paper}>
                    <InlineMessage type='info' height={100} className={classes.emptyBox}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='TenantTheme.Upload.Theme.info.message'
                                    defaultMessage={'The theme should be a zip file containing CSS'
                                    + 'and images compliant with the API Manager theme format'}
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                </Paper>
                <Paper className={classes.paperUpload}>
                    <Dropzone
                        multiple={false}
                        accept='.zip'
                        className={classes.dropzone}
                        activeClassName={classes.acceptDrop}
                        rejectClassName={classes.rejectDrop}
                        onDrop={(dropFile) => {
                            onDrop(dropFile);
                        }}
                    >
                        {({ getRootProps, getInputProps }) => (
                            <div {...getRootProps({ style: dropzoneStyles })}>
                                <input {...getInputProps()} />
                                <div className={classes.dropZoneWrapper}>
                                    {isFileAccepted ? (
                                        <div className={classes.uploadedFile}>
                                            <InsertDriveFileIcon color='primary' fontSize='large' />
                                            <Box fontSize='h6.fontSize' fontWeight='fontWeightLight'>
                                                <Typography>
                                                    {themeFile && themeFile.name}
                                                </Typography>
                                            </Box>
                                        </div>
                                    ) : (
                                        <>
                                            <Icon className={classes.dropIcon}>cloud_upload</Icon>
                                            <Typography>
                                                <FormattedMessage
                                                    id='upload.theme'
                                                    defaultMessage='Select the theme to upload.'
                                                />
                                            </Typography>
                                        </>
                                    )}
                                </div>
                            </div>
                        )}
                    </Dropzone>
                    <Grid className={classes.uploadButtonGrid}>
                        <Button
                            variant='contained'
                            color='primary'
                            onClick={uploadThemeFile}
                        >
                            <FormattedMessage
                                id='TenantTheme.Upload.Theme.button.upload'
                                defaultMessage='Upload'
                            />
                        </Button>
                    </Grid>
                </Paper>
            </main>
        </>
    );
}

UploadTheme.propTypes = {
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
    pageProps: PropTypes.shape({}).isRequired,
};

export default UploadTheme;
