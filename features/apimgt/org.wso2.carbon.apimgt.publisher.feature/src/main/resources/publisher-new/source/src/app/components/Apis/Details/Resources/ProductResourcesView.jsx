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
import {withStyles, withTheme} from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import {Link} from 'react-router-dom';

import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';

import InputLabel from '@material-ui/core/InputLabel';
import TextField from '@material-ui/core/TextField';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Checkbox from '@material-ui/core/Checkbox';
import EditIcon from '@material-ui/icons/edit';
import {FormattedMessage} from "react-intl";
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
class ProductResourcesView extends React.PureComponent {
    render() {
        const {classes, api} = this.props;
        const apiResources = api.apis;
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage id='Apis.Details.Resources.Resources.resources' defaultMessage='Resources'/>
                    </Typography>
                    <Button size='small' className={classes.button}>
                        <EditIcon className={classes.buttonIcon}/>
                        <FormattedMessage id='Apis.Details.Resources.Resources.edit.resources.button'
                                          defaultMessage='Edit Resources'/>
                    </Button>
                </div>
                <div className={classes.contentWrapper}>
                    <List>
                        {Object.keys(apiResources).map((key) => {
                            const resource = apiResources[key];
                            return (
                                <div className={classes.root}>
                                    <Typography className={classes.heading} variant='h5'>
                                        {resource.name}
                                    </Typography>

                                    {Object.keys(resource.operations).map((innerKey) => {
                                        const operation = (resource.operations)[innerKey];
                                        return (
                                            <div>
                                                <ExpansionPanel defaultExpanded className={classes.expansionPanel}>
                                                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                                                        <Typography className={classes.heading} variant='h6'>
                                                            {operation.uritemplate}
                                                        </Typography>
                                                    </ExpansionPanelSummary>
                                                    <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                                                        <Resource path={operation.uritemplate}
                                                                  method={operation.httpVerb}
                                                                  scopes={api.scopes}
                                                                  addRemoveToDeleteList={this.addRemoveToDeleteList}
                                                        />
                                                    </ExpansionPanelDetails>
                                                </ExpansionPanel>
                                            </div>);
                                    })}
                                </div>
                            );
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
    api: PropTypes.shape({id: PropTypes.string}).isRequired,
};

export default withStyles(styles)(ProductResourcesView);
