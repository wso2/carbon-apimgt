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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    linkColor: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    linkColorMain: {
        color: theme.palette.secondary.main,
    },
    rightMenu: {
        width: window.innerWidth - theme.custom.contentAreaWidth - theme.custom.leftMenuWidth - 50,
        borderLeft: 'solid 1px ' + theme.palette.secondary.main,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        right: 0,
        top: 0,
        overflowY: 'auto',
        backgroundColor: theme.palette.background.paper,
        padding: 10,
        fontSize: theme.typography.fontSize,
    },
    rightMenuToggle: {
        backgroundColor: theme.palette.secondary.main,
        padding: 5,
        width: 32,
        height: 32,
        borderTopLeftRadius: 5,
        borderBottomLeftRadius: 5,
        cursor: 'pointer',
        position: 'fixed',
        marginLeft: -53,
        marginTop: '-10px',
    },
});
/**
 *
 *
 * @class RightPanel
 * @extends {React.Component}
 */
class RightPanel extends React.Component {
    /**
     *Creates an instance of RightPanel.
     * @param {*} props
     * @memberof RightPanel
     */
    constructor(props) {
        super(props);
        this.toggleRightPanel = this.toggleRightPanel.bind(this);
    }

    state = {
        open: true,
        toggleButtonTop: 0,
    };

    /**
     *
     *
     * @memberof RightPanel
     */
    handleScroll = () => {
        const scrollTop = window.scrollY;

        if (scrollTop === 0) {
            this.setState({ toggleButtonTop: 10 });
        } else if (scrollTop > 0 && scrollTop < 72) {
            this.setState({ toggleButtonTop: scrollTop });
        } else {
            this.setState({ toggleButtonTop: 72 });
        }
    };

    /**
     *
     *
     * @memberof RightPanel
     */
    componentWillUnmount() {
        window.removeEventListener('scroll', this.handleScroll);
    }

    /**
     *
     *
     * @memberof RightPanel
     */
    componentDidMount() {
        // We are hidding the panel by default if the screen widht is less than 1600
        this.handleScroll();
        window.addEventListener('scroll', this.handleScroll);

        let hideByDefault = false;
        if (window.innerWidth > 1600) {
            hideByDefault = true;
        }
        this.setState({ open: hideByDefault });
    }

    /**
     *
     *
     * @memberof RightPanel
     */
    toggleRightPanel() {
        this.setState({ open: !this.state.open });
    }

    /**
     *
     *
     * @returns
     * @memberof RightPanel
     */
    render() {
        const { classes, theme } = this.props;

        return (
            <div className={classes.rightMenu}>
                <div onClick={this.toggleRightPanel} className={classes.rightMenuToggle} style={{ marginTop: '-' + this.state.toggleButtonTop + 'px' }}>
                    ?
                </div>
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu.
            </div>
        );
    }
}

RightPanel.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(RightPanel);
