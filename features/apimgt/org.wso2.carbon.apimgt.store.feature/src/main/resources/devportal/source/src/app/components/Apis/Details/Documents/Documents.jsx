/* eslint-disable react/jsx-props-no-spreading */
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
import { ResourceNotFound } from 'AppComponents/Base/Errors/index';
import { Redirect, Route, Switch } from 'react-router-dom';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import Progress from 'AppComponents/Shared/Progress';
import { matchPath } from 'react-router';
import DocList from 'AppComponents/Apis/Details/Documents/DocList';

const styles = (theme) => ({
    paper: {
        padding: theme.spacing(2),
        color: theme.palette.text.secondary,
        minHeight: 400,
        position: 'relative',
    },
    paperMenu: {
        color: theme.palette.text.secondary,
        minHeight: 400 + theme.spacing(4),
        height: '100%',
    },
    docContent: {
        paddingTop: theme.spacing(1),
    },
    parentListItem: {
        borderTop: 'solid 1px #ccc',
        borderBottom: 'solid 1px #ccc',
        color: theme.palette.grey[100],
        background: theme.palette.grey[100],
        cursor: 'default',
    },
    listRoot: {
        paddingTop: 0,
    },
    nested: {
        paddingLeft: theme.spacing(3),
        paddingTop: 3,
        paddingBottom: 3,
    },
    childList: {
        paddingTop: 0,
        marginTop: 0,
        paddingBottom: 0,
    },
    contentWrapper: {
        paddingRight: theme.spacing(3),
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
        paddingTop: theme.spacing(3),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    generateCredentialWrapper: {
        marginLeft: 0,
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    genericMessageWrapper: {
        margin: theme.spacing(2),
    },
    typeText: {
        color: '#000',
    },
    docLinkRoot: {
        paddingLeft: 0,
    },
    toggler: {
        height: '100%',
        paddingTop: 20,
        cursor: 'pointer',
        marginLeft: '-20px',
        display: 'block',
    },
    togglerTextParent: {
        writingMode: 'vertical-rl',
        transform: 'rotate(180deg)',
    },
    togglerText: {
        textOrientation: 'sideways',
    },
    toggleWrapper: {
        position: 'relative',
        background: '#fff9',
        paddingLeft: 20,
    },
    docsWrapper: {
        margin: 0,
    },
    docContainer: {
        display: 'flex',
        marginLeft: theme.spacing(3),
        marginRight: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
    docListWrapper: {
        width: 285,
    },
    docView: {
        flex: 1,
    },
    listItemRoot: {
        minWidth: 30,
    },
});
/**
 * Switch routes for documents.
 * @param {JSON} props The props passed down from parents.
 * @returns {JSX} Returning JSX to render.
 */
function Documents(props) {
    const { classes } = props;
    const { location: { pathname } } = props;
    let match = matchPath(pathname, {
        path: '/apis/:apiUuid/documents/:documentId',
        exact: true,
        strict: false,
    });
    const apiId = props.match.params.apiUuid;
    let documentId = match ? match.params.documentId : null;
    const [documentList, changeDocumentList] = useState(null);
    const [selectedDoc, setSelectedDoc] = useState(null);

    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(apiId);
        promisedApi
            .then((response) => {
                const overviewDoc = response.body.list.filter((item) => item.otherTypeName !== '_overview');
                const types = [];
                if (overviewDoc.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    for (let i = 0; i < overviewDoc.length; i++) {
                        const selectedType = overviewDoc[i].type;
                        let hasType = false;
                        for (let j = 0; j < types.length; j++) {
                            if (selectedType === types[j].docType) {
                                types[j].docs.push(overviewDoc[i]);
                                hasType = true;
                            }
                        }
                        if (!hasType) {
                            // Adding a new type entry
                            types.push({
                                docType: selectedType,
                                docs: [overviewDoc[i]],
                            });
                        }
                        if (overviewDoc[i].documentId === documentId) {
                            setSelectedDoc(overviewDoc[i]);
                        }
                    }
                }
                changeDocumentList(types);
                if (!documentId && types.length > 0) {
                    setSelectedDoc(types[0].docs[0]);
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
    useEffect(() => {
        if (documentList) {
            match = matchPath(pathname, {
                path: '/apis/:apiUuid/documents/:documentId',
                exact: true,
                strict: false,
            });
            documentId = match ? match.params.documentId : null;
            for (const type of documentList) {
                for (const doc of type.docs) {
                    if (doc.documentId === documentId) {
                        setSelectedDoc(doc);
                    }
                }
            }
        }
    }, [documentId]);
    if (!documentList) {
        return (
            <>
                <Typography variant='h4' component='h2' className={classes.titleSub}>
                    <FormattedMessage
                        id='Apis.Details.Documents.Documentation.title'
                        defaultMessage='API Documentation'
                    />
                </Typography>
                <Progress />
            </>
        );
    }
    if (documentList && documentList.length === 0) {
        return (
            <>
                <Typography variant='h4' component='h2' className={classes.titleSub}>
                    <FormattedMessage
                        id='Apis.Details.Documents.Documentation.title'
                        defaultMessage='API Documentation'
                    />
                </Typography>
                <div className={classes.genericMessageWrapper}>
                    <InlineMessage type='info' className={classes.dialogContainer}>
                        <Typography variant='h5' component='h3'>
                            <FormattedMessage
                                id='Apis.Details.Documents.Documentation.no.docs'
                                defaultMessage='No Documents Available'
                            />
                        </Typography>
                        <Typography component='p'>
                            <FormattedMessage
                                id='Apis.Details.Documents.Documentation.no.docs.content'
                                defaultMessage='No documents are available for this API'
                            />
                        </Typography>
                    </InlineMessage>
                </div>
            </>
        );
    }
    if (!selectedDoc) {
        return (<Progress />);
    }
    return (
        <Switch>
            <Redirect exact from={`/apis/${apiId}/documents`} to={`/apis/${apiId}/documents/${selectedDoc.documentId}`} />
            <Route
                path='/apis/:apiUuid/documents/:documentId'
                render={() => (
                    <DocList
                        {...props}
                        documentList={documentList}
                        selectedDoc={selectedDoc}
                        apiId={apiId}
                    />
                )}
            />
            <Route component={ResourceNotFound} />
        </Switch>
    );
}

export default injectIntl(withStyles(styles)(Documents));
