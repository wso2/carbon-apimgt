import ReactDOM from 'react-dom'
import React , {Component } from 'react'
import Publisher from "./src/App.js"

//ReactDOM.render(<Publisher/>, document.getElementById("app-root"));

class Sample extends Component {
    render() {
            return (<div>Hello, world!</div>);
        }
}

ReactDOM.render(<Sample/>, document.getElementById("app-root"));

