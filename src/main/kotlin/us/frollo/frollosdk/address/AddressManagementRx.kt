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

package us.frollo.frollosdk.address

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.model.coredata.address.Address

/**
 * Fetch address by ID from the cache
 *
 * @param addressId Unique card ID to fetch
 *
 * @return Rx Observable object of Address which can be observed using an Observer for future changes as well.
 */
fun AddressManagement.fetchAddressRx(addressId: Long): Observable<Address?> {
    return db.addresses().loadRx(addressId)
}

/**
 * Fetch addresses from the cache
 *
 * @return Rx Observable object of List<Address> which can be observed using an Observer for future changes as well.
 */
fun AddressManagement.fetchAddressesRx(): Observable<List<Address>> {
    return db.addresses().loadRx()
}

/**
 * Advanced method to fetch addresses by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches addresses from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Address> which can be observed using an Observer for future changes as well.
 */
fun AddressManagement.fetchAddressesRx(query: SimpleSQLiteQuery): Observable<List<Address>> {
    return db.addresses().loadByQueryRx(query)
}
