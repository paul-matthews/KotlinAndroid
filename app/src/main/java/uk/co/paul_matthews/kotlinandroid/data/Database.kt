package uk.co.paul_matthews.kotlinandroid.data

import android.app.Application
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase

/**
 * Created by pmatthews on 7/1/17.
 */
fun Application.getDatabase(): AKDatabase? {
    Singleton.instance.database = Singleton.instance.database ?:
            Room.databaseBuilder(this, AKDatabase::class.java, "person-db").build()
    return Singleton.instance.database
}

@Database(entities = arrayOf(Person::class), version = 1)
abstract class AKDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
}

/**
 * Inspired by https://medium.com/@adinugroho/singleton-in-kotlin-502f80fd8a63
 */
class Singleton private constructor() {
    private object Container { val INSTANCE = Singleton() }
    var database: AKDatabase? = null

    companion object {
        val instance: Singleton by lazy { Container.INSTANCE }
    }
}

/**
 * Inspired by https://medium.com/@joshfein/handling-null-in-rxjava-2-0-10abd72afa0b
 */
sealed class Optional<out M> {
    class Some<out M>(val value: M): Optional<M>()
    object None: Optional<Nothing>()
}