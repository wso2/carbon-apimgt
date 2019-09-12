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

import React from 'react';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';

const styles = theme => ({
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        paddingTop: theme.spacing.unit * 1.5,
        paddingLeft: theme.spacing.unit * 2.5,
    },
    itemWrapper: {
        width: 500,
    },
});

/** UI component to add new mediation policy
 * @param {classes} classes css classes
 * @param {string} apiId api uuid.
 * @param {string} id intenationalization id for the create new policy button
 * @param {string} defaultMsg default text for the create new policy button
 * @param {bool}  policyExists has mediation policies defined already
 * @returns {object} ui.
 */
function Create({
    api, sequences, classes, policyExists, handleInputChange, type,
}) {
    let id = 'Apis.Details.MediationPolicies.MediationPolicies.in.flow.desc';
    let defaultMsg = 'With custom In mediation sequences you can modify the default mediation flow request path. Log' +
                     'the mediation flow, execute operations on Message context properties, customize, format the ' +
                     'request, are some of them.';
    let buttonText = 'Add in In Flow';
    let titleText = 'Add New IN Mediation Policy';
    if (type === 'out') {
        id = 'Apis.Details.MediationPolicies.MediationPoliciesout.out.flow.desc';
        defaultMsg = 'With custom out mediation sequence you can modify the default mediation flow of response path.' +
                     ' Log the mediation flow, execute operations on Message context properties, customize, format ' +
                     'the response, are some of them.';
        buttonText = 'Add in Out Flow';
        titleText = 'Add New OUT Mediation Policy';
    } else if (type === 'fault') {
        id = 'Apis.Details.MediationPolicies.MediationPoliciesout.fault.flow.desc';
        defaultMsg = 'With custom fault mediation sequence you can modify the default mediation flow of fault path' +
                    '(in the case of error fault path will be executed). Log the mediation flow, execute operations' +
                    ' on Message context properties, customize, format the response, are some of them.';
        buttonText = 'Add in Fault Flow';
        titleText = 'Add New FAULT Mediation Policy';
    }
    if (!policyExists) {
        return (
            <Grid container justify='center'>
                <Grid item sm={5}>
                    <Card className={classes.card}>
                        <Typography className={classes.headline} gutterBottom variant='headline' component='h2'>
                            <FormattedMessage
                                id='Apis.Details.MediationPolicies.MediationPolicies.Create.title'
                                defaultMessage={titleText}
                            />
                        </Typography>
                        <Divider />
                        <CardContent>
                            <Typography align='justify' component='p'>
                                <FormattedMessage
                                    id={id}
                                    defaultMessage={defaultMsg}
                                />
                            </Typography>
                        </CardContent>
                        <CardActions>
                            <Link to={{
                                pathname: '/apis/' + api.apiId + '/mediation-policy-add',
                                params: {
                                    api: { api },
                                    sequences: { sequences },
                                    handleInputChange: { handleInputChange },
                                    classes: { classes },
                                    type: { type },
                                },
                            }}
                            >
                                <Button variant='contained' color='primary' className={classes.button}>
                                    <FormattedMessage
                                        id='Apis.Details.MediationPolicies.MediationPolicies.Create.button'
                                        defaultMessage={buttonText}
                                    />
                                </Button>
                            </Link>
                        </CardActions>
                    </Card>
                </Grid>
            </Grid>
        );
    }
}

Create.propTypes = {

    id: PropTypes.shape({}).isRequired,
    defaultMsg: PropTypes.shape({}).isRequired,
    apiId: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    policyExists: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Create);
