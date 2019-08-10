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

import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SampleAPI from './SampleAPI/SampleAPI';
import CardView from './CardView/CardView';
import TableView from './TableView/TableView';
import TopMenu from './components/TopMenu';

const styles = theme => ({
    content: {
        flexGrow: 1,
    },
    contentInside: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 2,
    },
});
/**
 * Render the APIs Listing page, This is the Default Publisher Landing page as well
 *
 * @class Listing
 * @extends {React.Component}
 */
class Listing extends React.Component {
    /**
     *Creates an instance of Listing.
     * @param {*} props
     * @memberof Listing
     */
    constructor(props) {
        super(props);
        this.state = {
            apis: null,
        };
        this.state.listType = this.props.theme.custom.defaultApiView;
    }

    /**
     *
     * Switch the view between grid and list view
     * @param {String} value UUID(ID) of the deleted API
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };
    /**
     * Update Sample API
     *
     * @param {String} apiUUID
     * @memberof Listing
     */
    updateApi(apiUUID) {
        const { apis } = this.state;
        for (const apiIndex in apis.list) {
            if (apis.list.apiIndex && apis.list[apiIndex].id === apiUUID) {
                apis.list.splice(apiIndex, 1);
                break;
            }
        }
        this.setState({ apis });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Listing
     */
    render() {
        const { listType } = this.state;
        const {
            apis, notFound, classes, updateAPIsList,
        } = this.props;
        if (notFound) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <ResourceNotFound />
                    </div>
                </main>
            );
        }
        if (!apis) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <Progress />
                    </div>
                </main>
            );
        }
        if (apis.list.length === 0) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <SampleAPI />
                    </div>
                </main>
            );
        }

        return (
            <main className={classes.content}>
                <TopMenu setListType={this.setListType} apis={apis} />
                <div className={classes.contentInside}>
                    {listType === 'grid' ? (
                        <CardView updateAPIsList={updateAPIsList} apis={apis} />
                    ) : (
                        <TableView updateAPIsList={updateAPIsList} apis={apis} />
                    )}
                </div>
            </main>
        );
    }
}

Listing.propTypes = {
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
    classes: PropTypes.shape({
        content: PropTypes.string,
        contentInside: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
    apis: PropTypes.shape({ list: PropTypes.array, count: PropTypes.number, apiType: PropTypes.string }).isRequired,
    notFound: PropTypes.bool.isRequired,
    updateAPIsList: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
