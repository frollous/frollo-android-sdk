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

package us.frollo.frollosdk.paydays

import io.reactivex.Observable
import us.frollo.frollosdk.model.coredata.payday.Payday

// Payday

/**
 * Fetch payday from the cache
 *
 * @return Rx Observable object of Payday which can be observed using an Observer for future changes as well.
 */
fun Paydays.fetchPaydayRx(): Observable<Payday?> {
    return db.payday().loadRx()
}
