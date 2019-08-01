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
    TextField,
    Typography,
    withStyles,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import Dropzone from 'react-dropzone';
import API from '../../../../../data/api';

const styles = theme => ({
    fileinput: {
        display: 'none',
    },
    dropzone: {
        border: '1px dashed ' + theme.palette.primary.main,
        borderRadius: '5px',
        cursor: 'pointer',
        height: 75,
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
        margin: '10px 0',
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
    },
    addCertificateBtn: {
        borderColor: '#c4c4c4',
        borderRadius: '8px',
        borderStyle: 'dashed',
        borderWidth: 'thin',
    },
    certificateList: {
        maxHeight: '250px',
        overflow: 'scroll',
    },
    certDetailsHeader: {
        fontWeight: '600',
    },
    uploadCertDialogHeader: {
        fontWeight: '600',
    },
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the Certificates.
 */
function Certificates(props) {
    const {
        classes,
        certificates,
        uploadCertificate,
        deleteCertificate,
    } = props;
    const [certificate, setCertificate] = useState({ name: '', content: {} });
    const [certificateList, setCertificateList] = useState([]);
    const [alias, setAlias] = useState('');
    const [endpoint, setEndpoint] = useState('');
    const [uploadCertificateOpen, setUploadCertificateOpen] = useState(false);
    const [openCertificateDetails, setOpenCertificateDetails] = useState({ open: false, anchor: null, details: {} });
    const [certificateToDelete, setCertificateToDelete] = useState({ open: false, alias: '' });

    const closeCertificateUpload = () => {
        setUploadCertificateOpen(false);
        setCertificate({ name: '', content: '' });
        setAlias('');
        setEndpoint('');
    };

    /**
     * Method to upload the certificate content by calling the rest api.
     * */
    const saveCertificate = () => {
        uploadCertificate(certificate.content, endpoint, alias);
        closeCertificateUpload();
    };

    /**
     * Show the selected certificate details in a popover.
     *
     * @param {any} event The button click event.
     * @param {string} certAlias  The alias of the certificate which information is required.
     * */
    const showCertificateDetails = (event, certAlias) => {
        API.getCertificateStatus(certAlias).then((response) => {
            setOpenCertificateDetails({
                details: response.body,
                open: true,
                alias: certAlias,
                anchor: event.currentTarget,
            });
        }).catch((err) => {
            console.error(err);
        });
    };

    /**
     * Delete endpoint certificate represented by the alias.
     *
     * @param {string} certificateAlias The alias of the certificate that is needed to be deleted.
     * */
    const deleteEndpointCertificate = (certificateAlias) => {
        deleteCertificate(certificateAlias);
        setCertificateToDelete({ open: false, alias: '' });
    };

    /**
     * Handled the file upload action of the dropzone.
     *
     * @param {array} file The accepted file list by the dropzone.
     * */
    const onDrop = (file) => {
        const certificateFile = file[0];
        if (certificateFile) {
            setCertificate({ name: certificateFile.name, content: certificateFile });
        }
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
                        id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.certificates'
                        defaultMessage='Certificates'
                    />
                </Typography>
            </Grid>
            <Grid item>
                <List className={classes.certificateList}>
                    {(certificateList.length > 0 ? (
                        certificateList.map((cert) => {
                            return (
                                <ListItem>
                                    <ListItemAvatar>
                                        <Icon>
                                            lock
                                        </Icon>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={cert.alias}
                                        secondary={cert.endpoint}
                                    />
                                    <ListItemSecondaryAction>
                                        <IconButton
                                            onClick={event => showCertificateDetails(event, cert.alias)}
                                        >
                                            <Icon>
                                                info
                                            </Icon>
                                        </IconButton>
                                        <IconButton
                                            onClick={() => setCertificateToDelete({ open: true, alias: cert.alias })}
                                            color='secondary'
                                        >
                                            <Icon>
                                                delete
                                            </Icon>
                                        </IconButton>
                                    </ListItemSecondaryAction>
                                </ListItem>
                            );
                        }))
                        : (
                            <ListItem>
                                <ListItemAvatar>
                                    <Icon color='primary'>
                                        info
                                    </Icon>
                                </ListItemAvatar>
                                <ListItemText>
                                    You do not have any certificates uploaded
                                </ListItemText>
                            </ListItem>
                        ))}
                </List>
                <List>
                    <ListItem
                        button
                        className={classes.addCertificateBtn}
                        onClick={() => setUploadCertificateOpen(true)}
                    >
                        <ListItemAvatar>
                            <IconButton>
                                <Icon>
                                    add
                                </Icon>
                            </IconButton>
                        </ListItemAvatar>
                        <ListItemText primary='Add Certificate' />
                    </ListItem>
                </List>
            </Grid>
            <Dialog open={certificateToDelete.open}>
                <DialogTitle>
                    <Icon style={{ color: '#dd1c30' }}>
                        warning
                    </Icon>
                    {'Delete Certificate'}
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.confirm.certificate.delete'
                            defaultMessage='Do you want to delete the Certificate'
                        /> { ' ' + certificateToDelete.alias + '?'}
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.delete.cannot.undone'
                            defaultMessage=' This cannot be undone.'
                        />
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => deleteEndpointCertificate(certificateToDelete.alias)}
                        color='primary'
                        autoFocus
                    >
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.delete.ok.button'
                            defaultMessage='OK'
                        />
                    </Button>
                    <Button
                        onClick={() => setCertificateToDelete({ open: false, alias: '' })}
                        color='secondary'
                    >
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.delete.cancel.button'
                            defaultMessage='Cancel'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog open={openCertificateDetails.open}>
                <DialogTitle>
                    <Typography className={classes.certDetailsHeader}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.certificate.details.of'
                            defaultMessage='Details of'
                        />
                        {' ' + openCertificateDetails.alias}
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.status'
                            defaultMessage='Status'
                        />
                        {' : ' + openCertificateDetails.details.status}
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.subject'
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
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.details.close.button'
                            defaultMessage='Close'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog open={uploadCertificateOpen}>
                <DialogTitle>
                    <Typography className={classes.uploadCertDialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.uploadCertificate'
                            defaultMessage='Upload Certificate'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Grid>
                        <div>
                            <TextField
                                required
                                id='certificateEndpoint'
                                label={<FormattedMessage
                                    id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.endpoint'
                                    defaultMessage='Endpoint'
                                />}
                                value={endpoint}
                                placeholder='Endpoint'
                                onChange={event => setEndpoint(event.target.value)}
                                margin='normal'
                                fullWidth
                            />
                            <TextField
                                required
                                id='certificateAlias'
                                label={<FormattedMessage
                                    id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.alias'
                                    defaultMessage='Alias'
                                />}
                                value={alias}
                                placeholder='My Alias'
                                onChange={event => setAlias(event.target.value)}
                                margin='normal'
                                fullWidth
                            />
                            <Dropzone
                                multiple={false}
                                accept={
                                    'application/pkcs8,' +
                                    'application/pkcs10, application/pkix-crl,' +
                                    'application/pkcs7-mime,' +
                                    'application/x-x509-ca-cert,' +
                                    'application/x-x509-user-cert,' +
                                    'application/x-pkcs7-crl,' +
                                    'application/x-pkcs12,' +
                                    'application/x-pkcs7-certificates,' +
                                    'application/x-pkcs7-certreqresp,' +
                                    '.p8, .p10, .csr, .cer, .crl, .p7c, .crt, .der, .p12, .pfx, .p7b, .spc, .p7r'
                                }
                                className={classes.dropzone}
                                activeClassName={classes.acceptDrop}
                                rejectClassName={classes.rejectDrop}
                                onDrop={(dropFile) => {
                                    onDrop(dropFile);
                                }}
                            >
                                <div className={classes.dropZoneWrapper}>
                                    {certificate.name === '' ?
                                        <div>
                                            <Icon style={{ fontSize: 56 }}>
                                                cloud_upload
                                            </Icon>
                                            <Typography>
                                                <FormattedMessage
                                                    id={'Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.'
                                                    + 'click.or.drop.to.upload.file'}
                                                    defaultMessage='Click or drag the certificate file to upload.'
                                                />
                                            </Typography>
                                        </div> :
                                        <div className={classes.uploadedFile}>
                                            <Icon style={{ fontSize: 56 }}>
                                                insert_drive_file
                                            </Icon>
                                            {certificate.name}
                                        </div>
                                    }
                                </div>
                            </Dropzone>
                        </div>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={saveCertificate}
                        color='primary'
                        autoFocus
                        disabled={alias === '' || endpoint === '' || certificate.name === ''}
                    >
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.config.save.button'
                            defaultMessage='Save'
                        />
                    </Button>
                    <Button onClick={closeCertificateUpload} color='secondary'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.cancel.button'
                            defaultMessage='Close'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </Grid>
    );
}

Certificates.propTypes = {
    classes: PropTypes.shape({
        fileinput: PropTypes.shape({}),
        button: PropTypes.shape({}),
    }).isRequired,
    certificates: PropTypes.shape({}).isRequired,
    uploadCertificate: PropTypes.func.isRequired,
    deleteCertificate: PropTypes.func.isRequired,

};
export default injectIntl(withStyles(styles)(Certificates));
