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

package us.frollo.frollosdk.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchGoals
import us.frollo.frollosdk.extensions.sqlForGoalIds
import us.frollo.frollosdk.extensions.sqlForGoalPeriods
import us.frollo.frollosdk.extensions.sqlForGoals
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.mapping.toGoalPeriod
import us.frollo.frollosdk.model.api.goals.GoalCreateRequest
import us.frollo.frollosdk.model.api.goals.GoalPeriodResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.api.goals.GoalUpdateRequest
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalPeriod
import us.frollo.frollosdk.model.coredata.goals.GoalPeriodRelation
import us.frollo.frollosdk.model.coredata.goals.GoalRelation
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.network.api.GoalsAPI
import java.math.BigDecimal

/** Manages user goals and tracking */
class Goals(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Goals"
    }

    private val goalsAPI: GoalsAPI = network.create(GoalsAPI::class.java)

    // Goal

    /**
     * Fetch goal by ID from the cache
     *
     * @param goalId Unique goal ID to fetch
     *
     * @return LiveData object of Resource<Goal> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoal(goalId: Long): LiveData<Resource<Goal>> =
            Transformations.map(db.goals().load(goalId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goal by ID from the cache along with other associated data.
     *
     * @param goalId Unique goal ID to fetch
     *
     * @return LiveData object of Resource<GoalRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalWithRelation(goalId: Long): LiveData<Resource<GoalRelation>> =
            Transformations.map(db.goals().loadWithRelation(goalId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goals from the cache
     *
     * @param frequency Filter by frequency of the goal (optional)
     * @param status Filter by the status of the goal (optional)
     * @param target Filter by target type of the goal (optional)
     * @param trackingStatus Filter by the tracking status of the goal (optional)
     * @param trackingType Filter by tracking type of the goal (optional)
     * @param accountId Filter by the accountId with which the goals are associated with (optional)
     *
     * @return LiveData object of Resource<List<Goal> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoals(
        frequency: GoalFrequency? = null,
        status: GoalStatus? = null,
        target: GoalTarget? = null,
        trackingStatus: GoalTrackingStatus? = null,
        trackingType: GoalTrackingType? = null,
        accountId: Long? = null
    ): LiveData<Resource<List<Goal>>> =
            Transformations.map(db.goals().loadByQuery(
                    sqlForGoals(
                            frequency = frequency,
                            status = status,
                            target = target,
                            trackingStatus = trackingStatus,
                            trackingType = trackingType,
                            accountId = accountId))
            ) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch goals by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Goal>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoals(query: SimpleSQLiteQuery): LiveData<Resource<List<Goal>>> =
            Transformations.map(db.goals().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goals from the cache with associated data
     *
     * @param frequency Filter by frequency of the goal (optional)
     * @param status Filter by the status of the goal (optional)
     * @param target Filter by target type of the goal (optional)
     * @param trackingStatus Filter by the tracking status of the goal (optional)
     * @param trackingType Filter by tracking type of the goal (optional)
     * @param accountId Filter by the accountId with which the goals are associated with (optional)
     *
     * @return LiveData object of Resource<List<GoalRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalsWithRelation(
        frequency: GoalFrequency? = null,
        status: GoalStatus? = null,
        target: GoalTarget? = null,
        trackingStatus: GoalTrackingStatus? = null,
        trackingType: GoalTrackingType? = null,
        accountId: Long? = null
    ): LiveData<Resource<List<GoalRelation>>> =
            Transformations.map(db.goals().loadByQueryWithRelation(
                    sqlForGoals(
                            frequency = frequency,
                            status = status,
                            target = target,
                            trackingStatus = trackingStatus,
                            trackingType = trackingType,
                            accountId = accountId))
            ) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch goals by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<GoalRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<GoalRelation>>> =
            Transformations.map(db.goals().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Refresh a specific goal by ID from the host
     *
     * @param goalId ID of the goal to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshGoal(goalId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        goalsAPI.fetchGoal(goalId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshGoal", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh all available goals from the host.
     *
     * @param status Filter goals by their current status (optional)
     * @param trackingStatus Filter goals by their current tracking status (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshGoals(
        status: GoalStatus? = null,
        trackingStatus: GoalTrackingStatus? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        goalsAPI.fetchGoals(status, trackingStatus).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshGoals", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalsResponse(
                            response = resource.data,
                            status = status,
                            trackingStatus = trackingStatus,
                            completion = completion)
                }
            }
        }
    }

    /**
     * Create a new goal on the host
     *
     * @param name Name of the goal
     * @param description Additional description of the goal for the user (Optional)
     * @param imageUrl Image URL of an icon/picture associated with the goal (Optional)
     * @param target Target of the goal
     * @param trackingType Tracking method the goal uses
     * @param frequency Frequency of contributions to the goal
     * @param startDate Start date of the goal. Defaults to today (Optional)
     * @param endDate End date of the goal. Required for open ended and date based goals
     * @param periodAmount Amount to be saved each period. Required for open ended and amount based goals
     * @param startAmount Amount already contributed to a goal. Defaults to zero (Optional)
     * @param targetAmount Target amount to reach for the goal. Required for amount and date based goals
     * @param accountId ID of the Account with which the Goal is associated with
     * @param metadata Optional metadata payload (of data type org.json.JSONObject) to append to the goal
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createGoal(
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        target: GoalTarget,
        trackingType: GoalTrackingType,
        frequency: GoalFrequency,
        startDate: String? = null,
        endDate: String?,
        periodAmount: BigDecimal?,
        startAmount: BigDecimal = BigDecimal(0),
        targetAmount: BigDecimal?,
        accountId: Long,
        metadata: JsonObject? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = GoalCreateRequest(
                name = name,
                description = description,
                imageUrl = imageUrl,
                target = target,
                trackingType = trackingType,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                periodAmount = periodAmount,
                startAmount = startAmount,
                targetAmount = targetAmount,
                accountId = accountId,
                metadata = metadata ?: JsonObject())

        if (!request.valid()) {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA)
            completion?.invoke(Result.error(error))
            return
        }

        goalsAPI.createGoal(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createGoal", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Update a goal on the host
     *
     * @param goal Updated goal data model
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateGoal(goal: Goal, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = GoalUpdateRequest(
                name = goal.name,
                description = goal.description,
                imageUrl = goal.imageUrl,
                metadata = goal.metadata ?: JsonObject())

        goalsAPI.updateGoal(goal.goalId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateGoal", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Cancel a specific goal by ID from the host
     *
     * @param goalId ID of the goal to be abandoned
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteGoal(goalId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        goalsAPI.deleteGoal(goalId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteGoal", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    removeCachedGoals(longArrayOf(goalId))
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    // Goal Period

    /**
     * Fetch goal period by ID from the cache
     *
     * @param goalPeriodId Unique goal period ID to fetch
     *
     * @return LiveData object of Resource<GoalPeriod> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriod(goalPeriodId: Long): LiveData<Resource<GoalPeriod>> =
            Transformations.map(db.goalPeriods().load(goalPeriodId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goal periods by goal ID from the cache
     *
     * @param goalId Goal ID of the goal periods to fetch (optional)
     * @param trackingStatus Filter by the tracking status (optional)
     *
     * @return LiveData object of Resource<List<GoalPeriod>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriods(
        goalId: Long? = null,
        trackingStatus: GoalTrackingStatus? = null
    ): LiveData<Resource<List<GoalPeriod>>> =
            Transformations.map(db.goalPeriods().loadByQuery(sqlForGoalPeriods(goalId, trackingStatus))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch goal periods by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goal periods from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<GoalPeriod>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriods(query: SimpleSQLiteQuery): LiveData<Resource<List<GoalPeriod>>> =
            Transformations.map(db.goalPeriods().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goal period by ID from the cache with associated data
     *
     * @param goalPeriodId Unique goal period ID to fetch
     *
     * @return LiveData object of Resource<GoalPeriodRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriodWithRelation(goalPeriodId: Long): LiveData<Resource<GoalPeriodRelation>> =
            Transformations.map(db.goalPeriods().loadWithRelation(goalPeriodId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch goal periods by goal ID from the cache with associated data
     *
     * @param goalId Goal ID of the goal periods to fetch (optional)
     * @param trackingStatus Filter by the tracking status (optional)
     *
     * @return LiveData object of Resource<List<GoalPeriodRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriodsWithRelation(
        goalId: Long? = null,
        trackingStatus: GoalTrackingStatus? = null
    ): LiveData<Resource<List<GoalPeriodRelation>>> =
            Transformations.map(db.goalPeriods().loadByQueryWithRelation(sqlForGoalPeriods(goalId, trackingStatus))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch goal periods by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goal periods from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<GoalPeriodRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchGoalPeriodsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<GoalPeriodRelation>>> =
            Transformations.map(db.goalPeriods().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Refresh goal period by ID from the host
     *
     * @param goalId Goal ID of the goal period to refresh
     * @param goalPeriodId Unique goal period ID to refresh
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshGoalPeriod(goalId: Long, goalPeriodId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        goalsAPI.fetchGoalPeriod(goalId = goalId, periodId = goalPeriodId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshGoalPeriod", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalPeriodResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh goal periods by goal ID from the host
     *
     * @param goalId Goal ID of the goal periods to refresh
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshGoalPeriods(goalId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        goalsAPI.fetchGoalPeriods(goalId = goalId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshGoalPeriods", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalPeriodsResponse(response = resource.data, goalId = goalId, completion = completion)
                }
            }
        }
    }

    // Response Handlers

    private fun handleGoalResponse(response: GoalResponse?, completion: OnFrolloSDKCompletionListener<Result>?) {
        response?.let {
            doAsync {
                val model = response.toGoal()

                db.goals().insert(model)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleGoalsResponse(
        response: List<GoalResponse>?,
        status: GoalStatus?,
        trackingStatus: GoalTrackingStatus?,
        completion: OnFrolloSDKCompletionListener<Result>?
    ) {
        response?.let {
            doAsync {
                val models = mapGoalsResponse(response)

                db.goals().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.goalId }.toHashSet()
                val allGoalIds = db.goals().getIdsByQuery(sqlForGoalIds(status, trackingStatus)).toHashSet()
                val staleIds = allGoalIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedGoals(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleGoalPeriodResponse(response: GoalPeriodResponse?, completion: OnFrolloSDKCompletionListener<Result>?) {
        response?.let {
            doAsync {
                val model = response.toGoalPeriod()

                db.goalPeriods().insert(model)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleGoalPeriodsResponse(
        response: List<GoalPeriodResponse>?,
        goalId: Long,
        completion: OnFrolloSDKCompletionListener<Result>?
    ) {
        response?.let {
            doAsync {
                val models = mapGoalPeriodsResponse(response)

                db.goalPeriods().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.goalPeriodId }.toHashSet()
                val allGoalPeriodIds = db.goalPeriods().getIdsByGoalIds(longArrayOf(goalId)).toHashSet()
                val staleIds = allGoalPeriodIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedGoalPeriods(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapGoalsResponse(models: List<GoalResponse>): List<Goal> =
            models.map { it.toGoal() }.toList()

    private fun mapGoalPeriodsResponse(models: List<GoalPeriodResponse>): List<GoalPeriod> =
            models.map { it.toGoalPeriod() }.toList()

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoals(goalIds: LongArray) {
        if (goalIds.isNotEmpty()) {
            db.goals().deleteMany(goalIds)

            // Manually delete goal periods associated to this goal
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val goalPeriodIds = db.goalPeriods().getIdsByGoalIds(goalIds)
            removeCachedGoalPeriods(goalPeriodIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoalPeriods(goalPeriodIds: LongArray) {
        if (goalPeriodIds.isNotEmpty()) {
            db.goalPeriods().deleteMany(goalPeriodIds)
        }
    }
}