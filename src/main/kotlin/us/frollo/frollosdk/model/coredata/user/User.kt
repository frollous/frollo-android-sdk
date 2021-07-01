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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "user",
    indices = [Index("user_id")]
)

/**
 * Data representation of User
 */
data class User(

    /** Unique ID of the user */
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Long,

    /** First name of the user (optional) */
    @ColumnInfo(name = "first_name") var firstName: String?,

    /** Email address of the user */
    @ColumnInfo(name = "email") var email: String,

    /** User verified their email address */
    @ColumnInfo(name = "email_verified") val emailVerified: Boolean,

    /** Status of the user's account */
    @ColumnInfo(name = "status") val status: UserStatus,

    /** Primary currency of the user */
    @ColumnInfo(name = "primary_currency") val primaryCurrency: String,

    /** User has a valid password */
    @ColumnInfo(name = "valid_password") val validPassword: Boolean,

    /** An array of [RegisterStep] decoded from a json array stored in the database. (Optional) */
    @ColumnInfo(name = "register_steps") val registerSteps: List<RegisterStep>?,

    /** Date user registered (format pattern - yyyy-MM-dd) */
    @ColumnInfo(name = "registration_date") val registrationDate: String,

    /** Facebook ID associated with the user (optional) */
    @ColumnInfo(name = "facebook_id") val facebookId: String?,

    /** Attribution of the user */
    @ColumnInfo(name = "attribution") var attribution: Attribution?,

    /** Last name of the user (optional) */
    @ColumnInfo(name = "last_name") var lastName: String?,

    /** Mobile phone number of the user (optional) */
    @ColumnInfo(name = "mobile_number") var mobileNumber: String?,

    /** Gender of the user (optional) */
    @ColumnInfo(name = "gender") var gender: Gender?,

    /** Current residential address of the user */
    @Embedded(prefix = "residential_address_") var residentialAddress: UserAddress?,

    /** Mailing address of the user */
    @Embedded(prefix = "mailing_address_") var mailingAddress: UserAddress?,

    /** Previous residential address of the user */
    @Embedded(prefix = "previous_address_") var previousAddress: UserAddress?,

    /** Number of people in the household (optional) */
    @ColumnInfo(name = "household_size") var householdSize: Int?,

    /** Household type of the user (optional) */
    @ColumnInfo(name = "marital_status") var householdType: HouseholdType?,

    /** Occupation of the user (optional) */
    @ColumnInfo(name = "occupation") var occupation: Occupation?,

    /** Industry the user works in (optional) */
    @ColumnInfo(name = "industry") var industry: Industry?,

    /** Date of birth of the user (optional) (format pattern - yyyy-MM-dd) */
    @ColumnInfo(name = "date_of_birth") var dateOfBirth: String?,

    /** Drivers license of the user */
    @ColumnInfo(name = "driver_license") var driverLicense: String?,

    /** A list of [FeatureFlag] decoded from a json array stored in the database. (Optional) */
    @ColumnInfo(name = "features") val features: List<FeatureFlag>?,

    /** Foreign tax user (optional) */
    @ColumnInfo(name = "foreign_tax") var foreignTax: Boolean?,

    /** Tax residency (optional) */
    @ColumnInfo(name = "tax_residency") var taxResidency: String?,

    /** Tax file number (optional) */
    @ColumnInfo(name = "tfn") var tfn: String?,

    /** Tax identification number (optional) */
    @ColumnInfo(name = "tin") var tin: String?,

    /** External ID (Optional) */
    @ColumnInfo(name = "external_id") val externalId: String?
)
