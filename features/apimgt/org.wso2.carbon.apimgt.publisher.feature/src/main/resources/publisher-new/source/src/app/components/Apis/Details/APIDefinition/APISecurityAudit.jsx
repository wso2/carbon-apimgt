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
import { withStyles } from '@material-ui/core/styles';
import { Line } from 'rc-progress';
import Grid from '@material-ui/core/Grid';
import Progress from 'AppComponents/Shared/Progress';

const styles = theme => ({
    rootPaper: {
        padding: theme.spacing.unit * 3,
        margin: theme.spacing.unit * 2,
    },
    gridDiv: {
        display: 'flex',
        'flex-direction': 'row',
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
        };
        this.keyCount = 0;
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        // Include code to pass in the data from the backend
        this.setState({ loading: true });
        const { apiId } = this.props;
        const api = new API();

        api.getSecurityAuditReport(apiId)
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

    /**
     * @inheritdoc
     */
    render() {
        // TODO - Make a criticality map where 1 is lowest and 4 is highest and color code if possible
        // TODO - Add the Progress Circle and Progress Bars
        // TODO - Test the json data after finishing the structure
        const { classes } = this.props;
        const {
            report, overallGrade, numErrors, loading,
        } = this.state;
        const reportObject = JSON.parse(report);

        if (loading) {
            return <Progress />;
        }
        return (
            <div>
                {report && (
                    <div
                        width='100%'
                        height='calc(100vh - 51px)'
                    >
                        <Typography variant='h4' styles={{ padding: 30 }}>API Security Audit Report</Typography>
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
                                            <Line
                                                percent={((Math.round(reportObject.validation.grade)) / 25) * 100}
                                                strokeColor='#3d98c7'
                                            />
                                            <Typography variant='body1'>
                                                <strong>
                                                    Security - ({Math.round(reportObject.security.grade)} / 25)
                                                </strong>
                                            </Typography>
                                            <Line
                                                percent={((Math.round(reportObject.security.grade)) / 25) * 100}
                                                strokeColor='#3d98c7'
                                            />
                                            <Typography variant='body1'>
                                                <strong>
                                                    Data Validation - ({Math.round(reportObject.data.grade)} / 50)
                                                </strong>
                                            </Typography>
                                            <Line
                                                percent={((Math.round(reportObject.data.grade)) / 50) * 100}
                                                strokeColor='#3d98c7'
                                            />
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
                                    <strong>Grade:</strong> {Math.round(reportObject.validation.grade)} / 25
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.validation.criticality}
                                </Typography>
                                <Typography variant='body1'>{
                                    (reportObject.validation.issueCounter !== 0) ?
                                        (reportObject.validation.issues.map((issue) => {
                                            return (
                                                <Grid container spacing={3}>
                                                    <Grid item xs={12}>
                                                        <Paper elevation={1} className={classes.rootPaper}>
                                                            <div className={classes.gridDiv}>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '75%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>Description:</strong> {issue.message}
                                                                </Typography>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '25%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>
                                                                        Score Impact:
                                                                    </strong> {Math.round(issue.score)}
                                                                </Typography>
                                                            </div>
                                                        </Paper>
                                                    </Grid>
                                                </Grid>
                                            );
                                        })) : (null)
                                }
                                </Typography>
                            </div>
                        </Paper>

                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' style={{ marginBottom: 18 }}>Security</Typography>
                                <Typography variant='body1'>
                                    <strong>Number of Issues:</strong> {reportObject.security.issueCounter}
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Grade:</strong> {Math.round(reportObject.security.grade)} / 25
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.security.criticality}
                                </Typography>
                                <Typography variant='body1'>{
                                    (reportObject.security.issueCounter !== 0) ?
                                        (reportObject.security.issues.map((issue) => {
                                            return (
                                                <Grid container spacing={3}>
                                                    <Grid item xs={12}>
                                                        <Paper elevation={1} className={classes.rootPaper}>
                                                            <div className={classes.gridDiv}>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '75%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>Description:</strong> {issue.message}
                                                                </Typography>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '25%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>
                                                                        Score Impact:
                                                                    </strong> {Math.round(issue.score)}
                                                                </Typography>
                                                            </div>
                                                        </Paper>
                                                    </Grid>
                                                </Grid>
                                            );
                                        })) : (null)
                                }
                                </Typography>
                            </div>
                        </Paper>
                        <Paper elevation={1} className={classes.rootPaper}>
                            <div>
                                <Typography variant='h5' style={{ marginBottom: 18 }}>Data Validation</Typography>
                                <Typography variant='body1'>
                                    <strong>Number of Issues:</strong> {reportObject.data.issueCounter}
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Grade:</strong> {Math.round(reportObject.data.grade)} / 50
                                </Typography>
                                <Typography variant='body1'>
                                    <strong>Criticality:</strong> {reportObject.data.criticality}
                                </Typography>
                                <hr />
                                <Typography variant='body1'>{
                                    (reportObject.data.issueCounter !== 0) ?
                                        (reportObject.data.issues.map((issue) => {
                                            return (
                                                <Grid container spacing={3}>
                                                    <Grid item xs={12}>
                                                        <Paper elevation={1} className={classes.rootPaper}>
                                                            <div className={classes.gridDiv}>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '75%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>Description:</strong> {issue.message}
                                                                </Typography>
                                                                <Typography
                                                                    variant='body1'
                                                                    style={{ width: '25%' }}
                                                                    key={this.getKey()}
                                                                >
                                                                    <strong>
                                                                        Score Impact:
                                                                    </strong> {Math.round(issue.score)}
                                                                </Typography>
                                                            </div>
                                                        </Paper>
                                                    </Grid>
                                                </Grid>
                                            );
                                        })) : (null)
                                }
                                </Typography>
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
