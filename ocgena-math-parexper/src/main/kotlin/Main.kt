import ru.misterpotz.expression.paramspace.EmptyParameterSpace
import ru.misterpotz.expression.m
import ru.misterpotz.expression.plus
import ru.misterpotz.expression.times

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")


    val m5 = (-5).m
    val p10 = 10.m

    val expr = m5 + p10 * p10

    println(expr.printTree())
    println(expr.evaluate(EmptyParameterSpace))
}