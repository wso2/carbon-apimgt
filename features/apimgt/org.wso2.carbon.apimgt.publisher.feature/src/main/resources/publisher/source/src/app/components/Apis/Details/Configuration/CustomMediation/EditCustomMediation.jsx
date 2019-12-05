import React, { useState, useContext, useEffect } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography'
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Configurations from 'Config';
import IconButton from "@material-ui/core/IconButton";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import cloneDeep from 'lodash.clonedeep';
import Icon from '@material-ui/core/Icon';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import API from 'AppData/api.js';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import DialogTitle from '@material-ui/core/DialogTitle';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';

import MediatorProperties from './MediatorProperties'

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
      <Typography
        component="div"
        role="tabpanel"
        hidden={value !== index}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        {...other}
      >
        <Box p={2}>{children}</Box>
      </Typography>
    );
}
  
TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    
};
  
function a11yProps(index) {
    return {
      id: `simple-tab-${index}`,
      'aria-controls': `simple-tabpanel-${index}`,
    };
}

function LinkTab(props) {
    return (
      <Tab
        component="a"
        onClick={event => {
          event.preventDefault();
        }}
        {...props}
      />
    );
}

const useStyles = makeStyles(theme => ({
    paper: {
        padding: theme.spacing(1, 0),   
    },
    textField: {
        width: 600,
    },
    mandatoryStar: {
        color: theme.palette.error.main,
    },
    tab: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.paper,
        height: 400,
        width: '100%',
        overflowY: 'auto',
        overflowX: 'hidden'
    },
    wrapper: {
        padding: theme.spacing(2, 0),
        display: 'flex',
    },
    input: {
        display: 'none',
    },
    mediators: {
        padding: theme.spacing(0, 4),
    },
    head: {
        color:  theme.palette.common.black,
        fontSize: 14,    
    },
    iconButtonOverride: {
        justifyContent: 'left',
        width: '100%',
        display: 'flex',
        borderRadius: 0,
    },
    box: {
        display: 'inline-flex', 
        overflow: 'auto', 
        width: '100%', 
        height: 310,
        alignItems: 'center'
    },
    save: {
       paddingBottom:10 
    },
    toolbar: {
        flexGrow: 1,
    },
    title: {
        flexGrow: 1,
    },
    deleteButtonOverride: {
        borderRadius: 0,
        height:5,
        width:2,      
    },
    card: {
        maxWidth: 150,
        height:130
      },      
}));

function EditCustomMediation(props) {

    const {
       intl,type,selectedMediationPolicy
    } = props;
    const { api } = useContext(ApiContext);

    const classes = useStyles();
    const [value, setValue] = React.useState(0);
    const [addedMediators, setAddedMediators] = useState([]);
    const [mediationName, setMediationName] = useState(null);
    const [addedMediationNames, setAddedMediationNames] = useState([]);
    const [errors, setErrors] = useState(null);
    const [seqCustom, setSeqCustom] = useState(null);
    const [localSelectedPolicyFile, setLocalSelectedPolicyFile] = useState(selectedMediationPolicy);
    const { id: apiId } = api;
    const [mediatorId, setMediatorId] = useState(null);
    const [mediatorLogo, setMediatorLogo] = useState(null);
    const [mediatorName, setMediatorName] = useState(null);
    const [editing, setEditing] = useState(false);

    function startEditing(obj) {
        setMediatorId(obj.id);
        setMediatorLogo(obj.src);
        setMediatorName(obj.name);
        setEditing(true);
        setDialogWidth('sm');  
    }
    
    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    
    const { mediatorIcons } = Configurations;

    function generateId() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
          var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
          return v.toString(16);
        });
    }
    const addMediator = (src, name) => {
       
        const mediatorObj = { src:src, id:'', name:name };
        const newAddedMediators = cloneDeep(addedMediators);
        mediatorObj.id = generateId();
        newAddedMediators.push(mediatorObj); 
        setAddedMediators(newAddedMediators);  
    }

    // const addName = (name) => {
        
    //     const nameObj = { name:name, id:0 };
    //     const newAddedMediationNames = addedMediationNames;
    //     newAddedMediationNames.push(nameObj);
    //     setAddedMediationNames(newAddedMediationNames);
    //     //addedMediationNames.forEach((mediationName, index) => nameObj.id = index + 1);      
    // }

    const deleteMediator = ( id ) => {
        //setEditing(false);
        let updatedMediators = cloneDeep(addedMediators);
        setAddedMediators(updatedMediators.filter((mediatorObj) => {
            return mediatorObj.id !== id;
        }));
        
    }

    const handleNameChange = (event) => {
            
            let seqNames = event.target.value;
            let error = '';
            let formIsValid = true;

            if(seqNames.length === 0){
                formIsValid = false;
                error = (intl.formatMessage({
                    id: 'Apis.Details.Configuration.CustomMediation.EditCustomMediation.name.empty',
                    defaultMessage: 'Mediation Sequence should not be empty.',
                }))    
            } else if(typeof seqNames !== "undefined"){
                    if(!seqNames.match(/^[a-z_A-Z]+$/)){
                    formIsValid = false;
                    error = (intl.formatMessage({
                        id: 'Apis.Details.Configuration.CustomMediation.EditCustomMediation.name.invalid',
                        defaultMessage: 'field name is not valid.name should be without spaces and special charectors.',
                    }))
                    }        
                }
            setErrors( error );
            setMediationName(seqNames);
            return formIsValid;        
    }

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

    const saveDesignedMediationPolicy = (newPolicy) => {
        
        const promisedApi = API.addMediationPolicy(newPolicy, apiId, type);
        //console.log(newPolicy,apiId,type);
        promisedApi
            .then((response) => { 
                const {
                    body: { id, type: policyType, name },
                } = response;
                console.log(response);
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
                console.log(errorResponse);
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

  return (
       
        <React.Fragment>
            <FormLabel component="designNewClicked" style={{display: 'flex', marginTop:10}}>
                <FormattedMessage
                    id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.button.select'
                    defaultMessage="Create New Clicked"
                />
            </FormLabel>
                <Paper className={classes.paper} elevation={0}>
                    <form noValidate autoComplete="off">
                        <TextField
                        className={classes.textField}
                        autoFocus
                        id='outlined-mediation-name'
                        value={mediationName}
                        error={errors}
                        label={
                            <React.Fragment>
                                <FormattedMessage id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.name' defaultMessage='Name' />
                                <sup className={classes.mandatoryStar}>*</sup>
                            </React.Fragment>
                        }
                        helperText =  { errors && `${errors}`}
                        name='mediation-name'
                        margin='normal'
                        variant='outlined'
                        onChange={handleNameChange}
                        />    
                    </form>    
                </Paper>
                <div>
                <MediatorProperties
                setEditing={setEditing}
                editing={editing}
                mediatorId={mediatorId}
                mediatorLogo={mediatorLogo}
                mediatorName={mediatorName}
                />
                </div>
                <Grid container className={classes.wrapper}>
                    <Grid item xs={10}>
                        <Paper className={classes.tab}>
                            <AppBar position="relative" color="default">
                                <Tabs
                                value={value}
                                onChange={handleChange}
                                >
                                <LinkTab label="Design" href="/drafts" {...a11yProps(0)} />
                                <LinkTab label="Source" href="/trash" {...a11yProps(1)} />
                                </Tabs>
                            </AppBar>
                            
                            <TabPanel value={value} index={0}>
                                <div className={classes.box} >
                                    {addedMediators.map((mediatorObj) => {
                                        return (addedMediators.length > 0 ) ?
                                            <div style={{ display: 'flex' }}>
                                                <Card className={classes.card}>
                                                    <CardActionArea>
                                                        <Button onClick={() => startEditing(mediatorObj)}>
                                                            <img src = {mediatorObj.src} style={{ paddingRight: 10, height: 'fit-content' }}/>
                                                        </Button>
                                                    </CardActionArea>
                                                    <CardActions style={{ justifyContent: 'center'}}>
                                                        <IconButton onClick={() => deleteMediator(mediatorObj.id)} classes={{root :classes.deleteButtonOverride}}>
                                                            <Icon style={{position:'absolute'}}>delete</Icon>
                                                        </IconButton>
                                                    </CardActions>
                                                    </Card>
                                                <Icon style={{ paddingRight: 10, fontSize: 60, height: 'fit-content'}}>arrow_right_alt</Icon> 
                                            </div>           
                                            : {}        
                                    }
                                    )}
                                </div>
                            </TabPanel>
                            <TabPanel value={value} index={1}>
                                Source
                            </TabPanel>
                        </Paper>
                    </Grid>
                    <Grid item xs={2} className={classes.mediators} >
                        <Paper className={classes.tab}>
                                <Table stickyHeader>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell align='center' className={classes.head}>Mediators</TableCell>     
                                            </TableRow>
                                        </TableHead>
                                    
                                        <TableBody>
                                            {mediatorIcons.map((mediator) => {
                                                return (
                                                    <TableRow>
                                                        <TableCell component="th" align='left' style={{ padding: 0 }}>
                                                            <IconButton classes={{root :classes.iconButtonOverride}} onClick={() => addMediator(mediator.src2, mediator.name)}>
                                                                <img src={mediator.src1} style={{ paddingRight: 10 }}/>
                                                                <Typography> { mediator.name} </Typography>
                                                            </IconButton>
                                                        </TableCell>
                                                    </TableRow>
                                                )})}
                                        </TableBody>
                                </Table>    
                        </Paper>
                    </Grid>
                </Grid>    
                <div className={classes.save}>
                <Button
                    color='primary'
                    variant='contained'
                    className={classes.save}
                    //onClick={()=> addName(mediationName)}
                >
                    <FormattedMessage
                        id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.save.btn'
                        defaultMessage='Save'
                    />
                </Button>
                </div>
                <div>
                    {addedMediationNames.map((nameObj) => {
                        console.log(nameObj);
                        return (
                            <p>{nameObj.name}</p>
                        )}
                    )}
                </div>
                {/* {localSelectedPolicyFile
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
                    
                    {seqCustom && seqCustom.map(mediationName => (
                        
                    <div>
                        
                        <IconButton>
                        <Icon>delete</Icon>
                        </IconButton>
                        <Button>
                        <Icon>arrow_downward</Icon>
                        </Button>
                        <FormControlLabel
                        control={
                            <Radio
                            inputProps={{
                                seq_id: mediationName.id,
                                seq_name: mediationName.name,
                                seq_type: mediationName.type
                            }}
                            color="primary"
                            />
                        }
                        label={mediationName}
                        value={mediationName}
                        checked={localSelectedPolicyFile.name === mediationName}
                        />
                    </div>
                    
                    ))}
                </RadioGroup>}                          */}
        </React.Fragment>
    );
}

EditCustomMediation.propTypes = {
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
};

export default injectIntl((EditCustomMediation))
