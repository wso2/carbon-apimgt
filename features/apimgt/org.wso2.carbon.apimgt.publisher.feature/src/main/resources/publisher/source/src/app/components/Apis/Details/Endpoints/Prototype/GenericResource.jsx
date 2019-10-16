/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React, { useContext, useEffect } from 'react';
import { isRestricted } from 'AppData/AuthManager';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import {
    Chip,
    ExpansionPanel,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    Grid,
    Typography,
    makeStyles,
} from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import * as MonacoEditor from 'monaco-editor';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Utils from 'AppData/Utils';

/**
 * The generic resource component.
 *
 * @param {any} props The input props
 * @return {any} The HTML representation of the component.
 * */
function GenericResource(props) {
    const {
        resourcePath, resourceMethod, scriptContent, onChange,
    } = props;
    const { api } = useContext(APIContext);
    const editorRef = React.createRef();
    let editor = null;
    const useStyles = makeStyles((theme) => {
        let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[resourceMethod] : null;
        let chipTextColor = '#000000';
        if (!chipColor) {
            console.log('Check the theme settings. The resourceChipColors is not populated properlly');
            chipColor = '#cccccc';
        } else {
            chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[resourceMethod]);
        }

        return ({
            editor: {
                width: '100%',
                height: '500px',
            },
            chipActive: {
                borderRadius: '5px',
                width: '80%',
                backgroundColor: chipColor,
                color: chipTextColor,
            },
            resourcePathContainer: {
                paddingTop: theme.spacing(),
            },
            prototypeResourceHeader: {
                borderBottom: `${chipColor} 1px solid`,
            },
            chipExpansionPanel: {
                backgroundColor: Utils.hexToRGBA(chipColor, 0.1),
                border: `${chipColor} 1px solid`,
            },
        });
    });

    const classes = useStyles();
    useEffect(() => {
        editor = MonacoEditor.editor.create(editorRef.current, {
            value: scriptContent,
            language: 'javascript',
            wordWrap: 'on',
            automaticLayout: true,
            readOnly: isRestricted(['apim:api_create'], api),
        });
        editor.onDidChangeModelContent(() => {
            onChange(editor.getValue(), resourcePath, resourceMethod);
        });
    }, []);
    return (
        <Grid item>
            <ExpansionPanel className={classes.chipExpansionPanel}>
                <ExpansionPanelSummary
                    className={classes.prototypeResourceHeader}
                    expandIcon={<ExpandMoreIcon />}
                >
                    <Grid container spacing={12}>
                        <Grid xs={1}>
                            <Chip
                                label={resourceMethod}
                                className={classes.chipActive}
                            />
                        </Grid>
                        <Grid xs className={classes.resourcePathContainer}>
                            <Typography>
                                {resourcePath}
                            </Typography>
                        </Grid>
                    </Grid>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails className={classes.genericResourceContent}>
                    <Grid container direction='column'>
                        <Grid item>
                            <Typography variant='subtitle2'>
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.Prototype.InlineEndpoints.script'
                                    defaultMessage='Script'
                                />
                            </Typography>
                        </Grid>
                        <Grid item>
                            <div ref={editorRef} className={classes.editor} />
                        </Grid>
                    </Grid>
                </ExpansionPanelDetails>
            </ExpansionPanel>
        </Grid>
    );
}

GenericResource.propTypes = {
    resourcePath: PropTypes.string.isRequired,
    resourceMethod: PropTypes.string.isRequired,
    scriptContent: PropTypes.string.isRequired,
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    onChange: PropTypes.func.isRequired,
};

export default GenericResource;
