import React from 'react'

const TransitionStateButton = (props) => {
    return (
        <div className="btn-group" role="group">
            <input type="button" style={{color: 'black'}} className="btn btn-primary lc-state-btn" data-lcstate={props.state.targetState}
                   defaultValue={props.state.event} onClick={props.updateLifeCycleState}/>
        </div>
    );
};

export default TransitionStateButton