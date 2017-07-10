package uk.co.paul_matthews.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.paul_matthews.kotlinandroid.data.Person
import uk.co.paul_matthews.kotlinandroid.data.PersonData


val Any.tag: String
    get() = this::class.simpleName.toString()

class MainActivity : AppCompatActivity() {
    val personData by lazy { PersonData(application) }
    val ui by lazy {
        Ui {
            Log.d(tag, "Person clicked: $it")
        }
    }
    val subscriptions: MutableList<Disposable> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Subscribe to all peopleData change events
        val peopleSub = personData.getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(tag, "Found users, displaying them")
                    ui.updateInfo(it)
                }
        subscriptions.add(peopleSub)
    }

    override fun onResume() {
        super.onResume()

        // Subscribe to all submit btn clicks
        val submitSub = ui.getSubmitClicks().subscribe {
            Log.d(tag, "Submit clicked.")
            val (username, firstName, lastName) = ui.getFormData()
            subscriptions.add(personData.insertPerson(Person(username, firstName, lastName))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.d(tag, "Inserted user")
                        ui.clearForm()
                    })
        }
        subscriptions.add(submitSub)

        // Subscribe to all reset btn clicks
        val resetSub = ui.getResetClicks().subscribe {
            Log.d(tag, "Reset clicked.")
            ui.clearForm()
        }
        subscriptions.add(resetSub)
    }

    override fun onPause() {
        super.onPause()

        subscriptions.map { it.dispose() }
        subscriptions.clear()
    }

    /**
     * Encapsulating the UI interaction
     */
    inner class Ui(val infoItemAction: (Person) -> Unit) {
        val info: RecyclerView = info_list
        val usernameField: EditText = username_field
        val firstNameField: EditText = first_name_field
        val lastNameField: EditText = last_name_field
        private val submitClicks = RxView.clicks(submit)
        private val resetClicks = RxView.clicks(reset)
        val infoLayoutManager by lazy {
            LinearLayoutManager(this@MainActivity)
        }
        val infoAdapter by lazy {
            MainInfoAdapter(mutableListOf(), infoItemAction)
        }
        init {
            info.setHasFixedSize(true)
            info.layoutManager = infoLayoutManager
            info.adapter = infoAdapter
        }

        fun getSubmitClicks(): Observable<Any?> = submitClicks

        fun getResetClicks(): Observable<Any?> = resetClicks

        fun updateInfo(people: List<Person>) {
            infoAdapter?.update(people)
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

/**
 * The Adapter for the info list view
 */
class MainInfoAdapter(val items: MutableList<Person>, val listener: (Person) -> Unit):
        RecyclerView.Adapter<MainInfoAdapter.Holder>() {

    override fun onBindViewHolder(holder: Holder, position: Int) =
            holder.bind(items[position], listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(inflate(parent, R.layout.person_line))

    override fun getItemCount() = items.size

    fun update(replacementItems: List<Person>) {
        items.clear()
        items.addAll(replacementItems)
        notifyDataSetChanged()
    }

    fun inflate(parent: ViewGroup, res: Int): View =
            LayoutInflater.from(parent.context).inflate(res, parent, false)

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun  bind(person: Person, listener: (Person) -> Unit) = with(itemView) {
            this.findViewById<TextView>(R.id.username).text = person.username
            this.findViewById<TextView>(R.id.first_name).text = person.firstName
            this.findViewById<TextView>(R.id.last_name).text = person.lastName
            setOnClickListener {
                listener(person)
            }
        }
    }
}