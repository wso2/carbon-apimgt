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
import {withStyles, withTheme} from '@material-ui/core/styles';
import {FormattedMessage} from 'react-intl';
import {injectIntl} from 'react-intl';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Box from '@material-ui/core/Box';

import PropTypes from 'prop-types';
import Api from 'AppData/api';
import {ApiContext} from "./ApiContext";

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
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
    tcell: {
        padding: '0x',
    }
});

class Topics extends React.Component {
    /**
     *Creates an instance of Topics.
     * @param {*} props
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
     *
     *
     * @memberof Topics
     */
    componentDidMount() {
        const {api} = this.props;
        let promisedApi = null;

        const apiClient = new Api();
        promisedApi = apiClient.getAllTopics(api.id);

        promisedApi
            .then((response) => {
                if (response.obj.list !== undefined) {
                    this.setState({topics: response.obj.list});
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                } else if (status === 401) {
                    this.setState({isAuthorize: false});
                    const params = qs.stringify({reference: this.props.location.pathname});
                    this.props.history.push({pathname: '/login', search: params});
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof Resources
     */
    render() {

        const { topics } = this.state;
        if (this.state.notFound) {
            return <div>resource not found...</div>;
        }
        if (!topics) {
            return <div>loading...</div>;
        }
        const { classes } = this.props;
        return (
            <Box display='flex' flexDirection='row' style={{minHeight:'200px'}}>
                <Table>
                    {topics && topics.length !== 0 && topics.map(topic => (
                        <TableRow style={{borderStyle: 'hidden', padding:'0px'}} key={topic.name}>
                            <TableCell style={{padding:'0px'}}>
                                <Typography component='p' variant='body2'>
                                    {topic.name}
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
    classes: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(Topics));
