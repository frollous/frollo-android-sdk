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

import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.coredata.aggregation.providers.AggregatorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderAuthType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderEncryption
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderEncryptionType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFieldType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormField
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormRow
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderMFAType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID

internal fun testProviderResponseData(providerId: Long? = null): ProviderResponse {
    val encryption = ProviderEncryption(
        encryptionType = ProviderEncryptionType.ENCRYPT_VALUES,
        alias = "09282016_1",
        pem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1eXKHvPBlS4A41OvQqFn0SfNH7OgEs2MXMLeyp3xKorEipEKuzv/JDtHFHRAfYwyeiC0q+me0R8GLA6NEDGDfpxGv/XUFyza609ZqtCTOiGCp8DcjLG0mPljdGA1Df0BKhF3y5uata1y0dKSI8aY8lXPza+Tsw4TtjdmHbJ2rR3sFZkYch1RTmNKxKDxMgUmtIk785lIfLJ2x6lvh4ZS9QhuAnsoVM91WWKHrLHYfAeA/zD1TxHDm5/4wPbmFLEBe2+5zGae19nsA/9zDwKP4whpte9HuDDQa5Vsq+aWj5pDJuvFgwA/DStqcHGijn5gzB/JXEoE9qx+dcG92PpvfwIDAQAB\n------END PUBLIC KEY------"
    )

    val field1 = ProviderFormField(
        fieldId = "762",
        image = null,
        isOptional = false,
        maxLength = 8,
        name = "AccountNumber",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.TEXT,
        validations = null,
        value = null,
        valueEditable = true
    )

    val row1 = ProviderFormRow(
        fields = listOf(field1),
        fieldRowChoice = "Choice1",
        form = "0001",
        hint = "Account Number",
        rowId = "4512",
        label = "Account Number"
    )

    val loginForm = ProviderLoginForm(
        formId = "7224",
        forgetPasswordUrl = "https://example.com/forgot",
        formType = ProviderFormType.LOGIN,
        help = "Fill in the form",
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(row1)
    )

    return ProviderResponse(
        providerId = providerId ?: randomNumber().toLong(),
        containerNames = listOf("bank", "credit_card"),
        providerName = randomUUID(),
        popular = true,
        providerStatus = ProviderStatus.SUPPORTED,
        authType = ProviderAuthType.CREDENTIALS,
        baseUrl = "https://example.com/",
        encryption = encryption,
        forgetPasswordUrl = "https://example.com/iforgot",
        helpMessage = randomUUID(),
        largeLogoUrl = "https://example.com/large_logo.png",
        largeLogoRevision = 1,
        loginForm = loginForm,
        loginHelpMessage = randomUUID(),
        loginUrl = "https://example.com/login",
        mfaType = ProviderMFAType.TOKEN,
        oAuthSite = false,
        smallLogoUrl = "https://example.com/small_logo.png",
        smallLogoRevision = 1,
        aggregatorType = AggregatorType.YODLEE,
        permissions = null,
        productsAvailable = false
    )
}
