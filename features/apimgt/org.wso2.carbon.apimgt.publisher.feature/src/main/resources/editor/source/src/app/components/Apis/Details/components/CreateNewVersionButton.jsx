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

import LibraryAdd from '@material-ui/icons/LibraryAdd';
import PropTypes from 'prop-types';
import { Link, withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';

const styles = (theme) => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
    },
    backLink: {
        alignItems: 'center',
        textDecoration: 'none',
        display: 'flex',
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    backText: {
        color: theme.palette.primary.main,
        cursor: 'pointer',
        fontFamily: theme.typography.fontFamily,
    },
    createNewVersionWrapper: {
        display: 'flex',
        justifyContent: 'flex-end',
    },
    createNewVersion: {
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        color: theme.custom.createNewVersionButtonColor || 'inherit',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
});

/**
 *
 * Function to create a 'CreateNewVersion' button
 *
 * @param {any} props props
 * @returns {*} React CreateNewVersion function component
 * @constructor
 */
function CreateNewVersionButton(props) {
    const { api, classes } = props;
    return (
        <>
            {/* allowing create new version based on scopes */}
            <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.API_COPY}>
                <div className={classes.createNewVersionWrapper}>
                    <VerticalDivider height={70} />
                    <Link
                        className={classes.createNewVersion}
                        to={'/apis/' + api.id + '/new_version'}
                        style={{ minWidth: 95 }}
                    >

                        <div>
                            <LibraryAdd />
                        </div>
                        <Typography variant='caption'>
                            <FormattedMessage
                                id='Apis.Details.components.CreateNewVersionButton.create.new.version'
                                defaultMessage='Create New Version'
                            />
                        </Typography>
                    </Link>
                </div>
            </ScopeValidation>
        </>
    );
}

CreateNewVersionButton.propTypes = {
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withRouter(withStyles(styles, { withTheme: true })(CreateNewVersionButton));
