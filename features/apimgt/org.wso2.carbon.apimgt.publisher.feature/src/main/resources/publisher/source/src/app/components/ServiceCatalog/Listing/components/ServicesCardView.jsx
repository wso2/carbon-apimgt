import React from 'react';
import Grid from '@material-ui/core/Grid';
import ServiceCard from './ServiceCard';

/**
 *
 * @returns
 */
export default function ServicesCardView(props) {
    const { serviceList, onDelete } = props;

    return (
        <Grid
            container
            direction='row'
            justify='flex-start'
            alignItems='flex-start'
            spacing={3}
        >
            {serviceList.map((service) => (
                <Grid item>
                    <ServiceCard onDelete={onDelete} service={service} />
                </Grid>
            ))}
        </Grid>
    );
}
