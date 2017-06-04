import React, {Component} from 'react'

const NotFound = (props) => {
    return (
        <div>
            <div className="message message-danger">
                <h4><i className="icon fw fw-error"/>404 Page Not Found!</h4>
                <p>
                    Sorry the page you are looking for <span style={{color: 'green'}}> {props.location.pathname} </span>
                    is not available.
                </p>
            </div>

        </div>
    );
};

export default NotFound