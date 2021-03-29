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

package us.frollo.frollosdk.model.coredata.managedproduct

import com.google.gson.annotations.SerializedName

/** Data representation of ManagedProduct object */
data class ManagedProduct(

    /** Unique ID of [ManagedProduct] */
    @SerializedName("id") val productId: Long,

    /** Name of [ManagedProduct] */
    @SerializedName("name") val name: String,

    /** ID of Provider to which the [ManagedProduct] belongs */
    @SerializedName("provider_id") val providerId: Long,

    /** Container of [ManagedProduct] */
    @SerializedName("container") val container: String,

    /** AccountType of [ManagedProduct] */
    @SerializedName("account_type") val accountType: String,

    /** List of Terms & Conditions of the [ManagedProduct] */
    @SerializedName("terms_conditions") val termsConditions: List<TermsCondition>
)
