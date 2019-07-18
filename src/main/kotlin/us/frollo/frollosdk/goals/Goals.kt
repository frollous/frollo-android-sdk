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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchGoals
import us.frollo.frollosdk.extensions.sqlForGoalIds
import us.frollo.frollosdk.extensions.sqlForGoals
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.model.api.goals.GoalCreateRequest
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalRelation
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.network.api.GoalsAPI
import java.math.BigDecimal

class Goals(network: NetworkService, private val db: SDKDatabase, private val authentication: Authentication) {

    companion object {
        private const val TAG = "Goals"
    }

    private val goalsAPI: GoalsAPI = network.create(GoalsAPI::class.java)

    // Goal

    fun fetchGoal(goalId: Long): LiveData<Resource<Goal>> =
            Transformations.map(db.goals().load(goalId)) { model ->
                Resource.success(model)
            }

    fun fetchGoalWithRelation(goalId: Long): LiveData<Resource<GoalRelation>> =
            Transformations.map(db.goals().loadWithRelation(goalId)) { model ->
                Resource.success(model)
            }

    fun fetchGoals(
        frequency: GoalFrequency? = null,
        status: GoalStatus? = null,
        trackingStatus: GoalTrackingStatus? = null,
        accountId: Long? = null
    ): LiveData<Resource<List<Goal>>> =
            Transformations.map(db.goals().loadByQuery(sqlForGoals(frequency, status, trackingStatus, accountId))) { models ->
                Resource.success(models)
            }

    fun fetchGoals(query: SimpleSQLiteQuery): LiveData<Resource<List<Goal>>> =
            Transformations.map(db.goals().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    fun fetchGoalsWithRelation(
        frequency: GoalFrequency? = null,
        status: GoalStatus? = null,
        trackingStatus: GoalTrackingStatus? = null,
        accountId: Long? = null
    ): LiveData<Resource<List<GoalRelation>>> =
            Transformations.map(db.goals().loadByQueryWithRelation(sqlForGoals(frequency, status, trackingStatus, accountId))) { models ->
                Resource.success(models)
            }

    fun fetchGoalsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<GoalRelation>>> =
            Transformations.map(db.goals().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    fun refreshGoal(goalId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshGoal", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

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

    fun refreshGoals(
        status: GoalStatus? = null,
        trackingStatus: GoalTrackingStatus? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshGoals", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        goalsAPI.fetchGoals(status, trackingStatus).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshGoals", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleGoalsResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    fun createGoal(
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        type: String? = null,
        subType: String? = null,
        target: GoalTarget,
        trackingType: GoalTrackingType,
        frequency: GoalFrequency,
        startDate: String? = null,
        endDate: String?,
        periodAmount: BigDecimal?,
        startAmount: BigDecimal = BigDecimal(0),
        targetAmount: BigDecimal?,
        accountId: Long,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#createGoal", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        val request = GoalCreateRequest(
                name = name,
                description = description,
                imageUrl = imageUrl,
                type = type,
                subType = subType,
                target = target,
                trackingType = trackingType,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                periodAmount = periodAmount,
                startAmount = startAmount,
                targetAmount = targetAmount,
                accountId = accountId)

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

    fun deleteGoal(goalId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#deleteGoal", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

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

    // Response Handlers

    private fun handleGoalResponse(response: GoalResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
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
        status: GoalStatus? = null,
        trackingStatus: GoalTrackingStatus? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
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

    private fun mapGoalsResponse(models: List<GoalResponse>): List<Goal> =
            models.map { it.toGoal() }.toList()

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoals(goalIds: LongArray) {
        db.goals().deleteMany(goalIds)

        // Manually delete goal periods associated to this goal
        // as we are not using ForeignKeys because ForeignKey constraints
        // do not allow to insert data into child table prior to parent table
        val goalPeriodIds = db.goalPeriods().getIdsByGoalIds(goalIds)
        removeCachedGoalPeriods(goalPeriodIds)
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoalPeriods(goalPeriodIds: LongArray) {
        db.goalPeriods().deleteMany(goalPeriodIds)
    }
}