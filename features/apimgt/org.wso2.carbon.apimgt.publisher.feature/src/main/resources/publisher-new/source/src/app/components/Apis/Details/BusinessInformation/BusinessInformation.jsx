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
import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import FormControl from '@material-ui/core/FormControl';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    root: {
        padding: 20,
        marginTop: 20,
    },
});

class BusinessInformation extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            businessOwner: null,
            businessOwnerEmail: null,
            technicalOwner: null,
            technicalOwnerEmail: null,
        };
    }


    handleChange = name => (event) => {
        let { value } = event.target;
        const { checked } = event.target;
        if (name === 'accessControlRoles' || name === 'visibleRoles') {
            value = value.split(',');
        } else if (name === 'isDefaultVersion') {
            value = value === 'yes';
        } else if (name === 'responseCaching') {
            value = checked ? 'Enabled' : 'Disabled';
        }
        this.setState({
            [name]: value,
        });
    };


    handleSubmit(oldAPI, updateAPI) {
        const {
            businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail,
        } = this.state;

        if (businessOwner) {
            oldAPI.businessInformation.businessOwner = businessOwner;
        }
        if (businessOwnerEmail) {
            oldAPI.businessInformation.businessOwnerEmail = businessOwnerEmail;
        }
        if (technicalOwner) {
            oldAPI.businessInformation.technicalOwner = technicalOwner;
        }
        if (technicalOwnerEmail) {
            oldAPI.businessInformation.technicalOwnerEmail = technicalOwnerEmail;
        }
        updateAPI(oldAPI);
    }
    render() {
        const { classes } = this.props;
        const {
            businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail,
        } = this.state;

        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        Business Information
                    </Typography>
                </div>
                <ApiContext.Consumer>
                    {({ api, updateAPI }) => (
                        <Grid container spacing={24}>
                            <Grid item xs={12}>
                                <Paper className={classes.root} elevation={1}>
                                    <FormControl margin='normal' className={classes.FormControlOdd}>
                                        <TextField
                                            fullWidth
                                            id='name'
                                            label='Business Owner'
                                            helperText='Provide the name of the business owner'
                                            type='text'
                                            name='name'
                                            margin='normal'
                                            value={businessOwner || api.businessInformation.businessOwner}
                                            onChange={this.handleChange('businessOwner')}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            autoFocus
                                        />
                                    </FormControl>
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <TextField
                                            fullWidth
                                            id='name'
                                            label='Business Owner Email'
                                            helperText='Provide the email of the business owner'
                                            type='text'
                                            name='name'
                                            margin='normal'
                                            value={businessOwnerEmail || api.businessInformation.businessOwnerEmail}
                                            onChange={this.handleChange('businessOwnerEmail')}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            autoFocus
                                        />
                                    </FormControl>
                                    <FormControl margin='normal' className={classes.FormControlOdd}>
                                        <TextField
                                            fullWidth
                                            id='name'
                                            label='Technical Owner'
                                            helperText='Provide the name of the business owner'
                                            type='text'
                                            name='name'
                                            margin='normal'
                                            value={technicalOwner || api.businessInformation.technicalOwner}
                                            onChange={this.handleChange('technicalOwner')}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            autoFocus
                                        />
                                    </FormControl>
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <TextField
                                            fullWidth
                                            id='name'
                                            label='Technical Owner Email'
                                            helperText='Provide the name of the business owner'
                                            type='text'
                                            name='name'
                                            margin='normal'
                                            value={technicalOwnerEmail || api.businessInformation.technicalOwnerEmail}
                                            onChange={this.handleChange('technicalOwnerEmail')}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            autoFocus
                                        />
                                    </FormControl>
                                </Paper>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={16}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    onClick={() => this.handleSubmit(api, updateAPI)}
                                                >
                                                    <FormattedMessage id='save' defaultMessage='Save' />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button>
                                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Grid>
                        </Grid>
                    )}
                </ApiContext.Consumer>
            </div>
        );
    }
}

BusinessInformation.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(BusinessInformation);
