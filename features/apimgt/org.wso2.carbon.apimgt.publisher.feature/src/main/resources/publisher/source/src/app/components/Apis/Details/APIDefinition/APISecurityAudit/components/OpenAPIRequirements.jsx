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
import PropTypes from 'prop-types';

/**
 * Function to display the OpenAPI Requirement component
 * @param {*} props props
 * @returns {*} OpenAPIRequirements component
 */
export default function OpenAPIRequirements(props) {
    const {
        classes,
        reportObject,
        errorColumns,
        options,
        getRowData,
        getErrorMuiTheme,
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
                                    <MuiThemeProvider theme={getErrorMuiTheme()}>
                                        <MUIDataTable
                                            title='Semantic Errors'
                                            data={getRowData(
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
                                    <MuiThemeProvider theme={getErrorMuiTheme()}>
                                        <MUIDataTable
                                            title='Structural Errors'
                                            data={getRowData(
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

OpenAPIRequirements.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    // TODO - Check if these proptypes are required and whether they are correct
    reportObject: PropTypes.shape({}).isRequired,
    errorColumns: PropTypes.shape({}).isRequired,
    options: PropTypes.shape({}).isRequired,
    getRowData: PropTypes.shape({}).isRequired,
    getErrorMuiTheme: PropTypes.shape({}).isRequired,
};
