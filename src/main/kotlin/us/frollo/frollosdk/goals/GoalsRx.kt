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

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForGoalPeriods
import us.frollo.frollosdk.extensions.sqlForGoals
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalPeriod
import us.frollo.frollosdk.model.coredata.goals.GoalPeriodRelation
import us.frollo.frollosdk.model.coredata.goals.GoalRelation
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType

// Goal

/**
 * Fetch goal by ID from the cache
 *
 * @param goalId Unique goal ID to fetch
 *
 * @return Rx Observable object of Goal which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalRx(goalId: Long): Observable<Goal?> {
    return db.goals().loadRx(goalId)
}

/**
 * Fetch goal by ID from the cache along with other associated data.
 *
 * @param goalId Unique goal ID to fetch
 *
 * @return Rx Observable object of GoalRelation which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalWithRelationRx(goalId: Long): Observable<GoalRelation?> {
    return db.goals().loadWithRelationRx(goalId)
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
 * @return Rx Observable object of List<Goal which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalsRx(
    frequency: GoalFrequency? = null,
    status: GoalStatus? = null,
    target: GoalTarget? = null,
    trackingStatus: GoalTrackingStatus? = null,
    trackingType: GoalTrackingType? = null,
    accountId: Long? = null
): Observable<List<Goal>> {
    return db.goals().loadByQueryRx(
        sqlForGoals(
            frequency = frequency,
            status = status,
            target = target,
            trackingStatus = trackingStatus,
            trackingType = trackingType,
            accountId = accountId
        )
    )
}

/**
 * Advanced method to fetch goals by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Goal> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalsRx(query: SimpleSQLiteQuery): Observable<List<Goal>> {
    return db.goals().loadByQueryRx(query)
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
 * @return Rx Observable object of List<GoalRelation> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalsWithRelationRx(
    frequency: GoalFrequency? = null,
    status: GoalStatus? = null,
    target: GoalTarget? = null,
    trackingStatus: GoalTrackingStatus? = null,
    trackingType: GoalTrackingType? = null,
    accountId: Long? = null
): Observable<List<GoalRelation>> {
    return db.goals().loadByQueryWithRelationRx(
        sqlForGoals(
            frequency = frequency,
            status = status,
            target = target,
            trackingStatus = trackingStatus,
            trackingType = trackingType,
            accountId = accountId
        )
    )
}

/**
 * Advanced method to fetch goals by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<GoalRelation> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<GoalRelation>> {
    return db.goals().loadByQueryWithRelationRx(query)
}

// Goal Period

/**
 * Fetch goal period by ID from the cache
 *
 * @param goalPeriodId Unique goal period ID to fetch
 *
 * @return Rx Observable object of GoalPeriod which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodRx(goalPeriodId: Long): Observable<GoalPeriod?> {
    return db.goalPeriods().loadRx(goalPeriodId)
}

/**
 * Fetch goal periods by goal ID from the cache
 *
 * @param goalId Goal ID of the goal periods to fetch (optional)
 * @param trackingStatus Filter by the tracking status (optional)
 *
 * @return Rx Observable object of List<GoalPeriod> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodsRx(
    goalId: Long? = null,
    trackingStatus: GoalTrackingStatus? = null
): Observable<List<GoalPeriod>> {
    return db.goalPeriods().loadByQueryRx(sqlForGoalPeriods(goalId, trackingStatus))
}

/**
 * Advanced method to fetch goal periods by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goal periods from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<GoalPeriod> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodsRx(query: SimpleSQLiteQuery): Observable<List<GoalPeriod>> {
    return db.goalPeriods().loadByQueryRx(query)
}

/**
 * Fetch goal period by ID from the cache with associated data
 *
 * @param goalPeriodId Unique goal period ID to fetch
 *
 * @return Rx Observable object of GoalPeriodRelation which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodWithRelationRx(goalPeriodId: Long): Observable<GoalPeriodRelation?> {
    return db.goalPeriods().loadWithRelationRx(goalPeriodId)
}

/**
 * Fetch goal periods by goal ID from the cache with associated data
 *
 * @param goalId Goal ID of the goal periods to fetch (optional)
 * @param trackingStatus Filter by the tracking status (optional)
 *
 * @return Rx Observable object of List<GoalPeriodRelation> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodsWithRelationRx(
    goalId: Long? = null,
    trackingStatus: GoalTrackingStatus? = null
): Observable<List<GoalPeriodRelation>> {
    return db.goalPeriods().loadByQueryWithRelationRx(sqlForGoalPeriods(goalId, trackingStatus))
}

/**
 * Advanced method to fetch goal periods by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goal periods from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<GoalPeriodRelation> which can be observed using an Observer for future changes as well.
 */
fun Goals.fetchGoalPeriodsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<GoalPeriodRelation>> {
    return db.goalPeriods().loadByQueryWithRelationRx(query)
}
