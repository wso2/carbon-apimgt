import React from 'react';
import { NavLink } from 'react-router-dom';

/**
 *
 * Auth method selector class
 * @class Selector
 * @extends {Component}
 */
const SelectLogin = () => {
    const styles = {
        margin: 'auto',
        width: '50%',
    };
    const pStyles = {
        borderRadius: '25px',
        margin: 0,
        padding: '50px',
        position: 'absolute',
        border: '1px solid orange',
        top: '50%',
        left: '50%',
        msTransform: 'translate(-50%, -50%)',
        transform: 'translate(-50%, -50%)',
    };
    return (
        <div style={styles}>
            <div style={pStyles}>
                <h1> Select login method</h1>
                <p>
                    <NavLink to='/login/basic'>
                        <button>Basic Login</button>
                    </NavLink>
                </p>
                <hr />
                <p>
                    <a href='/publisher-new/services/configs'>
                        <button>Login using ext IDP</button>
                    </a>
                </p>
            </div>
        </div>
    );
};

export default SelectLogin;
