/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model.api.goals

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.testGoalRequestTargetData
import java.math.BigDecimal

class GoalCreateRequestTest {

    @Test
    fun testValid() {
        // Test Target Amount
        var request = testGoalRequestTargetData(
                target = GoalTarget.AMOUNT,
                targetAmount = BigDecimal(20000),
                periodAmount = null)
        assertFalse(request.valid())
        request = testGoalRequestTargetData(
                target = GoalTarget.AMOUNT,
                targetAmount = BigDecimal(20000),
                periodAmount = BigDecimal(300))
        assertTrue(request.valid())

        // Test Target Date
        request = testGoalRequestTargetData(
                target = GoalTarget.DATE,
                targetAmount = BigDecimal(20000),
                endDate = null)
        assertFalse(request.valid())
        request = testGoalRequestTargetData(
                target = GoalTarget.DATE,
                targetAmount = BigDecimal(20000),
                endDate = "2019-12-31")
        assertTrue(request.valid())

        // Test Target Open Ended
        request = testGoalRequestTargetData(
                target = GoalTarget.OPEN_ENDED,
                periodAmount = BigDecimal(300),
                endDate = null)
        assertFalse(request.valid())
        request = testGoalRequestTargetData(
                target = GoalTarget.OPEN_ENDED,
                periodAmount = BigDecimal(300),
                endDate = "2019-12-31")
        assertTrue(request.valid())
    }
}