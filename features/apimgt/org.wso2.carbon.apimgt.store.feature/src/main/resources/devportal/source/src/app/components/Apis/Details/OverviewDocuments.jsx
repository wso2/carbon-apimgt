/* eslint-disable react/prop-types */
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
import { useHistory } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

/**
 * Add two numbers.
 * @param {JSON} theme The second number.
 * @returns {JSON} The theme object.
 */
const styles = (theme) => ({
    root: {
        padding: theme.spacing(3),
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
        padding: theme.spacing(1),
        width: 30,
        height: 30,
        marginRight: 20,
        textAlign: 'center',
    },
    subscriptionBox: {
        paddingLeft: theme.spacing(2),
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
    listWrapper: {
        padding: 0,
        margin: 0,
        width: '100%',
    },
    listItemStyle: {
        padding: 0,
        margin: 0,
    },
    listItemIcon: {
        minWidth: 30,
        color: '#BBBEBC66',
    },
    listItemPrimary: {
        fontSize: '14px',
    },
});

/**
 * Add two numbers.
 * @param {number} props The second number.
 * @returns {JSX} jsx.
 */
function OverviewDocuments(props) {
    const [docs, setDocs] = useState([]);
    const { apiId } = props;
    const history = useHistory();
    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(apiId);
        promisedApi
            .then((response) => {
                if (response.obj.list.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    setDocs(response.obj.list);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    Alert.error('Error occurred');
                }
            });
    }, []);

    const gotoDoc = (documentId) => {
        history.push('/apis/' + apiId + '/documents/' + documentId);
    };
    /**
     *
     *
     * @returns
     * @memberof Overview
     */

    const { classes } = props;
    if (docs.length === 0) {
        return (
            <Grid container className={classes.root} spacing={2}>
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
            </Grid>
        );
    }

    return (
        docs.length > 0 && (
            <List
                component='nav'
                aria-label='Available document list'
                className={classes.listWrapper}
            >
                {docs.map((doc, index) => (
                    index <= 1
                    && (
                        <ListItem button onClick={() => gotoDoc(doc.documentId)} className={classes.listItemStyle} key={doc.name}>
                            <ListItemIcon classes={{ root: classes.listItemIcon }}>
                                <Icon>insert_drive_file</Icon>
                            </ListItemIcon>
                            <ListItemText
                                primary={doc.name}
                                // secondary={truncateString(100, doc.summary)}
                                classes={{ primary: classes.listItemPrimary }}
                            />
                        </ListItem>
                    )
                ))}
            </List>
        )
    );
}

OverviewDocuments.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(OverviewDocuments);
