package uk.co.paul_matthews.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.paul_matthews.kotlinandroid.data.Person
import uk.co.paul_matthews.kotlinandroid.data.PersonData

val Any.tag: String
    get() = this::class.simpleName.toString()

class MainActivity : AppCompatActivity() {
    val personData by lazy { PersonData(application) }
    val ui by lazy { Ui() }
    val subscriptions: MutableList<Disposable> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        displayPeople()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(ui.submitClicks.subscribe {
            Log.d(tag, "Submit clicked.")
            val (username, firstName, lastName) = ui.getFormData()
            subscriptions.add(personData.insertPerson(Person(username, firstName, lastName))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.d(tag, "Inserted user")
                        // Refresh the database
                        displayPeople()
                        ui.clearForm()
                    })
        })

        subscriptions.add(ui.resetClicks.subscribe {
            Log.d(tag, "Reset clicked.")
            ui.clearForm()
        })
    }

    override fun onPause() {
        super.onPause()

        subscriptions.map { it.dispose() }
        subscriptions.clear()
    }

    fun displayPeople() {
        Log.d(tag, "Triggering display people")
        subscriptions.add(personData.getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(tag, "Found users, displaying them")
                    ui.displayInfo(it.fold("", { acc, (uname, first, last) ->
                        "$acc$uname: $first $last\n"
                    }))
                })
    }

    inner class Ui {
        val infoText: TextView = info_label
        val usernameField: EditText = username_field
        val firstNameField: EditText = first_name_field
        val lastNameField: EditText = last_name_field

        val submitClicks by lazy {
            RxView.clicks(submit)
        }

        val resetClicks by lazy {
            RxView.clicks(reset)
        }

        fun displayInfo(info: String) {
            infoText.text = info
        }

        fun getFormData(): Triple<String, String, String> = Triple(usernameField.text.toString(),
                firstNameField.text.toString(), lastNameField.text.toString())

        fun clearForm() {
            listOf(usernameField, firstNameField, lastNameField).map {
                it.setText("")
            }
        }
    }
}

