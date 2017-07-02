package uk.co.paul_matthews.kotlinandroid.data

import android.app.Application
import android.arch.persistence.room.*
import android.content.ContextWrapper
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by pmatthews on 6/30/17.
 */

class PersonData(val app: Application): ContextWrapper(app) {
    fun getAll(): Observable<Optional<List<Person>>> = Observable.fromCallable {
        val response = app.getDatabase()?.personDao()?.getAll()
        if (response == null || response.isEmpty()) {
            Optional.None
        } else {
            Optional.Some(response)
        }
    }.subscribeOn(Schedulers.io())

    fun retrieve(username: String): Observable<Optional<Person>> = Observable.fromCallable {
            val response = app.getDatabase()?.personDao()?.getPerson(username)
            if (response == null) {
                Optional.None
            } else {
                Optional.Some(response)
            }
        }.subscribeOn(Schedulers.io())

    fun insert(person: Person): Observable<Optional.None> = Observable.fromCallable {
        app.getDatabase()?.personDao()?.insert(person)
        Optional.None
    }.subscribeOn(Schedulers.io())
}

@Entity(primaryKeys = arrayOf("username"))
data class Person(
        val username: String,
        val firstName: String,
        val lastName: String)

@Dao
interface PersonDao {
    companion object {
        const val OBJ = "person"
        const val USERNAME = "username"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
    }

    @Query("SELECT * FROM $OBJ")
    fun getAll(): List<Person>

    @Query("SELECT * FROM $OBJ WHERE $USERNAME = :username")
    fun getPerson(username: String): Person

    @Query("SELECT * FROM $OBJ WHERE $USERNAME IN (:usernames)")
    fun getPeople(usernames: List<String>): List<Person>

    @Insert
    fun insert(person: Person)

    @Insert
    fun insertAll(vararg persons: Person)
}
