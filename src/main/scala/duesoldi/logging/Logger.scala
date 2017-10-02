package duesoldi.logging

class Logger(loggingEnabled: Boolean) {

  def info(message: => String) {
    if (loggingEnabled) {
      System.out.println(message)
    }
  }

  def error(message: => String) {
    if (loggingEnabled) {
      System.err.println(message)
    }
  }

}
