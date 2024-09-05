package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE show = 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE notOnServer = 1")
    suspend fun getAllUnsent(): List<PostEntity>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE authorId = :id ORDER BY id DESC")
    fun getMyWalLPagingSource(id:Int): PagingSource<Int, PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Upsert
    suspend fun save(post: PostEntity)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getById(id: Int): PostEntity

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    suspend fun likeById(id: Int, userId: Int) {
        val post = getById(id)
        val likesList = post.likeOwnerIds.toMutableList()
        if (post.likedByMe) {
            likesList.remove(userId)
        } else {
            likesList.add(userId)
        }
        save(post.copy(likeOwnerIds = likesList, likedByMe = !post.likedByMe))
    }

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}