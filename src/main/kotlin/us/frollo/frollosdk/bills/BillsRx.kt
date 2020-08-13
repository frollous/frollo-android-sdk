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

package us.frollo.frollosdk.bills

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForBillPayments
import us.frollo.frollosdk.extensions.sqlForBills
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.model.coredata.bills.BillPaymentRelation
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillRelation
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType

// Bill

/**
 * Fetch bill by ID from the cache
 *
 * @param billId Unique bill ID to fetch
 *
 * @return Rx Observable object of Bill which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillRx(billId: Long): Observable<Bill?> {
    return db.bills().loadRx(billId)
}

/**
 * Fetch bills from the cache
 *
 * @param frequency Filter by frequency of the bill payments (optional)
 * @param paymentStatus Filter by the payment status (optional)
 * @param status Filter by the status of the bill (optional)
 * @param type Filter by the type of the bill (optional)
 *
 * @return Rx Observable object of List<Bill which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillsRx(
    frequency: BillFrequency? = null,
    paymentStatus: BillPaymentStatus? = null,
    status: BillStatus? = null,
    type: BillType? = null
): Observable<List<Bill>> {
    return db.bills().loadByQueryRx(sqlForBills(frequency, paymentStatus, status, type))
}

/**
 * Advanced method to fetch bills by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches bills from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Bill> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillsRx(query: SimpleSQLiteQuery): Observable<List<Bill>> {
    return db.bills().loadByQueryRx(query)
}

/**
 * Fetch bill by ID from the cache along with other associated data.
 *
 * @param billId Unique bill ID to fetch
 *
 * @return Rx Observable object of BillRelation which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillWithRelationRx(billId: Long): Observable<BillRelation?> {
    return db.bills().loadWithRelationRx(billId)
}

/**
 * Fetch bills from the cache along with other associated data.
 *
 * @param frequency Filter by frequency of the bill payments (optional)
 * @param paymentStatus Filter by the payment status (optional)
 * @param status Filter by the status of the bill (optional)
 * @param type Filter by the type of the bill (optional)
 *
 * @return Rx Observable object of List<BillRelation which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillsWithRelationRx(
    frequency: BillFrequency? = null,
    paymentStatus: BillPaymentStatus? = null,
    status: BillStatus? = null,
    type: BillType? = null
): Observable<List<BillRelation>> {
    return db.bills().loadByQueryWithRelationRx(sqlForBills(frequency, paymentStatus, status, type))
}

/**
 * Advanced method to fetch bills by SQL query from the cache along with other associated data.
 *
 * @param query SimpleSQLiteQuery: Select query which fetches bills from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<BillRelation> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<BillRelation>> {
    return db.bills().loadByQueryWithRelationRx(query)
}

// Bill Payment

/**
 * Fetch bill payment by ID from the cache
 *
 * @param billPaymentId Unique bill payment ID to fetch
 *
 * @return Rx Observable object of BillPayment which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentRx(billPaymentId: Long): Observable<BillPayment?> {
    return db.billPayments().loadRx(billPaymentId)
}

/**
 * Fetch bill payments by bill ID from the cache
 *
 * @param billId Bill ID of the bill payments to fetch (optional)
 * @param fromDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
 * @param toDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
 * @param frequency Filter by frequency of the bill payments (optional)
 * @param paymentStatus Filter by the payment status (optional)
 *
 * @return Rx Observable object of List<BillPayment> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentsRx(
    billId: Long? = null,
    fromDate: String? = null,
    toDate: String? = null,
    frequency: BillFrequency? = null,
    paymentStatus: BillPaymentStatus? = null
): Observable<List<BillPayment>> {
    return db.billPayments().loadByQueryRx(sqlForBillPayments(billId, fromDate, toDate, frequency, paymentStatus))
}

/**
 * Advanced method to fetch bill payments by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches bill payments from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<BillPayment> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentsRx(query: SimpleSQLiteQuery): Observable<List<BillPayment>> {
    return db.billPayments().loadByQueryRx(query)
}

/**
 * Fetch bill payment by ID from the cache with associated data
 *
 * @param billPaymentId Unique bill payment ID to fetch
 *
 * @return Rx Observable object of BillPaymentRelation which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentWithRelationRx(billPaymentId: Long): Observable<BillPaymentRelation?> {
    return db.billPayments().loadWithRelationRx(billPaymentId)
}

/**
 * Fetch bill payments from the cache with associated data
 *
 * @param billId Bill ID of the bill payments to fetch (optional)
 * @param fromDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
 * @param toDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
 * @param frequency Filter by frequency of the bill payments (optional)
 * @param paymentStatus Filter by the payment status (optional)
 *
 * @return Rx Observable object of List<BillPaymentRelation> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentsWithRelationRx(
    billId: Long? = null,
    fromDate: String? = null,
    toDate: String? = null,
    frequency: BillFrequency? = null,
    paymentStatus: BillPaymentStatus? = null
): Observable<List<BillPaymentRelation>> {
    return db.billPayments().loadByQueryWithRelationRx(sqlForBillPayments(billId, fromDate, toDate, frequency, paymentStatus))
}

/**
 * Advanced method to fetch bill payments by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches bill payments from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<BillPaymentRelation> which can be observed using an Observer for future changes as well.
 */
fun Bills.fetchBillPaymentsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<BillPaymentRelation>> {
    return db.billPayments().loadByQueryWithRelationRx(query)
}
