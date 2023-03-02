package utils

fun mprintln(string: Any? = null) {
    if (string != null) {
        print(string)
    }
    spprintln()
}

expect fun spprintln()
