package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "provider_account",
        indices = [Index("provider_account_id"),
                   Index("provider_id")])

/**
 * Data representation of Provider Account
 */
data class ProviderAccount(

        /** Unique ID for the provider account */
        @PrimaryKey
        @ColumnInfo(name = "provider_account_id") val providerAccountId: Long,

        /** Parent provider ID */
        @ColumnInfo(name = "provider_id") val providerId: Long,

        /** Editable by the user */
        @ColumnInfo(name = "editable") val editable: Boolean,

        /** Refresh Status */
        @Embedded(prefix = "r_status_") val refreshStatus: RefreshStatus?,

        /** Login Form for MFA etc (optional) */
        @ColumnInfo(name = "login_form") val loginForm: ProviderLoginForm?

): IAdapterModel