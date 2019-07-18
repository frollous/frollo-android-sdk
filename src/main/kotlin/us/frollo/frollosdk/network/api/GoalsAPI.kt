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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.goals.GoalResponse

internal interface GoalsAPI {
    companion object {
        // Goal URLs
        const val URL_GOALS = "goals"
        const val URL_GOAL = "goals/{goal_id}"
    }

    // Goal API

    // Query parameters: {status, tracking_status}
    @GET(URL_GOALS)
    fun fetchGoals(@QueryMap queryParams: Map<String, String>): Call<List<GoalResponse>>

    @GET(URL_GOAL)
    fun fetchGoal(@Path("goal_id") goalId: Long): Call<GoalResponse>

    @DELETE(URL_GOAL)
    fun deleteGoal(@Path("goal_id") goalId: Long): Call<Void>
}