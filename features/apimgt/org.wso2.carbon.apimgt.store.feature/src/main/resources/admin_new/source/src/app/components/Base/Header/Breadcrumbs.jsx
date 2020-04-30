import React from 'react';
import Typography from '@material-ui/core/Typography';
import { Breadcrumbs as MUIBreadcrumbs } from '@material-ui/core';
import Link from '@material-ui/core/Link';

/**
 * Render header component
 * @param {JSON} event .
 * @returns {void}.
 */
function handleClick(event) {
    event.preventDefault();
}

/**
 * Render breadcrumb component
 * @returns {JSX} breadcrumbs.
 */
export default function Breadcrumbs() {
    return (
        <MUIBreadcrumbs aria-label='breadcrumb'>
            <Link color='inherit' href='/' onClick={handleClick}>
        Material-UI
            </Link>
            <Link color='inherit' href='/getting-started/installation/' onClick={handleClick}>
        Core
            </Link>
            <Typography color='textPrimary'>Breadcrumb</Typography>
        </MUIBreadcrumbs>
    );
}
