/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.model;

import com.expedia.adaptivealerting.modelservice.util.RequestValidator;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class CreateDetectorMappingRequest {
    private Expression expression;
    private Detector detector;
    private User user;

    public void validate() {
        RequestValidator.validateExpression(this.getExpression());
        RequestValidator.validateUser(this.getUser());
        RequestValidator.validateDetector(this.getDetector());
    }
}


