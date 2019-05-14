import React, { Component } from "react";
import { Link } from "react-router-dom";
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
import CustomIcon from '../Shared/CustomIcon';

const styles = theme => ({
  list: {
    width: theme.custom.drawerWidth
  },
  drawerStyles: {
    top: theme.mixins.toolbar["@media (min-width:600px)"].minHeight
  },
  listText: {
    color: theme.palette.getContrastText(theme.palette.background.drawer)
  }
});

class GlobalNavBar extends Component {
  render() {
    const { open, toggleGlobalNavBar, classes, theme } = this.props;
    // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
    const commonStyle = {
      style: { top: 64 }
    };
    const paperStyles = {
      style: {
        backgroundColor: theme.palette.background.drawer,
        top: 64
      }
    };
    let strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    return (
      <div>
        <Drawer
          className={classes.drawerStyles}
          PaperProps={paperStyles}
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
              <List>
                <Link to="/apis">
                  <ListItem button>
                    <ListItemIcon>
                      <CustomIcon width={32} height={32} icon="api" className={classes.listText} strokeColor={strokeColor} />
                    </ListItemIcon>
                    <ListItemText
                      classes={{ primary: classes.listText }}
                      primary="APIs"
                    />
                  </ListItem>
                </Link>
                <Link to="/api-products">
                  <ListItem button>
                    <ListItemIcon>
                      <CustomIcon width={32} height={32} icon="api" className={classes.listText} strokeColor={strokeColor} />
                    </ListItemIcon>
                    <ListItemText
                      classes={{ primary: classes.listText }}
                      primary="API Products"
                    />
                  </ListItem>
                </Link>
                <Link to="/applications">
                  <ListItem button>
                    <ListItemIcon>
                      <CustomIcon width={32} height={32} icon="applications" className={classes.listText} strokeColor={strokeColor} />
                    </ListItemIcon>
                    <ListItemText
                      classes={{ primary: classes.listText }}
                      primary="Applications"
                    />
                  </ListItem>
                </Link>
              </List>
            </div>
          </div>
        </Drawer>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(GlobalNavBar);
