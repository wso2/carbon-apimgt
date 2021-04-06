/* eslint-disable react/no-array-index-key */
/* eslint-disable react/prop-types */
/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable no-unused-expressions */
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
import { FormattedMessage, injectIntl } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import Button from '@material-ui/core/Button';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import useWindowSize from 'AppComponents/Shared/UseWindowSize';
import Details from 'AppComponents/Apis/Details/Documents/Details';

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
        background: theme.custom.apiDetailPages.documentBackground,
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
        '& .material-icons': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    titleSub: {
        marginLeft: theme.spacing(2),
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
        color: theme.palette.text.primary,
    },
    toggler: {
        height: '100%',
        padding: '20px 0 0 0',
        cursor: 'pointer',
        marginLeft: '-20px',
        display: 'block',
        minWidth: 'inherit',
        flexDirection: 'column',
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
        paddingLeft: 20,
        background: theme.custom.apiDetailPages.documentBackground,
    },
    docsWrapper: {
        margin: 0,
        background: theme.custom.apiDetailPages.documentBackground,
    },
    docContainer: {
        display: 'flex',
        marginLeft: 20,
        marginRight: 20,
        marginTop: 20,
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
 * Show document list.
 * @param {JSON} props The second number.
 * @returns {JSX} The sum of the two numbers.
 */
function DocList(props) {
    const {
        classes, documentList, apiId, selectedDoc,
    } = props;
    const [selectedIndexA, changeSelectedIndexA] = useState(0);
    const [selectedIndexB, changeSelectedIndexB] = useState(0);
    const [width] = useWindowSize();
    const [showDocList, setShowDocList] = useState(!(width < 1400));
    const toggleDocList = () => {
        setShowDocList(!showDocList);
    };
    const handleListItemClick = (event, doc) => {
        const path = `/apis/${apiId}/documents/${doc.documentId}`;
        props.history.push(path);
    };
    const makeActive = () => {
        let iA = 0;
        for (const type of documentList) {
            let iB = 0;
            for (const doc of type.docs) {
                if (doc.documentId === selectedDoc.documentId) {
                    changeSelectedIndexA(iA);
                    changeSelectedIndexB(iB);
                }
                iB++;
            }
            iA++;
        }
    };
    useEffect(() => {
        makeActive();
    }, [selectedDoc]);
    useEffect(() => {
        width < 1400 ? setShowDocList(false) : setShowDocList(true);
    }, [width]);

    return (
        <>
            <Typography variant='h4' className={classes.titleSub}>
                <FormattedMessage id='Apis.Details.Documents.Documentation.title' defaultMessage='API Documentation' />
            </Typography>
            <div className={classes.docContainer}>
                {showDocList && (
                    <div className={classes.docListWrapper}>
                        <div className={classes.paperMenu}>
                            <List component='nav' className={classes.listRoot}>
                                {documentList.map((type, indexA) => (
                                    <React.Fragment key={indexA}>
                                        <ListItem component='div' className={classes.parentListItem}>
                                            <ListItemIcon classes={{ root: classes.listItemRoot }}>
                                                <CustomIcon strokeColor='#444' width={24} height={24} icon='docs' />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary={type.docType}
                                                classes={{ root: classes.typeText }}
                                            />
                                        </ListItem>
                                        {type.docs.length > 0 && (
                                            <List component='div' className={classes.childList}>
                                                {type.docs.map((doc, indexB) => (
                                                    <ListItem
                                                        button
                                                        className={classes.nested}
                                                        classes={{
                                                            selected: classes.selected,
                                                        }}
                                                        selected={
                                                            selectedIndexA === indexA && selectedIndexB === indexB
                                                        }
                                                        onClick={(event) => handleListItemClick(event, doc)}
                                                        key={indexB}
                                                    >
                                                        <ListItemIcon classes={{ root: classes.listItemRoot }}>
                                                            <>
                                                                {doc.sourceType === 'MARKDOWN' && <Icon>code</Icon>}
                                                                {doc.sourceType === 'INLINE' && (
                                                                    <Icon>description</Icon>
                                                                )}
                                                                {doc.sourceType === 'URL' && <Icon>open_in_new</Icon>}
                                                                {doc.sourceType === 'FILE' && (
                                                                    <Icon>arrow_downward</Icon>
                                                                )}
                                                            </>
                                                        </ListItemIcon>
                                                        <ListItemText
                                                            inset
                                                            primary={doc.name}
                                                            classes={{ root: classes.docLinkRoot }}
                                                            aria-label={'View ' + doc.name + ' document'}
                                                        />
                                                    </ListItem>
                                                ))}
                                            </List>
                                        )}
                                    </React.Fragment>
                                ))}
                            </List>
                        </div>
                    </div>
                )}
                <div className={classes.toggleWrapper}>
                    <Button
                        className={classes.toggler}
                        onClick={toggleDocList}
                        aria-label='Toggle the document list'
                    >
                        <div className={classes.togglerTextParent}>
                            <div className={classes.togglerText}>
                                {showDocList ? (
                                    <FormattedMessage
                                        id='Apis.Details.Documents.Documentation.hide'
                                        defaultMessage='HIDE'
                                    />
                                ) : (
                                    <FormattedMessage
                                        id='Apis.Details.Documents.Documentation.show'
                                        defaultMessage='SHOW'
                                    />
                                )}
                            </div>
                        </div>
                        {showDocList ? <Icon>keyboard_arrow_left</Icon> : <Icon>keyboard_arrow_right</Icon>}
                    </Button>
                </div>
                <div className={classes.docView}>
                    {selectedDoc && (
                        <Details
                            documentList={documentList}
                            selectedDoc={selectedDoc}
                            apiId={apiId}
                        />
                    )}
                </div>
            </div>
        </>
    );
}

DocList.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(DocList));
