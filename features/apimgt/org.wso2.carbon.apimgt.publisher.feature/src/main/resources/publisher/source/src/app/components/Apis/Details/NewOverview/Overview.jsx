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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import API from 'AppData/api';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import Resources from './Resources';
import Operations from './Operations';
import ProductResources from './ProductResources';
import StatusBar from './StatusBar';
import Configuration from './Configuration';
import MetaData from './MetaData';
import Endpoints from './Endpoints';

const styles = theme => ({
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    contentWrapper: {
        marginTop: theme.spacing(2),
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
        margin: theme.spacing(0.5),
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
        marginRight: theme.spacing(3),
    },
    subtitle: {
        marginTop: theme.spacing(0),
    },
    specialGap: {
        marginTop: theme.spacing(3),
    },
    resourceTitle: {
        marginBottom: theme.spacing(3),
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
        marginRight: theme.spacing(1),
    },
    leftSideWrapper: {
        paddingRight: theme.spacing(2),
    },
    notConfigured: {
        color: 'rgba(0, 0, 0, 0.40)',
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
    let loadEndpoints;

    if (api.apiType === API.CONSTS.API) {
        loadEndpoints = <Endpoints parentClasses={classes} api={api} />;
    }
    function getResourcesClassForAPIs(apiType) {
        switch (apiType) {
            case 'GRAPHQL':
                return <Operations parentClasses={classes} api={api} />;
            case 'APIProduct':
                return <ProductResources parentClasses={classes} api={api} />;
            case 'WS':
                return '';
            default:
                return <Resources parentClasses={classes} api={api} />;
        }
    }

    if (newApi.apiType === API.CONSTS.APIProduct) {
        api.type = API.CONSTS.APIProduct;
    }
    return (
        <React.Fragment>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.Overview.Overview.topic.header'
                        defaultMessage='Overview'
                    />
                </Typography>
            </div>
            <div className={classes.contentWrapper}>
                <Paper className={classes.root}>
                    <Grid container spacing={24}>
                        <Grid item xs={12} md={12} lg={12} className={classes.leftSideWrapper}>
                            <StatusBar api={api} />
                        </Grid>
                        <Grid item xs={12} md={12} lg={12}>
                            <Grid container spacing={24}>
                                <Grid item xs={12} md={6} lg={6}>
                                    <MetaData parentClasses={classes} />
                                </Grid>
                                <Grid item xs={12} md={6} lg={6}>
                                    <Configuration parentClasses={classes} />
                                </Grid>
                            </Grid>
                        </Grid>
                        <Grid item xs={12} md={12} lg={12}>
                            <div className={classes.specialGap}>
                                <Grid container spacing={24}>
                                    <Grid item xs={12} md={6} lg={6}>
                                        {getResourcesClassForAPIs(api.type)}
                                    </Grid>
                                    <Grid item xs={12} md={6} lg={6}>
                                        {loadEndpoints}
                                    </Grid>
                                </Grid>
                            </div>
                        </Grid>
                    </Grid>
                </Paper>
            </div>
        </React.Fragment>
    );
}

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(Overview);
