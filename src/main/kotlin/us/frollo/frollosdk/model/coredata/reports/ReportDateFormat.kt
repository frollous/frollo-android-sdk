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

package us.frollo.frollosdk.model.coredata.reports

/** Date format patterns to convert date from stored date string to user's current locale */
class ReportDateFormat {
    companion object {
        /** Date format to convert daily date from stored date string to user's current locale */
        const val DAILY = "yyyy-MM-dd"

        /** Date format to convert monthly date from stored date string to user's current locale */
        const val MONTHLY = "yyyy-MM"

        /**
         * Date formatter to convert weekly date from stored date string to user's current locale.
         *
         * NOTE: W = week of month (1-5) (the first week starts on the first day of the month)
         */
        const val WEEKLY = "yyyy-MM-W"

        /** Date formatter pattern for the dates in the request query */
        const val DATE_PATTERN_FOR_REQUEST = "yyyy-MM-dd"
    }
}