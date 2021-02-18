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
import { FormattedMessage, injectIntl } from 'react-intl';
import { CircularProgressbarWithChildren } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';
import Paper from '@material-ui/core/Paper';
import { withStyles, createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles';
import { Line } from 'rc-progress';
import Progress from 'AppComponents/Shared/Progress';
import { withRouter } from 'react-router';
import InlineMessage from 'AppComponents/Shared/InlineMessage';

import MUIDataTable from 'mui-datatables';

import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import MonacoEditor from 'react-monaco-editor';

const styles = (theme) => ({
    rootPaper: {
        padding: theme.spacing(3),
        margin: theme.spacing(2),
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
    referenceErrorTypography: {
        width: '70%',
        marginTop: '15%',
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
        display: 'table',
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
    columnOne: {
        display: 'block',
        width: '50%',
        float: 'left',
    },
    columnTwo: {
        width: '40%',
        float: 'right',
    },
    head: {
        fontWeight: 200,
        marginBottom: 20,
    },
    linkText: {
        float: 'right',
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
            externalApiId: null,
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
                    externalApiId: response.body.externalApiId,
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

    getErrorMuiTheme = () => createMuiTheme({
        typography: {
            useNextVariants: true,
        },
        overrides: {
            MUIDataTableBodyCell: {
                root: {
                    width: '100%',
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
     * @param {*} rowType The type of row - normal or error
     * @return {*} dataObject The dataObject array
     */
    getRowData(issues, category, rowType) {
        const dataObject = [];
        for (const item in issues) {
            if ({}.hasOwnProperty.call(issues, item)) {
                for (let i = 0; i < issues[item].issues.length; i++) {
                    const rowObject = [];
                    if (rowType === 'error') {
                        if (issues[item].issues[i].specificDescription) {
                            rowObject.push(
                                issues[item].issues[i].specificDescription, issues[item].issues[i].pointer,
                                category, rowType, item,
                            );
                        } else {
                            rowObject.push(
                                issues[item].description, issues[item].issues[i].pointer,
                                category, rowType, item,
                            );
                        }
                    } else if (rowType === 'normal') {
                        if (issues[item].issues[i].specificDescription) {
                            rowObject.push(
                                this.criticalityObject[issues[item].criticality],
                                issues[item].issues[i].specificDescription,
                                this.roundScore(issues[item].issues[i].score), issues[item].issues[i].pointer,
                                issues[item].issues[i].tooManyImpacted,
                                issues[item].issues[i].pointersAffected, category, issues[item].tooManyError,
                                rowType, item,
                            );
                        } else {
                            rowObject.push(
                                this.criticalityObject[issues[item].criticality],
                                issues[item].description, this.roundScore(issues[item].issues[i].score),
                                issues[item].issues[i].pointer, issues[item].issues[i].tooManyImpacted,
                                issues[item].issues[i].pointersAffected, category, issues[item].tooManyError,
                                rowType, item,
                            );
                        }
                    }
                    dataObject.push(rowObject);
                }
            }
        }
        return dataObject;
    }

    /**
     * Method to get the URL to display for each issue
     * @param {*} category Category of the issue
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
                url = baseUrl + 'datavalidation/datavalidation/';
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
            const lastTerms = [];
            const termObject = searchTerm.split('/');
            const regexPatterns = [];
            for (let i = 0; i < termObject.length; i++) {
                if (termObject[i] !== '' && termObject[i] !== '0') {
                    let appendedString = '"' + termObject[i] + '":';
                    if (appendedString.includes('~1')) {
                        appendedString = appendedString.replace(/~1/i, '/');
                    }
                    regexPatterns.push(appendedString);
                }
            }

            for (let j = 0; j < regexPatterns.length; j++) {
                if (regexPatterns[j] !== '') {
                    if (j !== 0 && lastTerms.length !== 0 && lastTerms[lastTerms.length - 1] !== null) {
                        lastTerms.push(editor.getModel().findNextMatch(
                            regexPatterns[j],
                            { lineNumber: lastTerms[lastTerms.length - 1].range.endLineNumber, column: 1 },
                            true, true, null, false,
                        ));
                    } else {
                        lastTerms.push(editor.getModel().findNextMatch(regexPatterns[j], 1, true, true, null, true));
                    }
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
     * @returns {*} roundScore Rounded off score
     */
    roundScore(score) {
        if (score !== 0) {
            return Math.round(score * 100) / 100;
        } else {
            return 0;
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        const { classes } = this.props;
        const {
            report, overallScore, numErrors, externalApiId, loading, apiDefinition,
        } = this.state;

        const reportObject = JSON.parse(report);
        const linkToDetailedReport = 'https://platform.42crunch.com/apis/' + externalApiId + '/security-audit-report';
        if (loading) {
            return (
                <div>
                    <InlineMessage type='info' height={140}>
                        <div className={classes.contentWrapper}>
                            <Typography
                                variant='h5'
                                component='h3'
                                className={classes.head}
                            >
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.AuditApi.WaitForReport'
                                    defaultMessage='Please wait...'
                                />
                            </Typography>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.AuditApi.WaitForReport.Content'
                                    defaultMessage='Auditing an API for the first time will take some time'
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                    <Progress />
                </div>
            );
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
            {
                name: 'isError',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'ReferenceUrl',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
        ];

        const errorColumns = [
            {
                name: 'Description',
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
                name: 'Issue Category',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'isError',
                options: {
                    display: 'excluded',
                    filter: false,
                    sort: false,
                },
            },
            {
                name: 'ReferenceUrl',
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
            print: false,
            download: false,
            selectableRows: false,
            expandableRows: true,
            expandableRowsOnClick: true,
            renderExpandableRow: (rowData) => {
                let searchTerm = null;
                if (rowData[3] === 'error') {
                    searchTerm = reportObject.index[rowData[1]];

                    return (
                        <TableRow className={classes.tableRow}>
                            <TableCell className={classes.columnOne}>
                                <MonacoEditor
                                    height='250px'
                                    theme='vs-dark'
                                    value={apiDefinition}
                                    options={editorOptions}
                                    editorDidMount={(editor, monaco) => this.editorDidMount(editor, monaco, searchTerm)}
                                />
                            </TableCell>
                            <TableCell className={classes.columnTwo}>
                                <Typography variant='body1' className={classes.referenceErrorTypography}>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.AuditApi.ReferenceSection'
                                        description='Link to visit for detail on how to remedy issue'
                                        defaultMessage='Visit this {link} to view a detailed description, possible
                                        exploits and remediation for this issue.'
                                        values={{
                                            link: (
                                                <strong>
                                                    <a
                                                        href={this.getMoreDetailUrl(rowData[2])}
                                                        target='_blank'
                                                        rel='noopener noreferrer'
                                                    >
                                                        link
                                                    </a>
                                                </strong>),
                                        }}
                                    />
                                </Typography>
                            </TableCell>
                        </TableRow>
                    );
                } else {
                    searchTerm = reportObject.index[rowData[3]];
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
                                                    >
                                                        link
                                                    </a>
                                                </strong>),
                                        }}
                                    />
                                </Typography>
                            </TableCell>
                        </TableRow>
                    );
                }
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
                                    <Typography variant='body1' className={classes.linkText}>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.AuditApi.LinkToDetailedReport'
                                            defaultMessage='{linkToDetailedReportText} {link} {afterLinkText}'
                                            values={{
                                                linkToDetailedReportText: 'Check out the ',
                                                link: (
                                                    <b>
                                                        <a
                                                            href={linkToDetailedReport}
                                                            target='_blank'
                                                            rel='noopener noreferrer'
                                                        >
                                                            detailed Report
                                                        </a>
                                                    </b>),
                                                afterLinkText: ' from 42Crunch',
                                            }}
                                        />
                                    </Typography>
                                    <div className={classes.auditSummaryDiv}>
                                        <div className={classes.auditSummarySubDiv}>
                                            <CircularProgressbarWithChildren
                                                value={overallScore}
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
                                        </div>
                                        <div className={classes.auditSummaryDivRight}>
                                            {{}.hasOwnProperty.call(reportObject, 'score')
                                                && (
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
                                                )}
                                            {numErrors !== null
                                                && (
                                                    <Typography variant='body1'>
                                                        <FormattedMessage
                                                            id='Apis.Details.APIDefinition.AuditApi.TotalNumOfErrors'
                                                            defaultMessage='{totalNumOfErrorsText} {totalNumOfErrors}'
                                                            values={{
                                                                totalNumOfErrorsText: (
                                                                    <strong>Total Number of Errors: </strong>
                                                                ),
                                                                totalNumOfErrors: numErrors,
                                                            }}
                                                        />
                                                    </Typography>
                                                )}
                                            {{}.hasOwnProperty.call(reportObject, 'criticality')
                                                && (
                                                    <>
                                                        <Typography variant='body1'>
                                                            <FormattedMessage
                                                                id={'Apis.Details.APIDefinition'
                                                                + '.AuditApi.OverallCriticality'}
                                                                defaultMessage={'{overallCriticalityText}'
                                                                + ' {overallCriticality}'}
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
                                                                title={(
                                                                    <>
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.severity'}
                                                                            defaultMessage='Severity ranges from: '
                                                                        />
                                                                        <br />
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.info'}
                                                                            defaultMessage='1. INFO'
                                                                        />
                                                                        <br />
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.low'}
                                                                            defaultMessage='2. LOW'
                                                                        />
                                                                        <br />
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.medium'}
                                                                            defaultMessage='3. MEDIUM'
                                                                        />
                                                                        <br />
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.high'}
                                                                            defaultMessage='4. HIGH'
                                                                        />
                                                                        <br />
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.critical'}
                                                                            defaultMessage='5. CRITICAL'
                                                                        />
                                                                        <br />
                                                                    </>
                                                                )}
                                                            >
                                                                <Button className={classes.helpButton}>
                                                                    <HelpOutline className={classes.helpIcon} />
                                                                </Button>
                                                            </Tooltip>
                                                        </Typography>
                                                    </>
                                                )}
                                            <hr />
                                            {{}.hasOwnProperty.call(reportObject, 'security')
                                                && (
                                                    <Typography variant='body1'>
                                                        <FormattedMessage
                                                            id='Apis.Details.APIDefinition.AuditApi.SecuritySummary'
                                                            defaultMessage='{securitySummary}'
                                                            values={{
                                                                securitySummary: (
                                                                    <strong>
                                                                        Security -
                                                                        (
                                                                        {
                                                                            this.roundScore(reportObject.security.score)
                                                                        }
                                                                        {' '}
                                                                        / 30)
                                                                    </strong>
                                                                ),
                                                            }}
                                                        />
                                                        <Line
                                                            percent={((this.roundScore(reportObject.security.score)
                                                                / 30
                                                            ) * 100)}
                                                            strokeColor='#3d98c7'
                                                        />
                                                    </Typography>
                                                )}
                                            {{}.hasOwnProperty.call(reportObject, 'data')
                                                && (
                                                    <Typography variant='body1'>
                                                        <FormattedMessage
                                                            id={'Apis.Details.APIDefinition'
                                                            + '.AuditApi.DataValidationSummary'}
                                                            defaultMessage='{dataValidationSummary}'
                                                            values={{
                                                                dataValidationSummary: (
                                                                    <strong>
                                                                        Data Validation -
                                                                        (
                                                                        {this.roundScore(reportObject.data.score)}
                                                                        {' '}
                                                                        / 70)
                                                                    </strong>
                                                                ),
                                                            }}
                                                        />
                                                        <Line
                                                            percent={((this.roundScore(reportObject.data.score) / 70
                                                            ) * 100)}
                                                            strokeColor='#3d98c7'
                                                        />
                                                    </Typography>
                                                )}
                                            {{}.hasOwnProperty.call(reportObject, 'validationErrors')
                                                && (
                                                    <InlineMessage type='warning' height={140}>
                                                        <div className={classes.contentWrapper}>
                                                            <Typography
                                                                variant='h5'
                                                                component='h3'
                                                                className={classes.head}
                                                            >
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                    + '.AuditApi.FailedToValidate.Heading'}
                                                                    defaultMessage='Failed to Validate OpenAPI File'
                                                                />
                                                            </Typography>
                                                            <Typography component='p' className={classes.content}>
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                    + '.AuditApi.FailedToValidate.Content'}
                                                                    defaultMessage={'Fix the critical errors '
                                                                    + 'shown below and run the audit again.'}
                                                                />
                                                            </Typography>
                                                        </div>
                                                    </InlineMessage>
                                                )}
                                        </div>
                                    </div>
                                </div>
                            </Paper>
                        </div>
                        {
                            <div className={classes.paperDiv}>
                                <Paper elevation={1} className={classes.rootPaper}>
                                    <div>
                                        <Typography variant='h5' className={classes.sectionHeadingTypography}>
                                            <FormattedMessage
                                                id='Apis.Details.APIDefinition.AuditApi.OpenApiFormatRequirements'
                                                defaultMessage='OpenAPI Format Requirements'
                                            />
                                        </Typography>
                                        {{}.hasOwnProperty.call(reportObject, 'semanticErrors')
                                            && (
                                                <>
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
                                                </>
                                            )}
                                        {{}.hasOwnProperty.call(reportObject, 'validationErrors')
                                            && (
                                                <>
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
                                                </>
                                            )}
                                        {{}.hasOwnProperty.call(reportObject, 'warnings')
                                            && (
                                                <>
                                                    <div>
                                                        <Typography variant='body1'>
                                                            <MuiThemeProvider theme={this.getErrorMuiTheme()}>
                                                                <MUIDataTable
                                                                    title='Best Practices Issues'
                                                                    data={this.getRowData(
                                                                        reportObject.warnings.issues,
                                                                        'OpenAPI Format Requirements',
                                                                        'error',
                                                                    )}
                                                                    columns={errorColumns}
                                                                    options={options}
                                                                />
                                                            </MuiThemeProvider>
                                                        </Typography>
                                                    </div>
                                                </>
                                            )}
                                        {!{}.hasOwnProperty.call(reportObject, 'validationErrors')
                                        && !{}.hasOwnProperty.call(reportObject, 'semanticErrors')
                                        && !{}.hasOwnProperty.call(reportObject, 'warnings')
                                        && (
                                            <Typography variant='body1'>
                                                <FormattedMessage
                                                    id='Apis.Details.APIDefinition.AuditApi.OASNoIssuesFound'
                                                    defaultMessage='No Issues Found'
                                                />
                                            </Typography>
                                        )}
                                    </div>
                                </Paper>
                            </div>
                        }
                        {{}.hasOwnProperty.call(reportObject, 'security')
                            && (
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
                                            <>
                                                <Typography variant='body1'>
                                                    <FormattedMessage
                                                        id='Apis.Details.APIDefinition.AuditApi.securityCriticality'
                                                        defaultMessage='{securityCriticalityText} {securityCriticality}'
                                                        values={{
                                                            securityCriticalityText: (
                                                                <strong>Severity:</strong>
                                                            ),
                                                            securityCriticality: (
                                                                // eslint-disable-next-line max-len
                                                                this.criticalityObject[reportObject.security.criticality]
                                                            ),
                                                        }}
                                                    />
                                                    <Tooltip
                                                        placement='right'
                                                        classes={{
                                                            tooltip: classes.htmlTooltip,
                                                        }}
                                                        title={(
                                                            <>
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.severity'}
                                                                    defaultMessage='Severity ranges from: '
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.info'}
                                                                    defaultMessage='1. INFO'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.low'}
                                                                    defaultMessage='2. LOW'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.medium'}
                                                                    defaultMessage='3. MEDIUM'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.high'}
                                                                    defaultMessage='4. HIGH'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.critical'}
                                                                    defaultMessage='5. CRITICAL'
                                                                />
                                                                <br />
                                                            </>
                                                        )}
                                                    >
                                                        <Button className={classes.helpButton}>
                                                            <HelpOutline className={classes.helpIcon} />
                                                        </Button>
                                                    </Tooltip>
                                                </Typography>
                                            </>
                                            {(reportObject.security.issueCounter !== 0)
                                            && (
                                                <div>
                                                    <hr />
                                                    <Typography variant='body1'>
                                                        <MuiThemeProvider theme={this.getMuiTheme()}>
                                                            <MUIDataTable
                                                                title='Issues'
                                                                data={this.getRowData(
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
                                            )}
                                        </div>
                                    </Paper>
                                </div>
                            )}
                        {{}.hasOwnProperty.call(reportObject, 'data')
                            && (
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
                                            <>
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
                                                        title={(
                                                            <>
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.severity'}
                                                                    defaultMessage='Severity ranges from: '
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.info'}
                                                                    defaultMessage='1. INFO'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.low'}
                                                                    defaultMessage='2. LOW'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.medium'}
                                                                    defaultMessage='3. MEDIUM'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.high'}
                                                                    defaultMessage='4. HIGH'
                                                                />
                                                                <br />
                                                                <FormattedMessage
                                                                    id={'Apis.Details.APIDefinition'
                                                                            + '.AuditApi.tooltip.critical'}
                                                                    defaultMessage='5. CRITICAL'
                                                                />
                                                                <br />
                                                            </>
                                                        )}
                                                    >
                                                        <Button className={classes.helpButton}>
                                                            <HelpOutline className={classes.helpIcon} />
                                                        </Button>
                                                    </Tooltip>
                                                </Typography>
                                            </>
                                            {(reportObject.data.issueCounter !== 0)
                                            && (
                                                <div>
                                                    <hr />
                                                    <Typography variant='body1'>
                                                        <MuiThemeProvider theme={this.getMuiTheme()}>
                                                            <MUIDataTable
                                                                title='Issues'
                                                                data={this.getRowData(
                                                                    reportObject.data.issues,
                                                                    'Data Validation',
                                                                    'normal',
                                                                )}
                                                                columns={columns}
                                                                options={options}
                                                            />
                                                        </MuiThemeProvider>
                                                    </Typography>
                                                </div>
                                            )}
                                        </div>
                                    </Paper>
                                </div>
                            )}
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

export default withRouter(injectIntl(withStyles(styles)(APISecurityAudit)));
