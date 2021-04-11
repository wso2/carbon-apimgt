import React from 'react';
import Grid from '@material-ui/core/Grid';
import ServiceCard from './ServiceCard';

/**
 *
 * @returns
 */
export default function ServicesCardView(props) {
    const { serviceList, onDelete } = props;
    const numberOfServices = serviceList.length;
    return (
        <Grid
            container
            direction='row'
            justify={numberOfServices > 5 ? 'center' : 'flex-start'}
            alignItems='flex-start'
            spacing={4}
        >
            {serviceList.map((service) => (
                <Grid item>
                    <ServiceCard onDelete={onDelete} service={service} />
                </Grid>
            ))}
        </Grid>
    );
}
