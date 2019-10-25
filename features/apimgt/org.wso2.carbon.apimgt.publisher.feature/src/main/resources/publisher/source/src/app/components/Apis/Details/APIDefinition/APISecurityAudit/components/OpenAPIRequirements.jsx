/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { MuiThemeProvider } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';

/**
 * Function to display the OpenAPI Requirement component
 * @returns {*} OpenAPIRequirements component
 */
export default function OpenAPIRequirements() {
    const {
        classes,
        reportObject,
    } = props;

    return (
        <div className={classes.paperDiv}>
            <Paper elevation={1} className={classes.rootPaper}>
                <div>
                    <Typography variant='h5' className={classes.sectionHeadingTypography}>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.OpenApiFormatRequirements'
                            defaultMessage='OpenAPI Format Requirements'
                        />
                    </Typography>
                    {{}.hasOwnProperty.call(reportObject, 'semanticErrors') &&
                        <React.Fragment>
                            <div>
                                <Typography variant='body1'>
                                    <MuiThemeProvider theme={this.getMuiTheme()}>
                                        <MUIDataTable
                                            title='Semantic Errors'
                                            data={this.getRowData(
                                                reportObject.semanticErrors.issues,
                                                'OpenAPI Format Requirements',
                                                'error',
                                            )}
                                            columns={errorColumns}
                                            options={options}
                                        />
                                    </MuiThemeProvider>
                                </Typography>
                            </div>
                        </React.Fragment>
                    }
                    {{}.hasOwnProperty.call(reportObject, 'validationErrors') &&
                        <React.Fragment>
                            <div>
                                <Typography variant='body1'>
                                    <MuiThemeProvider theme={this.getErrorMuiTheme()}>
                                        <MUIDataTable
                                            title='Structural Errors'
                                            data={this.getRowData(
                                                reportObject.validationErrors.issues,
                                                'OpenAPI Format Requirements',
                                                'error',
                                            )}
                                            columns={errorColumns}
                                            options={options}
                                        />
                                    </MuiThemeProvider>
                                </Typography>
                            </div>
                        </React.Fragment>
                    }
                    {!{}.hasOwnProperty.call(reportObject, 'validationErrors') &&
                    !{}.hasOwnProperty.call(reportObject, 'semanticErrors') &&
                    <Typography variant='body1'>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.OASNoIssuesFound'
                            defaultMessage='No Issues Found'
                        />
                    </Typography>
                    }
                </div>
            </Paper>
        </div>
    );
}