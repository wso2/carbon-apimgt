import React, {Component} from 'react'

const APINotFound = (props) => {
    return (
        <div>
            <div className="message message-danger">
                <h4><i className="icon fw fw-error"/>404 API Not Found!</h4>
                <p>
                    Can't find an API associate with the given API ID <span
                    style={{color: 'green'}}> {props.match.params.api_uuid} </span>
                </p>
            </div>

        </div>
    );
};

export default APINotFound