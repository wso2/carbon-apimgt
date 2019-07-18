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
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import PeopleOutlineIcon from '@material-ui/icons/PeopleOutline';
import ApiContext from '../components/ApiContext';

function Lifecycle(props) {
    const { parentClasses } = props;
    return (
        <ApiContext.Consumer>
            {({ api, isAPIProduct }) => (
                <Paper className={classNames({ [parentClasses.root]: true })}>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.Lifecycle.lifecycle.status'
                                defaultMessage='Lifecycle Status'
                            />
                        </Typography>
                        <Link to={'/apis/' + api.id + '/lifecycle'}>
                            <Button variant='contained' color='default'>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Lifecycle.edit'
                                    defaultMessage='Edit'
                                />
                            </Button>
                        </Link>
                    </div>

                    {/* LifeCycle Status */}
                    <Typography
                        variant='h5'
                        className={classNames({
                            [parentClasses.subtitle]: true,
                            [parentClasses.lifecycleWrapper]: true,
                        })}
                    >
                        <PeopleOutlineIcon className={parentClasses.lifecycleIcon} />
                        {isAPIProduct ? api.state : api.lifeCycleStatus}
                    </Typography>
                </Paper>
            )}
        </ApiContext.Consumer>
    );
}

Lifecycle.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
};

export default Lifecycle;
