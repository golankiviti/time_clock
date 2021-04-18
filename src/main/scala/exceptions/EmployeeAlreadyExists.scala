package exceptions

case class EmployeeAlreadyExists(name: String) extends Exception(
  s"An employee named ${name} already exists, there for cannot add him again."
)

case class EmployeeDoesNotExists(name: String) extends Exception(s"The employee named ${name} does not exists.")

case class EmployeeAlreadyHasAnOpenSession(name: String) extends Exception(
  s"The employee named ${name} already has an existing working session.\nOne must end a working session before he can open another one."
)

case class EmployeeDoesNotHaveAnOpenSession(name: String) extends Exception(
  s"The employee named ${name} does not have an open working session.\nOne must start a working session before ending one."
)

