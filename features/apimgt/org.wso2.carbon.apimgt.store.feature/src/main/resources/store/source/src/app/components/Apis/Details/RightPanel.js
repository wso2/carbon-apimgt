import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';


const styles = theme => ({
    linkColor: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    linkColorMain: {
        color: theme.palette.secondary.main,
    },
    rightMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        textAlign: 'center',
        height: '100vh',
        backgroundColor: theme.palette.background.paper,
        borderLeft: 'solid 1px ' + theme.palette.secondary.main,
        position: 'absolute',
        right: 0,
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
    },
    rightMenuToggle: {
        backgroundColor: theme.palette.secondary.main,
        padding: 5,
        width: 32,
        height: 32,
        borderTopLeftRadius: 5,
        borderBottomLeftRadius: 5,
        cursor: 'pointer',
        position: 'absolute',
        marginLeft: -32,
    },
    rightPanelContent: {
        width: window.innerWidth - theme.palette.custom.contentAreaWidth - theme.palette.custom.leftMenuWidth,
        padding: 10,
        overflowY: 'auto',
        height: '100vh',
    }

});

class RightPanel extends React.Component {
    constructor(props){
        super(props);
        this.toggleRightPanel = this.toggleRightPanel.bind(this);
    }
  state = {
    open: true,
  };

  componentDidMount() { // We are hidding the panel by default if the screen widht is greater than 1600
    let hideByDefault = false;
    if(window.innerWidth > 1600){
        hideByDefault = true;
    }
    this.setState({open: hideByDefault} );
  }
  toggleRightPanel() {

    this.setState({open: !this.state.open} );
  }
  render() {
    const { classes, theme } = this.props;
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const strokeColorMain = theme.palette.secondary.main;
    
    return (
        <div className={classes.rightMenu}>
            <div onClick={this.toggleRightPanel} className={classes.rightMenuToggle}>?</div> 
            
                <Slide direction="left"  mountOnEnter unmountOnExit in={this.state.open} >
                    <div className={classes.rightPanelContent}>
                        <Typography>
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu.

Nulla pharetra dolor ut elementum aliquam. Vivamus iaculis convallis nunc vel accumsan. Vestibulum sed consequat velit, eget rhoncus tellus. Ut vel arcu lobortis, condimentum magna quis, ultricies turpis. Nunc et dolor metus. Donec fringilla quam ut metus pellentesque, a aliquet purus rutrum. Aenean molestie, dui eu euismod feugiat, tellus felis suscipit leo, non lacinia metus nisl quis nulla. In condimentum congue turpis, efficitur finibus felis interdum id. Suspendisse et cursus tortor, vitae gravida nunc.

Nulla urna metus, consequat vel iaculis et, rhoncus nec nibh. Curabitur efficitur felis nec pellentesque aliquam. Duis luctus turpis velit. Aenean eu arcu vel odio accumsan consectetur et sit amet lectus. Donec eget ipsum imperdiet, mollis enim vel, ullamcorper massa. Sed condimentum quam ut faucibus tincidunt. Nunc efficitur auctor lacus, non pharetra ligula ultricies ac. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Morbi volutpat ultrices nisi. Mauris massa risus, venenatis ac mollis eu, elementum eget mi.

Aliquam tempor, odio in hendrerit porta, arcu neque tincidunt odio, at pulvinar enim arcu non nibh. Ut lacinia lorem eu ligula rhoncus vehicula. Praesent condimentum purus vel urna maximus condimentum. Praesent quis tortor et purus mollis fringilla. Nunc pharetra neque in ipsum volutpat pulvinar. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis maximus euismod dolor, ac efficitur massa porttitor at. Duis a enim vitae metus imperdiet consectetur. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Donec ut egestas arcu. Vestibulum suscipit elit augue, quis pharetra ex pharetra eu. Aliquam mattis metus eget efficitur pulvinar.

In hac habitasse platea dictumst. Nulla sed sollicitudin tortor. Sed non faucibus ipsum. Cras blandit auctor sapien, eget fringilla mi vulputate non. Maecenas quis fermentum sapien. Vestibulum sit amet nulla dui. Donec pellentesque libero non diam varius lacinia.
                        </Typography>
                    </div>
                </Slide>
        </div>
        
    );
  }
}

RightPanel.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(RightPanel);
