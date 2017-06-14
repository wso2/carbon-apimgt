import React from 'react'

const ListingHeader = (props) => {
    return (
        <div className="well well-sm">
            <strong>Display</strong>
            <div className="btn-group">
                <a href="#" id="list" className={props.isActive('list')}
                   onClick={() => props.setListType('list')}>
                    <span className="glyphicon glyphicon-th-list"/>List
                </a>
                <a href="#" id="grid" className={props.isActive('grid')}
                   onClick={() => props.setListType('grid')}>
                    <span className="glyphicon glyphicon-th"/>Grid
                </a>
            </div>
        </div>
    );
};

export default ListingHeader