import java.util.regex.Pattern

fun main(args: Array<String>) {
    val text = "Here is some reasoning text"
    val regex = Regex("</?(?!think>|thinking>|thought>|reasoning>)[a-zA-Z][a-zA-Z0-9_]*(?:_of_[a-zA-Z0-9_]+)*>")
    val result = regex.replace(text, "")
    println("Text: " + text)
    println("Result: " + result)
    
    val text2 = "<think>"
    println("Tag result: " + regex.replace(text2, ""))

    val text3 = "This is a <word> that is <of> a test </word>"
    println("Tag result 3: " + regex.replace(text3, ""))
}

main(emptyArray())
