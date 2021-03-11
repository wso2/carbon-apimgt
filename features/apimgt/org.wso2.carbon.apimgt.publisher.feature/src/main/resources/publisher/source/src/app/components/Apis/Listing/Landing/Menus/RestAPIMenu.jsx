
import React from 'react';
import {
    Link as MUILink,
} from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from '../components/LandingMenuItem';
import LandingMenu from '../components/LandingMenu';

const RestAPIMenu = (props) => {
    const { icon, deploying, handleDeploySample } = props;
    return (
        <LandingMenu
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api'
                    defaultMessage='REST API'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                id='itest-id-landing-rest-create-default'
                linkTo='/apis/create/rest'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.content'
                        defaultMessage='Design and prototype a new REST API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.title'
                    defaultMessage='Start From Scratch'
                />
            </LandingMenuItem>

            <LandingMenuItem
                id='itest-id-landing-upload-oas'
                linkTo='/apis/create/openapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.content'
                        defaultMessage='Upload definition or provide the url'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.title'
                    defaultMessage='Import Open API'
                />
            </LandingMenuItem>

            {(deploying !== null && handleDeploySample !== null) && (
                <Box mt={2}>
                    {!deploying ? (
                        <Typography variant='body1'>

                            <MUILink
                                id='itest-id-landing-sample-deploy'
                                onClick={handleDeploySample}
                            >
                                <FormattedMessage
                                    id={'Apis.Listing.SampleAPI.SampleAPI.'
                                        + 'rest.d.sample.title'}
                                    defaultMessage='Deploy Sample API'
                                />
                            </MUILink>
                        </Typography>
                    )
                        : (
                            <CircularProgress
                                size={24}
                            />
                        )}
                    <Typography variant='body2'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPI.rest.d.sample.content'
                            defaultMessage={`This is a sample API for Pizza Shack
                                    online pizza delivery store`}
                        />
                    </Typography>
                </Box>
            )}
        </LandingMenu>
    );
};

export default RestAPIMenu;
