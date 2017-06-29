package uk.co.paul_matthews.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainActivity : AppCompatActivity() {
    val tag: String by lazy {
        this::class.simpleName.toString()
    }

    val numbersObserver: Observable<Int> =
        Observable.create(ObservableOnSubscribe<Int> {
            val rand = Random()
            for (i in 1..5) {
                if (Thread.interrupted()) {
                    break
                }
                Thread.sleep(500)
                it?.onNext(rand.nextInt(10))
            }
            it?.onComplete()
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNumbers()
                .take(3)
                .subscribe(getLogger("first".padEnd(7)))
        getNumbers()
                .subscribe(getLogger("second".padEnd(7)))
        getNumbers()
                .filter({ it > 5 })
                .subscribe(getLogger("gt 5".padEnd(7)))

        getNumbers()
                .subscribeBy(onComplete = {
                    getNumbers().subscribe(getLogger("delayed".padEnd(7)))
                })
    }

    fun getNumbers(): Observable<Int> = numbersObserver

    /**
     * Get a logging subscriber
     */
    fun getLogger(id: String): Observer<Int> = object: Observer<Int> {
        var count = 0
        override fun onComplete() {
            Log.d(tag, "$id) connection complete")
        }

        override fun onError(e: Throwable?) {
            Log.e(tag, "$id) Error with ${e?.message}: " + e?.stackTrace)
        }

        override fun onNext(t: Int?) {
            Log.d(tag, "$id) [${++count}] Observed: $t")
        }

        override fun onSubscribe(d: Disposable?) {
            Log.d(tag, "$id) Subscribed!")
        }
    }
}
