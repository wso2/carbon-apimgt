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
/* eslint-disable array-callback-return */
/* eslint no-param-reassign: ["error", { "props": true, "ignorePropertyModificationsFor": ["operationObj"] }] */

import React, { useState, useContext } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import cloneDeep from 'lodash.clonedeep';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import CustomSplitButton from 'AppComponents/Shared/CustomSplitButton';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import ProductResourcesEditWorkspace from './ProductResourcesEditWorkspace';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: theme.spacing(2),
    },
    buttonWrapper: {
        marginTop: theme.spacing(4),
    },
}));

/**
 *
 *
 * @returns
 */
function ProductResourcesEdit() {
    const classes = useStyles();

    // Get the current api product object from the context
    const { api, updateAPI } = useContext(APIContext);
    const apiCopy = cloneDeep(api);
    const history = useHistory();
    const { apis } = apiCopy;

    // Define states
    const [apiResources, setApiResources] = useState(apis);
    const [isUpdating, setUpdating] = useState(false);
    // Initialize the rest api libraries

    const handleSave = () => {
        setUpdating(true);

        const updatePromise = updateAPI({ apis: apiResources }, true);
        updatePromise
            .then(() => {
                setUpdating(false);
            })
            .catch((error) => {
                setUpdating(false);
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 401) {
                    doRedirectToLogin();
                }
            });
    };

    const handleSaveAndDeploy = () => {
        setUpdating(true);

        const updatePromise = updateAPI({ apis: apiResources }, true);
        updatePromise
            .then(() => {
                setUpdating(false);
            })
            .catch((error) => {
                setUpdating(false);
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 401) {
                    doRedirectToLogin();
                }
            }).finally(() => history.push({
                pathname: api.isAPIProduct() ? `/api-products/${api.id}/deployments`
                    : `/apis/${api.id}/deployments`,
                state: 'deploy',
            }));
    };

    return (
        <div className={classes.root}>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.ProductResources.ProductResourcesEdit.title'
                        defaultMessage='Manage Resources'
                    />
                </Typography>
            </div>
            <div className={classes.contentWrapper}>
                <ProductResourcesEditWorkspace
                    apiResources={apiResources}
                    setApiResources={setApiResources}
                    api={api}
                />
                <div className={classes.buttonWrapper}>
                    <Grid container direction='row' alignItems='flex-start' spacing={1}>
                        <Grid item>
                            <div>
                                <CustomSplitButton
                                    handleSave={handleSave}
                                    handleSaveAndDeploy={handleSaveAndDeploy}
                                    isUpdating={isUpdating}
                                />
                            </div>
                        </Grid>
                        <Grid item>
                            <Link to={'/apis/' + api.id + '/overview'}>
                                <Button>
                                    <FormattedMessage
                                        id='Apis.Details.ProductResources.ProductResourcesEdit.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </div>
            </div>
        </div>
    );
}

export default ProductResourcesEdit;
