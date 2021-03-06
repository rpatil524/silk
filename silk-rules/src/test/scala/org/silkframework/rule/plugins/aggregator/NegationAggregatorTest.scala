/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.silkframework.rule.plugins.aggregator

import org.silkframework.rule.plugins.aggegrator.NegationAggregator
import org.silkframework.rule.similarity.{Aggregator, WeightedSimilarityScore}
import org.silkframework.test.PluginTest
import org.silkframework.testutil.approximatelyEqualTo


class NegationAggregatorTest extends PluginTest {

  val aggregator = NegationAggregator()

  it should "return the negation of the input" in {
    aggregator.evaluateValue(WeightedSimilarityScore(1.0, 1)).score.get should be(approximatelyEqualTo(-1.0))
    aggregator.evaluateValue(WeightedSimilarityScore(-1.0, 1)).score.get should be(approximatelyEqualTo(1.0))
    aggregator.evaluateValue(WeightedSimilarityScore(0.0, 1)).score.get should be(approximatelyEqualTo(0.0))
  }

  it should "interpret missing values as a similarity score of 1" in {
    aggregator.evaluateValue(WeightedSimilarityScore(None)).score.get should be(approximatelyEqualTo(1.0))
  }

  override def pluginObject: Aggregator = NegationAggregator()
}
