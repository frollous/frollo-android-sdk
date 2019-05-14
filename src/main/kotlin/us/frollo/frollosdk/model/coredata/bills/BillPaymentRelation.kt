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

package us.frollo.frollosdk.model.coredata.bills

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel

/** Bill Payment with associated data */
data class BillPaymentRelation(

    /** Bill Payment */
    @Embedded
    var billPayment: BillPayment? = null,

    /** Associated Bill
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "bill_id", entityColumn = "bill_id", entity = Bill::class)
    var bills: List<BillRelation>? = null

) : IAdapterModel {

    /** Associated Bill */
    val bill: BillRelation?
        get() {
            val models = bills
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}