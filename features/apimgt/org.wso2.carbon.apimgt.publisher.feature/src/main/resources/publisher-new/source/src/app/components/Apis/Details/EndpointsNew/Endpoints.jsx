/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useState } from 'react';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Switch from '@material-ui/core/Switch';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Collapse from '@material-ui/core/Collapse';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';

import EndpointOverview from './EndpointOverview';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    endpointTypesWrapper: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
        margin: '2px',
    },
    root: {
        flexGrow: 1,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: theme.spacing.unit * 2,
    },
    endpointContent: {
        margin: theme.spacing.unit * 2,
        display: 'flex',
        flexDirection: 'row',
    },
    buttonSection: {
        marginTop: theme.spacing.unit * 2,
    },
});

/**
 * The base layout of the endpoits view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Endpoints(props) {
    const { classes } = props;
    const [productionChecked, setProductionChecked] = useState(true);

    return (
        <div className={classes.root}>
            <form>
                <div>
                    <Typography variant='h4' align='left' className={classes.titleWrapper}>
                        <FormattedMessage id='Endpoints' defaultMessage='Endpoints' />
                    </Typography>
                </div>
                <ApiContext.Consumer>
                    {({ api }) => (
                        <div>
                            <Grid container>
                                <Grid item xs={12}>
                                    <div className={classes.endpointTypesWrapper}>
                                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                                            <FormattedMessage
                                                id='Production.and.SandBox'
                                                defaultMessage='Production and SandBox'
                                            />
                                        </Typography>
                                        <Switch
                                            checked={productionChecked}
                                            onChange={() => setProductionChecked(!productionChecked)}
                                            value='checkedProduction'
                                            color='primary'
                                        />
                                    </div>
                                    <Divider variant='middle' />
                                    <Collapse in={productionChecked}>
                                        <EndpointOverview api={api} />
                                    </Collapse>
                                    {/* {(productionChecked === true) ?  : <div />} */}
                                </Grid>
                                <Grid item xs={12}>
                                    <div className={classes.endpointTypesWrapper}>
                                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                                            <FormattedMessage id='Prototype' defaultMessage='Prototype' />
                                        </Typography>
                                        <Switch
                                            checked={!productionChecked}
                                            onChange={() => setProductionChecked(!productionChecked)}
                                            value='checkedSandbox'
                                            color='primary'
                                        />
                                    </div>
                                    <Divider variant='middle' />
                                    <Collapse in={!productionChecked}>
                                        Portotyped
                                    </Collapse>
                                    {/* {(productionChecked === false) ? <EndpointOverview /> : <div />} */}
                                </Grid>
                            </Grid>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={16}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    <Button type='submit' variant='contained' color='primary'>
                                        <FormattedMessage id='save' defaultMessage='Save' />
                                    </Button>
                                </Grid>
                                <Grid item>
                                    <Button onClick={() => this.props.history.push('/apis')}>
                                        <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                    </Button>
                                </Grid>
                            </Grid>
                        </div>)}
                </ApiContext.Consumer>
            </form>
        </div>
    );
}

Endpoints.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.shape({}),
        buttonSection: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
    }).isRequired,
};

export default injectIntl(withStyles(styles)(Endpoints));
