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

package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Provider encryption */
data class ProviderEncryption(

        /** Encryption Type */
        @ColumnInfo(name = "type") @SerializedName("encryption_type") val encryptionType: ProviderEncryptionType,

        /** Encryption alias to be appended to login form values (optional) */
        @ColumnInfo(name = "alias") @SerializedName("alias") val alias: String?,

        /** PEM Public key to be used to encrypt login forms (optional) */
        @ColumnInfo(name = "pem") @SerializedName("pem") val pem: String?
)