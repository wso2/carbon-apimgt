import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { withStyles } from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import { mailFolderListItems, otherMailFolderListItems } from './tileData';
import CustomIcon from './CustomIcon';


const styles = theme => ({
    linkColor: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    linkColorMain: {
        color: theme.palette.secondary.main,
    },
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: 90,
        textAlign: 'center',
        height: '100vh',
    },
    leftLInk: {
        paddingTop: 10,
        paddingBottom: 10,  
        fontSize: 11,
        cursor: 'pointer'
    },
    leftLInkMain: {
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        paddingBottom: 5,
        paddingTop: 5,
        height: 70,
        fontSize: 12,
        cursor: 'pointer',
    }

});

class LeftMenu extends React.Component {
  state = {
    active: 'overview',
  };

  handleDrawerOpen = () => {
    this.setState({ open: true });
  };

  handleDrawerClose = () => {
    this.setState({ open: false });
  };
  handleMenuSelect(menuLink) {
    this.setState({active:menuLink});
    console.info(menuLink);
  }

  render() {
    const { classes, theme } = this.props;
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const strokeColorMain = theme.palette.secondary.main;
    return (
        <div className={classes.LeftMenu}>
            <div className={classes.leftLInkMain}>
                <CustomIcon strokeColor={strokeColorMain} width={32} height={32} icon="api" />
                <div className={classes.linkColorMain}>APIs</div>
            </div>
            <div className={classes.leftLInk} 
                onClick={( () => this.handleMenuSelect('overview') ) }
                style={{backgroundColor: this.state.active === "overview" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="overview" />
                <div className={classes.linkColor}>OVERVIEW</div>
            </div>
            <div className={classes.leftLInk} 
                onClick={( () => this.handleMenuSelect('credentials') ) }
                style={{backgroundColor: this.state.active === "credentials" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="credentials" />
                <div className={classes.linkColor}>CREDENTIALS</div>
            </div>
            <div className={classes.leftLInk}
                onClick={( () => this.handleMenuSelect('comments') ) }
                style={{backgroundColor: this.state.active === "comments" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="comments" />
                <div className={classes.linkColor}>COMMENTS</div>
            </div>
            <div className={classes.leftLInk}
                onClick={( () => this.handleMenuSelect('test') ) }
                style={{backgroundColor: this.state.active === "test" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="test" />
                <div className={classes.linkColor}>TEST</div>
            </div>
            <div className={classes.leftLInk}
                onClick={( () => this.handleMenuSelect('docs') ) }
                style={{backgroundColor: this.state.active === "docs" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="docs" />
                <div className={classes.linkColor}>DOCS</div>
            </div>
            <div className={classes.leftLInk}
                onClick={( () => this.handleMenuSelect('sdk') ) }
                style={{backgroundColor: this.state.active === "sdk" ? theme.palette.background.appBar : ''}}
                >
                <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="sdk" />
                <div className={classes.linkColor}>SDK</div>
            </div>
        </div>
        
    );
  }
}

LeftMenu.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(LeftMenu);
