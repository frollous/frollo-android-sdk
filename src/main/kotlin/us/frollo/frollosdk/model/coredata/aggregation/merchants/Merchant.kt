package us.frollo.frollosdk.model.coredata.aggregation.merchants

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.
@Entity(tableName = "merchant",
        indices = [Index("merchant_id")])
data class Merchant(
        @PrimaryKey
        @ColumnInfo(name = "merchant_id") val merchantId: Long,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "merchant_type") val merchantType: MerchantType,
        @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String
): IAdapterModel