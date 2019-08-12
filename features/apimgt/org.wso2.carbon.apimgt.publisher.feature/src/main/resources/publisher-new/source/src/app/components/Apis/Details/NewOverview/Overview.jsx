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

import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import API from 'AppData/api';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';

import CheckItem from './CheckItem';
import Resources from './Resources';
import ProductResources from './ProductResources';
import Policies from './Policies';
import Configuration from './Configuration';
import Endpoints from './Endpoints';
import BusinessInformation from './BusinessInformation';
import Scopes from './Scopes';
import Documents from './Documents';
import AdditionalProperties from './AdditionalProperties';
import Lifecycle from './Lifecycle';

const styles = theme => ({
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    buttonSuccess: {
        backgroundColor: green[500],
        '&:hover': {
            backgroundColor: green[700],
        },
    },
    checkItem: {
        textAlign: 'center',
    },
    divider: {
        marginTop: 20,
        marginBottom: 20,
    },
    chip: {
        margin: theme.spacing.unit / 2,
        padding: 0,
        height: 'auto',
        '& span': {
            padding: '0 5px',
        },
    },
    imageContainer: {
        display: 'flex',
    },
    imageWrapper: {
        marginRight: theme.spacing.unit * 3,
    },
    subtitle: {
        marginTop: theme.spacing.unit,
    },
    specialGap: {
        marginTop: theme.spacing.unit * 3,
    },
    resourceTitle: {
        marginBottom: theme.spacing.unit * 3,
    },
    ListRoot: {
        padding: 0,
        margin: 0,
    },
    titleWrapper: {
        display: 'flex',
    },
    title: {
        flex: 1,
    },
    helpButton: {
        padding: 0,
        minWidth: 20,
    },
    helpIcon: {
        fontSize: 16,
    },
    htmlTooltip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0, 0, 0, 0.87)',
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(14),
        border: '1px solid #dadde9',
        '& b': {
            fontWeight: theme.typography.fontWeightMedium,
        },
    },
    lifecycleWrapper: {
        display: 'flex',
        alignItems: 'center',
    },
    lifecycleIcon: {
        fontSize: 36,
        color: 'green',
        marginRight: theme.spacing.unit,
    },
});

/**
 * API Overview page
 *
 * @param {*} props
 * @returns
 */
function Overview(props) {
    const { classes, api: newApi } = props;
    const { api } = useContext(ApiContext);
    let loadResources;
    let loadScopes;
    let loadEndpoints;
    let endpointsCheckItem;
    let scopesCheckItem;
    if (newApi.type !== 'WS') {
        loadResources = <Resources parentClasses={classes} api={newApi} />;
        loadScopes = <Scopes parentClasses={classes} />;
    }
    if (newApi.apiType === API.CONSTS.APIProduct) {
        endpointsCheckItem = null;
        scopesCheckItem = null;
        loadResources = <ProductResources parentClasses={classes} api={newApi} />;
        loadEndpoints = null;
        loadScopes = null;
    } else if (newApi.apiType === API.CONSTS.API) {
        endpointsCheckItem = <CheckItem itemSuccess itemLabel='Endpoints' />;
        scopesCheckItem = <CheckItem itemSuccess={newApi.scopes.length > 0} itemLabel='Scopes' />;
        loadEndpoints = <Endpoints parentClasses={classes} api={newApi} />;
    }
    return (
        <Grid container spacing={7}>
            <Grid item xs={12}>
                <Grid container>
                    {endpointsCheckItem}
                    <CheckItem itemSuccess={false} itemLabel='Policies' />
                    <CheckItem itemSuccess itemLabel='Resources' />
                    {scopesCheckItem}
                    <CheckItem itemSuccess={false} itemLabel='Documents' />
                    <CheckItem itemSuccess={false} itemLabel='Business Information' />
                    <CheckItem itemSuccess={false} itemLabel='Description' />
                </Grid>
            </Grid>
            <Grid item xs={12}>
                <Grid container spacing={7}>
                    <Grid item xs={12} md={6} lg={6}>
                        <Configuration parentClasses={classes} />
                        {loadResources}
                        <AdditionalProperties parentClasses={classes} />
                    </Grid>
                    <Grid item xs={12} md={6} lg={6}>
                        <Lifecycle parentClasses={classes} />
                        {loadEndpoints}
                        <BusinessInformation parentClasses={classes} />
                        {loadScopes}
                        <Documents parentClasses={classes} api={api} />
                        <Policies parentClasses={classes} />
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(Overview);
