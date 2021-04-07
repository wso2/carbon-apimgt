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

import React, { Component } from 'react';
import AsyncApiComponent from '@asyncapi/react-component';
import '@asyncapi/react-component/lib/styles/fiori.css';
import { FormattedMessage, injectIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import { ApiContext } from '../../ApiContext';

const styles = (theme) => ({
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    editorPane: {
        width: '100%',
        maxHeight: '100vh',
        minHeight: '100vh',
        overflow: 'auto',
    },
    editorRoot: {
        height: '100%',
    },
});


class AsyncApiDefinitionUI extends Component {
    static contextType = ApiContext;

    constructor(props) {
        super(props);
    }

    render() {
        const { classes } = this.props;
        // Avoid rendering the 'servers' portion from the AsyncAPI definition.
        const asyncApiDefinition = JSON.parse(this.context.api.apiDefinition);
        delete asyncApiDefinition.servers;
        return (
            <>
                <Typography variant='h4' className={classes.titleSub}>
                    <FormattedMessage
                        id='Apis.Details.Async.Definition.title'
                        defaultMessage='AsyncAPI Specification'
                    />
                </Typography>
                <Grid container spacing={1} className={classes.editorRoot}>
                    <Grid item className={classes.editorPane}>
                        <AsyncApiComponent schema={JSON.stringify(asyncApiDefinition)} />
                    </Grid>
                </Grid>
            </>
        );
    }
}

AsyncApiDefinitionUI.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(AsyncApiDefinitionUI));
