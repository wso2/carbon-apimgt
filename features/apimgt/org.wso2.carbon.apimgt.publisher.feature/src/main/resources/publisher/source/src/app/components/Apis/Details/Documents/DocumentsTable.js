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
import {Table, Popconfirm} from 'antd';

class DocumentsTable extends Component {
    constructor(props) {
        super(props);
        this.columns = [{
            title: 'Name',
            dataIndex: 'name',
            key: 'name'
        }, {
            title: 'Source',
            dataIndex: 'sourceType',
            key: 'sourceType'
        }, {
            title: 'Actions',
            dataIndex: 'actions',
            key: 'actions',
            render: (text1, record) => (<div>
                <a href="#" onClick={() => this.props.onEditAPIDocument(record)}>Edit | </a>
                <a href="#" onClick={() => this.props.viewDocContentHandler(record)}>View | </a>
                <Popconfirm title="Are you sure you want to delete this document?"
                            onConfirm={() => this.props.deleteDocHandler(record.documentId)}
                            okText="Yes" cancelText="No">
                    <a href="#">Delete</a>
                </Popconfirm>
            </div>)
        }
        ];
        //TODO: Add permission/valid scope checks for document Edit/Delete actions
    }

    render() {

        return (
            <div style={{paddingTop: 20}}>
                <h3 style={{paddingBottom: 15}}>Current Documents</h3>
                <Table dataSource={ this.props.documentsList } columns={this.columns}/>
            </div>
        );
    }
}

export default DocumentsTable;