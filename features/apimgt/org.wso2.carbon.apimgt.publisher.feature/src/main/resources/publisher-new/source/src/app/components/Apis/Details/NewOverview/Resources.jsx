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
import { withStyles, withTheme } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import { Link } from 'react-router-dom';

import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import Api from 'AppData/api';

function RenderMethodBase(props) {
    const { theme, method } = props;
    let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[method] : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[method]);
    }
    return <Chip label={method} style={{ backgroundColor: chipColor, color: chipTextColor, height: 20 }} />;
}

RenderMethodBase.propTypes = {
    classes: PropTypes.object.isRequired,
};

const RenderMethod = withTheme()(RenderMethodBase);

const styles = {
    root: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    heading: {
        marginRight: 20,
    },
};

class Resources extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            paths: null,
            swagger: {},
        };
        this.restApi = new Api();
    }

    componentDidMount() {
        this.api_uuid = this.props.api.id;
        const promised_api = this.restApi.getSwagger(this.api_uuid);
        promised_api
            .then((response) => {
                if (response.obj.paths !== undefined) {
                    this.setState({ paths: response.obj.paths });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    const params = JSON.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    render() {
        const { paths } = this.state;
        if (this.state.notFound) {
            return <div>resource not found...</div>;
        }
        if (!paths) {
            return <div>loading...</div>;
        }
        const { classes, parentClasses, api } = this.props;

        return (
                    <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
                        {console.info("api....", api)}
                        <div className={parentClasses.titleWrapper}>
                            <Typography variant='h5' component='h3' className={parentClasses.title}>
                                Resources
                            </Typography>
                            <Link to={'/apis/' + api.id + '/resources'}>
                                <Button variant='contained' color='default'>
                                    Edit
                                </Button>
                            </Link>
                        </div>
                        <div className={classes.root}>
                            <div className={classes.contentWrapper}>
                                {Object.keys(paths).map((key) => {
                                    const path = paths[key];
                                    return (
                                        <div className={classes.root}>
                                            <Typography className={classes.heading} variant='body1'>
                                                {key}
                                            </Typography>
                                            {Object.keys(path).map((innerKey) => {
                                                return <RenderMethod method={innerKey} />;
                                            })}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    </Paper>
        );
    }
}
Resources.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    method: PropTypes.string.isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    parentClasses: PropTypes.object.isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Resources);
