package duesoldi.logging

class Logger(name: String, loggingEnabled: Boolean = true) {

  def info(message: => String) {
    if (loggingEnabled) {
      System.out.println(s"[$name] $message")
    }
  }

  def error(message: => String) {
    if (loggingEnabled) {
      System.err.println(s"[$name] $message")
    }
  }

}
