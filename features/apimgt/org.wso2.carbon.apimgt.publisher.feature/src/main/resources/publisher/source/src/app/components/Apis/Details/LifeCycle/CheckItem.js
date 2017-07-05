import React from 'react'

const CheckItem = (props) => {
    return (
        <div className="form-actions">
            <label className="control-label col-sm-2"/>
            <label className="checkbox col-sm-4" id="checkListItem">
                <input type="checkbox" id="checkItem" name="checkItem" value={props.item.name}
                       checked={props.item.value}/>
                <span className="helper">{props.item.name}</span>
            </label>
        </div>
    );
};

export default CheckItem