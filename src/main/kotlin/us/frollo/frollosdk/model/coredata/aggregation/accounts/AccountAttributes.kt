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

package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Account attributes */
data class AccountAttributes(
        /** Account Type */
        @ColumnInfo(name = "account_type") @SerializedName("container") val accountType: AccountType,

        /** Account Sub Type */
        @ColumnInfo(name = "account_sub_type") @SerializedName("account_type") val accountSubType: AccountSubType,

        /** Account Group */
        @ColumnInfo(name = "account_group") @SerializedName("group") val group: AccountGroup,

        /** Account Classification */
        @ColumnInfo(name = "account_classification") @SerializedName("classification") val classification: AccountClassification?
)