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
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import List, { ListItem, ListItemText } from '@material-ui/core/List';
import Avatar from '@material-ui/core/Avatar';
import Typography from '@material-ui/core/Typography';
import { Link, FileDownload } from '@material-ui/icons';
import ExpansionPanel, { ExpansionPanelSummary, ExpansionPanelDetails } from '@material-ui/core/ExpansionPanel';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import API from '../../../../data/api';
import Loading from '../../../Base/Loading/Loading';
import DocumentView from './DocumentView';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        width: '100%',
        paddingTop: 10,
    },
    summary: {
        textDecoration: 'none',
        display: 'flex',
        paddingLeft: 0,
        cursor: 'pointer',
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightRegular,
    },
    listItem: {
        paddingLeft: 0,
    },
});
/**
 *
 *
 * @class Documentation
 * @extends {React.Component}
 */
class Documentation extends React.Component {
    /**
     *Creates an instance of Documentation.
     * @param {*} props
     * @memberof Documentation
     */
    constructor(props) {
        super(props);
        this.client = new API();
        this.state = {
            api: null,
            documentsList: null,
        };
        this.api_id = this.props.match.params.api_uuid;
        this.initialDocSourceType = null;
        this.viewDocContentHandler = this.viewDocContentHandler.bind(this);
    }

    /**
     *
     *
     * @memberof Documentation
     */
    componentDidMount() {
        const promised_api = this.client.getDocumentsByAPIId(this.api_id);
        promised_api
            .then((response) => {
                const types = [];
                if (response.obj.list.length > 0) {
                    // Rearanging the response to group them by the sourceType property.
                    const allDocs = response.obj.list;
                    for (let i = 0; i < allDocs.length; i++) {
                        const selectedType = allDocs[i].type;
                        let hasType = false;
                        for (let j = 0; j < types.length; j++) {
                            if (selectedType === types[j].docType) {
                                types[j].docs.push(allDocs[i]);
                                hasType = true;
                            }
                        }
                        if (!hasType) {
                            // Adding a new type entry
                            types.push({
                                docType: selectedType,
                                docs: [allDocs[i]],
                            });
                        }
                    }
                }

                this.setState({ documentsList: types });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @param {*} summary
     * @returns
     * @memberof Documentation
     */
    truncateSummary(summary) {
        let newSummery = summary;
        const maxCount = 200;
        if (summary.length > maxCount && summary.length > maxCount + 5) {
            newSummery = summary.substring(1, 200) + ' ... ';
        }
        return newSummery;
    }

    /**
     *
     *
     *    On click listener for 'View' link on each document related row in the documents table.
     *    1- If the document type is 'URL' open it in new tab
     *    2- If the document type is 'INLINE' open the content with an inline editor
     *    3- If the document type is 'FILE' download the file
     *
     * @param {*} doc
     * @memberof Documentation
     */
    viewDocContentHandler(doc) {
        const promised_get_content = this.client.getFileForDocument(this.api_id, doc.documentId);
        promised_get_content
            .then((done) => {
                this.downloadFile(done);
            })
            .catch((error_response) => {
                throw error_response;
                const error_data = JSON.parse(error_response.data);
                const messageTxt = 'Error[' + error_data.code + ']: ' + error_data.description + ' | ' + error_data.message + '.';
                console.error(messageTxt);
            });
    }

    /**
     *
     *
     * @param {*} response
     * @memberof Documentation
     */
    downloadFile(response) {
        let fileName = '';
        const contentDisposition = response.headers['content-disposition'];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers['content-type'];
        const blob = new Blob([response.data], {
            type: contentType,
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement('a');
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }

    /**
     *
     *
     * @returns
     * @memberof Documentation
     */
    render() {
        const { classes } = this.props;
        if (!this.state.documentsList) {
            return <Loading />;
        }
        return (
            <div className={classes.root}>
                {this.state.documentsList && this.state.documentsList.length > 0 && (
                    <div>
                        {this.state.documentsList.map(item => (
                            <div key={item.docType}>
                                <ExpansionPanel defaultExpanded>
                                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                                        <Typography className={classes.heading}>{item.docType}</Typography>
                                    </ExpansionPanelSummary>
                                    <ExpansionPanelDetails>
                                        <List>
                                            {item.docs.map(doc => (
                                                <ListItem key={doc.documentId} className={classes.listItem}>
                                                    {doc.sourceType === 'INLINE' && <DocumentView doc={doc} truncateSummary={this.truncateSummary(doc.summary)} />}
                                                    {doc.sourceType === 'FILE' && (
                                                        <a onClick={() => this.viewDocContentHandler(doc)} className={classes.summary}>
                                                            <Avatar>
                                                                <FileDownload />
                                                            </Avatar>
                                                            <ListItemText primary={doc.name} secondary={this.truncateSummary(doc.summary)} />
                                                        </a>
                                                    )}
                                                    {doc.sourceType === 'URL' && (
                                                        <a href={doc.sourceUrl} target='_blank' className={classes.summary}>
                                                            <Avatar>
                                                                <Link />
                                                            </Avatar>
                                                            <ListItemText primary={doc.name} secondary={this.truncateSummary(doc.summary)} />
                                                        </a>
                                                    )}
                                                </ListItem>
                                            ))}
                                        </List>
                                    </ExpansionPanelDetails>
                                </ExpansionPanel>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        );
    }
}

Documentation.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Documentation);
