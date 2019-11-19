import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography'
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
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
import MediationPolicies from '../../NewOverview/MediationPolicies';

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
    index: PropTypes.any.isRequired,
    value: PropTypes.any.isRequired,
    
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
    }
}));

function EditCustomMediation() {

    const classes = useStyles();
    
    const [value, setValue] = React.useState(0);
    const [addedMediators, setAddedMediators] = useState([]);
    const [validity, setValidity] = useState({});

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    
    const { mediatorIcons } = Configurations;

    const addMediator = (src) => {
       
        const mediatorObj = { src:src, id:0 };
        const newAddedMediators = cloneDeep(addedMediators);
        newAddedMediators.push(mediatorObj); 
        setAddedMediators(newAddedMediators);
        addedMediators.forEach((src, index) => mediatorObj.id = index + 1);
            
    }

    const deleteMediator = ( id ) => {
        
        let updatedMediators = cloneDeep(addedMediators);
        setAddedMediators(updatedMediators.filter((mediatorObj) => {
            return mediatorObj.id !== id;
        }));
       
    }

    // function nameValidation() {
        
    //         const nameValidity = mediation-name;
    //         console.log(value)
    //         if (nameValidity === null) {
    //             APIValidation.apiParameter.validate(field + ':' + value).then((result) => {
    //                 if (result.body.list.length > 0 && value.toLowerCase() === result.body.list[0]
    //                     .name.toLowerCase()) {
    //                     updateValidity({
    //                         ...validity,
    //                         name: { details: [{ message: 'Name ' + value + ' already exists' }] },
    //                     });
    //                 } else {
    //                     updateValidity({ ...validity, name: nameValidity });
    //                 }
    //             });
    //         } else {
    //             updateValidity({ ...validity, name: nameValidity });
    //         }
    // }

    return (
       
        <React.Fragment>
                <Paper className={classes.paper} elevation={0}>   
                    
                <input
                    accept='application/xml,text/xml'
                    className={classes.input}
                    id="upload-button-file"
                    multiple
                    type="file"
                />
                    <label htmlFor="upload-button-file">
                        <Button 
                            component="span"
                            variant='contained'   
                        >
                            <FormattedMessage
                                id='Apis.Details.MediationPolicies.Edit.EditCustomMediation.upload.btn'
                                defaultMessage='Upload Mediation Flow'
                            />
                        </Button>
                    </label>

                    <form noValidate autoComplete="off">
                        <TextField
                        className={classes.textField}
                        autoFocus
                        id='outlined-mediation-name'
                        label={
                            <React.Fragment>
                                <FormattedMessage id='Apis.Details.MediationPolicies.Edit.EditCustomMediation.name' defaultMessage='Name' />
                                <sup className={classes.mandatoryStar}>*</sup>
                            </React.Fragment>
                        }
                        name='mediation-name'
                        margin='normal'
                        variant='outlined'
                        />
                    </form>
                    {/* <Button
                    color='primary'
                    variant='contained'
                    className={classes.save}
                    onClick={nameValidation}
                    value={MediationPolicies.name}
                >
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditCustomMediation.save.btn'
                        defaultMessage='Save'
                    />
                </Button> */}
                </Paper>
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
                                                <Button onClick={() => deleteMediator(mediatorObj.id)}>
                                                    <img src = {mediatorObj.src} style={{ paddingRight: 10, height: 'fit-content' }}/>
                                                           
                                                </Button>
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
                                                            <IconButton classes={{root :classes.iconButtonOverride}} onClick={() => addMediator(mediator.src2)}>
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
                >
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditCustomMediation.save.btn'
                        defaultMessage='Save'
                    />
                </Button>
                </div>                           
        </React.Fragment>
    );
}

export default EditCustomMediation
