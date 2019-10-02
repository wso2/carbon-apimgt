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
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api.js';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { isRestricted } from 'AppData/AuthManager';
import Joi from '@hapi/joi';

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
    paperRoot: {
        padding: 20,
        marginTop: 20,
    },
});

/**
 *
 *
 * @class BusinessInformation
 * @extends {React.Component}
 */
class BusinessInformation extends React.Component {
    /**
     *Creates an instance of BusinessInformation.
     * @param {*} props
     * @memberof BusinessInformation
     */
    constructor(props) {
        super(props);
        const {
            businessOwner,
            businessOwnerEmail,
            technicalOwner,
            technicalOwnerEmail,
        } = this.props.api.businessInformation;
        this.state = {
            businessOwner,
            businessOwnerEmail,
            technicalOwner,
            technicalOwnerEmail,
        };
        this.isValidBusinessOwnerName = true;
        this.isValidBusinessOwnerEmail = true;
        this.isValidTechnicalOwnerEmail = true;
    }

    isNotValid = () => {
        return !(this.isValidBusinessOwnerName && this.isValidBusinessOwnerEmail && this.isValidTechnicalOwnerEmail);
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
        // Validate whether mandatory fields are filled and given valid email addresses.
        if (name === 'businessOwner') {
            this.isValidBusinessOwnerName = (value !== '');
        }
        if (name === 'businessOwnerEmail') {
            const emailStatus = Joi.string().email({ tlds: true }).required().validate(value).error;
            this.isValidBusinessOwnerEmail = (emailStatus === null);
        }
        if (name === 'technicalOwnerEmail') {
            const emailStatus = Joi.string().email({ tlds: true }).required().validate(value).error;
            this.isValidTechnicalOwnerEmail = (emailStatus === null);
        }
        this.setState({
            [name]: value,
        });
    };

    /**
     *
     *
     * @param {*} oldAPI
     * @param {*} updateAPI
     * @memberof BusinessInformation
     */
    handleSubmit(updateAPI) {
        const {
            businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail,
        } = this.state;
        const businessInformation = {
            businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail,
        };
        updateAPI({ businessInformation });
    }

    /**
     *
     * @inheritdoc
     * @returns
     * @memberof BusinessInformation
     */
    render() {
        const { classes, api, updateAPI } = this.props;
        const {
            businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail,
        } = this.state;

        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.BusinessInformation.BusinessInformation.business.info'
                            defaultMessage='Business Information'
                        />
                    </Typography>
                </div>
                <Grid container spacing={7}>
                    <Grid item xs={12}>
                        <form noValidate autoComplete='off'>
                            <TextField
                                error={!this.isValidBusinessOwnerName}
                                autoFocus
                                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                fullWidth
                                id='name'
                                label={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id={
                                                'Apis.Details.BusinessInformation.BusinessInformation' +
                                                '.business.owner.name'
                                            }
                                            defaultMessage='Business Owner'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </React.Fragment>
                                }
                                helperText={this.isValidBusinessOwnerName ?
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.business.owner.name.helper.text'
                                        }
                                        defaultMessage='Provide the name of the business owner'
                                    /> :
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.business.owner.name.helper.text' +
                                            '.error'
                                        }
                                        defaultMessage='Name of the business owner should not be empty'
                                    />
                                }
                                type='text'
                                name='name'
                                margin='normal'
                                variant='outlined'
                                value={businessOwner}
                                onChange={this.handleChange('businessOwner')}
                                InputLabelProps={{
                                    shrink: true,
                                }}
                            />

                            <TextField
                                error={!this.isValidBusinessOwnerEmail}
                                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                fullWidth
                                id='name'
                                label={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id={
                                                'Apis.Details.BusinessInformation.BusinessInformation.' +
                                                'business.owner.email'
                                            }
                                            defaultMessage='Business Owner Email'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </React.Fragment>
                                }
                                helperText={this.isValidBusinessOwnerEmail ?
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.business.owner.email.helper.text'
                                        }
                                        defaultMessage='Provide the email of the business owner'
                                    /> :
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.business.owner.email.helper.text' +
                                            '.error'
                                        }
                                        defaultMessage='Please enter a valid email address'
                                    />
                                }
                                type='email'
                                name='name'
                                margin='normal'
                                value={businessOwnerEmail}
                                onChange={this.handleChange('businessOwnerEmail')}
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                variant='outlined'
                                autoFocus
                            />

                            <TextField
                                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                fullWidth
                                id='name'
                                label={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id={
                                                'Apis.Details.BusinessInformation.BusinessInformation' +
                                                '.technical.owner.name'
                                            }
                                            defaultMessage='Technical Owner'
                                        />
                                    </React.Fragment>
                                }
                                helperText={
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.technical.owner.name.helper.text'
                                        }
                                        defaultMessage='Provide the name of the technical owner'
                                    />
                                }
                                type='text'
                                name='name'
                                margin='normal'
                                value={technicalOwner}
                                onChange={this.handleChange('technicalOwner')}
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                autoFocus
                                variant='outlined'
                            />

                            <TextField
                                error={!this.isValidTechnicalOwnerEmail}
                                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                fullWidth
                                id='name'
                                label={
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.technical.owner.email'
                                        }
                                        defaultMessage='Technical Owner Email'
                                    />
                                }
                                helperText={this.isValidTechnicalOwnerEmail ?
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.technical.owner.email.helper.text'
                                        }
                                        defaultMessage='Provide the email of the technical owner'
                                    /> :
                                    <FormattedMessage
                                        id={
                                            'Apis.Details.BusinessInformation.BusinessInformation' +
                                            '.technical.owner.email.helper.text' +
                                            '.error'
                                        }
                                        defaultMessage='Please enter a valid email address'
                                    />
                                }
                                type='email'
                                name='name'
                                margin='normal'
                                value={technicalOwnerEmail}
                                onChange={this.handleChange('technicalOwnerEmail')}
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                autoFocus
                                variant='outlined'
                            />
                        </form>
                        <div className={classes.buttonWrapper}>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={4}
                                className={classes.buttonSection}
                            >
                                <Grid container direction='row' justify='flex-end' alignItems='center'>
                                    <Grid item>
                                        <Typography variant='caption' display='block' gutterBottom>
                                            <sup style={{ color: 'red' }}>*</sup> Mandatory fields
                                        </Typography>
                                    </Grid>
                                </Grid>
                                <Grid item>
                                    <div>
                                        <Button
                                            variant='contained'
                                            color='primary'
                                            onClick={() => this.handleSubmit(updateAPI)}
                                            disabled={
                                                isRestricted(['apim:api_create', 'apim:api_publish'], api) ||
                                                this.isNotValid()
                                            }
                                        >
                                            <FormattedMessage id='save' defaultMessage='Save' />
                                        </Button>
                                    </div>
                                </Grid>
                                <Grid item>
                                    <Link
                                        to={
                                            (api.apiType === API.CONSTS.APIProduct ? '/api-products/' : '/apis/') +
                                            api.id +
                                            '/overview'
                                        }
                                    >
                                        <Button>
                                            <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                        </Button>
                                    </Link>
                                </Grid>
                            </Grid>
                        </div>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

BusinessInformation.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
        businessInformation: PropTypes.shape({
            businessOwner: PropTypes.string,
            businessOwnerEmail: PropTypes.string,
            technicalOwner: PropTypes.string,
            technicalOwnerEmail: PropTypes.string,
            apiType: PropTypes.oneOf([API.CONSTS.API, API.CONSTS.APIProduct]),
        }).isRequired,
    }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default withAPI(withStyles(styles)(BusinessInformation));
