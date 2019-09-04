/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import DoneIcon from '@material-ui/icons/CheckCircle';
import CancelIcon from '@material-ui/icons/Cancel';
import Select from '@material-ui/core/Select';
import { FormattedMessage, injectIntl } from 'react-intl';
import AuthManager from '../../../../data/AuthManager';

const styles = theme => ({
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
    },
    dense: {
        marginTop: theme.spacing.unit,
    },
    menu: {
        width: 200,
    },
    button: {
        minWidth: theme.spacing.unit * 2,
        padding: 0,
    },
    textArea: {
        flex: 1,
    },
});

class InlineEditableField extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            editable: false,
            newValue: null,
            initValue: null,
        };
        this.editInlineToggle = this.editInlineToggle.bind(this);
        this.saveField = this.saveField.bind(this);
        this.isNotCreator = AuthManager.isNotCreator();
    }
    editInlineToggle() {
        this.setState({ editable: !this.state.editable, newValue: this.props.fieldValue });
    }

    handleChange = name => (event) => {
        this.setState({
            [name]: event.target.value,
        });
    };
    static getDerivedStateFromProps(props, current_state) {
        if (current_state.initValue !== props.fieldValue) {
            return {
                newValue: props.fieldValue,
            };
        }
        return null;
    }
    componentDidMount() {
        this.setState({ newValue: this.props.fieldValue, initValue: this.props.fieldValue });
    }

    saveField() {
        this.setState({ editable: false });
        this.props.saveFieldCallback(this.props.fieldName, this.state.newValue, this.fieldIndex);
    }
    render() {
        const { classes, intl } = this.props;

        if (this.state.editable) {
            if (typeof this.props.fieldIndex === 'number') {
                this.fieldIndex = this.props.fieldIndex;
            } else {
                this.fieldIndex = false;
            }
            return (
                <form className={classes.container} noValidate autoComplete='off' onSubmit={this.saveField}>
                    {this.props.type === 'select' && (
                        <Select
                            native
                            value={this.state.newValue}
                            onChange={this.handleChange('newValue')}
                            disabled={this.isNotCreator}
                            inputProps={{
                                name: 'newValue',
                                id: 'newValue-native-simple',
                            }}
                        >
                            {this.props.defaultValues.map(val => (
                                <option value={val}>{val}</option>
                            ))}
                        </Select>
                    )}
                    {this.props.type === 'input' &&
                    <input type='text' onChange={this.handleChange('newValue')} value={this.state.newValue}
                           disabled={this.isNotCreator}
                    />}
                    {this.props.type === 'textarea' &&
                    <TextField
                        id='standard-textarea'
                        placeholder={intl.formatMessage({
                            id: 'Apis.Details.Resources.InlineEditableField.placeholder',
                            defaultMessage: 'Placeholder',
                        })}
                        multiline
                        className={classes.textArea}
                        margin='normal'
                        onChange={this.handleChange('newValue')}
                        value={this.state.newValue}
                        disabled={this.isNotCreator}
                    />}
                    <Button className={classes.button} onClick={this.saveField} disabled={this.isNotCreator}>
                        <DoneIcon />
                    </Button>
                    <Button className={classes.button} onClick={this.editInlineToggle} disabled={this.isNotCreator}>
                        <CancelIcon />
                    </Button>
                </form>
            );
            {
                /* <input type="text" className="inline-edit-input" defaultValue={this.props.fieldValue} onChange={this.handleValueChange}/> */
            }
        } else {
            return (
                <span onClick={this.editInlineToggle} className='fieldView'>
                    {this.state.newValue ?
                        <span>{this.state.newValue}</span> :
                        this.props.initText ?
                            <span>{this.props.initText}</span>
                            : (<span>
                                <FormattedMessage
                                    id='Apis.Details.Resources.InlineEditableField.click.to.add'
                                    defaultMessage='Click to add'
                                />
                            </span>)
                    }
                </span>
            );
        }
    }
}

InlineEditableField.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(InlineEditableField));
