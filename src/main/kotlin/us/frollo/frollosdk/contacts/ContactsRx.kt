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

package us.frollo.frollosdk.contacts

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForContacts
import us.frollo.frollosdk.model.coredata.contacts.Contact
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod

// Contact

/**
 * Fetch contact by ID from the cache
 *
 * @param contactId Unique contact ID to fetch
 *
 * @return Rx Observable object of Contact which can be observed using an Observer for future changes as well.
 */
fun Contacts.fetchContactRx(contactId: Long): Observable<Contact?> {
    return db.contacts().loadRx(contactId)
}

/**
 * Fetch contacts from the cache
 *
 * @param paymentMethod Filter by [PaymentMethod] of the contact (optional)
 *
 * @return Rx Observable object of List<Contact> which can be observed using an Observer for future changes as well.
 */
fun Contacts.fetchContactsRx(paymentMethod: PaymentMethod? = null): Observable<List<Contact>> {
    return db.contacts().loadByQueryRx(sqlForContacts(paymentMethod))
}

/**
 * Advanced method to fetch contacts by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches contacts from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Contact> which can be observed using an Observer for future changes as well.
 */
fun Contacts.fetchContactsRx(query: SimpleSQLiteQuery): Observable<List<Contact>> {
    return db.contacts().loadByQueryRx(query)
}
