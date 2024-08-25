package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.data.User
import soil.playground.query.key.users.GetUserKey
import soil.query.compose.QueryConfig
import soil.query.compose.QueryObject
import soil.query.compose.rememberQuery

typealias GetUserQueryObject = QueryObject<User>

@Composable
fun rememberGetUserQuery(
    userId: Int,
    builderBlock: QueryConfig.Builder.() -> Unit = {}
): GetUserQueryObject {
    val key = remember(userId) { GetUserKey(userId) }
    return rememberQuery(key, config = QueryConfig(builderBlock))
}
