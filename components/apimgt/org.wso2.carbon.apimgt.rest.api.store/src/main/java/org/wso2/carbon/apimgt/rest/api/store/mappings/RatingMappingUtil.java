/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.AvgRating;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.rest.api.store.dto.AvgRatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;

/**
 * Mapping class for Comment Object and Comment List object to DTO and vise-versa
 *
 */
public class RatingMappingUtil {

    /** Converts an ArtifactResource object into corresponding REST API Comment DTO object
     *
     * @param rating Comment object
     * @return a new Comment object corresponding to given ArtifactResource object
     */
    public static RatingDTO fromRatingToDTO(Rating rating) {

        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setRatingId(rating.getUuid());
        ratingDTO.setApiId(rating.getApiId());
        ratingDTO.setSubscriberName(rating.getSubscriber());
        ratingDTO.setRating(Integer.parseInt(rating.getRating()));

        return ratingDTO;
    }

    /** Converts an ArtifactResource object into corresponding REST API Average Rating DTO object
     *
     * @param apiId UUID of the API
     * @param averageRating Average Rating object
     * @return a new Comment object corresponding to given ArtifactResource object
     */
    public static AvgRatingDTO fromAverageRatingToDTO(String apiId, AvgRating averageRating) {

        AvgRatingDTO averageRatingDTO = new AvgRatingDTO();
        averageRatingDTO.setApiId(averageRating.getApiId());
        averageRatingDTO.setAvgRating(averageRating.getAvgRating());
        return averageRatingDTO;
    }
}



