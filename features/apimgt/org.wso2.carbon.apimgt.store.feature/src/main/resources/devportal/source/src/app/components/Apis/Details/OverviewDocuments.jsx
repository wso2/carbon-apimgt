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
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import API from '../../../data/api';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
        maxWidth: theme.custom.contentAreaWidth,
    },
    iconClass: {
        marginRight: 10,
        color: theme.palette.secondary.main,
    },
    boxBadge: {
        background: theme.palette.grey.A400,
        color: theme.palette.getContrastText(theme.palette.grey.A400),
        fontSize: theme.typography.h5.fontSize,
        padding: theme.spacing.unit,
        width: 30,
        height: 30,
        marginRight: 20,
        textAlign: 'center',
    },
    subscriptionBox: {
        paddingLeft: theme.spacing.unit * 2,
    },
    linkStyle: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        fontSize: theme.typography.fontSize,
    },
    subscriptionTop: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    resourceWrapper: {
        height: 192,
        overflow: 'auto',
    },
    actionPanel: {
        justifyContent: 'flex-start',
    },
    linkToTest: {
        textDecoration: 'none',
    },
    emptyBox: {
        background: '#ffffff55',
        color: '#444',
        border: 'solid 1px #fff',
        padding: theme.spacing(2),
        marginTop: 50,
    },
});

function OverviewDocuments(props) {
    const [docs, setDocs] = useState([]);

    useEffect(() => {
        const restApi = new API();
        const { apiId, setDocsCount } = props;
        const promisedApi = restApi.getDocumentsByAPIId(apiId);
        promisedApi
            .then((response) => {
                if (response.obj.list.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    setDocs(response.obj.list);
                    setDocsCount(response.obj.count);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    Alert.error('Error occured');
                }
            });
    }, []);
    /**
     *
     *
     * @returns
     * @memberof Overview
     */

    const { classes, apiId } = props;
    if (docs.length === 0) {
        return (
            <Grid item xs={12}>
                <div className={classes.emptyBox}>
                    <Typography variant='body2'>
                        <FormattedMessage
                            id='Apis.Details.Overview.documents.no.content'
                            defaultMessage='No Documents Available'
                        />
                    </Typography>
                </div>
            </Grid>
        );
    }

    return (
        <React.Fragment>
            <Grid item xs={12}>
                <div className={classes.subscriptionTop}>
                    <div className={classes.boxBadge}>{docs.length}</div>
                    <Link to={'/apis/' + apiId + '/docs'} className={classes.linkStyle}>
                        <FormattedMessage id='Apis.Details.Overview.documents.count.sufix' defaultMessage='Documents' />
                    </Link>
                </div>
            </Grid>
            <Grid item xs={12}>
                <Typography variant='subtitle2'>
                    <FormattedMessage id='Apis.Details.Overview.documents.last.updated' defaultMessage='Last Updated' />
                </Typography>
                {docs.length > 0 && (
                    <div className={classes.subscriptionBox}>
                        <Link to={'/apis/' + apiId + '/docs'} className={classes.linkStyle}>
                            {docs[0].name}
                        </Link>
                        {/* <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Details.Overview.documents.last.updated'
                            defaultMessage='Last Updated'
                        />
                        21 minutes ago
                    </Typography> */}
                    </div>
                )}
            </Grid>
        </React.Fragment>
    );
}

OverviewDocuments.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(OverviewDocuments);
