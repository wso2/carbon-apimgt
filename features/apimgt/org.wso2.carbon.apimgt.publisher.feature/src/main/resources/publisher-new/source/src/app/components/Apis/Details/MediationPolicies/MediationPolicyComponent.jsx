/* eslint-disable require-jsdoc */
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
/* eslint no-param-reassign: ["error", { "props": false }] */

import React, { useEffect, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import InputLabel from '@material-ui/core/InputLabel';
// import Typography from '@material-ui/core/Typography';
import Input from '@material-ui/core/Input';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import Alert from 'AppComponents/Shared/Alert';
// import Dropzone from 'react-dropzone';
import API from 'AppData/api.js';
import PropTypes from 'prop-types';
// import API from 'AppData/api.js';

const styles = theme => ({
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    paperRoot: {
        padding: 20,
        marginTop: 20,
    },
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        padding: `${theme.spacing.unit * 2}px 2px`,
    },
    itemWrapper: {
        width: 500,
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

function MediationPolicyComponent({
    api, sequences, classes, type, handleInputChange,
}) {
    const { mediationPolicies } = api;
    const [selectedPolicy, setSelectedPolicy] = useState({
        selectedPolicy: mediationPolicies.filter(seq => seq.type === type)[0],
    });
    const [policyFile, setPolicyFile] = useState({
        id: selectedPolicy.id, name: selectedPolicy.name, type: selectedPolicy.type, content: {},
    });
    const [policiesList, setPoliciesList] = useState([]);
    const [id, setId] = useState({ id: 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc' });
    const [defaultMsg, setDefaultMsg] = useState({ defaultMsg: 'In Flow' });
    const [name, setName] = useState({ name: 'In Flow' });
    const [helperTextId, setHelperTextId] = useState({
        helperTextId: 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.helper.text',
    });
    const [defaultHelperText, setDefaultHelperText] = useState({
        defaultHelperText: 'Provide the custom mediation sequence to engage in In flow',
    });

    const apiId = api.id;

    // if (type === 'IN') {
    //     const inSeq = api.inSequence;
    //     if (inSeq.id !== policyFile.id) {
    //         setPolicyFile({
    //             id: inSeq.id, name: inSeq.name, type: inSeq.type, content: {},
    //         });
    //     }
    // } else if (type === 'OUT') {
    //     const outSeq = api.outSequence;
    //     if (outSeq.id !== policyFile.id) {
    //         setPolicyFile({
    //             id: outSeq.id, name: outSeq.name, type: outSeq.type, content: {},
    //         });
    //     }
    // } else if (type === 'FAULT') {
    //     const faultSeq = api.faultSequence;
    //     if (faultSeq.id !== policyFile.id) {
    //         setPolicyFile({
    //             id: faultSeq.id, name: faultSeq.name, type: faultSeq.type, content: {},
    //         });
    //     }
    // }
    // setSeqId(policyFile.id);
    /**
     * Handled the file upload action of the dropzone.
     *
     * @param {file} policy The accepted file list by the dropzone.
     * */
    // const onDrop = (policy) => {
    //     // const policy = file[0];
    //     if (policy) {
    //         setPolicyFile({
    //             id: policy.id, name: policy.name, type: policy.type, content: policy,
    //         });
    //     }
    //     const promisedApi = API.addMediationPolicy(policy, apiId, type);
    //     promisedApi.then(() => {
    //         Alert.info(FormattedMessage({
    //             id: 'Apis.Details.MediationPolicies.MediationPoliciesComponente.success',
    //             defaultMessage: 'Mediation policy added successfully',
    //         }));
    //     }).catch((errorResponse) => {
    //         console.log(errorResponse);
    //         Alert.error(JSON.stringify(errorResponse));
    //     });
    // };

    const onChange = (event) => {
        setPolicyFile({
            id: event.currentTarget.id, name: event.target.value, type, content: {},
        });
        setSelectedPolicy(policyFile);
        handleInputChange(policyFile);
    };
    useEffect(() => {
        setPoliciesList(sequences);
        setPolicyFile(policyFile);
    }, [sequences], policyFile);
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

    const handleDownload = () => {
        const promisedGetContent = API.getMediationPolicyContent(policyFile.id, apiId);
        promisedGetContent
            .then((done) => {
                downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPoliciesComponent.download.error'
                        defaultMessage='Error downloading the file'
                    />);
                }
            });
    };
    if (type === 'in') {
        if (id !== 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc') {
            setId('Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc');
        }
        if (defaultMsg !== 'In Flow') {
            setDefaultMsg('In Flow');
        }
        if (name !== 'In Flow') {
            setName('In Flow');
        }
        if (helperTextId !== 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.helper.text') {
            setHelperTextId('Apis.Details.MediationPolicies.MediationPolicies.in.flow.helper.text');
        }
        if (defaultHelperText !== 'Provide the custom mediation sequence to engage in In flow') {
            setDefaultHelperText('Provide the custom mediation sequence to engage in In flow');
        }
    } else if (type === 'out') {
        if (id !== 'Apis.Details.MediationPolicies.MediationPolicies.out.flow.desc') {
            setId('Apis.Details.MediationPolicies.MediationPolicies.out.flow.desc');
        }
        if (defaultMsg !== 'Out Flow') {
            setDefaultMsg('Out Flow');
        }
        if (name !== 'Out Flow') {
            setName('Out Flow');
        }
        if (helperTextId !== 'Apis.Details.MediationPolicies.MediationPolicies.out.flow.helper.text') {
            setHelperTextId('Apis.Details.MediationPolicies.MediationPolicies.out.flow.helper.text');
        }
        if (defaultHelperText !== 'Provide the custom mediation sequence to engage in Out flow') {
            setDefaultHelperText('Provide the custom mediation sequence to engage in Out flow');
        }
        // setDefaultMsg('Out Flow');
        // setName('Out Flow');
        // setHelperTextId('Apis.Details.MediationPolicies.MediationPolicies.out.flow.helper.text');
        // setDefaultHelperText('Provide the custom mediation sequence to engage in Out flow');
    } else if (type === 'fault') {
        if (id !== 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc') {
            setId('Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc');
        }
        if (defaultMsg !== 'Fault Flow') {
            setDefaultMsg('Fault Flow');
        }
        if (name !== 'Fault Flow') {
            setName('Fault Flow');
        }
        if (helperTextId !== 'Apis.Details.MediationPolicies.MediationPolicies.fault.flow.helper.text') {
            setHelperTextId('Apis.Details.MediationPolicies.MediationPolicies.fault.flow.helper.text');
        }
        if (defaultHelperText !== 'Provide the custom mediation sequence to engage in Fault flow') {
            setDefaultHelperText('Provide the custom mediation sequence to engage in Fault flow');
        }
        // setDefaultMsg('Fault Flow');
        // setName('Fault Flow');
        // setHelperTextId('Apis.Details.MediationPolicies.MediationPolicies.fault.flow.helper.text');
        // setDefaultHelperText('Provide the custom mediation sequence to engage in Fault flow');
    }
    return (
        // <div className={classes.root}>
        <FormControl className={classes.formControl}>
            <div className={classes.itemWrapper}>
                <InputLabel htmlFor={id}>
                    <FormattedMessage
                        id={id}
                        defaultMessage={defaultMsg}
                    />
                </InputLabel>
                <Select
                    fullWidth
                    margin='none'
                    name={name}
                    value={policyFile.name}
                    onChange={onChange}
                    input={<Input id={id} />}
                    MenuProps={{
                        PaperProps: {
                            style: {
                                width: 200,
                            },
                        },
                    }}
                >
                    <MenuItem
                        key='none'
                        value='None'
                        id='none'
                        type=''
                        style={{
                            fontWeight: '500',
                        }}
                    >
                        <FormattedMessage
                            id={helperTextId}
                            defaultMessage={defaultHelperText}
                        />
                    </MenuItem>
                    {policiesList.map(seq => (
                        <MenuItem
                            key={seq.id}
                            value={seq.name}
                            id={seq.id}
                            type={seq.type}
                            style={{
                                fontWeight: policiesList.indexOf(seq.name) !== -1 ?
                                    '500' : '400',
                            }}
                        >
                            {seq.name}
                        </MenuItem>
                    ))}
                </Select>
                <FormHelperText>
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.none'
                        defaultMessage='None'
                    />
                </FormHelperText>
            </div>
            <Button onClick={handleDownload}>
                <Icon>arrow_downward</Icon>
                <FormattedMessage
                    id='Apis.Details.MediationPolicies.MediationPoliciesComponent.download'
                    defaultMessage='Download'
                />
            </Button>
            {/* <Dropzone
                multiple={false}
                className={classes.dropzone}
                activeClassName={classes.acceptDrop}
                rejectClassName={classes.rejectDrop}
                onDrop={(dropFile) => {
                    onDrop(dropFile);
                }}
            >
                <div className={classes.dropZoneWrapper}>
                    <Icon className={classes.dropIcon}>cloud_upload</Icon>
                    <Typography>
                        <FormattedMessage
                            id={'Apis.Details.MediationPolicies.MediationPoliciesComponent.'
                                                    + 'click.or.drop.to.upload.file'}
                            defaultMessage='Click or drag the mediation file to upload.'
                        />
                    </Typography>
                </div>
            </Dropzone> */}
        </FormControl>
    );
}

MediationPolicyComponent.propTypes = {

    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    apiId: PropTypes.shape({}).isRequired,
    name: PropTypes.shape({}).isRequired,
    sequences: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    policiesList: PropTypes.shape({}).isRequired,
    type: PropTypes.shape({}).isRequired,
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
};

export default withStyles(styles)(MediationPolicyComponent);
