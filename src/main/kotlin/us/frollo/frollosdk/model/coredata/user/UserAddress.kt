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

package us.frollo.frollosdk.model.coredata.user

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.IAdapterModel
import java.io.Serializable

/**
 * Address of the user
 */
data class UserAddress(

    /** Unique ID of the address */
    @ColumnInfo(name = "id") @SerializedName("id") var addressId: Long,

    /** Full address in formatted form. (Optional) */
    @ColumnInfo(name = "long_form") @SerializedName("long_form") var longForm: String? = null

) : IAdapterModel, Serializable
