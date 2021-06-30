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

import React, { useState, useEffect, useContext } from 'react';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import Box from '@material-ui/core/Box';
import IconButton from '@material-ui/core/IconButton';
import Dropzone from 'react-dropzone';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Tooltip from '@material-ui/core/Tooltip';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import Dialog from '@material-ui/core/Dialog';
import Alert from 'AppComponents/Shared/Alert';
import Utils from 'AppData/Utils';
import API from 'AppData/api.js';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';

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
const styles = (theme) => ({
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        padding: `${theme.spacing(2)}px 2px`,
    },
    dropzone: {
        border: '1px dashed ' + theme.palette.primary.main,
        borderRadius: '5px',
        cursor: 'pointer',
        height: 'calc(100vh - 50em)',
        padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
        margin: '10px 0',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
        '& span': {
            fontSize: 64,
            color: theme.palette.primary.main,
        },
    },
    radioWrapper: {
        flexDirection: 'row',
    },
});
/**
 * The component to manage IN mediation policies.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function EditMediationPolicy(props) {
    const {
        classes, updateMediationPolicy, selectedMediationPolicy, setEditing, editing, type, intl,
    } = props;
    const { api } = useContext(ApiContext);

    const [globalMediationPolicies, setGlobalMediationPolicies] = useState(null);
    // user uploaded api specific mediation policies
    const [seqCustom, setSeqCustom] = useState(null);
    const [provideBy, setProvideBy] = useState();
    const { id: apiId } = api;
    const NONE = 'none';
    const [localSelectedPolicyFile, setLocalSelectedPolicyFile] = useState(selectedMediationPolicy);
    function updatePoliciesFromBE() {
        const globalPromise = API.getGlobalMediationPolicies();
        const customPromise = API.getMediationPolicies(apiId);
        Promise.all([globalPromise, customPromise])
            .then((values) => {
                setGlobalMediationPolicies([...values[0].obj.list.filter((seq) => seq.type === type)]);
                setSeqCustom([...values[1].obj.list.filter((seq) => seq.type === type)]);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.MediationPolicies.Edit.EditMediationPolicy.error',
                        defaultMessage: 'Error retrieving mediation policies',
                    }));
                }
            });
    }
    function setActivePolicy(policy) {
        if (policy.name !== NONE) {
            Object.assign(policy, { content: '' });
            setLocalSelectedPolicyFile(policy);
            // updateMediationPolicy(policy);
        } else {
            Object.assign(policy, { content: '', id: NONE });
            setLocalSelectedPolicyFile(policy);
            // updateMediationPolicy(policy);
        }
    }
    useEffect(() => {
        if (selectedMediationPolicy) {
            const { shared } = selectedMediationPolicy;
            if (shared) {
                setProvideBy('global');
            } else if (selectedMediationPolicy.name === NONE) {
                setProvideBy('none');
            } else {
                setProvideBy('custom');
            }
        } else {
            setProvideBy('none');
        }
    }, [selectedMediationPolicy]);
    useEffect(() => {
        updatePoliciesFromBE();
    }, []);

    useEffect(() => {
        if (provideBy === 'custom' && seqCustom && seqCustom.length > 0) {
            setActivePolicy(seqCustom[0]);
        } else if (provideBy === 'global' && globalMediationPolicies && globalMediationPolicies.length > 0) {
            setActivePolicy(globalMediationPolicies[0]);
        }
    }, [provideBy]);

    // useEffect(() => {
    //     if (globalInMediationPolicies && globalInMediationPolicies.length > 0) {
    //         setProvideBy('global');
    //     } else if (inSeqCustom && inSeqCustom.length > 0) {
    //         setProvideBy('custom');
    //     } else {
    //         setProvideBy('none');
    //     }
    // }, [globalInMediationPolicies, inSeqCustom]);
    const saveMediationPolicy = (newPolicy) => {
        const promisedApi = API.addMediationPolicy(newPolicy, apiId, type);
        promisedApi
            .then((response) => {
                const {
                    body: { id, type: policyType, name },
                } = response;
                updatePoliciesFromBE();
                setLocalSelectedPolicyFile({
                    id,
                    type: policyType,
                    name,
                    shared: false,
                    content: '',
                });
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.MediationPolicies.Edit.EditMediationPolicy.success',
                    defaultMessage: 'Mediation policy added successfully',
                }));
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                if (errorResponse.response.body.description !== null) {
                    Alert.error(errorResponse.response.body.description);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.MediationPolicies.Edit.AddMediationPolicy.error',
                        defaultMessage: 'Error while adding mediation policy',
                    }));
                }
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
        }
    };
    /**
     * Handles the mediation policy select event.
     * @param {any} event The event pass to the layout
     */
    function handleChange(event) {
        const policy = {
            name: event.target.getAttribute('seq_name'),
            id: event.target.getAttribute('seq_id'),
            type: event.target.getAttribute('seq_type'),
        };
        setActivePolicy(policy);
    }
    /**
     * Handles the Global mediation policy download.
     * @param {any} policyToDownload policy file id that is to be downloaded.
     */
    function downloadGlobalMediationPolicyContent(policyToDownload) {
        const promisedGetContent = API.getGlobalMediationPolicyContent(policyToDownload);
        promisedGetContent
            .then((response) => {
                Utils.forceDownload(response);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.download.error'
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
        const promisedGetContent = API.getMediationPolicyContent(policyToDownload, apiId);
        promisedGetContent
            .then((done) => {
                Utils.forceDownload(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.download.error'
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
                setSeqCustom(seqCustom.filter((seq) => seq.id !== policyToDelete));
                Alert.info(<FormattedMessage
                    id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.delete.success'
                    defaultMessage='Mediation policy deleted successfully.'
                />);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.delete.error'
                        defaultMessage='Error deleting the file'
                    />);
                }
            });
    }
    const handleDownload = (policyToDownload) => {
        const isGlobalMediationPolicy = globalMediationPolicies.filter(
            (policy) => policy.id === policyToDownload,
        ).length > 0;
        if (isGlobalMediationPolicy) {
            downloadGlobalMediationPolicyContent(policyToDownload);
        } else {
            downloadCustomMediationPolicyContent(policyToDownload);
        }
    };
    const handleDelete = (policyToDelete) => {
        const isGlobalMediationPolicy = globalMediationPolicies.filter(
            (policy) => policy.id === policyToDelete,
        ).length > 0;
        if (isGlobalMediationPolicy) {
            Alert.error(<FormattedMessage
                id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.global.delete'
                defaultMessage='Cannot delete Global mediation policies.'
            />);
        } else {
            deleteCustomMediationPolicy(policyToDelete);
        }
    };
    function cancelEditing() {
        setEditing(false);
    }
    function doneEditing() {
        updateMediationPolicy(localSelectedPolicyFile);
        setEditing(false);
    }
    function handleChangeProvideBy(event) {
        const inputValue = event.target.value;
        setProvideBy(inputValue);
        if (inputValue === NONE) {
            setActivePolicy({ name: NONE, type: NONE });
        } else {
            setActivePolicy({});
        }
    }
    return (
        <Dialog
            disableBackdropClick
            disableEscapeKeyDown
            maxWidth='sm'
            fullWidth
            aria-labelledby='confirmation-dialog-title'
            open={editing}
        >
            <DialogTitle id='confirmation-dialog-title'>
                <FormattedMessage
                    id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.select.policy'
                    defaultMessage='Select a Mediation Policy'
                />
            </DialogTitle>
            {globalMediationPolicies && seqCustom && (
                <DialogContent dividers>
                    <RadioGroup value={provideBy} onChange={handleChangeProvideBy} className={classes.radioWrapper}>
                        <FormControlLabel
                            value='none'
                            control={<Radio color='primary' />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.none'
                                    defaultMessage='None'
                                />
                            )}
                        />
                        <FormControlLabel
                            value='global'
                            control={<Radio color='primary' />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.common.policies'
                                    defaultMessage='Common Policies'
                                />
                            )}
                        />
                        <FormControlLabel
                            value='custom'
                            control={<Radio color='primary' />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.custom.policies'
                                    defaultMessage='Custom Policies'
                                />
                            )}
                        />
                    </RadioGroup>
                    {provideBy === 'custom' && (
                        <>
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
                                        <input {...getInputProps()} accept='application/xml,text/xml' />
                                        <div className={classes.dropZoneWrapper}>
                                            <Icon className={classes.dropIcon}>cloud_upload</Icon>
                                            <Typography>
                                                <FormattedMessage
                                                    id={
                                                        'Apis.Details.MediationPolicies.Edit.EditMediationPolicy.'
                                                        + 'click.or.drop.to.upload.file'
                                                    }
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
                                value={localSelectedPolicyFile.name}
                                onChange={handleChange}
                            >
                                <FormLabel component='customPolicies'>
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.Edit.MediationPolicies.'
                                            + 'EditMediationPolicies.custom.mediation.policies'
                                        }
                                        defaultMessage='Custom Mediation Policies'
                                    />
                                </FormLabel>
                                {seqCustom.map((seq) => (
                                    <div>
                                        <IconButton onClick={() => handleDelete(seq.id)}>
                                            <Icon>delete</Icon>
                                        </IconButton>
                                        <Button onClick={() => handleDownload(seq.id)}>
                                            <Icon>arrow_downward</Icon>
                                        </Button>
                                        <FormControlLabel
                                            control={(
                                                <Radio
                                                    inputProps={{
                                                        seq_id: seq.id,
                                                        seq_name: seq.name,
                                                        seq_type: seq.type,
                                                    }}
                                                    color='primary'
                                                />
                                            )}
                                            label={seq.name}
                                            value={seq.name}
                                            checked={localSelectedPolicyFile.name === seq.name}
                                        />
                                    </div>
                                ))}
                            </RadioGroup>
                        </>
                    )}
                    {provideBy === 'global' && (
                        <RadioGroup
                            aria-label='inflow'
                            name='inflow'
                            className={classes.radioGroup}
                            value={localSelectedPolicyFile.name}
                            onChange={handleChange}
                        >
                            {globalMediationPolicies.map((seq) => (
                                <Box display='flex' justifyContent='space-between'>
                                    <FormControlLabel
                                        control={(
                                            <Radio
                                                inputProps={{
                                                    seq_id: seq.id,
                                                    seq_name: seq.name,
                                                    seq_type: seq.type,
                                                }}
                                                color='primary'
                                            />
                                        )}
                                        label={seq.name}
                                        value={seq.name}
                                        checked={localSelectedPolicyFile.name === seq.name}
                                    />
                                    <Box mr={22}>
                                        <Tooltip
                                            title={(
                                                <FormattedMessage
                                                    id='Apis.Details.MediationPolicies.EditMediationPolicy.download'
                                                    defaultMessage='Download'
                                                />
                                            )}
                                            aria-label='Download policy'
                                            placement='right-end'
                                            interactive
                                        >
                                            <Button onClick={() => handleDownload(seq.id)}>
                                                <Icon>vertical_align_bottom</Icon>
                                            </Button>
                                        </Tooltip>
                                    </Box>
                                </Box>
                            ))}
                        </RadioGroup>
                    )}
                </DialogContent>
            )}
            <DialogActions>
                <Button onClick={cancelEditing} color='primary'>
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.cancel.btn'
                        defaultMessage='Cancel'
                    />
                </Button>
                <Button
                    onClick={doneEditing}
                    color='primary'
                    variant='contained'
                    disabled={provideBy === 'custom' && seqCustom && seqCustom.length === 0}
                >
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditMediationPolicy.select.btn'
                        defaultMessage='Select'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
}
EditMediationPolicy.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
    setEditing: PropTypes.func.isRequired,
    editing: PropTypes.bool.isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(EditMediationPolicy));
