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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.cards.CardResponse
import us.frollo.frollosdk.model.coredata.cards.CardDesignType
import us.frollo.frollosdk.model.coredata.cards.CardIssuer
import us.frollo.frollosdk.model.coredata.cards.CardStatus
import us.frollo.frollosdk.model.coredata.cards.CardType
import us.frollo.frollosdk.testutils.randomElement
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString

internal fun testCardResponseData(
    cardId: Long? = null,
    accountId: Long? = null,
    status: CardStatus? = null
): CardResponse {
    return CardResponse(
        cardId = cardId ?: randomNumber().toLong(),
        name = randomString(20),
        nickName = randomString(100),
        cardholderName = randomString(100),
        accountId = accountId ?: randomNumber().toLong(),
        issuer = CardIssuer.values().randomElement(),
        type = CardType.values().randomElement(),
        status = status ?: CardStatus.values().randomElement(),
        designType = CardDesignType.values().randomElement(),
        panLastDigits = "4979",
        createdDate = "2019-01-02",
        cancelledDate = "2019-11-02",
        expiryDate = "2019-12-02",
        pinSetDate = "2019-12-03"
    )
}
