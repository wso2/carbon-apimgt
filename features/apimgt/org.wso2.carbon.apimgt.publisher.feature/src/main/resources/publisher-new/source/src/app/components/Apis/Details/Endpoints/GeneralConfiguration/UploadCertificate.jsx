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
import PropTypes from 'prop-types';
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    Icon,
    TextField,
    Typography,
} from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import Dropzone from 'react-dropzone';
import CircularProgress from '@material-ui/core/CircularProgress';
import SelectPolicies from '../../../Create/Components/SelectPolicies';

const dropzoneStyles = {
    border: '1px dashed ',
    borderRadius: '5px',
    cursor: 'pointer',
    height: 75,
    padding: '8px 0px',
    position: 'relative',
    textAlign: 'center',
    width: '100%',
    margin: '10px 0',
};

const useStyles = makeStyles(theme => ({
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
        marginTop: 20,
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
}));

/**
 * This component is used to upload the certificates
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the Certificates.
 */
export default function UploadCertificate(props) {
    const {
        uploadCertificate,
        isMutualSSLEnabled,
        uploadCertificateOpen,
        setUploadCertificateOpen,
    } = props;
    const [alias, setAlias] = useState('');
    const [policy, setPolicy] = useState('');
    const [endpoint, setEndpoint] = useState('');
    const [isSaving, setSaving] = useState(false);
    const [certificate, setCertificate] = useState({ name: '', content: {} });
    const classes = useStyles();

    const closeCertificateUpload = () => {
        setUploadCertificateOpen(false);
        setCertificate({ name: '', content: '' });
        setAlias('');
        setEndpoint('');
        setPolicy('');
    };

    /**
     * On change functionality to handle the policy dropdown
     *
     * @param {*} event
     */
    function handleOnChange(event) {
        const { value } = event.target;
        setPolicy(value);
    }

    /**
     * Method to upload the certificate content by calling the rest api.
     * */
    const saveCertificate = () => {
        setSaving(true);
        if (isMutualSSLEnabled) {
            uploadCertificate(certificate.content, policy, alias)
                .then(closeCertificateUpload)
                .finally(() => setSaving(false));
        } else {
            uploadCertificate(certificate.content, endpoint, alias)
                .then(closeCertificateUpload)
                .finally(() => setSaving(false));
        }
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


    return (
        <Dialog open={uploadCertificateOpen}>
            <DialogTitle>
                <Typography className={classes.uploadCertDialogHeader}>
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.uploadCertificate'
                        defaultMessage='Upload Certificate'
                    />
                </Typography>
            </DialogTitle>
            <DialogContent>
                <Grid>
                    <div>
                        {isMutualSSLEnabled ? (
                            <SelectPolicies
                                multiple={false}
                                policies={policy}
                                helperText='Select a throttling policy for the certificate'
                                onChange={handleOnChange}
                                required
                            />
                        ) :
                            <TextField
                                required
                                id='certificateEndpoint'
                                label={
                                    <FormattedMessage
                                        id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.endpoint'
                                        defaultMessage='Endpoint'
                                    />
                                }
                                value={endpoint}
                                placeholder='Endpoint'
                                onChange={event => setEndpoint(event.target.value)}
                                margin='normal'
                                fullWidth
                            />
                        }

                        <TextField
                            required
                            id='certificateAlias'
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.alias'
                                    defaultMessage='Alias'
                                />
                            }
                            value={alias}
                            placeholder='My Alias'
                            onChange={event => setAlias(event.target.value)}
                            margin='normal'
                            variant='outlined'
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
                            {({ getRootProps, getInputProps }) => (
                                <div {...getRootProps({ style: dropzoneStyles })}>
                                    <input {...getInputProps()} />
                                    <div className={classes.dropZoneWrapper}>
                                        {certificate.name === '' ? (
                                            <div>
                                                <Icon style={{ fontSize: 56 }}>cloud_upload</Icon>
                                                <Typography>
                                                    <FormattedMessage
                                                        id={
                                                            'Apis.Details.Endpoints.GeneralConfiguration' +
                                                                '.UploadCertificate.click.or.drop.to.upload.file'
                                                        }
                                                        defaultMessage={
                                                            'Click or drag the certificate ' +
                                                                ' file to upload.'
                                                        }
                                                    />
                                                </Typography>
                                            </div>
                                        ) : (
                                            <div className={classes.uploadedFile}>
                                                <Icon style={{ fontSize: 56 }}>insert_drive_file</Icon>
                                                {certificate.name}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </Dropzone>
                    </div>
                </Grid>
            </DialogContent>
            <DialogActions>
                <Button
                    onClick={saveCertificate}
                    color='primary'
                    autoFocus
                    disabled={
                        alias === '' ||
                            (!isMutualSSLEnabled && endpoint === '') ||
                            certificate.name === '' ||
                            (isMutualSSLEnabled && policy === '') ||
                            isSaving
                    }
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.config.save.button'
                        defaultMessage='Save'
                    />
                    {isSaving && <CircularProgress size={24} />}
                </Button>
                <Button onClick={closeCertificateUpload} color='secondary'>
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
}

UploadCertificate.defaultProps = {
    isMutualSSLEnabled: false,
};

UploadCertificate.propTypes = {
    certificates: PropTypes.shape({}).isRequired,
    uploadCertificate: PropTypes.func.isRequired,
    isMutualSSLEnabled: PropTypes.bool,
    setUploadCertificateOpen: PropTypes.func.isRequired,
    uploadCertificateOpen: PropTypes.bool.isRequired,

};
