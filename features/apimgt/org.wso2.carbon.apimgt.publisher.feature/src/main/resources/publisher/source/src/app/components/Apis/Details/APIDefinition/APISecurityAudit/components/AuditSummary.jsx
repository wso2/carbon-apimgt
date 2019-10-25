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
import { CircularProgressbarWithChildren } from 'react-circular-progressbar';
import VisibilitySensor from 'react-visibility-sensor';
import { Line } from 'rc-progress';
import { FormattedMessage } from 'react-intl';
import Tooltip from '@material-ui/core/Tooltip';
import Button from '@material-ui/core/Button';
import HelpOutline from '@material-ui/icons/HelpOutline';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import PropTypes from 'prop-types';

/**
 * Function for the Audit Summary Component in API Security Audit feature
 */
export default function AuditSummary(props) {
    const {
        classes,
        criticalityObject,
        reportObject,
        numErrors,
        overallScore,
    } = props;

    return (
        <div className={classes.paperDiv}>
            <Paper elevation={1} className={classes.rootPaper}>
                <div>
                    <Typography variant='h5' className={classes.sectionHeadingTypography}>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.AuditApi.AuditScoreSummary'
                            defaultMessage='Audit Score and Summary'
                        />
                    </Typography>
                    <div className={classes.auditSummaryDiv}>
                        <div className={classes.auditSummarySubDiv}>
                            <VisibilitySensor>
                                {({ isVisible }) => {
                                    const progressScore = isVisible ? overallScore : 0;
                                    return (
                                        <CircularProgressbarWithChildren
                                            value={progressScore}
                                        >
                                            <Typography
                                                variant='body1'
                                                className={classes.circularProgressBarScore}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi
                                                                    .OverallScoreProgress'
                                                    defaultMessage='{overallScore}'
                                                    values={{
                                                        overallScore: (
                                                            <strong>{Math.round(overallScore)}</strong>
                                                        ),
                                                    }}
                                                />
                                            </Typography>
                                            <Typography
                                                variant='body1'
                                                className={classes.circularProgressBarScoreFooter}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi.ScoreFooter'
                                                    defaultMessage='out of 100'
                                                />
                                            </Typography>
                                        </CircularProgressbarWithChildren>
                                    );
                                }}
                            </VisibilitySensor>
                        </div>
                        <div className={classes.auditSummaryDivRight}>
                            {{}.hasOwnProperty.call(reportObject, 'score') &&
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.overallScore'
                                        defaultMessage='{overallScoreText} {overallScore} / 100'
                                        values={{
                                            overallScoreText: <strong>Overall Score:</strong>,
                                            overallScore: this.roundScore(overallScore),
                                        }}
                                    />
                                </Typography>
                            }
                            {numErrors !== null &&
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.TotalNumOfErrors'
                                        defaultMessage='{totalNumOfErrorsText} {totalNumOfErrors}'
                                        values={{
                                            totalNumOfErrorsText: <strong>Total Number of Errors: </strong>,
                                            totalNumOfErrors: numErrors,
                                        }}
                                    />
                                </Typography>
                            }
                            {{}.hasOwnProperty.call(reportObject, 'criticality') &&
                                <React.Fragment>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.AuditApi.OverallCriticality'
                                            defaultMessage='{overallCriticalityText} {overallCriticality}'
                                            values={{
                                                overallCriticalityText: (
                                                    <strong>Overall Severity:</strong>
                                                ),
                                                overallCriticality: (
                                                    criticalityObject[reportObject.criticality]
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
                                                            'Severity ranges from INFO, LOW, MEDIUM, ' +
                                                            'HIGH to CRITICAL, with INFO being ' +
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
                            }
                            <hr />
                            {{}.hasOwnProperty.call(reportObject, 'security') &&
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.SecuritySummary'
                                        defaultMessage='{securitySummary}'
                                        values={{
                                            securitySummary: (
                                                <strong>
                                                    Security -
                                                    ({
                                                        this.roundScore(reportObject.security.score)
                                                    } / 30)
                                                </strong>
                                            ),
                                        }}
                                    />
                                    <VisibilitySensor>
                                        {({ isVisible }) => {
                                            const progressScore = isVisible ?
                                                ((this.roundScore(reportObject.security.score) / 30
                                                ) * 100) : 0;
                                            return (
                                                <Line
                                                    percent={progressScore}
                                                    strokeColor='#3d98c7'
                                                />
                                            );
                                        }
                                        }
                                    </VisibilitySensor>
                                </Typography>
                            }
                            {{}.hasOwnProperty.call(reportObject, 'data') &&
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.DataValidationSummary'
                                        defaultMessage='{dataValidationSummary}'
                                        values={{
                                            dataValidationSummary: (
                                                <strong>
                                                    Data Validation -
                                                    ({this.roundScore(reportObject.data.score)} / 70)
                                                </strong>
                                            ),
                                        }}
                                    />
                                    <VisibilitySensor>
                                        {({ isVisible }) => {
                                            const progressScore = isVisible ?
                                                ((this.roundScore(reportObject.data.score) / 70
                                                ) * 100) : 0;
                                            return (
                                                <Line
                                                    percent={progressScore}
                                                    strokeColor='#3d98c7'
                                                />
                                            );
                                        }
                                        }
                                    </VisibilitySensor>
                                </Typography>
                            }
                            {{}.hasOwnProperty.call(reportObject, 'validationErrors') &&
                                <InlineMessage type='warning' height={140}>
                                    <div className={classes.contentWrapper}>
                                        <Typography
                                            variant='h5'
                                            component='h3'
                                            className={classes.head}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.FailedToValidate.Heading'
                                                defaultMessage='Failed to Validate OpenAPI File'
                                            />
                                        </Typography>
                                        <Typography component='p' className={classes.content}>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.FailedToValidate.Content'
                                                defaultMessage='Fix the critical errors shown below and run the audit again.'
                                            />
                                        </Typography>
                                    </div>
                                </InlineMessage>
                            }
                        </div>
                    </div>
                </div>
            </Paper>
        </div>
    );
}

AuditSummary.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    // TODO - Check if these proptypes are required and whether they are correct
    criticalityObject: PropTypes.shape({}).isRequired,
    reportObject: PropTypes.shape({}).isRequired,
    numErrors: PropTypes.shape({}).isRequired,
    overallScore: PropTypes.shape({}).isRequired,
};
