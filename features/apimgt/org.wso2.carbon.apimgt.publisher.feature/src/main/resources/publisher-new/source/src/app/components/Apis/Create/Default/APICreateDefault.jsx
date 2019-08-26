/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React, { Component } from 'react';
import 'react-toastify/dist/ReactToastify.min.css';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import APIInputForm from 'AppComponents/Apis/Create/Components/APIInputForm';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
        width: theme.custom.contentAreaWidth,
    },
    buttonProgress: {
        position: 'relative',
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 6.25,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    buttonSection: {
        paddingTop: theme.spacing.unit * 2,
    },
    subTitle: {
        color: theme.palette.grey[500],
    },
});

/**
 * Create API with inline Endpoint
 * @class APICreateForm
 * @extends {Component}
 */
class APICreateDefault extends Component {
    /**
     * Creates an instance of APICreateForm.
     * @param {any} props @inheritDoc
     * @memberof APICreateForm
     */
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
        };
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Render API Create with endpoint UI
     * @memberof APICreateForm
     */
    render() {
        const {
            classes, type, handleSubmit, isAPIProduct, inputChange, api, valid, oasVersion, handleOASVersionChange,
        } = this.props;
        const { loading } = this.state;
        let mainTitle = <FormattedMessage id='create.new.rest.api' defaultMessage='New REST API' />;

        if (isAPIProduct) {
            mainTitle = <FormattedMessage id='create.new.api.product' defaultMessage='New API Product' />;
        }
        return (
            <Grid container spacing={7} className={classes.root}>
                <Grid item xs={12}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            {type === 'ws' ? (
                                <FormattedMessage id='create.new.websocket.api' defaultMessage='New WebSocket API' />
                            ) : (mainTitle)}
                        </Typography>
                        <Typography variant='h5' align='left' className={classes.subTitle}>
                            <FormattedMessage
                                id='Apis.Create.Default.APICreateDefault.gateway.url'
                                defaultMessage='Gateway_URL/'
                            />
                            {api.version ? api.version : '{apiVersion}'}/{api.context ? api.context : '{context}'}
                        </Typography>
                    </div>
                    <form onSubmit={handleSubmit}>
                        <FormControl component='fieldset' className={classes.formControl}>
                            <FormLabel component='legend'>Open API version</FormLabel>
                            <RadioGroup
                                aria-label='oas_version'
                                name='oas_version'
                                className={classes.group}
                                value={oasVersion}
                                onChange={handleOASVersionChange}
                            >
                                <FormControlLabel value='v3' control={<Radio />} label='OpenAPI 3' />
                                <FormControlLabel value='v2' control={<Radio />} label='OpenAPI 2' />
                            </RadioGroup>
                        </FormControl>
                        <APIInputForm
                            api={api}
                            handleInputChange={inputChange}
                            valid={valid}
                            isAPIProduct={isAPIProduct}
                        />
                        <Grid
                            container
                            direction='row'
                            alignItems='flex-start'
                            spacing={4}
                            className={classes.buttonSection}
                        >
                            <Grid item>
                                <ScopeValidation
                                    resourcePath={isAPIProduct ? resourcePath.API_PRODUCTS : resourcePath.APIS}
                                    resourceMethod={resourceMethod.POST}
                                >
                                    <div>
                                        <Button type='submit' disabled={loading} variant='contained' color='primary'>
                                            <FormattedMessage id='create' defaultMessage='Create' />
                                        </Button>
                                        {loading && <CircularProgress size={24} className={classes.buttonProgress} />}
                                    </div>
                                </ScopeValidation>
                            </Grid>
                            <Grid item>
                                <Button
                                    onClick={() => this.props.history.push(isAPIProduct ? '/api-products' : '/apis')}
                                >
                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                </Button>
                            </Grid>
                        </Grid>
                    </form>
                </Grid>
            </Grid>
        );
    }
}

APICreateDefault.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    type: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.string,
    }).isRequired,
    handleSubmit: PropTypes.func.isRequired,
    inputChange: PropTypes.func.isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    api: PropTypes.shape({}).isRequired,
    oasVersion: PropTypes.string.isRequired,
    handleOASVersionChange: PropTypes.func.isRequired,
};

export default withStyles(styles)(APICreateDefault);
