/**
 * Copyright (c), WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import { CircularProgressbarWithChildren } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';
import VisibilitySensor from 'react-visibility-sensor';
import Paper from '@material-ui/core/Paper';
import { withStyles, createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles';
import { Line } from 'rc-progress';
import Progress from 'AppComponents/Shared/Progress';

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
            overallGrade: 0,
            numErrors: 0,
            loading: false,
            apiDefinition: null,
        };
        this.keyCount = 0;
        this.dataArray = [];
        this.securityArray = [];
        this.validationArray = [];
        this.criticalityMap = {
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
        // Include code to pass in the data from the backend
        this.setState({ loading: true });
        const { apiId } = this.props;
        const newApi = new API();
        const promisedDefinition = newApi.getSwagger(apiId);
        promisedDefinition.then((response) => {
            this.setState({
                apiDefinition: JSON.stringify(response.obj, null, 1),
            });
        });

        newApi.getSecurityAuditReport(apiId)
            .then((response) => {
                this.setState({
                    report: response.body.report,
                    overallGrade: response.body.grade,
                    numErrors: response.body.numErrors,
                    loading: false,
                });
            })
            .catch((error) => {
                this.setState({ loading: false });
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error('Something went wrong while retrieving the API Security Report');
                }
            });
    }

    /**
     * @inheritdoc
     */
    getKey() {
        return this.keyCount++;
    }

    getMuiTheme = () => createMuiTheme({
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
     * @return {*} dataArray array
     */
    getRowData(issues, category) {
        const dataArray = [];
        issues.forEach((issue) => {
            const rowData = [];
            rowData.push(
                this.criticalityMap[issue.criticality],
                issue.message, Math.round(issue.score), issue.pointerImpacted, category,
            );
            dataArray.push(rowData);
        });
        return dataArray;
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
     * TODO - Amend this to be unique for each issue
     * @param {*} editor
     * @param {*} monaco
     */
    editorDidMount = (editor, monaco) => {
        const { classes } = this.props;
        // editor.setSelection(new monaco.Range(18, 8, 18, 15));
        // const line = editor.getAction('actions.find').run(searchTerm);
        editor.revealLineInCenter(15);
        editor.deltaDecorations([], [
            {
                range: new monaco.Range(15, 1, 15, 50),
                options: {
                    isWholeLine: true,
                    className: classes.inlineDecoration,
                    glyphMarginClassName: classes.contentLine,
                },
            },
        ]);
    }

    // editorDidMountParent(editor, monaco) {
    // }

    /**
     * @inheritdoc
     */
    render() {
        // TODO - Test the json data after finishing the structure
        const { classes } = this.props;
        const {
            report, overallGrade, numErrors, loading, apiDefinition,
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
                name: 'Pointer Impacted',
                options: {
                    // customBodyRenderer: (value, tableMeta, updateValue,  tableViewObj = this) => {
                    //     if (tableMeta.rowData) {
                    //         const path = tableMeta.rowData[3];
                    //     }
                    // },
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
                // const colSpan = rowData.length + 1;

                // const path = rowData[3] + '';
                // const splitPath = path.split(',');
                // const searchTerm = splitPath[(splitPath.length - 1)];
                return (
                    <TableRow>
                        <TableCell colSpan='2'>
                            <MonacoEditor
                                width='85%'
                                height='250px'
                                theme='vs-dark'
                                value={apiDefinition}
                                options={editorOptions}
                                editorDidMount={this.editorDidMount}
                            />
                        </TableCell>
                        <TableCell>
                            <Typography variant='body1' style={{ width: '70%' }}>
                                Visit this
                                <strong>
                                    <a
                                        href={this.getMoreDetailUrl(rowData[4])}
                                        target='_blank'
                                        rel='noopener noreferrer'
                                    >
                                    &nbsp;link&nbsp;
                                    </a>
                                </strong>
                                to view a detailed description, possible exploits and remediation for this issue.
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
                        <Typography variant='h4' style={{ paddingTop: 30, paddingLeft: 20 }}>
                            API Security Audit Report
                        </Typography>
                        <div style={{ marginTop: 30 }}>
                            <Paper elevation={1} className={classes.rootPaper}>
                                <div>
                                    <Typography variant='h5' styles={{ marginLeft: '40px' }}>
                                        Audit Score and Summary
                                    </Typography>
                                    {/** Show total score and possibly a Progress Ring */}
                                    <div style={{ display: 'flex', marginTop: 25 }}>
                                        <div style={{ width: 250, marginLeft: 40, marginRight: 40 }}>
                                            <VisibilitySensor>
                                                {({ isVisible }) => {
                                                    const gradeProgressScore = isVisible ? overallGrade : 0;
                                                    return (
                                                        <CircularProgressbarWithChildren
                                                            value={gradeProgressScore}
                                                        >
                                                            <Typography
                                                                variant='body1'
                                                                style={{
                                                                    fontSize: 70,
                                                                    color: '#3d98c7',
                                                                    marginTop: 18,
                                                                }}
                                                            >
                                                                <strong>{Math.round(overallGrade)}</strong>
                                                            </Typography>
                                                            <Typography
                                                                variant='body1'
                                                                style={{ fontSize: 18, marginTop: 10 }}
                                                            >out of 100
                                                            </Typography>
                                                        </CircularProgressbarWithChildren>
                                                    );
                                                }}
                                            </VisibilitySensor>
                                        </div>
                                        <div style={{ flexGrow: 1, marginLeft: 200, marginTop: 10 }}>
                                            <Typography variant='body1'>
                                                <strong>Overall Grade:</strong> {Math.round(overallGrade)} / 100
                                            </Typography>
                                            <Typography variant='body1'>
                                                <strong>
                                                    Total Number of Errors:
                                                </strong> {numErrors}
                                            </Typography>
                                            <Typography variant='body1'>
                                                <strong>Overall Criticality:</strong> {reportObject.criticality}
                                            </Typography>
                                            <hr />
                                            <Typography variant='body1'>
                                                <strong>OpenAPI Format
                                                 Requirements - ({Math.round(reportObject.validation.grade)} / 25)
                                                </strong>
                                            </Typography>
                                            <VisibilitySensor>
                                                {({ isVisible }) => {
                                                    const gradeProgressScore = isVisible ?
                                                        (((Math.round(reportObject.validation.grade)) / 25) * 100) : 0;
                                                    return (
                                                        <Line
                                                            percent={gradeProgressScore}
                                                            strokeColor='#3d98c7'
                                                        />
                                                    );
                                                }
                                                }
                                            </VisibilitySensor>
                                            <Typography variant='body1'>
                                                <strong>
                                                    Security - ({Math.round(reportObject.security.grade)} / 25)
                                                </strong>
                                            </Typography>
                                            <VisibilitySensor>
                                                {({ isVisible }) => {
                                                    const gradeProgressScore = isVisible ?
                                                        (((Math.round(reportObject.security.grade)) / 25) * 100) : 0;
                                                    return (
                                                        <Line
                                                            percent={gradeProgressScore}
                                                            strokeColor='#3d98c7'
                                                        />
                                                    );
                                                }
                                                }
                                            </VisibilitySensor>
                                            <Typography variant='body1'>
                                                <strong>
                                                    Data Validation - ({Math.round(reportObject.data.grade)} / 50)
                                                </strong>
                                            </Typography>
                                            <VisibilitySensor>
                                                {({ isVisible }) => {
                                                    const gradeProgressScore = isVisible ?
                                                        (((Math.round(reportObject.data.grade)) / 25) * 100) : 0;
                                                    return (
                                                        <Line
                                                            percent={gradeProgressScore}
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
                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' style={{ marginBottom: 18 }}>
                                        OpenAPI Format Requirements
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Number of Issues:</strong> {reportObject.validation.issueCounter}
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Score:</strong> {Math.round(reportObject.validation.grade)} / 25
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.validation.criticality}
                                </Typography>
                                {(reportObject.validation.issueCounter !== 0) &&
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
                                }
                            </div>
                        </Paper>

                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' style={{ marginBottom: 18 }}>Security</Typography>
                                <Typography variant='body1'>
                                    <strong>Number of Issues:</strong> {reportObject.security.issueCounter}
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Score:</strong> {Math.round(reportObject.security.grade)} / 25
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.security.criticality}
                                </Typography>
                                {(reportObject.security.issueCounter !== 0) &&
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
                                }
                            </div>
                        </Paper>
                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' style={{ marginBottom: 18 }}>Data Validation</Typography>
                                <Typography variant='body1'>
                                    <strong>Number of Issues:</strong> {reportObject.data.issueCounter}
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Score:</strong> {Math.round(reportObject.data.grade)} / 50
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.data.criticality}
                                </Typography>
                                {(reportObject.data.issueCounter !== 0) &&
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
                                }
                            </div>
                        </Paper>
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
};

export default withStyles(styles)(APISecurityAudit);
