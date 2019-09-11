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
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import Dropzone from 'react-dropzone';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api.js';

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
const styles = theme => ({
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        padding: `${theme.spacing.unit * 2}px 2px`,
    },
    dropzone: {
        border: '1px dashed ' + theme.palette.primary.main,
        borderRadius: '5px',
        cursor: 'pointer',
        height: 'calc(100vh - 50em)',
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '80%',
        margin: '10px 0',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        '& span': {
            fontSize: 16,
            color: theme.palette.primary.main,
        },
    },
});
/**
* Download the mediation policy related file
* @param {any} response Response of download file
*/
const downloadFile = (response) => {
    let fileName = '';
    const contentDisposition = response.headers['content-disposition'];

    if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
        const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = fileNameReg.exec(contentDisposition);
        if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
    }
    const contentType = response.headers['content-type'];
    const blob = new Blob([response.data], {
        type: contentType,
    });
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
        window.navigator.msSaveBlob(blob, fileName);
    } else {
        const URL = window.URL || window.webkitURL;
        const downloadUrl = URL.createObjectURL(blob);

        if (fileName) {
            const aTag = document.createElement('a');
            if (typeof aTag.download === 'undefined') {
                window.location = downloadUrl;
            } else {
                aTag.href = downloadUrl;
                aTag.download = fileName;
                document.body.appendChild(aTag);
                aTag.click();
            }
        } else {
            window.location = downloadUrl;
        }

        setTimeout(() => {
            URL.revokeObjectURL(downloadUrl);
        }, 100);
    }
};

/**
 * The component to manage IN mediation policies.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function EditFaultMediationPolicy(props) {
    const {
        classes, api, updateMediationPolicy,
    } = props;
    const [globalFaultMediationPolicies, setGlobalFaultMediationPolicies] = useState([]);
    // user uploaded api specific mediation policies
    const [faultSeqCustom, setFaultSeqCustom] = useState([]);
    const { id } = api;
    const type = 'FAULT';
    const selectedPolicy = api.mediationPolicies.filter(seq => seq.type === type)[0];
    const [selectedPolicyFile, setSelectedPolicyFile] = useState({
        id: selectedPolicy !== (null || undefined) ? selectedPolicy.id : '',
        name: selectedPolicy !== (null || undefined) ? selectedPolicy.name : '',
        type: selectedPolicy !== (null || undefined) ? selectedPolicy.type : '',
        content: {},
    });
    const [fileToUpload, setFileToUpload] = useState('');
    useEffect(() => {
        if (globalFaultMediationPolicies.length <= 0) {
            API.getGlobalMediationPolicies()
                .then((response) => {
                    setGlobalFaultMediationPolicies([...response.obj.list.filter(seq => seq.type === type)]);
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                        Alert.error(<FormattedMessage
                            id='Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.global.error'
                            defaultMessage='Error retrieving Global mediation policies'
                        />);
                    }
                });
        }
        API.getMediationPolicies(id)
            .then((response) => {
                setFaultSeqCustom([...response.obj.list.filter(seq => seq.type === type)]);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.custom.error'
                        defaultMessage='Error retrieving mediation policies'
                    />);
                }
            });
    }, [selectedPolicyFile]);
    const saveMediationPolicy = (newPolicy) => {
        const promisedApi = API.addMediationPolicy(newPolicy, api.id, type);
        promisedApi.then(() => {
            Alert.info(FormattedMessage({
                id: 'Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.success',
                defaultMessage: 'Mediation policy added successfully',
            }));
        }).catch((errorResponse) => {
            setFileToUpload('');
            console.log(errorResponse);
            Alert.error(JSON.stringify(errorResponse));
        });
    };
    /**
     * Handled the file upload action of the dropzone.
     * @param {file} policy The accepted file list by the dropzone.
     * */
    const onDrop = (policy) => {
        const policyFile = policy[0];
        if (policyFile) {
            setFileToUpload(policyFile.name);
            saveMediationPolicy(policyFile);
            setSelectedPolicyFile({ name: policyFile.name, content: policyFile });
        }
    };

    /**
    * Handles the mediatio n policy select event.
    * @param {any} event The event pass to the layout
    */
    function handleChange(event) {
        const policy = event.target;
        if (policy.name !== 'none') {
            setSelectedPolicyFile({
                id: policy.name, name: policy.value, type, content: '',
            });
            updateMediationPolicy({
                id: policy.name, name: policy.value, type, content: '',
            });
        } else {
            setSelectedPolicyFile({
                id: 'none', name: policy.value, type, content: '',
            });
            updateMediationPolicy({
                id: 'none', name: policy.value, type, content: '',
            });
        }
    }
    /**
    * Handles the Global mediation policy download.
    */
    function downloadGlobalMediationPolicyContent() {
        const promisedGetContent = API.getGlobalMediationPolicyContent(selectedPolicyFile.id, api.id);
        promisedGetContent
            .then((done) => {
                downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.download.error'
                        defaultMessage='Error downloading the file'
                    />);
                }
            });
    }

    /**
    * Handles the custom mediation policy download.
    */
    function downloadCustomMediationPolicyContent() {
        const promisedGetContent = API.getMediationPolicyContent(selectedPolicyFile.id, api.id);
        promisedGetContent
            .then((done) => {
                downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.download.error'
                        defaultMessage='Error downloading the file'
                    />);
                }
            });
    }
    const handleDownload = () => {
        const isGlobalMediationPolicy =
        globalFaultMediationPolicies.filter(policy => policy.id === selectedPolicyFile.id).length > 0;
        if (isGlobalMediationPolicy) {
            downloadGlobalMediationPolicyContent();
        } else {
            downloadCustomMediationPolicyContent();
        }
    };
    return (
        <FormControl className={classes.formControl}>
            <div className={classes.titleWrapper}>
                <Dropzone
                    multiple={false}
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
                            {fileToUpload === '' ? (
                                <div className={classes.dropZoneWrapper}>
                                    <Icon className={classes.dropIcon}>cloud_upload</Icon>
                                    <Typography>
                                        <FormattedMessage
                                            id={'Apis.Details.MediationPolicies.Edit.EditFaultMediationPolicy.'
                                            + 'click.or.drop.to.upload.file'}
                                            defaultMessage='Click or drag the mediation file to upload.'
                                        />
                                    </Typography>
                                </div>
                            ) : (
                                <div className={classes.uploadedFile}>
                                    <Icon style={{ fontSize: 56 }}>insert_drive_file</Icon>
                                    {fileToUpload}
                                </div>
                            )}
                        </div>
                    )}
                </Dropzone>
                <RadioGroup
                    aria-label='faultflow'
                    name='faultflow'
                    className={classes.radioGroup}
                    value={selectedPolicyFile.name}
                    onChange={handleChange}
                >
                    <FormLabel component='customPolicies'>
                        <FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.custom.fault.policies'
                            defaultMessage='Custom Fault Mediation Policies'
                        />
                    </FormLabel>
                    {faultSeqCustom.map(seq => (
                        <FormControlLabel
                            name={seq.id}
                            type={seq.type}
                            control={<Radio />}
                            label={<FormattedMessage
                                id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.custom.fault.policy'
                                defaultMessage={seq.name}
                            />}
                            value={seq.name}
                            checked={selectedPolicyFile.name === seq.name}
                        />
                    ))}
                    <FormControlLabel
                        name='none'
                        type={type}
                        control={<Radio />}
                        label={<FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditFaultMediationPolicies.none'
                            defaultMessage='No Mediation'
                        />}
                        value='none'
                    />
                    <FormLabel component='globalPolicies'>
                        <FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.global.fault.policies'
                            defaultMessage='Global FAULT Mediation Policies'
                        />
                    </FormLabel>
                    {globalFaultMediationPolicies.map(seq => (
                        <FormControlLabel
                            name={seq.id}
                            type={seq.type}
                            control={<Radio />}
                            label={<FormattedMessage
                                id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.global.fault.policy'
                                defaultMessage={seq.name}
                            />}
                            value={seq.name}
                            checked={selectedPolicyFile.name === seq.name}
                        />
                    ))}
                </RadioGroup>
                {/* </div> */}
                <Button onClick={handleDownload}>
                    <Icon>arrow_downward</Icon>
                    <FormattedMessage
                        id='Apis.Details.Edit.MediationPolicies.EditFaultMediationPolicies.download'
                        defaultMessage='Download'
                    />
                </Button>
            </div>
        </FormControl>
    );
}
EditFaultMediationPolicy.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    engagedPolicyFile: PropTypes.shape({}).isRequired,
    type: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
};

export default withStyles(styles)(EditFaultMediationPolicy);
