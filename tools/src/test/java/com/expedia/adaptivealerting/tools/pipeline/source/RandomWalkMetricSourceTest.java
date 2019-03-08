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
package com.expedia.adaptivealerting.tools.pipeline.source;

import com.expedia.adaptivealerting.core.util.ThreadUtil;
import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@Slf4j
public final class RandomWalkMetricSourceTest {

    @Test
    public void testNoArgConstructor() {
        val metricSource = new RandomWalkMetricSource();
        assertNotNull(metricSource.next());
    }

    @Test
    public void testArgConstructor() {
        val metricSource = new RandomWalkMetricSource("random-walk", 100L, 0);
        assertNotNull(metricSource.next());
    }

    @Test
    public void testStartAndStop() {
        MetricDataSubscriber subscriber = (MetricData metricData) -> log.info("metricData={}", metricData);

        val metricSource = new RandomWalkMetricSource();
        metricSource.addSubscriber(subscriber);
        metricSource.start();
        ThreadUtil.sleep(1000L);
        metricSource.stop();
        metricSource.removeSubscriber(subscriber);
    }
}
