package system.exceptions

/**
  * A common trait for the ariadne system exceptions
  * Created by Matteo Gabellini on 09/08/2017.
  */
trait AriadneException extends Exception {
    def message: String
}

/**
  * Basic implementation of an AriadneException, this class provide the linking between the type AriadneException
  * defined by the trait and the java class Exception
  *
  * @param message the detail message. The detail message is saved for
  *                later retrieval by the method of the java class Exception
  **/
class BasicAriadneException(override val message: String) extends Exception(message) with AriadneException

/**
  * The exception throwed when an incorrect configuration is loaded
  *
  * @param subjectName the subject that tries to load its configuration
  **/
case class IncorrectConfigurationException(val subjectName: String)
    extends BasicAriadneException(subjectName + " tried to load the configuration but it is incorrect")
        with AriadneException


/**
  * The exception throwed when an incorrect initialization message is sended to an actor
  *
  * @param subjectName        the subject that received the incorrect init message
  * @param initMessageContent the content of the message received
  **/
case class IncorrectInitMessageException(val subjectName: String, val initMessageContent: List[Any])
    extends BasicAriadneException(subjectName + " received an init message that is incorrect, the message content is " + initMessageContent.toString())
