import React, {Component} from 'react'
import ReactDom from 'react-dom'

class Clock extends Component {

    render() {
        return (
            <h3>
                hello time = {this.props.ctime}
            </h3>
        );
    }
}

class Outer extends Component {

    constructor(props) {
        super(props);
        this.state = {
            ctime: "init"
        }
    }

    componentDidMount() {
        setInterval(() => this.tupdater(), 1000);
    }

    tupdater() {
        this.setState({
            time: new Date().getTime()
        })
    }

    render() {
        return (
            <div>
                <Clock ctime={this.state.ctime}/>
            </div>
        );
    }
}

export default Outer;