package system.exceptions

/**
  * Created by Matteo Gabellini on 09/08/2017.
  */
trait AriadneException extends Exception

/**
  * The exception throwed when an incorrect configuration is loaded
  *
  * @param subjectName the subject that tries to load its configuration
  **/
case class IncorrectConfigurationException(val subjectName: String,
                                           private val cause: Throwable = None.orNull)
    extends Exception(subjectName + " tried to load the configuration but it is incorrect", cause)


/**
  * The exception throwed when an incorrect initialization message is sended to an actor
  *
  * @param subjectName        the subject that received the incorrect init message
  * @param initMessageContent the content of the message received
  **/
case class IncorrectInitMessageException(val subjectName: String, val initMessageContent: List[Any],
                                         private val cause: Throwable = None.orNull)
    extends Exception(subjectName + " received an init message that is incorrect, the message content is " + initMessageContent.toString(), cause)
