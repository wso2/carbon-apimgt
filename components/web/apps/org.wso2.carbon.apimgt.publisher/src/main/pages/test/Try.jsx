import React, {Component} from 'react';
import ReactDOM from 'react-dom';

/*function Tick(prop) {
 const element = (
 <div>
 <p>
 <h1>
 Hello {prop.name}
 </h1>
 Current time is {new Date().toLocaleTimeString()}
 </p>
 </div>
 );
 return element;
 }*/

class Tick extends Component {
    constructor(props) {
        super(props);
        this.state = {
            date: new Date(),
            number: 1
        };
        // this.tick = this.tick.bind(this);
    }

    /**
     * lifecycle hooks : hook runs after the component output has been rendered to the DOM
     */
    componentDidMount() {
        console.log("DEBUG: calling componentDidMount");
        this.intId = setInterval(() => {
            this.tick()
        }, 1000);
    }

    componentWillUnmount() {
        console.log("DEBUG: calling componentWillUnmount");
        clearInterval(this.intId);
    }

    tick() {
        this.setState((prevStates, props) => {
            return {
                date: new Date(),
                number: prevStates.number + 3
            }
        });
    }

    render() {
        return (
            <div>
                <h1>
                    Hello {this.props.name}
                </h1>
                <h3>
                    Current number is {this.state.number}
                </h3>
                <p>
                    Current time is {this.state.date.toLocaleTimeString()}
                </p>
            </div>
        );
    }
}

export default Tick;