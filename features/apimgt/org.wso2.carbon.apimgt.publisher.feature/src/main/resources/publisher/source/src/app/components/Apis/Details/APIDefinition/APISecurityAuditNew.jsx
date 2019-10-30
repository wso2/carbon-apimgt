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
import { FormattedMessage } from 'react-intl';

import 'react-circular-progressbar/dist/styles.css';
import { withStyles, createMuiTheme } from '@material-ui/core/styles';
import Progress from 'AppComponents/Shared/Progress';
import { withRouter } from 'react-router';

import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import MonacoEditor from 'react-monaco-editor';

import AuditSummary from './APISecurityAudit/components/AuditSummary';
import OpenAPIRequirements from './APISecurityAudit/components/OpenAPIRequirements';
import Security from './APISecurityAudit/components/Security';
import Data from './APISecurityAudit/components/Data';

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
});


/**
 * This Component hosts the API Security Audit Component
 * More specifically, rendering of the Security Audit
 * Report.
 */
class APISecurityAudit1 extends Component {
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
                                category, rowType,
                            );
                        } else {
                            rowObject.push(
                                issues[item].description, issues[item].issues[i].pointer,
                                category, rowType,
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
                                rowType,
                            );
                        } else {
                            rowObject.push(
                                this.criticalityObject[issues[item].criticality],
                                issues[item].description, this.roundScore(issues[item].issues[i].score),
                                issues[item].issues[i].pointer, issues[item].issues[i].tooManyImpacted,
                                issues[item].issues[i].pointersAffected, category, issues[item].tooManyError,
                                rowType,
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
            {
                name: 'isError',
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
                                                    >link
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
                                                    >link
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
                        <AuditSummary
                            classes={classes}
                            criticalityObject={this.criticalityObject}
                            reportObject={reportObject}
                            numErrors={numErrors}
                            overallScore={overallScore}
                            roundScore={this.roundScore}
                        />
                        <OpenAPIRequirements
                            classes={classes}
                            reportObject={reportObject}
                            errorColumns={errorColumns}
                            options={options}
                            getRowData={this.getRowData}
                            getMuiTheme={this.getMuiTheme}
                            getErrorMuiTheme={this.getErrorMuiTheme}
                        />
                        {{}.hasOwnProperty.call(reportObject, 'security') &&
                            <Security
                                classes={classes}
                                reportObject={reportObject}
                                criticalityObject={this.criticalityObject}
                                columns={columns}
                                options={options}
                                getRowData={this.getRowData}
                                getMuiTheme={this.getMuiTheme}
                            />
                        }
                        {{}.hasOwnProperty.call(reportObject, 'data') &&
                            <Data
                                classes={classes}
                                reportObject={reportObject}
                                criticalityObject={this.criticalityObject}
                                columns={columns}
                                options={options}
                                getRowData={this.getRowData}
                                getMuiTheme={this.getMuiTheme}
                            />
                        }
                    </div>
                )}
            </div>
        );
    }
}

APISecurityAudit1.propTypes = {
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

export default withRouter(withStyles(styles)(APISecurityAudit1));
