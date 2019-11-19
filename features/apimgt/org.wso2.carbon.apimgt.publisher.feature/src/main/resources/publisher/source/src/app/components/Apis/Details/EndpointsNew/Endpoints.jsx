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
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';

import EndpointOverview from './EndpointOverview';
import ApiContext from '../components/ApiContext';

const styles = (theme) => ({
    endpointTypesWrapper: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
        margin: '2px',
    },
    root: {
        flexGrow: 1,
        paddingRight: '10px',
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    buttonSection: {
        marginTop: theme.spacing(2),
    },
});

/**
 * The base component of the endpoints view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Endpoints(props) {
    const { classes } = props;
    const [modifiedAPI, setModifiedAPI] = useState({});

    const saveAPI = (oldAPI, updateFunc) => {
        if (modifiedAPI !== {}) {
            updateFunc(modifiedAPI);
        }
    };

    return (
        <>
            <Typography variant='h4' gutterBottom>
                <FormattedMessage
                    id='Apis.Details.EndpointsNew.Endpoints.endpoints.header'
                    defaultMessage='Endpoints'
                />
            </Typography>
            <ApiContext.Consumer>
                {({ api, updateAPI }) => (
                    <div>
                        <Grid container>
                            <Grid item xs={12}>
                                <EndpointOverview api={api} onChangeAPI={setModifiedAPI} />
                            </Grid>
                        </Grid>
                        <Grid
                            container
                            direction='row'
                            alignItems='flex-start'
                            spacing={1}
                            className={classes.buttonSection}
                        >
                            <Grid item>
                                <Button
                                    type='submit'
                                    variant='contained'
                                    color='primary'
                                    onClick={() => saveAPI(modifiedAPI, updateAPI)}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.EndpointsNew.Endpoints.save'
                                        defaultMessage='Save'
                                    />
                                </Button>
                            </Grid>
                            <Grid item>
                                <Link to={'/apis/' + api.id + '/overview'}>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.EndpointsNew.Endpoints.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            </Grid>
                        </Grid>
                    </div>
                )}
            </ApiContext.Consumer>
        </>
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
