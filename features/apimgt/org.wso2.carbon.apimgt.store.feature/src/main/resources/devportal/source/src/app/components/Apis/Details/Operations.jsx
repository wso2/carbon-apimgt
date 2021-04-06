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
import Typography from '@material-ui/core/Typography';
import { withStyles, withTheme } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import Api from 'AppData/api';

/**
 * @param {JSON} props props from parent
 * @returns {JSX} chip item
 */
function RenderMethodBase(props) {
    const { theme, method } = props;
    let chipColor = theme.custom.operationChipColor
        ? theme.custom.operationChipColor[method]
        : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.operationChipColor[method]);
    }
    return <Chip label={method} style={{ backgroundColor: chipColor, color: chipTextColor, height: 20 }} />;
}

RenderMethodBase.propTypes = {
    theme: PropTypes.shape({}).isRequired,
    method: PropTypes.shape({}).isRequired,
};

const RenderMethod = withTheme(RenderMethodBase);
/**
 *
 *
 * @param {*} theme
 */
const styles = {
    root: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    heading: {
        marginRight: 20,
    },
};
/**
 *
 *
 * @class Operations
 * @extends {React.Component}
 */
class Operations extends React.Component {
    /**
     *Creates an instance of Operations.
     * @param {*} props
     * @memberof Operations
     */
    constructor(props) {
        super(props);
        this.state = {
            operations: null,
        };
        this.api = new Api();
    }

    /**
     *
     *
     * @memberof Operations
     */
    componentDidMount() {
        const { api } = this.props;
        this.setState({ operations: api.operations });
    }

    /**
     * @returns {JSX} operations
     * @memberof Operations
     */
    render() {
        const { operations } = this.state;
        if (!operations) {
            return (
                <div>
                    <FormattedMessage
                        id='Apis.Details.Operations.notFound'
                        defaultMessage='Operations Not Found'
                    />
                </div>
            );
        }
        const { classes } = this.props;

        return (
            <Table>
                {operations && operations.length !== 0 && operations.map((item) => (
                    <TableRow style={{ borderStyle: 'hidden' }} key={item.target + '_' + item.verb}>
                        <TableCell>
                            <Typography className={classes.heading} component='p' variant='body2'>
                                {item.target}
                            </Typography>
                        </TableCell>
                        <TableCell>
                            <RenderMethod method={item.verb.toLowerCase()} />
                        </TableCell>
                    </TableRow>
                ))}
            </Table>
        );
    }
}
Operations.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,

};

export default injectIntl(withStyles(styles)(Operations));
