import React, { Component } from "react";
import {
  ListItemIcon,
  Drawer,
  List,
  Divider,
  withStyles,
  ListItem,
  ListItemText
} from "@material-ui/core";
import APIsIcon from "@material-ui/icons/Power";
import EndpointsIcon from "@material-ui/icons/ZoomOutMapOutlined";
import HomeIcon from "@material-ui/icons/Home";

const styles = theme => ({
  list: {
    width: 250
  },
  drawerStyles: {
    top: theme.mixins.toolbar["@media (min-width:600px)"].minHeight
  }
});

const homeIcon = (
  <div>
    <ListItem button>
      <ListItemIcon>
        <HomeIcon />
      </ListItemIcon>
      <ListItemText primary="Home" />
    </ListItem>
  </div>
);

const globalPages = (
  <div>
    <ListItem button>
      <ListItemIcon>
        <APIsIcon />
      </ListItemIcon>
      <ListItemText primary="APIs" />
    </ListItem>
    <ListItem button>
      <ListItemIcon>
        <EndpointsIcon />
      </ListItemIcon>
      <ListItemText primary="Endpoints" />
    </ListItem>
  </div>
);

class GlobalNavBar extends Component {
  render() {
    const { open, toggleGlobalNavBar, classes } = this.props;
    // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
    const commonStyle = {
      style: { top: 64 }
    };
    return (
      <div>
        <Drawer
          className={classes.drawerStyles}
          PaperProps={commonStyle}
          SlideProps={commonStyle}
          ModalProps={commonStyle}
          BackdropProps={commonStyle}
          open={open}
          onClose={toggleGlobalNavBar}
        >
          <div
            tabIndex={0}
            role="button"
            onClick={toggleGlobalNavBar}
            onKeyDown={toggleGlobalNavBar}
          >
            <div className={classes.list}>
              <List>{homeIcon}</List>
              <Divider />
              <List>{globalPages}</List>
            </div>
          </div>
        </Drawer>
      </div>
    );
  }
}

export default withStyles(styles)(GlobalNavBar);
