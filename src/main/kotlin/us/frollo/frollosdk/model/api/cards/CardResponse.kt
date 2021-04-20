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

package us.frollo.frollosdk.model.api.cards

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.cards.CardDesignType
import us.frollo.frollosdk.model.coredata.cards.CardIssuer
import us.frollo.frollosdk.model.coredata.cards.CardStatus
import us.frollo.frollosdk.model.coredata.cards.CardType

internal data class CardResponse(
    @SerializedName("id") val cardId: Long,
    @SerializedName("account_id") val accountId: Long,
    @SerializedName("status") val status: CardStatus,
    @SerializedName("design_type") val designType: CardDesignType,
    @SerializedName("created_at") val createdDate: String, // Eg: 2011-12-03T10:15:30+01:00
    @SerializedName("cancelled_at") val cancelledDate: String?, // Eg: 2011-12-03T10:15:30+01:00
    @SerializedName("name") val name: String?,
    @SerializedName("nick_name") val nickName: String?,
    @SerializedName("pan_last_digits") val panLastDigits: String?,
    @SerializedName("expiry_date") val expiryDate: String?, // MM/yy or MM/yyyy
    @SerializedName("cardholder_name") val cardholderName: String?,
    @SerializedName("type") val type: CardType?,
    @SerializedName("issuer") val issuer: CardIssuer?,
    @SerializedName("pin_set_at") val pinSetDate: String? // Eg: 2011-12-03T10:15:30+01:00
)
