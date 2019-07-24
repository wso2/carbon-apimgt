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

import React, { useState } from 'react';
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
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the Certificates.
 */
function Certificates(props) {
    const { classes } = props;
    const [certificate, setCertificate] = useState({ name: '', content: '' });
    const [certificateList, setCertificateList] = useState([
        {
            alias: 'Active',
            endpoint: 'http://localhost/service',
            cn: 'CN=localhost, OU=wso2, O=wso2, L=colombo, ST=western, C=lk',
            color: '#0fc520',
        },
        {
            alias: 'About to expire (1 week)',
            endpoint: 'http://localhost/service',
            cn: 'CN=localhost, OU=wso2, O=wso2, L=colombo, ST=western, C=lk',
            color: '#dca80a',
        },
        {
            alias: 'Expired',
            endpoint: 'http://localhost/service',
            cn: 'CN=localhost, OU=wso2, O=wso2, L=colombo, ST=western, C=lk',
            color: '#c50f0f',
        },
    ]);
    const [alias, setAlias] = useState('');
    const [endpoint, setEndpoint] = useState('');
    const [uploadCertificateOpen, setUploadCertificateOpen] = useState(false);

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
        // TODO: Call backend api and upload certificate.
        // TODO: Based on the response, display message.
        certificateList.push({
            alias,
            endpoint,
            cn: 'CN=localhost, OU=wso2, O=wso2, L=colombo, ST=western, C=lk',
            color: '#dca80a',
        });
        setCertificateList(certificateList);
        closeCertificateUpload();
    };

    /**
     * Delete endpoint certificate represented by the alias.
     *
     * @param {string} certificateAlias The alias of the certificate that is needed to be deleted.
     * */
    const deleteCertificate = (certificateAlias) => {
        setCertificateList(() => {
            return certificateList.filter((cert) => {
                return cert.alias !== certificateAlias;
            });
        });
    };

    /**
     * Handled the file upload action of the dropzone.
     *
     * @param {array} file The accepted file list by the dropzone.
     * */
    const onDrop = (file) => {
        const certificateFile = file[0];
        let encodedContent = '';
        if (certificateFile) {
            const reader = new FileReader();
            reader.onload = (e) => {
                encodedContent = btoa(e.target.result);
                setCertificate({ name: certificateFile.name, content: encodedContent });
            };
            reader.readAsBinaryString(certificateFile);
        }
    };

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
                                        <Icon style={{ color: cert.color }}>
                                            security
                                        </Icon>
                                    </ListItemAvatar>
                                    <ListItemText primary={cert.alias + ' | ' + cert.endpoint} secondary={cert.cn} />
                                    <ListItemSecondaryAction>
                                        <IconButton onClick={() => deleteCertificate(cert.alias)}>
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
            <Dialog open={uploadCertificateOpen}>
                <DialogTitle>
                    <Typography>
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
                                id='certificateEndpoint'
                                label={<FormattedMessage
                                    id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.endpoint'
                                    defaultMessage='Endpoint'
                                />}
                                value={alias}
                                placeholder='Endpoint'
                                onChange={event => setAlias(event.target.value)}
                                margin='normal'
                                fullWidth
                            />
                            <TextField
                                id='certificateAlias'
                                label={<FormattedMessage
                                    id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.alias'
                                    defaultMessage='Alias'
                                />}
                                value={endpoint}
                                placeholder='My Alias'
                                onChange={event => setEndpoint(event.target.value)}
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
                                        <Icon style={{ fontSize: 56 }}>
                                            cloud_upload
                                        </Icon> :
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
                    <Button onClick={closeCertificateUpload} color='primary'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.cancel.button'
                            defaultMessage='Close'
                        />
                    </Button>
                    <Button onClick={saveCertificate} color='primary' autoFocus>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.Certificates.config.save.button'
                            defaultMessage='Save'
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

};
export default injectIntl(withStyles(styles)(Certificates));
