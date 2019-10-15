/* eslint-disable indent */
/* eslint-disable react/jsx-indent */
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

import React, { Component } from 'react';
import Typography from '@material-ui/core/Typography';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';
import Tooltip from '@material-ui/core/Tooltip';
import Button from '@material-ui/core/Button';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import { CircularProgressbarWithChildren } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';
import VisibilitySensor from 'react-visibility-sensor';
import Paper from '@material-ui/core/Paper';
import { withStyles, createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles';
import { Line } from 'rc-progress';
import Progress from 'AppComponents/Shared/Progress';
import { withRouter } from 'react-router';

import MUIDataTable from 'mui-datatables';

import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import MonacoEditor from 'react-monaco-editor';

const styles = theme => ({
    rootPaper: {
        padding: theme.spacing.unit * 3,
        margin: theme.spacing.unit * 2,
    },
    inlineDecoration: {
        background: '#FF0000',
    },
    contentLine: {
        background: '#add8e6',
    },
    htmlToolTip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0, 0, 0, 0.87)',
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(14),
        border: '1px solid #dadde9',
        '& b': {
            fontWeight: theme.typography.fontWeightMedium,
        },
    },
    helpButton: {
        padding: 0,
        minWidth: 20,
        'margin-left': 10,
    },
    helpIcon: {
        fontSize: 16,
    },
    tableRow: {
        'background-color': '#d3d3d3',
    },
    referenceTypography: {
        width: '70%',
    },
    subheadingTypography: {
        paddingTop: 30,
        paddingLeft: 20,
    },
    paperDiv: {
        marginTop: 30,
    },
    sectionHeadingTypography: {
        marginBottom: 18,
    },
    auditSummaryDiv: {
        display: 'flex',
        marginTop: 25,
    },
    auditSummarySubDiv: {
        width: 250,
        marginLeft: 40,
        marginRight: 40,
    },
    circularProgressBarScore: {
        fontSize: 70,
        color: '#3d98c7',
        marginTop: 18,
    },
    circularProgressBarScoreFooter: {
        fontSize: 18,
        marginTop: 10,
    },
    auditSummaryDivRight: {
        flexGrow: 1,
        marginLeft: 200,
        marginTop: 10,
    },
});


/**
 * This Component hosts the API Security Audit Component
 * More specifically, rendering of the Security Audit
 * Report.
 */
class APISecurityAudit extends Component {
    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            report: null,
            overallScore: 0,
            numErrors: 0,
            loading: false,
            apiDefinition: null,
        };
        this.criticalityObject = {
            1: 'INFO',
            2: 'LOW',
            3: 'MEDIUM',
            4: 'HIGH',
            5: 'CRITICAL',
        };
        this.searchTerm = null;
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.setState({ loading: true });
        const { apiId, history, intl } = this.props;
        const currentApi = new API();
        const promisedDefinition = currentApi.getSwagger(apiId);
        promisedDefinition.then((response) => {
            this.setState({
                apiDefinition: JSON.stringify(response.obj, null, 1),
            });
        })
            .catch((error) => {
                console.log(error);
            });

        currentApi.getSecurityAuditReport(apiId)
            .then((response) => {
                this.setState({
                    report: response.body.report,
                    overallScore: response.body.grade,
                    numErrors: response.body.numErrors,
                    loading: false,
                });
            })
            .catch((error) => {
                console.log(error);
                this.setState({ loading: false });
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.AuditApi.GetReportError',
                    defaultMessage: 'Something went wrong while retrieving the API Security Report',
                }));
                const redirectUrl = '/apis/' + apiId + '/api definition';
                history.push(redirectUrl);
            });
    }

    getMuiTheme = () => createMuiTheme({
        typography: {
            useNextVariants: true,
        },
        overrides: {
            MUIDataTableBodyCell: {
                root: {
                    width: '30%',
                },
            },
            MUIDataTableSelectCell: {
                root: {
                    display: 'none',
                },
            },
            MUIDataTableToolbarSelect: {
                title: {
                    display: 'none',
                },
            },
        },
    });

    /**
     * Get Row data for MUI Table
     * @param {*} issues Issues array
     * @param {String} category The category of the issue
     * @return {*} dataObject array
     */
    getRowData(issues, category) {
        const dataObject = [];
        for (const item in issues) {
            if ({}.hasOwnProperty.call(issues, item)) {
                for (let i = 0; i < issues[item].issues.length; i++) {
                    const rowObject = [];
                    if (issues[item].issues[i].specificDescription) {
                        rowObject.push(
                            this.criticalityObject[issues[item].criticality],
                            issues[item].issues[i].specificDescription,
                            this.roundScore(issues[item].issues[i].score), issues[item].issues[i].pointer,
                            issues[item].issues[i].tooManyImpacted,
                            issues[item].issues[i].pointersAffected, category, issues[item].tooManyError,
                        );
                    } else {
                        rowObject.push(
                            this.criticalityObject[issues[item].criticality],
                            issues[item].description, this.roundScore(issues[item].issues[i].score),
                            issues[item].issues[i].pointer, issues[item].issues[i].tooManyImpacted,
                            issues[item].issues[i].pointersAffected, category, issues[item].tooManyError,
                        );
                    }
                    dataObject.push(rowObject);
                }
            }
        }
        return dataObject;

        // TODO - This code block has to be removed after completing the revamp of the API Security Audit UI.
        // const dataObject = issues.map((issue) => {
        //     // const lengthOfIssue = issue.length;
        //     const rowObject = [];
        //     rowObject.push(
        //         this.criticalityObject[issue.criticality], issue.message,
        //         (Math.round(issue.score * 100) / 100), issue.pointer, category,
        //     );
        //     return rowObject;
        // });
        // return dataObject;
    }

    /**
     * Method to get the URL to display for each issue
     * TODO - Has to be replaced with API of database from 42Crunch when it is made available by them
     * @param {*} category Category of Issue
     * @returns {*} String URL
     */
    getMoreDetailUrl(category) {
        const baseUrl = 'https://apisecurity.io/ref/';
        let url = '';

        switch (category) {
            case 'OpenAPI Format Requirements':
                url = baseUrl + 'oasconformance/';
                break;
            case 'Security':
                url = baseUrl + 'security/';
                break;
            case 'Data Validation':
                url = baseUrl + 'security/datavalidation/';
                break;
            default:
                url = baseUrl;
        }
        return url;
    }

    /**
     * editorDidMount method for Monaco Editor
     * @param {*} editor Monaco Editor editor
     * @param {*} monaco Monaco Editor monaco
     * @param {String} searchTerm SearchTerm for pointer
     */
    editorDidMount = (editor, monaco, searchTerm) => {
        const { classes } = this.props;
        if (searchTerm !== '') {
            // let indexCount;
            const lastTerms = [];
            const termObject = searchTerm.split('/');
            // const lastTerms = termObject.map((term, index) => {
            //     indexCount++;
            //     return editor.getModel()
            //     .findNextMatch(term, index === 0 ? lastTerms[indexCount - 1] : 1, false, false, null, false);
            // });
            for (let i = 0; i < termObject.length; i++) {
                if (i === 0) {
                    lastTerms.push(editor.getModel().findNextMatch(termObject[i], 1, false, false, null, false));
                } else {
                    lastTerms.push(editor.getModel().findNextMatch(termObject[i], 100, false, false, null, false));
                }
            }
            const finalMatchIndex = lastTerms.length - 1;
            if (lastTerms[finalMatchIndex] != null) {
                editor.revealLineInCenter(lastTerms[finalMatchIndex].range.startLineNumber);
                editor.deltaDecorations([], [
                    {
                        range: new monaco.Range(
                            lastTerms[finalMatchIndex].range.startLineNumber,
                            lastTerms[finalMatchIndex].range.startColumn,
                            lastTerms[finalMatchIndex].range.endLineNumber,
                            lastTerms[finalMatchIndex].range.endColumn,
                        ),
                        options: {
                            isWholeLine: true,
                            className: classes.inlineDecoration,
                            glyphMarginClassName: classes.contentLine,
                        },
                    },
                ]);
            }
        }
    }

    /**
     * Method to round off the score of a section of the report
     * @param {*} score Score of section
     * @param {*} maxScore Maximum score of the section
     * @returns {*} roundScore Rounded off score
     */
    roundScore(score) {
        return Math.round(score * 100) / 100;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { classes } = this.props;
        const {
            report, overallScore, numErrors, loading, apiDefinition,
        } = this.state;

        const reportObject = JSON.parse(report);

        if (loading) {
            return <Progress />;
        }

        const columns = [
            {
                name: 'Severity',
                options: {
                    filter: true,
                    sort: true,
                },
            },
            {
                name: 'Description',
                options: {
                    filter: true,
                    sort: true,
                },
            },
            {
                name: 'Score Impact',
                options: {
                    filter: true,
                    sort: true,
                },
            },
            {
                name: 'Pointer',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'Too Many Impacted',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'Pointers Affected',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'Issue Category',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'Too Many Errors',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
        ];

        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
            glyphMargin: true,
        };

        const options = {
            filterType: 'dropdown',
            responsive: 'stacked',
            selectableRows: false,
            expandableRows: true,
            expandableRowsOnClick: true,
        renderExpandableRow: (rowData) => {
        let searchTerm = null;
        const indexNumber = rowData[3];
        const path = reportObject.index[indexNumber];
        searchTerm = path;

        // TODO - Remove the following code block after completing the pointer feature
        // if (path.includes('get') ||
        //     path.includes('put') ||
        //     path.includes('post') ||
        //     path.includes('delete')) {
        //     searchTerm = path;
        // } else {
        //     searchTerm = 'none';
        // }

        return (
                <TableRow className={classes.tableRow}>
                    <TableCell colSpan='2'>
                        <MonacoEditor
                            width='85%'
                            height='250px'
                            theme='vs-dark'
                            value={apiDefinition}
                            options={editorOptions}
                            editorDidMount={(editor, monaco) => this.editorDidMount(editor, monaco, searchTerm)}
                        />
                    </TableCell>
                    <TableCell>
                        <Typography variant='body1' className={classes.referenceTypography}>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.AuditApi.ReferenceSection'
                                description='Link to visit for detail on how to remedy issue'
                                defaultMessage='Visit this {link} to view a detailed description, possible
                                exploits and remediation for this issue.'
                                values={{
                                    link: (
                                        <strong>
                                            <a
                                                href={this.getMoreDetailUrl(rowData[6])}
                                                target='_blank'
                                                rel='noopener noreferrer'
                                            >link
                                            </a>
                                        </strong>),
                                }}
                            />
                        </Typography>
                    </TableCell>
                </TableRow>
                );
            },
        };
        return (
            <div>
                {report && (
                    <div
                        width='100%'
                        height='calc(100vh - 51px)'
                    >
                        <Typography variant='h4' className={classes.subheadingTypography}>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.AuditApi.ApiSecurityAuditReport'
                                defaultMessage='API Security Audit Report'
                            />
                        </Typography>
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
                                                                this.criticalityObject[reportObject.criticality]
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
                                            <hr />
                                            {/* <Typography variant='body1'>
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi.OpenApiSummary'
                                                    defaultMessage='{openApiSummary}'
                                                    values={{
                                                        openApiSummary: (
                                                            <strong>OpenAPI Format Requirements -
                                                                    ({(
                                                                Math.round(reportObject.validation.grade * 100) / 100)
                                                            } / 25)
                                                            </strong>
                                                        ),
                                                    }}
                                                /> */}

                                            {/* </Typography>
                                            <VisibilitySensor>
                                                {({ isVisible }) => {
                                                    const gradeProgressScore = isVisible ?
                                                        (((Math.round(reportObject.validation.grade * 100) / 100) / 25
                                                        ) * 100) : 0;
                                                    return (
                                                        <Line
                                                            percent={gradeProgressScore}
                                                            strokeColor='#3d98c7'
                                                        />
                                                    );
                                                }
                                                }
                                            </VisibilitySensor> */}
                                            <Typography variant='body1'>
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi.SecuritySummary'
                                                    defaultMessage='{securitySummary}'
                                                    values={{
                                                        securitySummary: (
                                                            <strong>
                                                                Security -
                                                                 ({this.roundScore(reportObject.security.score)} / 30)
                                                            </strong>
                                                        ),
                                                    }}
                                                />

                                            </Typography>
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

                                            </Typography>
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
                                        </div>
                                    </div>
                                </div>
                            </Paper>
                        </div>
                        {reportObject.validation !== null &&
                            <div className={classes.paperDiv}>
                                <Paper elevation={1} className={classes.rootPaper}>
                                    <div>
                                        <Typography variant='h5' className={classes.sectionHeadingTypography}>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.OpenApiFormatRequirements'
                                                defaultMessage='OpenAPI Format Requirements'
                                            />
                                        </Typography>
                                    </div>
                                </Paper>
                            </div>
                        }
                        {reportObject.security !== null &&
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
                                                            this.criticalityObject[reportObject.security.criticality]
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
                                        {(reportObject.data.issueCounter !== 0) &&
                                            <div>
                                                <hr />
                                                <Typography variant='body1'>
                                                    <MuiThemeProvider theme={this.getMuiTheme()}>
                                                        <MUIDataTable
                                                            title='Issues'
                                                            data={this.getRowData(
                                                                reportObject.security.issues,
                                                                'Security',
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
                        }
                        {reportObject.data !== null &&
                            <div className={classes.paperDiv}>
                                <Paper elevation={1} className={classes.rootPaper}>
                                    <div>
                                        <Typography variant='h5' className={classes.sectionHeadingTypography}>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.DataValidation'
                                                defaultMessage='Data Validation'
                                            />
                                        </Typography>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.DataValidationNumOfIssues'
                                                defaultMessage='{dataNumOfIssuesText} {dataNumOfIssues}'
                                                values={{
                                                    dataNumOfIssuesText: (
                                                        <strong>Number of Issues:</strong>
                                                    ),
                                                    dataNumOfIssues: reportObject.data.issueCounter,
                                                }}
                                            />
                                        </Typography>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.DataValidationScore'
                                                defaultMessage='{dataScoreText} {dataScore}  / 70'
                                                values={{
                                                    dataScoreText: (
                                                        <strong>Score:</strong>
                                                    ),
                                                    dataScore: (
                                                        (Math.round(reportObject.data.score * 100) / 100)
                                                    ),
                                                }}
                                            />
                                        </Typography>
                                        <React.Fragment>
                                            <Typography variant='body1'>
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi.dataCriticality'
                                                    defaultMessage='{dataCriticalityText} {dataCriticality}'
                                                    values={{
                                                        dataCriticalityText: (
                                                            <strong>Severity:</strong>
                                                        ),
                                                        dataCriticality: (
                                                            this.criticalityObject[reportObject.data.criticality]
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
                                        {(reportObject.data.issueCounter !== 0) &&
                                            <div>
                                                <hr />
                                                <Typography variant='body1'>
                                                    <MuiThemeProvider theme={this.getMuiTheme()}>
                                                        <MUIDataTable
                                                            title='Issues'
                                                            data={this.getRowData(
                                                                reportObject.data.issues,
                                                                'Data Validation',
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
                        }
                        {/* <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' className={classes.sectionHeadingTypography}>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.OpenApiFormatRequirements'
                                        defaultMessage='OpenAPI Format Requirements'
                                    />
                                </Typography>
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.OpenApiNumOfIssues'
                                        defaultMessage='{openApiNumOfIssuesText} {openApiNumOfIssues}'
                                        values={{
                                            openApiNumOfIssuesText: (
                                                <strong>Number of Issues:</strong>
                                            ),
                                            openApiNumOfIssues: reportObject.validation.issueCounter,
                                        }}
                                    />
                                </Typography>
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.OpenApiScore'
                                        defaultMessage='{openApiScoreText} {openApiScore}  / 25'
                                        values={{
                                            openApiScoreText: (
                                                <strong>Score:</strong>
                                            ),
                                            openApiScore: (
                                                (Math.round(reportObject.validation.grade * 100) / 100)
                                            ),
                                        }}
                                    />

                                </Typography>
                                <React.Fragment>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.AuditApi.OpenApiCriticality'
                                            defaultMessage='{openApiCriticalityText} {openApiCriticality}'
                                            values={{
                                                openApiCriticalityText: (
                                                    <strong>Criticality:</strong>
                                                ),
                                                openApiCriticality: reportObject.validation.criticality,
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
                                                            'Criticality ranges from 1 to 5, with 1 being' +
                                                                    ' low vulnerability and 5 being high vulnerability'
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
                                </React.Fragment> */}
                                {/* {(reportObject.validation.issueCounter !== 0) &&
                                    <div>
                                        <hr />
                                        <Typography variant='body1'>
                                            <MuiThemeProvider theme={this.getMuiTheme()}>
                                                <MUIDataTable
                                                    title='Issues'
                                                    data={this.getRowData(
                                                        reportObject.validation.issues,
                                                        'OpenAPI Format Requirements',
                                                    )}
                                                    columns={columns}
                                                    options={options}
                                                />
                                            </MuiThemeProvider>
                                        </Typography>
                                    </div>
                                } */}
                            {/* </div>
                        </Paper>

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
                                        defaultMessage='{securityScoreText} {securityScore}  / 25'
                                        values={{
                                            securityScoreText: <strong>Score:</strong>,
                                            securityScore: (Math.round(reportObject.security.grade * 100) / 100),
                                        }}
                                    />
                                </Typography>
                                <React.Fragment>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='APis.Dtails.APIDefinition.AuditApi.SecurityCriticality'
                                            defaultMessage='{securityCriticalityText} {securityCriticality}'
                                            values={{
                                                securityCriticalityText: (
                                                    <strong>Criticality:</strong>
                                                ),
                                                securityCriticality: reportObject.security.criticality,
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
                                                            'Criticality ranges from 1 to 5, with 1 being' +
                                                                    ' low vulnerability and 5 being high vulnerability'
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
                                </React.Fragment> */}
                                {/* {(reportObject.security.issueCounter !== 0) &&
                                    <div>
                                        <hr />
                                        <Typography variant='body1'>
                                            <MuiThemeProvider theme={this.getMuiTheme()}>
                                                <MUIDataTable
                                                    title='Issues'
                                                    data={this.getRowData(reportObject.security.issues, 'Security')}
                                                    columns={columns}
                                                    options={options}
                                                />
                                            </MuiThemeProvider>
                                        </Typography>
                                    </div>
                                } */}
                            {/* </div>
                        </Paper>
                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' className={classes.sectionHeadingTypography}>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.DataValidation'
                                        defaultMessage='Data Validation'
                                    />
                                </Typography>
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.DataValidationIssueCounter'
                                        defaultMessage='{dataValidationIssueCounterText} {dataValidationIssueCounter}'
                                        values={{
                                            dataValidationIssueCounterText: (
                                                <strong>Number of Issues:</strong>
                                            ),
                                            dataValidationIssueCounter: reportObject.data.issueCounter,
                                        }}
                                    />
                                </Typography>
                                <Typography variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.DataValidationScore'
                                        defaultMessage='{dataValidationScoreText} {dataValidationScore} / 50'
                                        values={{
                                            dataValidationScoreText: (
                                                <strong>Score:</strong>
                                            ),
                                            dataValidationScore: (Math.round(reportObject.data.grade * 100) / 100),
                                        }}
                                    />
                                </Typography>
                                <React.Fragment>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.AuditApi.DataValidationCriticality'
                                            defaultMessage='{dataValidationCriticalityText} {dataValidationCriticality}'
                                            values={{
                                                dataValidationCriticalityText: (
                                                    <strong>Criticality:</strong>
                                                ),
                                                dataValidationCriticality: reportObject.data.criticality,
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
                                                            'Criticality ranges from 1 to 5, with 1 being' +
                                                                    ' low vulnerability and 5 being high vulnerability'
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
                                </React.Fragment> */}
                                {/* {(reportObject.data.issueCounter !== 0) &&
                                    <div>
                                        <hr />
                                        <Typography variant='body1'>
                                            <MuiThemeProvider theme={this.getMuiTheme()}>
                                                <MUIDataTable
                                                    title='Issues'
                                                    data={this.getRowData(reportObject.data.issues, 'Data Validation')}
                                                    columns={columns}
                                                    options={options}
                                                />
                                            </MuiThemeProvider>
                                        </Typography>
                                    </div>
                                } */}
                            {/* </div>
                        </Paper> */}
                    </div>
                )}
            </div>
        );
    }
}

APISecurityAudit.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    theme: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default withRouter(withStyles(styles)(APISecurityAudit));
