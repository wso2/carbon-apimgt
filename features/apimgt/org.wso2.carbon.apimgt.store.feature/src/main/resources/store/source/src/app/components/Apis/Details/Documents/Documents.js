/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, {Component} from 'react'
import API from '../../../../data/api.js'
import {Button, message} from 'antd';
import DocumentsTable from './DocumentsTable';
import Loading from '../../../Base/Loading/Loading'
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper'


class Documents extends Component {
    constructor(props) {
        super(props);
	this.client = new API();
        this.state = {
	    api: null,
            documentsList: null,
        };
        this.api_id = this.props.match.params.api_uuid;
        this.initialDocSourceType = null;
    }

    componentDidMount() {
        const api = new API();
        let promised_api = api.getDocumentsByAPIId(this.api_id);
        promised_api.then(
            response => {
		console.info(response.obj)
                this.setState({documentsList: response.obj.list});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

       downloadFile(response) {
        let fileName = "";
        const contentDisposition = response.headers["content-disposition"];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers["content-type"];
        const blob = new Blob([response.data], {
            type: contentType
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement("a");
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

            setTimeout(function () {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }


    render() {
        if (!this.state.documentsList) {
            return <Loading/>
        }
        return (
            <div>
                <hr color="#f2f2f2"/>
                {
                    (this.state.documentsList && (this.state.documentsList.length > 0) ) ? (
                        <DocumentsTable apiId={this.api_id}
                                        client={this.client}
                                        documentsList={this.state.documentsList}
                                        viewDocContentHandler={this.viewDocContentHandler}
                                        downloadFile={this.downloadFile}
                        /> ) :
	    (<Grid container style={{paddingLeft:"40px"}}>
                    <Grid item xs={12} sm={6} md={9} lg={9} xl={10} >
                        <Paper style={{paddingLeft:"40px"}}>
            <div style={{paddingTop: 20}}>
                <h3 style={{paddingBottom: 15}}>No documents added into the API</h3>
            </div>
                        </Paper>
                    </Grid>
                </Grid>)
             }
            </div>
        );
    }
}

export default Documents;
