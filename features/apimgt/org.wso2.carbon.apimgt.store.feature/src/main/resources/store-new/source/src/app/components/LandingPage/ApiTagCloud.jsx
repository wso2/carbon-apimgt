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
import { TagCloud } from 'react-tagcloud';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import ApiThumb from 'AppComponents/Apis/Listing/ApiThumb';

/**
 * @returns {Object}
 */
const styles = {
    tagedApisWrapper: {
        display: 'flex',
        flexDirection: 'row',
        flexWrap: 'wrap',
    },
};

/**
 * Component used to handle API Tag Cloud
 * @class ApiTagCloud
 * @extends {React.Component}
 * @param {any} value @inheritDoc
 */
class ApiTagCloud extends React.Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            allTags: null,
            apis: null,
            selectedTag: null,
        };
        this.handleOnClick = this.handleOnClick.bind(this);
    }

    /**
     * @memberof ApiTagCloud
     */
    componentDidMount() {
        const api = new API();
        const promisedTags = api.getAllTags();
        promisedTags
            .then((response) => {
                this.setState({ allTags: response.body.list });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     *
     * @param {String} tag selected tag
     * @memberof ApiTagCloud
     */
    handleOnClick = (tag) => {
        const api = new API();
        const promisedApis = api.getAllAPIs({ query: 'tag:' + tag.value });
        promisedApis
            .then((response) => {
                this.setState({ apis: response.obj, selectedTag: tag.value });
            })
            .catch((error) => {
                Alert('Error retrieving APIs for tag ' + tag.value);
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    };

    /**
     * @returns {React.Component}
     * @memberof ApiTagCloud
     */
    render() {
        const { classes } = this.props;
        const { allTags, apis, selectedTag } = this.state;
        const options = {
            luminosity: 'light',
            hue: 'blue',
        };
        return (
            <div>
                <h1>Tags</h1>
                {allTags && (
                    <TagCloud
                        minSize={12}
                        maxSize={35}
                        colorOptions={options}
                        tags={allTags}
                        onClick={tag => this.handleOnClick(tag)}
                    />
                )}
                { apis && (
                    <div>
                        <h1>
                            {' ('}
                            {selectedTag}
                            {') '}
                        </h1>
                        <div className={classes.tagedApisWrapper}>
                            {apis.list.map(api => (
                                <ApiThumb api={api} key={api.id} />
                            ))}
                        </div>
                    </div>
                )}
            </div>
        );
    }
}

ApiTagCloud.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiTagCloud);
