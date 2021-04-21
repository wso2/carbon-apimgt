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
import { FormattedMessage, injectIntl } from 'react-intl';
import Dropzone from 'react-dropzone';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';
import InsertDriveFileIcon from '@material-ui/icons/InsertDriveFile';
import API from 'AppData/api';
import Validation from 'AppData/Validation';
import { required } from '@hapi/joi';

const dropzoneStyles = {
    border: '1px dashed #c4c4c4',
    borderRadius: '5px',
    cursor: 'pointer',
    height: 75,
    padding: '8px 0px',
    position: 'relative',
    textAlign: 'center',
    width: '100%',
    margin: '10px 0',
};

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
    uploadCertDialog: {
        maxWidth:"lg",
    },
}));

function UploadCertificate(props) {
const {

    uploadCertificate,
    uploadCertificateOpen,
    setUploadCertificateOpen,
    applicationId,
    nameList,
 } = props;
    const [name, setName] = useState('');
    const [isSaving, setSaving] = useState(false);
    const [certificate, setCertificate] = useState({ name: '', content: {} });
    const [nameValidity, setNameValidity] = useState();
    const classes = useStyles();
    const [isRejected, setIsRejected] = useState(false);

    const closeCertificateUpload = () => {
        setUploadCertificateOpen(false);
        setNameValidity();
        setCertificate({ name: '', content: '' });
        setName('');
    };

        /**
     * Handled the file upload action of the dropzone.
     *
     * @param {array} file The accepted file list by the dropzone.
     * */
    const onDrop = (file) => {
        const certificateFile = file[0];
        const rejectedFiles = ['pem', 'txt', 'jks', 'key', 'ca-bundle'];
        const extension = certificateFile.name.split('.');
        if (rejectedFiles.includes(extension[1])) {
            setIsRejected(true);
        } else {
            setIsRejected(false);
        }
        if (certificateFile) {
            setCertificate({ name: certificateFile.name, content: certificateFile });
        }
    };

    const addCertificate = () => {
       setSaving(true);
        uploadCertificate(certificate, name)
            .then(closeCertificateUpload)
            .finally(() => setSaving(false));
    };

    const handleNameOnBlur = () => {
        const nameValidation = Validation.name.required().validate(name).error;
        if (nameValidation) {
            setNameValidity({ isValid: false, message: nameValidation.details[0].message });
        } else {
            setNameValidity({ isValid: true, message: '' });
        }
    };

    const getHelperText = () => {

        if (nameValidity && !nameValidity.isValid) {
            return (nameValidity.message);
        }
        else if (nameList && nameList.includes(name)) {
            return (
                <FormattedMessage
                    id='Applications.Details.UploadCertificate.name.exist.error'
                    defaultMessage='Name already exists'
                />
            );
       }
        else {
            return (
                <FormattedMessage
                    id='Applications.Details.UploadCertificate.name.default.message'
                    defaultMessage='Name for the Certificate'
                />
            );
        }
    };




    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    return (
        <Dialog open={uploadCertificateOpen} className={classes.uploadCertDialog} fullWidth>
        <DialogTitle>
            <Typography className={classes.uploadCertDialogHeader}>
                <FormattedMessage
                    id='Applications.Details.UploadCertificate.uploadCertificate'
                    defaultMessage='Upload Certificate'
                />
            </Typography>
        </DialogTitle>
        <DialogContent>
            <Grid>
                <div>
                    <TextField
                        required
                        id='certificateName'
                        label={(
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.name'
                                defaultMessage='Name'
                            />
                        )}
                        value={name}
                        placeholder='My Name'
                        onChange={(event) => setName(event.target.value)}
                        onBlur={handleNameOnBlur}
                        margin='normal'
                        variant='outlined'
                        error={
                            (nameValidity && !nameValidity.isValid) || (nameList && nameList.includes(name))
                        }
                        helperText={getHelperText()}
                        fullWidth
                        inputProps={{ maxLength: 45 }}
                    />
                    <Dropzone
                        multiple={false}
                        accept={
                            'application/pkcs8,'
                                + 'application/pkcs10, application/pkix-crl,'
                                + 'application/pkcs7-mime,'
                                + 'application/x-x509-ca-cert,'
                                + 'application/x-x509-user-cert,'
                                + 'application/x-pkcs7-crl,'
                                + 'application/x-pkcs12,'
                                + 'application/x-pkcs7-certificates,'
                                + 'application/x-pkcs7-certreqresp,'
                                + '.p8, .p10, .cer, .cert, .p7c, .crt, .der, .p12, .pfx, .p7b, .spc, .p7r'
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
                                                        'Applcations.Details'
                                                            + '.UploadCertificate.click.or.drop.to.upload.file'
                                                    }
                                                    defaultMessage={
                                                        'Click or drag the certificate'
                                                            + ' file to upload.'
                                                    }
                                                />
                                            </Typography>
                                        </div>
                                    ) : iff(
                                        isRejected,
                                        <div classNames={classes.uploadedFile}>
                                            <InsertDriveFileIcon color='error' fontSize='large' />
                                            <Box fontSize='h6.fontSize' color='error' fontWeight='fontWeightLight'>
                                                <Grid xs={12}>
                                                    {certificate.name}
                                                </Grid>
                                                <Grid xs={12}>
                                                    <Typography variant='caption' color='error'>
                                                        <FormattedMessage
                                                            id={
                                                                'Applications.Detail'
                                                        + '.UploadCertificate.invalid.file'
                                                            }
                                                            defaultMessage='Invalid file type'
                                                        />
                                                    </Typography>
                                                </Grid>
                                            </Box>
                                        </div>,
                                        <div className={classes.uploadedFile}>
                                            <InsertDriveFileIcon color='primary' fontSize='large' />
                                            <Box fontSize='h6.fontSize' fontWeight='fontWeightLight'>
                                                <Typography>
                                                    {certificate.name}
                                                </Typography>
                                            </Box>
                                        </div>,
                                    )}
                                </div>
                            </div>
                        )}
                    </Dropzone>
                </div>
            </Grid>
        </DialogContent>
        <DialogActions>
            <Button onClick={closeCertificateUpload}>
                <FormattedMessage
                    id='Applications.Details.UploadCertificate.cancel.button'
                    defaultMessage='Close'
                />
            </Button>
            <Button
                onClick={addCertificate}
                variant='contained'
                color='primary'
                autoFocus
                disabled={
                    name === '' || (nameValidity && !nameValidity.isValid)
                        || certificate.name === ''
                        || isSaving || (nameList && nameList.includes(name)) || isRejected
                }
            >
                <FormattedMessage
                    id='Applications.Details.UploadCertificate.config.save.button'
                    defaultMessage='Save'
                />
                {isSaving && <CircularProgress size={24} />}
            </Button>
        </DialogActions>
    </Dialog>
    );
}

export default injectIntl((UploadCertificate));