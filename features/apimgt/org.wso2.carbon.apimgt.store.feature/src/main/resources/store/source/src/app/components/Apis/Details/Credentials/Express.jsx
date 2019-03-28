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

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import {withStyles} from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import Avatar from '@material-ui/core/Avatar';
import DoneIcon from '@material-ui/icons/Done';
// import ApiContext from '../../ApiContext'
import Api from '../../../../data/api';


const styles = theme => ({
    chip: {
        margin: theme.spacing.unit,
      },
});

class ExpressMode extends Component {

    constructor(props) {
        super(props);
        this.state = {
            tierSelected: null,
        };
    }
    
   
    render() {
        const { classes  } = this.props;
        return (
            <Grid container spacing={24}>
                    <Grid item>
                        <Chip
                            avatar={<Avatar>MB</Avatar>}
                            label="Primary Clickable Chip"
                            clickable
                            className={classes.chip}
                            color="primary"
                            deleteIcon={<DoneIcon />}
                        />
                    </Grid>
                </Grid>


        );
    }
}

ExpressMode.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(ExpressMode);