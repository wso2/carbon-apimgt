import React from 'react';
import { useTheme } from '@material-ui/core/styles';
import ApiThumbClassic from 'AppComponents/Apis/Listing/APICards/ApiThumbClassic';
import APIThumbPlain from 'AppComponents/Apis/Listing/APICards/APIThumbPlain';

export default function SimplePaper(props) {
    const theme = useTheme();
    const { custom } = theme;
    if (!custom.thumbnailTemplates || !custom.thumbnailTemplates.active) {
        return (
<<<<<<< HEAD
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classNames('image-thumbnail', classes.card)}
            >
                {isMonetizationEnabled && (
                    <div className={classes.textblock}>{api.monetizationLabel}</div>
                )}
                <CardMedia>
                    <Link to={detailsLink} aria-hidden='true' className={classes.suppressLinkStyles}>
                        {!defaultImage && ImageView}
                        {defaultImage && <img src={app.context + defaultImage} alt='img' />}
                    </Link>
                </CardMedia>
                {showInfo && (
                    <CardContent classes={{ root: classes.apiDetails }}>
                        <Link to={detailsLink} className={classes.textWrapper}>
                            <Typography
                                className={classes.thumbHeader}
                                variant='h5'
                                gutterBottom
                                onClick={this.handleRedirectToAPIOverview}
                                title={name}
                            >
                                {name}
                            </Typography>
                        </Link>
                        <div className={classes.row}>
                            <Typography variant='caption' gutterBottom align='left' className={classes.thumbBy}>
                                <FormattedMessage defaultMessage='By' id='Apis.Listing.ApiThumb.by' />
                                <FormattedMessage defaultMessage=' : ' id='Apis.Listing.ApiThumb.by.colon' />
                                {provider}
                            </Typography>
                        </div>
                        <div className={classes.thumbInfo}>
                            <div className={classes.row}>
                                <div className={classes.thumbLeft}>
                                    <Typography variant='subtitle1' component='div'>{version}</Typography>
                                    <Typography variant='caption' component='div' gutterBottom align='left'>
                                        <FormattedMessage defaultMessage='Version' id='Apis.Listing.ApiThumb.version' />
                                    </Typography>
                                </div>
                            </div>
                            <div className={classes.row}>
                                <div className={classes.thumbRight}>
                                    <Typography
                                        variant='subtitle1'
                                        component='div'
                                        align='right'
                                        className={classes.contextBox}
                                    >
                                        {context}
                                    </Typography>
                                    <Typography
                                        variant='caption'
                                        gutterBottom
                                        align='right'
                                        className={classes.context}
                                        Component='div'
                                    >
                                        <FormattedMessage defaultMessage='Context' id='Apis.Listing.ApiThumb.context' />
                                    </Typography>
                                </div>
                            </div>
                        </div>
                        <div className={classes.thumbInfo}>
                            {showRating && <div className={classes.thumbLeftAction}>
                                <Typography
                                    variant='subtitle1'
                                    component='div'
                                    aria-label='API Rating'
                                    gutterBottom
                                    align='left'
                                    className={classNames('api-thumb-rating', classes.ratingWrapper)}
                                >
                                    <StarRatingBar
                                        apiRating={api.avgRating}
                                        apiId={api.id}
                                        isEditable={false}
                                        showSummary={false}
                                    />
                                </Typography>
                            </div>}
                            <div className={classes.thumbRight}>
                                <Typography
                                    variant='subtitle1'
                                    gutterBottom
                                    align='right'
                                    className={classes.chipWrapper}
                                >
                                    {(api.type === 'GRAPHQL' || api.transportType === 'GRAPHQL') && (
                                        <Chip
                                            label={api.transportType === undefined ? api.type : api.transportType}
                                            color='primary'
                                        />
                                    )}
                                    {(api.lifeCycleStatus === 'PROTOTYPED') && (
                                        <Chip
                                        label={api.apiType === 'APIProduct' ? api.state : api.lifeCycleStatus}
                                        color='default'
                                    />
                                    )}
                                </Typography>
                            </div>
                        </div>
                    </CardContent>
                )}
            </Card>
=======
            <ApiThumbClassic {...props} />
>>>>>>> Updating api thumb
        );
    }

    return (
        <APIThumbPlain {...props} />
    )


}
