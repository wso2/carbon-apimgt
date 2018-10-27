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
import {withStyles} from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';

const styles = {
    card: {
        minWidth: 275,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
};

function DocumentType(props) {

    const {classes} = props;
    const bull = <span className={classes.bullet}>•</span>;
    const url = ``;

    return (
        <Grid container justify='center'>
            <Grid item sm={5}>
                <Card className={classes.card}>
                    <CardContent>
                        <Typography variant="h5" component="h2">
                            <FormattedMessage
                                id='create.inline.document'
                                defaultMessage='Add New Inline Document'
                            />
                        </Typography>
                        <Divider />
                        <Typography className={classes.title} gutterBottom>
                            <FormattedMessage
                                id='inline.document.description'
                                defaultMessage='This option is used to create an inline document for the API'
                            />
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Link to={url}>
                            <Button variant='contained' color='primary' className={classes.button}>
                                <FormattedMessage
                                    id='create.document'
                                    defaultMessage='Create Document'
                                />
                            </Button>
                        </Link>
                    </CardActions>
                </Card>

                <Card className={classes.card}>
                    <CardContent>
                        <Typography variant="h5" component="h2">
                            <FormattedMessage
                                id='create.file.url.document'
                                defaultMessage='Add New Document From File Or URL'
                            />
                        </Typography>
                        <Divider />
                        <Typography className={classes.title} gutterBottom>
                            <FormattedMessage
                                id='file.document.description'
                                defaultMessage='This option is used to create a document using a file or URL'
                            />
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Link to={url}>
                            <Button variant='contained' color='primary' className={classes.button}>
                                <FormattedMessage
                                    id='create.document'
                                    defaultMessage='Create Document'
                                />
                            </Button>
                        </Link>
                    </CardActions>
                </Card>
            </Grid>
        </Grid>
    );
}

DocumentType.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(DocumentType);