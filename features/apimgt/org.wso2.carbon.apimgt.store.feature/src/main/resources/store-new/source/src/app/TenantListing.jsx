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

import React, { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Settings from 'AppComponents/Shared/SettingsContext';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import Tenants from 'AppData/Tenants';

const styles = theme => ({
    root: {
        flexGrow: 1,
        display: 'flex',
        background: theme.palette.background.default,
        height: '100%',

    },
    paper: {
        padding: theme.spacing.unit * 2,
        textAlign: 'left',
        color: theme.palette.text.secondary,
        margin: 'auto',
        '-webkit-box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
        '-moz-box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
        'box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
        '&:hover': {
            background: theme.palette.grey[100],
            cursor: 'grab',
        },
    },
    list: {
        background: theme.palette.background.paper,
        display: 'block',
        margin: 'auto',
        'margin-top': '100px',
        padding: `${theme.spacing.unit * 3}px ${theme.spacing.unit * 2}px`,
        overflow: 'scroll',
    },
    listItem: {
        margin: 'auto',
    },
});


const tenantListing = (props) => {
    const settingContext = useContext(Settings);
    const [tenants, setTenants] = useState([]);
    const { tenantList, classes, theme } = props;

    useEffect(() => {
        if (tenantList || tenantList.length === 0) {
            const tenantApi = new Tenants();
            tenantApi.getTenantsByState().then((response) => {
                setTenants(response.body.list);
            }).catch((error) => {
                console.error('error when getting tenants ' + error);
            });
        } else {
            setTenants(tenantList);
        }
    }, []);

    return (
        <div className={classes.root}>
            <Grid container md={4} justify='left' spacing={0} className={classes.list}>
                {tenants.map(({ domain }) => {
                    return (
                        <Grid item xs={12} md={12} className={classes.listItem}>
                            <Link
                                style={{
                                    textDecoration: 'none',
                                }}
                                to={`/apis?tenant=${domain}`}
                                onClick={() => settingContext.setTenantDomain(domain)}
                            >
                                <Paper elevation={0} square className={classes.paper}>
                                    <Typography
                                        noWrap
                                        style={{
                                            fontSize: theme.typography.h5.fontSize,
                                            fontWeight: theme.typography.h1.fontWeight,
                                        }}
                                    >
                                        {domain}
                                    </Typography>
                                </Paper>
                            </Link>
                        </Grid>
                    );
                })}
            </Grid>
        </div>
    );
};

tenantListing.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.string,
        list: PropTypes.string,
        paper: PropTypes.string,
        listItem: PropTypes.string,
    }).isRequired,
    tenantList: PropTypes.arrayOf(PropTypes.string).isRequired,
    theme: PropTypes.shape({
        typography: PropTypes.shape({
            h5: PropTypes.shape({
                fontSize: PropTypes.string.isRequired,
            }).isRequired,
            h1: PropTypes.shape({
                fontWeight: PropTypes.string.isRequired,
            }).isRequired,
        }).isRequired,
    }).isRequired,
};
export default withStyles(styles, { withTheme: true })(tenantListing);
