package uk.co.paul_matthews.kotlinandroid.data

import android.app.Application
import android.arch.persistence.room.*
import android.content.ContextWrapper
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

/**
 * Created by pmatthews on 6/30/17.
 */

class PersonData(val app: Application): ContextWrapper(app) {
    fun getAll(): Flowable<List<Person>> = app.getDatabase()?.personDao()?.getAll()
            ?: Flowable.error { Error("Unable to query the database") }

    fun getPeople(usernames: List<String>): Flowable<List<Person>> = app.getDatabase()?.personDao()?.getPeople(usernames)
            ?: Flowable.error { Error("Unable to query the database") }


    fun getPerson(username: String): Maybe<Person> = Maybe.create<Person> {
        val person = app.getDatabase()?.personDao()?.getPerson(username)
        if (person != null) {
            it.onSuccess(person)
        } else {
            it.onComplete()
        }
    }.subscribeOn(Schedulers.io())

    fun insertPerson(person: Person): Completable = Completable.fromAction {
        app.getDatabase()?.personDao()?.insert(person)
    }.subscribeOn(Schedulers.io())

    fun insertPeople(people: Array<Person>): Completable = Completable.fromAction {
        app.getDatabase()?.personDao()?.insertAll(*people)
    }.subscribeOn(Schedulers.io())
}

@Entity data class Person(@PrimaryKey val username: String, val firstName: String, val lastName: String)

@Dao
interface PersonDao {
    companion object {
        const val OBJ = "person"
        const val USERNAME = "username"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
    }

    @Query("SELECT * FROM $OBJ")
    fun getAll(): Flowable<List<Person>>

    @Query("SELECT * FROM $OBJ WHERE $USERNAME = :username")
    fun getPerson(username: String): Person

    @Query("SELECT * FROM $OBJ WHERE $USERNAME IN (:usernames)")
    fun getPeople(usernames: List<String>): Flowable<List<Person>>

    @Insert
    fun insert(person: Person)

    @Insert
    fun insertAll(vararg persons: Person)
}
