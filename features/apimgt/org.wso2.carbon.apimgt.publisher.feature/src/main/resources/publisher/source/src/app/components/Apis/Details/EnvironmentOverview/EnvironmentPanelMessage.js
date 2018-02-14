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

import React, {Component} from 'react'
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import {withStyles} from 'material-ui/styles';

const styles = theme => ({
    messageLabel: {
        fontSize: '1.5em'
    }
});

class EnvironmentPanelMessage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isAuthorize: true,
        };
    }

    render() {
        const {message, classes} = this.props;

        return (
            <Grid container justify={'center'} alignItems={'center'}>
                <Grid item>
                    <Typography variant="display1" gutterBottom
                                className={classes.messageLabel}>
                        {message}
                    </Typography>
                </Grid>
            </Grid>
        );
    }
}

export default withStyles(styles)(EnvironmentPanelMessage);
