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
import IconButton from '@material-ui/core/IconButton';
import Dropzone from 'react-dropzone';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import Download from 'AppComponents/Shared/Download.js';
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
 * The component to manage IN mediation policies.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function EditInMediationPolicy(props) {
    const {
        classes, api, updateMediationPolicy,
    } = props;
    const [globalInMediationPolicies, setGlobalInMediationPolicies] = useState([]);
    // user uploaded api specific mediation policies
    const [inSeqCustom, setInSeqCustom] = useState([]);
    const { id } = api;
    const type = 'IN';
    const NONE = 'none';
    const selectedPolicy = api.mediationPolicies.filter(seq => seq.type === type)[0];
    const [selectedPolicyFile, setSelectedPolicyFile] = useState({
        id: selectedPolicy ? selectedPolicy.id : '',
        name: selectedPolicy ? selectedPolicy.name : '',
        type: selectedPolicy ? selectedPolicy.type : '',
        content: {},
    });
    useEffect(() => {
        if (globalInMediationPolicies.length <= 0) {
            API.getGlobalMediationPolicies()
                .then((response) => {
                    setGlobalInMediationPolicies([...response.obj.list.filter(seq => seq.type === type)]);
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                        Alert.error(<FormattedMessage
                            id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.global.error'
                            defaultMessage='Error retrieving Global mediation policies'
                        />);
                    }
                });
        }

        API.getMediationPolicies(id)
            .then((response) => {
                setInSeqCustom([...response.obj.list.filter(seq => seq.type === type)]);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.custom.error'
                        defaultMessage='Error retrieving mediation policies'
                    />);
                }
            });
    }, [selectedPolicyFile]);
    const saveMediationPolicy = (newPolicy) => {
        const promisedApi = API.addMediationPolicy(newPolicy, api.id, type);
        promisedApi.then(() => {
            Alert.info(FormattedMessage({
                id: 'Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.success',
                defaultMessage: 'Mediation policy added successfully',
            }));
        }).catch((errorResponse) => {
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
            saveMediationPolicy(policyFile);
            setSelectedPolicyFile({ name: policyFile.name, content: policyFile });
        }
    };
    /**
    * Handles the mediation policy select event.
    * @param {any} event The event pass to the layout
    */
    function handleChange(event) {
        const policy = event.target;
        if (policy.name !== NONE) {
            setSelectedPolicyFile({
                id: policy.name, name: policy.value, type, content: '',
            });
            updateMediationPolicy({
                id: policy.name, name: policy.value, type, content: '',
            });
        } else {
            setSelectedPolicyFile({
                id: NONE, name: policy.value, type, content: '',
            });
            updateMediationPolicy({
                id: NONE, name: policy.value, type, content: '',
            });
        }
    }
    /**
    * Handles the Global mediation policy download.
    * @param {any} policyToDownload policy file id that is to be downloaded.
    */
    function downloadGlobalMediationPolicyContent(policyToDownload) {
        const promisedGetContent = API.getGlobalMediationPolicyContent(policyToDownload, api.id);
        promisedGetContent
            .then((done) => {
                Download.downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.download.error'
                        defaultMessage='Error downloading the file'
                    />);
                }
            });
    }

    /**
    * Handles the custom mediation policy download.
    * @param {any} policyToDownload policy file id that is to be downloaded.
    */
    function downloadCustomMediationPolicyContent(policyToDownload) {
        const promisedGetContent = API.getMediationPolicyContent(policyToDownload, api.id);
        promisedGetContent
            .then((done) => {
                Download.downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.download.error'
                        defaultMessage='Error downloading the file'
                    />);
                }
            });
    }
    /**
    * Handles the custom mediation policy delete.
    * @param {any} policyToDelete policy file id that is to be deleted.
    */
    function deleteCustomMediationPolicy(policyToDelete) {
        const promisedGetContent = API.deleteMediationPolicy(policyToDelete, api.id);
        promisedGetContent
            .then(() => {
                setInSeqCustom(inSeqCustom.filter(seq => seq.id !== policyToDelete));
                Alert.info(<FormattedMessage
                    id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.delete.success'
                    defaultMessage='Mediation policy deleted successfully.'
                />);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.delete.error'
                        defaultMessage='Error deleting the file'
                    />);
                }
            });
    }
    const handleDownload = (policyToDownload) => {
        const isGlobalMediationPolicy = globalInMediationPolicies.filter(policy => policy.id === policyToDownload)
            .length > 0;
        if (isGlobalMediationPolicy) {
            downloadGlobalMediationPolicyContent(policyToDownload);
        } else {
            downloadCustomMediationPolicyContent(policyToDownload);
        }
    };
    const handleDelete = (policyToDelete) => {
        const isGlobalMediationPolicy = globalInMediationPolicies.filter(policy => policy.id === policyToDelete)
            .length > 0;
        if (isGlobalMediationPolicy) {
            Alert.error(<FormattedMessage
                id='Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.global.delete'
                defaultMessage='Cannot delete Global mediation policies.'
            />);
        } else {
            deleteCustomMediationPolicy(policyToDelete);
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
                            <div className={classes.dropZoneWrapper}>
                                <Icon className={classes.dropIcon}>cloud_upload</Icon>
                                <Typography>
                                    <FormattedMessage
                                        id={'Apis.Details.MediationPolicies.Edit.EditInMediationPolicy.'
                                            + 'click.or.drop.to.upload.file'}
                                        defaultMessage='Click or drag the mediation file to upload.'
                                    />
                                </Typography>
                            </div>
                        </div>
                    )}
                </Dropzone>
                <RadioGroup
                    aria-label='inflow'
                    name='inflow'
                    className={classes.radioGroup}
                    value={selectedPolicyFile.name}
                    onChange={handleChange}
                >
                    <FormLabel component='customPolicies'>
                        <FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.custom.in.policies'
                            defaultMessage='Custom IN Mediation Policies'
                        />
                    </FormLabel>
                    {inSeqCustom.map(seq => (
                        <div>
                            <IconButton onClick={() => handleDelete(seq.id)}>
                                <Icon>delete</Icon>
                            </IconButton>
                            <Button onClick={() => handleDownload(seq.id)}>
                                <Icon>arrow_downward</Icon>
                            </Button>
                            <FormControlLabel
                                name={seq.id}
                                type={seq.type}
                                control={<Radio />}
                                label={<FormattedMessage
                                    id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.custom.in.policy'
                                    defaultMessage={seq.name}
                                />}
                                value={seq.name}
                                checked={selectedPolicyFile.name === seq.name}
                            />
                        </div>
                    ))}
                    <FormControlLabel
                        name={NONE}
                        type={type}
                        control={<Radio />}
                        label={<FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.none'
                            defaultMessage='No Mediation'
                        />}
                        value={NONE}
                        checked={selectedPolicyFile.name === NONE || selectedPolicyFile.name === ''}

                    />
                    <FormLabel component='globalPolicies'>
                        <FormattedMessage
                            id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.global.in.policies'
                            defaultMessage='Global IN Mediation Policies'
                        />
                    </FormLabel>
                    {globalInMediationPolicies.map(seq => (
                        <div>
                            <Button onClick={() => handleDownload(seq.id)}>
                                <Icon>arrow_downward</Icon>
                            </Button>
                            <FormControlLabel
                                name={seq.id}
                                type={seq.type}
                                control={<Radio />}
                                label={<FormattedMessage
                                    id='Apis.Details.Edit.MediationPolicies.EditInMediationPolicies.global.in.policy'
                                    defaultMessage={seq.name}
                                />}
                                value={seq.name}
                                checked={selectedPolicyFile.name === seq.name}
                            />
                        </div>
                    ))}
                </RadioGroup>
            </div>
        </FormControl>
    );
}
EditInMediationPolicy.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    engagedPolicyFile: PropTypes.shape({}).isRequired,
    type: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
};

export default withStyles(styles)(EditInMediationPolicy);
