import React, { useState, useContext, useEffect } from "react";
import Dropzone from "react-dropzone";
import { withStyles } from "@material-ui/core/styles";
import Icon from "@material-ui/core/Icon";
import Typography from "@material-ui/core/Typography";
import { FormattedMessage, injectIntl } from "react-intl";
import PropTypes from "prop-types";
import API from "AppData/api.js";
import ApiContext from "AppComponents/Apis/Details/components/ApiContext";
import Alert from "AppComponents/Shared/Alert";
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import downloadFile from 'AppComponents/Shared/Download.js';

const dropzoneStyles = {
  border: "1px dashed ",
  borderRadius: "5px",
  cursor: "pointer",
  height: 100,
  padding: "8px 0px",
  position: "relative",
  textAlign: "center",
  width: "100%",
  margin: "10px 0"
};
const styles = theme => ({
  formControl: {
    display: "flex",
    flexDirection: "row",
    padding: `${theme.spacing.unit * 2}px 2px`
  },
  dropzone: {
    border: "1px dashed " + theme.palette.primary.main,
    borderRadius: "5px",
    cursor: "pointer",
    height: "calc(100vh - 50em)",
    padding: `${theme.spacing.unit * 2}px ${theme.spacing.unit * 2}px`,
    position: "relative",
    textAlign: "center",
    width: "100%",
    margin: "10px 0"
  },
  dropZoneWrapper: {
    height: "100%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    "& span": {
      fontSize: 64,
      color: theme.palette.primary.main
    }
  },
  radioWrapper: {
    flexDirection: "row"
  }
});

function UploadCustomMediation(props) {
    const { classes, updateMediationPolicy, selectedMediationPolicy, intl, type } = props;
    const [seqCustom, setSeqCustom] = useState(null);
    const { api } = useContext(ApiContext);
    const [localSelectedPolicyFile, setLocalSelectedPolicyFile] = useState(selectedMediationPolicy);
    const { id: apiId } = api;
    const NONE = 'none';

    function updatePoliciesFromBE() {
       
        const customPromise = API.getMediationPolicies(apiId); 
        Promise.all([ customPromise])
            .then((values) => {
                
                if(values.length > 0) {
                    setSeqCustom([...values[0].obj.list.filter(seq => seq.type === type)]);
                    
                }
               
            })
        .catch(error => {
            if (process.env.NODE_ENV !== "production") {
            console.log(error);
            Alert.error(
                intl.formatMessage({
                id:
                    "Apis.Details.MediationPolicies.Edit.EditMediationPolicy.error",
                defaultMessage: "Error retrieving mediation policies"
                })
            );
            }
        });
    }

    function setActivePolicy(policy) {
        if (policy.name !== NONE) {
            Object.assign(policy, { content: '' });
            setLocalSelectedPolicyFile(policy);
            updateMediationPolicy(policy);
        } else {
            Object.assign(policy, { content: '', id: NONE });
            setLocalSelectedPolicyFile(policy);
            updateMediationPolicy(policy);
        }
    }

    const saveUploadedMediationPolicy = (newPolicy) => {
       
        const promisedApi = API.addMediationPolicy(newPolicy, apiId, type);
        promisedApi
            .then((response) => {
                console.log(response);
                
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
        .catch(errorResponse => {
            console.log(errorResponse);
            if (errorResponse.response.body.description !== null) {
            Alert.error(errorResponse.response.body.description);
            } else {
            Alert.error(
                intl.formatMessage({
                id:
                    "Apis.Details.MediationPolicies.Edit.AddMediationPolicy.error",
                defaultMessage: "Error while adding mediation policy"
                })
            );
            }
        });
    };
   
    useEffect(() => {
        updatePoliciesFromBE();
    }, []);
   

    /**
     * Handled the file upload action of the dropzone.
     * @param {file} policy The accepted file list by the dropzone.
     * */
    const onDrop = policy => {
        const policyFile = policy[0];
        if (policyFile) {
        saveUploadedMediationPolicy(policyFile);
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
     * Handles the custom mediation policy delete.
     * @param {any} policyToDelete policy file id that is to be deleted.
     */

    const deleteUploadedCustomMediation = (policyToDelete) => {
        
        const promisedGetContent = API.deleteMediationPolicy(policyToDelete, api.id);
        promisedGetContent
            .then(() => {
                setSeqCustom(seqCustom.filter(seq => seq.id !== policyToDelete));
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

    const downloadUploadedCustomMediation = (policyToDownload) => {
        
        const promisedGetContent = API.getMediationPolicyContent(policyToDownload, apiId);
        console.log(promisedGetContent);
        promisedGetContent
            .then((done) => {
                downloadFile(done);
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

    return (
        <React.Fragment>
            <FormLabel component="uploadClicked" style={{display: 'flex', marginTop:10}}>
                <FormattedMessage
                    id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.button.select'
                    defaultMessage="Upload Clicked"
                />
            </FormLabel>
            <Dropzone
                multiple={false}
                className={classes.dropzone}
                activeClassName={classes.acceptDrop}
                rejectClassName={classes.rejectDrop}
                onDrop={dropFile => {
                  onDrop(dropFile);
                }}
            >
                {({ getRootProps, getInputProps }) => (
                <div {...getRootProps({ style: dropzoneStyles })}>
                    <input {...getInputProps()} accept="application/xml,text/xml" />
                    <div className={classes.dropZoneWrapper}>
                    <Icon className={classes.dropIcon}>cloud_upload</Icon>
                    <Typography>
                        <FormattedMessage
                        id={
                            "Apis.Details.MediationPolicies.Edit.EditMediationPolicy." +
                            "click.or.drop.to.upload.file"
                        }
                        defaultMessage="Click or drag the mediation file to upload."
                        />
                    </Typography>
                    </div>
                </div>
                )}
            </Dropzone>
            {localSelectedPolicyFile
            && 
            
            <RadioGroup
                aria-label="inflow"
                name="inflow"
                className={classes.radioGroup}
                value={localSelectedPolicyFile.name}
                onChange={handleChange}
                >
                <FormLabel component="customPolicies">
                <FormattedMessage
                    id={
                    'Apis.Details.Configuration.CustomMediation.' +
                    'UploadCustomMediation.custom.mediation.policies'
                    }
                    defaultMessage="Custom Mediation Policies"
                />
                </FormLabel>
                
                {seqCustom && seqCustom.map(seq => (
                    
                <div>
                    
                    <IconButton onClick={()=> deleteUploadedCustomMediation(seq.id)}>
                    <Icon>delete</Icon>
                    </IconButton>
                    <Button onClick={()=> downloadUploadedCustomMediation(seq.id)}>
                    <Icon>arrow_downward</Icon>
                    </Button>
                    <FormControlLabel
                    control={
                        <Radio
                        inputProps={{
                            seq_id: seq.id,
                            seq_name: seq.name,
                            seq_type: seq.type
                        }}
                        color="primary"
                        />
                    }
                    label={seq.name}
                    value={seq.name}
                    checked={localSelectedPolicyFile.name === seq.name}
                    />
                </div>
                
                ))}
            </RadioGroup>}
        </React.Fragment>
    );
}

UploadCustomMediation.propTypes = {
  classes: PropTypes.shape({}).isRequired,
  selectedMediationPolicy: PropTypes.shape({}).isRequired,
  type: PropTypes.string.isRequired,
  intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired
};

export default injectIntl(withStyles(styles)(UploadCustomMediation));
