/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react'

function Star(props) {
  return props.isRated ? (
    <span style={{ color: "gold" }}>★</span>
  ) : (
    <span style={{ color: "gold" }}>☆</span>
  );
}

function StarRatingBar(props) {
  const { rating } = props;
  return (
    <div>
      <Star name={1} isRated={rating >= 1} />
      <Star name={2} isRated={rating >= 2} />
      <Star name={3} isRated={rating >= 3} />
      <Star name={4} isRated={rating >= 4} />
      <Star name={5} isRated={rating >= 5} />
    </div>
  );
}

export default StarRatingBar;
