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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import ApiTableView from './ApiTableView';

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
            selectedTag: null,
            clicked: false,
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
                if (response.body.count !== 0) {
                    this.setState({ allTags: response.body.list });
                }
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
        this.setState({ clicked: true, selectedTag: tag.value });
    };

    /**
     * @returns {React.Component}
     * @memberof ApiTagCloud
     */
    render() {
        const { classes } = this.props;
        const { allTags, selectedTag, clicked } = this.state;
        const options = {
            luminosity: 'light',
            hue: 'blue',
        };
        return (
            <div>
                {allTags && (
                    <div>
                        <Typography variant='display1' className={classes.mainTitle}>
                            <FormattedMessage defaultMessage='Tags' id='Apis.Listing.ApiTagCloud.tags.heading' />
                        </Typography>
                        <TagCloud
                            minSize={12}
                            maxSize={35}
                            colorOptions={options}
                            tags={allTags}
                            onClick={tag => this.handleOnClick(tag)}
                        />
                    </div>
                )}
                {clicked && (
                    <div>
                        <h1>
                            {' ('}
                            {selectedTag}
                            {') '}
                        </h1>
                        <div className={classes.tagedApisWrapper}>
                            <ApiTableView tagSelected selectedTag={selectedTag} gridView />
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
