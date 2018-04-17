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

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingListDTO;

/**
 * Mapping class for Rating Objects
 *
 */
public class RatingMappingUtil {

    /** Converts an Rating object into corresponding REST API RatingDTO object
     *
     * @param rating Comment object
     * @return a new RatingDTO object corresponding to given Rating object
     */
    public static RatingDTO fromRatingToDTO(Rating rating) {

        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setRatingId(rating.getUuid());
        ratingDTO.setRating(rating.getRating());
        ratingDTO.setUsername(rating.getUsername());

        return ratingDTO;
    }

    /** Constructs a RatingListDTO from a list of RatingDTO objects and other information
     *
     * @param avgRating     Average Rating of the API
     * @param userRating    User Rating for the API
     * @param offset        starting index
     * @param limit         maximum number of ratings to return
     * @param ratingList    List of RatingDTO Objects available for the API
     * @return a new RatingLIstDTO object
     */
    public static RatingListDTO fromRatingDTOListToRatingListDTO(double avgRating, double userRating, Integer offset, Integer limit,
                                                    List<RatingDTO> ratingList) {

        RatingListDTO ratingListDTO = new RatingListDTO();
        List<RatingDTO> ratingDTOs = ratingListDTO.getList();


        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        ratingListDTO.setAvgRating(String.valueOf(decimalFormat.format(avgRating)));
        ratingListDTO.setUserRating(String.valueOf(decimalFormat.format(userRating)));

        if(ratingList == null){
            ratingList = new ArrayList<>();
            ratingListDTO.setList(ratingList);
        }
        //add the required range of objects to be returned
        int start = offset < ratingList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= ratingList.size() - 1 ? offset + limit - 1 : ratingList.size() - 1;
        for (int i = start; i <= end; i++) {
            ratingDTOs.add(ratingList.get(i));
        }
        ratingListDTO.setCount(ratingDTOs.size());
        return ratingListDTO;
    }

    /** Converts a List of User Rating objects into a RatingDTO list
     *
     * @param userRatingList List of rating objects of the API
     * @return a new list object of ratingDTO objects
     */
    public static List<RatingDTO> fromRatingListToDTOList(List<Rating> userRatingList) {
        List<RatingDTO> ratingDTOList = new ArrayList<>();
        for(Rating rating : userRatingList) {
            ratingDTOList.add(fromRatingToDTO(rating));
        }
        return  ratingDTOList;
    }

    /**
     * Converts a RatingDTO to a Rating
     *
     * @param username username of the invoked user
     * @param apiId UUID of the api
     * @param body request payload
     * @return a new rating object
     */
    public static Rating fromDTOToRating(String username, String apiId, RatingDTO body) {
        Rating rating = new Rating();
        rating.setApiId(apiId);
        rating.setRating(body.getRating());
        rating.setUsername(username);
        Instant time = APIUtils.getCurrentUTCTime();
        rating.setCreatedTime(time);
        rating.setLastUpdatedTime(time);

        return rating;
    }
}