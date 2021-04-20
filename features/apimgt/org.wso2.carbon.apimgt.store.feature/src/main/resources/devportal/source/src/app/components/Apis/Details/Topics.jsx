/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl } from 'react-intl';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Box from '@material-ui/core/Box';
import Alert from 'AppComponents/Shared/Alert';
import Progress from 'AppComponents/Shared/Progress';
import PropTypes from 'prop-types';
import Api from 'AppData/api';
import { ApiContext } from './ApiContext';

const styles = (theme) => ({
    root: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    heading: {
        marginRight: 20,
        color: theme.palette.getContrastText(theme.custom.infoBar.sliderBackground),
    },
});

/**
 * Render topics component
 */
class Topics extends React.Component {
    /**
     *Creates an instance of Topics.
     * @param {JSON} props props passed from it's parent
     * @memberof Topics
     */
    constructor(props) {
        super(props);
        this.state = {
            topics: null,
        };
        this.api = new Api();
    }

    /**
     * @memberof Topics
     */
    componentDidMount() {
        const { api, intl } = this.props;
        const apiClient = new Api();
        const promisedApi = apiClient.getAllTopics(api.id);

        promisedApi
            .then((response) => {
                if (response.obj.list !== undefined) {
                    this.setState({ topics: response.obj.list });
                }
            })
            .catch((error) => {
                const { status } = error;
                if (status === 404) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Topics.error.404',
                        defaultMessage: 'Resource not found',
                    }));
                    console.log(error);
                } else if (status === 401) {
                    Alert.error(error);
                    console.log(error);
                }
            });
    }

    /**
     * @returns {JSX} rendered output
     * @memberof Topics
     */
    render() {
        const { topics } = this.state;
        if (!topics) {
            return <Progress />;
        }
        return (
            <Box display='flex' flexDirection='row'>
                <Table>
                    {topics && topics.length !== 0 && topics.map((topic) => (
                        <TableRow style={{ borderStyle: 'hidden', padding: '0px' }} key={topic.name}>
                            <TableCell style={{ padding: '0px' }}>
                                <Typography component='p' variant='body2'>
                                    <b>{topic.name}</b>
                                    {' -'}
                                    {topic.type.toLowerCase()}
                                </Typography>
                            </TableCell>
                        </TableRow>
                    ))}
                </Table>
            </Box>
        );
    }
}

Topics.contextType = ApiContext;

Topics.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(Topics));
