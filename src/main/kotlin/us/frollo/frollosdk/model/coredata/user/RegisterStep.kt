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

import com.google.gson.annotations.SerializedName

/**
 * Register Steps
 *
 * Represents registration steps which are required
 */
data class RegisterStep(

    /** Step key */
    @SerializedName("key") var key: String,

    /** Step index */
    @SerializedName("index") var index: Int,

    /** Step required flag */
    @SerializedName("required") var required: Boolean,

    /** Step completed flag */
    @SerializedName("completed") var completed: Boolean
)
