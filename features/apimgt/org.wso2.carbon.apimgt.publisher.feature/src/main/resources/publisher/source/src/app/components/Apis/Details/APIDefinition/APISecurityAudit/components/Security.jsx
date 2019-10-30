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
import Tooltip from '@material-ui/core/Tooltip';
import Button from '@material-ui/core/Button';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { MuiThemeProvider } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import PropTypes from 'prop-types';

/**
  * Function to render the Security Component
  * @param {*} props props
  * @returns {*} Security component
  */
export default function Security(props) {
    const {
        classes,
        reportObject,
        criticalityObject,
        columns,
        options,
        getRowData,
        getMuiTheme,
    } = props;

    return (
        <div className={classes.paperDiv}>
            <Paper elevation={1} className={classes.rootPaper}>
                <div>
                    <Typography variant='h5' className={classes.sectionHeadingTypography}>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.Security'
                            defaultMessage='Security'
                        />
                    </Typography>
                    <Typography variant='body1'>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.SecurityNumOfIssues'
                            defaultMessage='{securityNumOfIssuesText} {securityNumOfIssues}'
                            values={{
                                securityNumOfIssuesText: (
                                    <strong>Number of Issues:</strong>
                                ),
                                securityNumOfIssues: reportObject.security.issueCounter,
                            }}
                        />
                    </Typography>
                    <Typography variant='body1'>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.SecurityScore'
                            defaultMessage='{securityScoreText} {securityScore}  / 30'
                            values={{
                                securityScoreText: (
                                    <strong>Score:</strong>
                                ),
                                securityScore: (
                                    (Math.round(reportObject.security.score * 100) / 100)
                                ),
                            }}
                        />
                    </Typography>
                    <React.Fragment>
                        <Typography variant='body1'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.AuditApi.securityCriticality'
                                defaultMessage='{securityCriticalityText} {securityCriticality}'
                                values={{
                                    securityCriticalityText: (
                                        <strong>Severity:</strong>
                                    ),
                                    securityCriticality: (
                                        criticalityObject[reportObject.security.criticality]
                                    ),
                                }}
                            />
                            <Tooltip
                                placement='right'
                                classes={{
                                    tooltip: classes.htmlTooltip,
                                }}
                                title={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.AuditApi.tooltip'
                                            defaultMessage={
                                                'Severity ranges from INFO, LOW, MEDIUM, HIGH ' +
                                                    'to CRITICAL, with INFO being ' +
                                                    'low vulnerability and CRITICAL' +
                                                    'being high vulnerability'
                                            }
                                        />
                                    </React.Fragment>
                                }
                            >
                                <Button className={classes.helpButton}>
                                    <HelpOutline className={classes.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </React.Fragment>
                    {(reportObject.security.issueCounter !== 0) &&
                        <div>
                            <hr />
                            <Typography variant='body1'>
                                <MuiThemeProvider theme={getMuiTheme()}>
                                    <MUIDataTable
                                        title='Issues'
                                        data={getRowData(
                                            reportObject.security.issues,
                                            'Security',
                                            'normal',
                                        )}
                                        columns={columns}
                                        options={options}
                                    />
                                </MuiThemeProvider>
                            </Typography>
                        </div>
                    }
                </div>
            </Paper>
        </div>
    );
}

Security.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    reportObject: PropTypes.shape({}).isRequired,
    criticalityObject: PropTypes.shape({}).isRequired,
    columns: PropTypes.shape({}).isRequired,
    options: PropTypes.shape({}).isRequired,
    getRowData: PropTypes.shape({}).isRequired,
    getMuiTheme: PropTypes.shape({}).isRequired,
};
