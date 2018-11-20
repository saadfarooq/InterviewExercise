import java.lang.Thread.sleep
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    /**
     * There are three async data sources: primary, secondary and tertiary. They may either return a corresponding value
     * after a random amount of time or fail with a ResponseType.ERROR value.
     *
     * Business logic:
     * 1. If primary source returns an error, the entire process should fail (error() should be called)
     * 2. If either secondary or tertiary source errors, the error should be ignored
     * 3. In the case where primary does return a value, the output should be a summation of the values returned with
     * any errored values in secondary and tertiary replaced with the ResponseType.EMPTY value (success() method can do
     * the logging)
     *
     * E.g. ResponseSummation(primary=PRIMARY, secondary=EMPTY, tertiary=TERTIARY) is a valid response meaning the
     * secondary call failed with error
     * ResponseSummation(primary=ERROR, secondary=EMPTY, tertiary=TERTIARY) is not valid because error should have been
     * called in this case.
     *
     * ResponseSummation(primary=PRIMARY, secondary=SECONDARY, tertiary=ERROR) is also not valid because ERROR should
     * have been replaced by EMPTY for tertiary call
     *
     * Best solution known to us uses better abstractions around async calls allowing for composition of results. You're
     * welcome to use any available library for this purpose or you could try to do it in the current context while
     * trying to avoid callback hell. Keep in a mind a good solution would scale to even more calls, say six, without
     * much work.
     *
     * Try to keep time limited to reasonable value. I would say two hours should be enough but you shouldn't
     * stretch to more than four. You should also just be spending the vast majority of your time in this file. If you
     * end up having build issues or anything that are taking away from time spent on the actual problem, reach out
     * instead of trying to solve it yourself. Also, don't worry about any side issues (such as stopping the other calls
     * if the primary call is the first one to finish with an error), just focus on the main problem.
     *
     * A problematic and incomplete attempt is below.
     */
    AsyncSource.primary {
        if (it == ResponseType.ERROR) {
            error()
        } else {
//            success(it, ??, ??)
        }
    }
    AsyncSource.secondary {
        if (it == ResponseType.ERROR) {
//            success(??, EMPTY, ??)
        }
    }
    AsyncSource.tertiary {
        if (it == ResponseType.ERROR) {
//            success(??, ??, EMPTY)
        }
    }

}

fun error() {
    println("An un-retrievable error occurred")
}

fun success(primary: ResponseType, secondary: ResponseType, tertiary: ResponseType) {
    ResponseSummation(primary, secondary, tertiary).let {
        println("Final out is: $it")
    }
}

data class ResponseSummation(val primary: ResponseType, val secondary: ResponseType, val tertiary: ResponseType)

class AsyncSource {
    companion object {
        fun primary(fn: (ResponseType) -> Unit) = mockBackgroundTask(fn, ResponseType.PRIMARY)
        fun secondary(fn: (ResponseType) -> Unit) = mockBackgroundTask(fn, ResponseType.SECONDARY)
        fun tertiary(fn: (ResponseType) -> Unit) = mockBackgroundTask(fn, ResponseType.TERTIARY)

        private fun mockBackgroundTask(fn: (ResponseType) -> Unit, responseType: ResponseType) = thread {
            (1..10).shuffled().first().let {
                val sleepTime = it * 100L
                val randomResponse = if (it > 3) responseType else ResponseType.ERROR
                sleep(sleepTime)
                println("** $responseType call returned $randomResponse after $sleepTime millis")
                fn(randomResponse)
            }
        }
    }
}

enum class ResponseType {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    ERROR,
    EMPTY
}
