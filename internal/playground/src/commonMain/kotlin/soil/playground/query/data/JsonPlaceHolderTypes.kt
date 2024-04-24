package soil.playground.query.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// JSON Placeholder Types
// https://jsonplaceholder.typicode.com/guide/

// NOTE: Kotlin Serialization cannot be built on the Compiler Server, therefore data types are precompiled.
// ref. https://github.com/JetBrains/kotlin-compiler-server/pull/540

@Serializable
data class User(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("username")
    val username: String,
    @SerialName("email")
    val email: String
)

typealias Users = List<User>

@Serializable
data class Post(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("userId")
    val userId: Int
)

typealias Posts = List<Post>

@Serializable
data class Comment(
    @SerialName("postId")
    val postId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("body")
    val body: String
)

typealias Comments = List<Comment>

@Serializable
data class Album(
    @SerialName("userId")
    val userId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String
)

typealias Albums = List<Album>

@Serializable
data class Photo(
    @SerialName("albumId")
    val albumId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("thumbnailUrl")
    val thumbnailUrl: String
)

typealias Photos = List<Photo>

@Serializable
data class Todo(
    @SerialName("userId")
    val userId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("completed")
    val completed: Boolean
)

typealias Todos = List<Todo>
