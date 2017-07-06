package uk.co.paul_matthews.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import uk.co.paul_matthews.kotlinandroid.data.PersonData

val Any.tag: String
    get() = this::class.simpleName.toString()

class MainActivity : AppCompatActivity() {
    val personData by lazy {
        PersonData(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(tag, "Here")
        personData.getPerson("fred")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(tag, "Found user: ${it}")
                }, {
                    Log.e(tag, "Error getting user: ${it.message}: ${it.stackTrace}")
                }, {
                    Log.d(tag, "No user found.")
                })

        personData.getPerson("pmatthews")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(tag, "Found user: ${it}")
                }, {
                    Log.e(tag, "Error getting user: ${it.message}: ${it.stackTrace}")
                }, {
                    Log.d(tag, "No user found.")
                })
    }
}
