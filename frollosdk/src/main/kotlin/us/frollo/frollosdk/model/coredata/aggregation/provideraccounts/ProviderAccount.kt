package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
@Entity(tableName = "provider_account",
        indices = [Index("provider_account_id")])
data class ProviderAccount(
        @PrimaryKey
        @ColumnInfo(name = "provider_account_id") val providerAccountId: Long,
        @ColumnInfo(name = "provider_id") val providerId: Long,
        @ColumnInfo(name = "editable") val editable: Boolean,
        @Embedded(prefix = "r_status_") val refreshStatus: ProviderAccountRefreshStatus?,
        @ColumnInfo(name = "login_form") val loginForm: ProviderLoginForm?
): IAdapterModel