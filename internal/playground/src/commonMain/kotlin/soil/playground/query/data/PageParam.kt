package soil.playground.query.data

import soil.playground.CommonParcelable
import soil.playground.CommonParcelize


// LazyColumn requires specifying an item of Parcelable type
@CommonParcelize
data class PageParam(
    val offset: Int = 0,
    val limit: Int = 10,
) : CommonParcelable
