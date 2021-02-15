/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useState } from 'react';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    Icon,
    IconButton,
    List,
    ListItem,
    ListItemAvatar,
    ListItemSecondaryAction,
    ListItemText,
    Typography,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import UploadCertificate from 'AppComponents/Apis/Details/Endpoints/GeneralConfiguration/UploadCertificate';
import API from '../../../../../data/api';

const useStyles = makeStyles((theme) => ({
    fileinput: {
        display: 'none',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        '& span.material-icons': {
            color: theme.palette.primary.main,
        },
    },
    uploadedFile: {
        fontSize: 11,
    },
    certificatesHeader: {
        fontWeight: 600,
        marginTop: theme.spacing(1),
    },
    addCertificateBtn: {
        borderColor: '#c4c4c4',
        borderRadius: '8px',
        borderStyle: 'dashed',
        borderWidth: 'thin',
    },
    certificateList: {
        maxHeight: '250px',
        overflow: 'auto',
    },
    certDetailsHeader: {
        fontWeight: '600',
    },
    uploadCertDialogHeader: {
        fontWeight: '600',
    },
    alertWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    warningIcon: {
        marginRight: 13,
        color: theme.custom.warningColor,
        '& .material-icons': {
            fontSize: 30,
        },
    },
    deleteIcon: {
        color: theme.palette.error.dark,
        cursor: 'pointer',
    },
    deleteIconDisable: {
        color: theme.palette.disabled,
    },
}));
/**
 * TODO: Generalize this component to work in Configuration page , upload mutual SSL certificates action
 * in source/src/app/components/Apis/Details/Configuration/components/APISecurity/components/TransportLevel.jsx ~tmkb
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the Certificates.
 */
function Certificates(props) {
    const {
        certificates, uploadCertificate, deleteCertificate, isMutualSSLEnabled, apiId, endpoints, aliasList,
    } = props;
    const [certificateList, setCertificateList] = useState([]);
    const [openCertificateDetails, setOpenCertificateDetails] = useState({ open: false, anchor: null, details: {} });
    const [certificateToDelete, setCertificateToDelete] = useState({ open: false, alias: '' });
    const [isDeleting, setDeleting] = useState(false);
    const [uploadCertificateOpen, setUploadCertificateOpen] = useState(false);
    const classes = useStyles();
    const [apiFromContext] = useAPI();

    /**
     * Show the selected certificate details in a popover.
     *
     * @param {any} event The button click event.
     * @param {string} certAlias  The alias of the certificate which information is required.
     * */
    const showCertificateDetails = (event, certAlias) => {
        if (!isMutualSSLEnabled) {
            API.getCertificateStatus(certAlias)
                .then((response) => {
                    setOpenCertificateDetails({
                        details: response.body,
                        open: true,
                        alias: certAlias,
                        anchor: event.currentTarget,
                    });
                })
                .catch((err) => {
                    console.error(err);
                });
        } else {
            API.getClientCertificateStatus(certAlias, apiId)
                .then((response) => {
                    setOpenCertificateDetails({
                        details: response.body,
                        open: true,
                        alias: certAlias,
                        anchor: event.currentTarget,
                    });
                })
                .catch((error) => {
                    console.error(error);
                });
        }
    };

    /**
     * Delete certificate represented by the alias.
     *
     * @param {string} certificateAlias The alias of the certificate that is needed to be deleted.
     * */
    const deleteCertificateByAlias = (certificateAlias) => {
        setDeleting(true);
        deleteCertificate(certificateAlias)
            .then(() => setCertificateToDelete({ open: false, alias: '' }))
            .finally(() => setDeleting(false));
    };

    useEffect(() => {
        setCertificateList(certificates);
    }, [certificates]);

    return (
        <Grid container direction='column'>
            {/* TODO: Add list of existing certificates */}
            <Grid>
                <Typography className={classes.certificatesHeader}>
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificates'
                        defaultMessage='Certificates'
                    />
                </Typography>
            </Grid>
            <Grid item>
                <List>
                    <ListItem
                        button
                        disabled={(isRestricted(['apim:api_create'], apiFromContext))}
                        className={classes.addCertificateBtn}
                        onClick={() => setUploadCertificateOpen(true)}
                    >
                        <ListItemAvatar>
                            <IconButton>
                                <Icon>add</Icon>
                            </IconButton>
                        </ListItemAvatar>
                        <ListItemText primary='Add Certificate' />
                    </ListItem>
                </List>
                <List className={classes.certificateList}>
                    {certificateList.length > 0 ? (
                        certificateList.map((cert) => {
                            return (
                                <ListItem>
                                    <ListItemAvatar>
                                        <Icon>lock</Icon>
                                    </ListItemAvatar>
                                    {isMutualSSLEnabled
                                        ? (<ListItemText primary={cert.alias} secondary={cert.tier} />)
                                        : <ListItemText primary={cert.alias} secondary={cert.endpoint} />}

                                    <ListItemSecondaryAction>
                                        <IconButton
                                            edge='end'
                                            onClick={(event) => showCertificateDetails(event, cert.alias)}
                                        >
                                            <Icon>info</Icon>
                                        </IconButton>
                                        <IconButton
                                            disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                            onClick={() => setCertificateToDelete({ open: true, alias: cert.alias })}
                                        >
                                            <Icon className={isRestricted(['apim:api_create'], apiFromContext)
                                                ? classes.deleteIconDisable : classes.deleteIcon}
                                            >
                                                {' '}
                                                delete
                                            </Icon>
                                        </IconButton>
                                    </ListItemSecondaryAction>
                                </ListItem>
                            );
                        })
                    ) : (
                        <ListItem>
                            <ListItemAvatar>
                                <Icon color='primary'>info</Icon>
                            </ListItemAvatar>
                            <ListItemText>You do not have any certificates uploaded</ListItemText>
                        </ListItem>
                    )}
                </List>
            </Grid>
            <Dialog open={certificateToDelete.open}>
                <DialogTitle>
                    <Typography className={classes.uploadCertDialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.deleteCertificate'
                            defaultMessage='Delete Certificate'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent className={classes.alertWrapper}>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.confirm.certificate.delete'
                            defaultMessage='Do you want to delete '
                        />
                        {' '}
                        { certificateToDelete.alias + '?'}
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => deleteCertificateByAlias(certificateToDelete.alias)}
                        variant='contained'
                        color='primary'
                        disabled={isDeleting}
                        autoFocus
                    >
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.delete.ok.button'
                            defaultMessage='OK'
                        />
                        {isDeleting && <CircularProgress size={24} />}

                    </Button>
                    <Button onClick={() => setCertificateToDelete({ open: false, alias: '' })}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.delete.cancel.button'
                            defaultMessage='Cancel'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog open={openCertificateDetails.open}>
                <DialogTitle>
                    <Typography className={classes.certDetailsHeader}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificate.details.of'
                            defaultMessage='Details of'
                        />
                        {' ' + openCertificateDetails.alias}
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.status'
                            defaultMessage='Status'
                        />
                        {' : ' + openCertificateDetails.details.status}
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.subject'
                            defaultMessage='Subject'
                        />
                        {' : ' + openCertificateDetails.details.subject}
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => setOpenCertificateDetails({ open: false, anchor: null, details: {} })}
                        color='primary'
                    >
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.Certificates.details.close.button'
                            defaultMessage='Close'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
            <UploadCertificate
                endpoints={endpoints}
                certificates={certificates}
                uploadCertificate={uploadCertificate}
                isMutualSSLEnabled={isMutualSSLEnabled}
                setUploadCertificateOpen={setUploadCertificateOpen}
                uploadCertificateOpen={uploadCertificateOpen}
                aliasList={aliasList}
            />
        </Grid>
    );
}

Certificates.defaultProps = {
    isMutualSSLEnabled: false,
    apiId: '',
};

Certificates.propTypes = {
    classes: PropTypes.shape({
        fileinput: PropTypes.shape({}),
        button: PropTypes.shape({}),
    }).isRequired,
    certificates: PropTypes.shape({}).isRequired,
    uploadCertificate: PropTypes.func.isRequired,
    deleteCertificate: PropTypes.func.isRequired,
    apiId: PropTypes.string,
    isMutualSSLEnabled: PropTypes.bool,
    endpoints: PropTypes.shape([]).isRequired,
    aliasList: PropTypes.shape([]).isRequired,
};
export default injectIntl((Certificates));
