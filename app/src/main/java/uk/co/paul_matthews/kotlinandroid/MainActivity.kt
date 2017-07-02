package uk.co.paul_matthews.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import uk.co.paul_matthews.kotlinandroid.data.Optional
import uk.co.paul_matthews.kotlinandroid.data.Person
import uk.co.paul_matthews.kotlinandroid.data.PersonData

class MainActivity : AppCompatActivity() {
    val tag: String by lazy {
        this::class.simpleName.toString()
    }
    val personData by lazy {
        PersonData(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        personData.retrieve("pmatthews")
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        is Optional.Some -> Log.d(tag, "Found user: ${it.value}")
                        is Optional.None -> {
                            Log.d(tag, "No user found")
                            personData.insert(Person("pmatthews", "Paul", "Matthews"))
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Log.d(tag, "Inserted Correctly")
                                    }, {
                                        Log.e(tag, "Unable to insert")
                                    })
                        }
                    }
                }, {
                    Log.e(tag, "Error getting user: ${it.message}: ${it.stackTrace}")
                })
    }
}
