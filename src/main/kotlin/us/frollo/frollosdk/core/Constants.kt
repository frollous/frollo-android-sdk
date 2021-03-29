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

package us.frollo.frollosdk.core

/** Argument Keys for the Intent Data */
object ARGUMENT {
    /**
     * Generic Key for any data sent with an intent.
     */
    internal const val ARG_DATA = "arg_data"

    /**
     * Key for authentication status sent with [ACTION.ACTION_AUTHENTICATION_CHANGED] broadcast.
     */
    const val ARG_AUTHENTICATION_STATUS = "arg_authentication_status"

    /**
     * Key for transaction IDs sent with [ACTION.ACTION_REFRESH_TRANSACTIONS] broadcast.
     */
    const val ARG_TRANSACTION_IDS = "arg_transaction_ids"

    /**
     * Key for Onboarding Step name sent with [ACTION.ACTION_ONBOARDING_STEP_COMPLETED] broadcast.
     */
    const val ARG_ONBOARDING_STEP_NAME = "arg_onboarding_step_name"
}

/** Intent Actions */
object ACTION {
    /**
     * Local Broadcast triggered when ever the User data is updated.
     * Listen to this to detect the update to User data
     */
    const val ACTION_USER_UPDATED = "us.frollo.frollosdk.ACTION_USER_UPDATED"

    /**
     * Local Broadcast triggered when ever the Transactions need to be refreshed.
     * Listen to this to detect the updates to Transactions
     */
    const val ACTION_REFRESH_TRANSACTIONS = "us.frollo.frollosdk.ACTION_REFRESH_TRANSACTIONS"

    /**
     * Local Broadcast triggered when ever the authentication status of the SDK changes.
     * Listen to this to detect if the SDK user has authenticated or been logged out.
     */
    const val ACTION_AUTHENTICATION_CHANGED = "us.frollo.frollosdk.ACTION_AUTHENTICATION_CHANGED"

    /**
     * Local Broadcast triggered when the Current Budget Period is ready.
     * Listen to this to detect when the Current Budget Period is ready.
     */
    const val ACTION_BUDGET_CURRENT_PERIOD_READY = "us.frollo.frollosdk.ACTION_BUDGET_CURRENT_PERIOD_READY"

    /**
     * Local Broadcast triggered when an Onboarding Step is completed.
     * Listen to this to detect when an Onboarding Step is completed.
     */
    const val ACTION_ONBOARDING_STEP_COMPLETED = "us.frollo.frollosdk.ACTION_ONBOARDING_STEP_COMPLETED"
}

internal object LIMIT {
    internal const val SQLITE_MAX_VARIABLE_NUMBER = 998
}
