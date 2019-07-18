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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.goals.GoalCreateRequest
import us.frollo.frollosdk.model.api.goals.GoalPeriodResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.api.goals.GoalUpdateRequest

internal interface GoalsAPI {
    companion object {
        // Goal URLs
        const val URL_GOALS = "goals"
        const val URL_GOAL = "goals/{goal_id}"

        // Goal Period URLs
        const val URL_GOAL_PERIODS = "goals/{goal_id}/periods"
    }

    // Goal API

    // Query parameters: {status, tracking_status}
    @GET(URL_GOALS)
    fun fetchGoals(@QueryMap queryParams: Map<String, String>): Call<List<GoalResponse>>

    @GET(URL_GOAL)
    fun fetchGoal(@Path("goal_id") goalId: Long): Call<GoalResponse>

    @POST(URL_GOALS)
    fun createGoal(@Body request: GoalCreateRequest): Call<GoalResponse>

    @PUT(URL_GOAL)
    fun updateGoal(@Path("goal_id") goalId: Long, @Body request: GoalUpdateRequest): Call<GoalResponse>

    @DELETE(URL_GOAL)
    fun deleteGoal(@Path("goal_id") goalId: Long): Call<Void>

    // Goal Period API

    @GET(URL_GOAL_PERIODS)
    fun fetchGoalPeriods(@Path("goal_id") goalId: Long): Call<List<GoalPeriodResponse>>
}