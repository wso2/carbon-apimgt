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
    paper: {
        padding: theme.spacing(2),
        color: theme.palette.text.secondary,
        minHeight: 400,
        position: 'relative',
    },
    paperMenu: {
        color: theme.palette.text.secondary,
        minHeight: 400 + theme.spacing(4),
        height: '100%',
    },
    docContent: {
        paddingTop: theme.spacing(1),
    },
    parentListItem: {
        borderTop: 'solid 1px #ccc',
        borderBottom: 'solid 1px #ccc',
        color: theme.palette.grey[100],
        background: theme.palette.grey[100],
        cursor: 'default',
    },
    listRoot: {
        paddingTop: 0,
    },
    nested: {
        paddingLeft: theme.spacing(3),
        paddingTop: 3,
        paddingBottom: 3,
    },
    childList: {
        paddingTop: 0,
        marginTop: 0,
        paddingBottom: 0,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
        paddingTop: theme.spacing(3),
        paddingRight: theme.spacing(3),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    generateCredentialWrapper: {
        marginLeft: 0,
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    genericMessageWrapper: {
        margin: theme.spacing(2),
    },
    typeText: {
        color: '#000',
    },
    docLinkRoot: {
        paddingLeft: 0,
    },
    toggler: {
        height: '100%',
        paddingTop: 20,
        cursor: 'pointer',
        marginLeft: '-20px',
        display: 'block',
    },
    togglerTextParent: {
        writingMode: 'vertical-rl',
        transform: 'rotate(180deg)',
    },
    togglerText: {
        textOrientation: 'sideways',
    },
    toggleWrapper: {
        position: 'relative',
        background: '#fff9',
        paddingLeft: 20,
    },
    docsWrapper: {
        margin: 0,
    },
    docContainer: {
        display: 'flex',
        marginLeft: theme.spacing(3),
        marginRight: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
    docListWrapper: {
        width: 285,
    },
    docView: {
        flex: 1,
    },
    listItemRoot: {
        minWidth: 30,
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
        this.state = { spec: '' };
    }

    componentDidMount() {
        const context = this.context;
        this.setState({ spec: context.api.apiDefinition });
    }

    render() {
        const { spec } = this.state;
        const { classes } = this.props;
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
                        <AsyncApiComponent schema={spec} />
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
