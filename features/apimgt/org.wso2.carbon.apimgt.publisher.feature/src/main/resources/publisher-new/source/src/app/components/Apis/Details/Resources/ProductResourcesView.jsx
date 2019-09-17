/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { withStyles, withTheme } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import List from '@material-ui/core/List';
import APIProduct from 'AppData/APIProduct';
import CONSTS from 'AppData/Constants';

import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import Resource from './Resource';

/**
 * Render method base.
 * @param {*} props
 */

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        width: 400,
    },
    mainTitle: {
        paddingLeft: 0,
    },
    scopes: {
        width: 400,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonMain: {
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewHeader: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        width: 300,
    },
    addResource: {
        width: 600,
        marginTop: 0,
    },
    buttonIcon: {
        marginRight: 10,
    },
    expansionPanel: {
        marginBottom: theme.spacing.unit,
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
});

/**
 * Component to show api product resources in resources tab
 */
class ProductResourcesView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            paths: null,
            scopes: [],
            notFound: false,
            showAddResource: false,
            showPolicy: false,
            apiPolicies: [],
            selectedPolicy: props.api.apiThrottlingPolicy,
            policyLevel: props.api.apiThrottlingPolicy ? 'perAPI' : 'perResource',
        };

        this.api_uuid = props.api.id;
        this.childResources = [];
    }

    componentDidMount() {
        const apiProduct = new APIProduct();
        const promised_api = apiProduct.getSwagger(this.api_uuid);
        promised_api
            .then((response) => {
                if (response.obj.paths !== undefined) {
                    this.setState({ paths: response.obj.paths });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }

    render() {
        const {
            policyLevel, apiPolicies, scopes, paths,
        } = this.state;
        const { classes, api } = this.props;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (paths === null) {
            return <Progress />;
        }
        const apiResources = api.apis;
        const apiResourcesDetails = apiResources
            .map((key) => {
                const operations = key.operations.map(item => item.target);
                const filteredPaths  = Object.keys(paths).filter(item => operations.includes(item))
                .reduce((acc,cur) => {
                    acc[cur] = paths[cur]
                    return acc;
                },{})
                return {
                    name:key.name,
                    paths:filteredPaths
                }
            })
            console.log("**", apiResourcesDetails)
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage id='Apis.Details.Resources.Resources.resources' defaultMessage='Resources' />
                    </Typography>
                    <Link to={'/api-products/' + api.id + '/resources/edit'}>
                        <Button size='small' className={classes.button}>
                            <Icon className={classes.buttonIcon}>edit</Icon>
                            <FormattedMessage
                                id='Apis.Details.Resources.Resources.edit.resources.button'
                                defaultMessage='Edit Resources'
                            />
                        </Button>
                    </Link>
                </div>
                <div className={classes.contentWrapper}>
                    <List>
                        {Object.keys(apiResourcesDetails).map((key) => {
                            const resource = apiResourcesDetails[key];
                            return( <div className={classes.root}>
                                    <Typography className={classes.heading} variant='h5'>
                                    {resource.name}
                                </Typography>
                                    <div className={classes.root}>
                                    {Object.keys(resource.paths).map((key) => {
                                        const path = resource.paths[key];
                                        const that = this;
                                        return (
                                            <div>
                                                <ExpansionPanel defaultExpanded className={classes.expansionPanel}>
                                                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                                                        <Typography className={classes.heading} variant='h6'>
                                                            {key}
                                                        </Typography>
                                                    </ExpansionPanelSummary>
                                                    <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                                                        {Object.keys(path).map((innerKey) => {
                                                            return CONSTS.HTTP_METHODS.includes(innerKey) ? (
                                                                <Resource
                                                                    path={key}
                                                                    method={innerKey}
                                                                    methodData={path[innerKey]}
                                                                    updatePath={that.updatePath}
                                                                    scopes={scopes}
                                                                    apiPolicies={apiPolicies}
                                                                    isAPIProduct
                                                                    onRef={ref => this.childResources.push(ref)}
                                                                    policyLevel={policyLevel}
                                                                />
                                                            ) : null;
                                                        })}
                                                    </ExpansionPanelDetails>
                                                </ExpansionPanel>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>);
                        })}
                    </List>
                </div>
            </div>
        );
    }
}

ProductResourcesView.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({ id: PropTypes.string }).isRequired,
};

export default withStyles(styles)(ProductResourcesView);
