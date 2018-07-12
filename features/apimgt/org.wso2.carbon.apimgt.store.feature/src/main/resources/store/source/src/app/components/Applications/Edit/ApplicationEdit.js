import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {withStyles} from 'material-ui/styles';
import Grid from 'material-ui/Grid';
import { Link } from 'react-router-dom';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import ArrowBack from 'material-ui-icons/ArrowBack';
import Application from "../../../data/Application.js";
import Loading from "../../Base/Loading/Loading";
import TextField from 'material-ui/TextField';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { FormControl, FormHelperText } from 'material-ui/Form';
import { MenuItem } from 'material-ui/Menu';
import API from "../../../data/api";
import Alert from "../../Shared/Alert";

const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginLeft: 20
    },
    buttonsWrapper: {
        marginTop: 40
    },
    legend: {
        marginBottom: 0,
        borderBottomStyle: 'none',
        marginTop: 20,
        fontSize: 12,
    },
    inputText: {
        marginTop: 20,
    },
    buttonAlignment: {
        marginLeft: 20,
    },
    buttonRightLink: {
        textDecoration: 'none',
    }
});

class ApplicationEdit extends Component {

    constructor(props){
        super(props);
        this.state = {
            application: null,
            name: null,
            quota: "Unlimited",
            description: null,
            id: null,
            tiers: []
        };
    }

    componentDidMount(){
        let promised_application = Application.get(this.props.match.params.application_id);
        const api = new API();
        let promised_tiers = api.getAllTiers("application");
        promised_application.then( application => {
            this.setState({
                application:application,
                quota:application.throttlingTier,
                name:application.name,
                description:application.description,
                id:application.id
            });
        }).catch(
            error => {
                console.error(error);
            }
        );
        promised_tiers.then((response) => {
                let tierResponseObj = response.body;
                let tiers = [];
                tierResponseObj.list.map(item => tiers.push(item.name));
                this.setState({tiers: tiers});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    handleChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    handleSubmit = (event) => {
        event.preventDefault();
        let updated_application = {
            id:this.state.id,
            name: this.state.name,
            throttlingTier: this.state.quota,
            description: this.state.description,
            lifeCycleStatus:this.state.application.lifeCycleStatus
        };
        let api = new API();
        let promised_update = api.updateApplication(updated_application,null);
        promised_update.then(response => {
            let appId = response.body.applicationId;
            let redirectUrl = "/applications/" + appId;
            this.props.history.push(redirectUrl);
            console.log("Application updated successfully.");
        }).catch(
            function (error) {
                Alert.error("Error while updating application");
                console.log("Error while updating application");
            });
    };

    render() {
        const { classes } = this.props;
        let application = this.state.application;
        let tiers = this.state.tiers;
        if (!application){
            return <Loading/>
        }
        return (
            <Grid container spacing={0} justify="flex-start">
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10} className={classes.titleBar}>
                    <div className={classes.buttonLeft}>
                        <Link to={"/applications/" + application.id}>
                            <Button  variant="raised" size="small" className={classes.buttonBack}
                                     color="default">
                                <ArrowBack />
                            </Button>
                        </Link>
                        <div className={classes.title}>
                            <Typography variant="display1">
                                Go Back
                            </Typography>
                        </div>
                    </div>
                </Grid>
                <Grid item xs={12} lg={6} xl={4}>
                    <form className={classes.container} noValidate autoComplete="off">
                        <TextField
                            label="Application Name"
                            value={this.state.name}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText="Enter a name to identify the Application. You will be able to pick this
                            application when subscribing to APIs "
                            fullWidth
                            name="name"
                            onChange={this.handleChange('name')}
                            placeholder="My Mobile Application"
                            autoFocus={true}
                            className={classes.inputText}
                        />
                        { tiers &&
                        <FormControl margin="normal">
                            <InputLabel htmlFor="quota-helper">Per Token Quota</InputLabel>
                            <Select
                                value={this.state.quota}
                                onChange={this.handleChange('quota')}
                                input={<Input name="quota" id="quota-helper" />}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier} value={tier}>{tier}</MenuItem>)}
                            </Select>
                            <FormHelperText>
                                Assign API request quota per access token. Allocated quota will be
                                shared among all the subscribed APIs of the application.
                            </FormHelperText>
                        </FormControl>
                        }
                        <TextField
                            label="Application Description"
                            InputLabelProps={{
                                shrink: true,
                            }}
                            value={this.state.description}
                            helperText="Describe the application"
                            fullWidth
                            multiline
                            rowsMax="4"
                            name="description"
                            onChange={this.handleChange('description')}
                            placeholder="This application is grouping apis for my mobile application"
                            className={classes.inputText}
                        />
                        <div className={classes.buttonsWrapper}>
                            <Button variant="raised" color="primary"  onClick={this.handleSubmit}>
                                Update
                            </Button>
                                <Link to={"/applications"} className={classes.buttonRightLink}>
                                    <Button variant="raised" className={classes.buttonAlignment}>
                                        Cancel
                                    </Button>
                                </Link>
                        </div>
                    </form>
                </Grid>
            </Grid>
        );
    }
}
ApplicationEdit.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ApplicationEdit);
