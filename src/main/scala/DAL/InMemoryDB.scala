package DAL
import java.time.LocalDateTime

import scala.collection.mutable.{ListBuffer, Map}
import entities.{Employee, Session}
import exceptions.{EmployeeAlreadyExists, EmployeeAlreadyHasAnOpenSession, EmployeeDoesNotExists, EmployeeDoesNotHaveAnOpenSession}

import scala.util.{Failure, Success, Try}

object InMemoryDB extends DB {

  var employees = scala.collection.mutable.Map[String, ListBuffer[Session]]()
  var openSessions = ListBuffer[String]()

  override def createEmployee(employeeName: String): Unit = {
    if (employeeExists(employeeName)) {
      throw EmployeeAlreadyExists(employeeName)
    } else {
      employees += (employeeName -> ListBuffer[Session]())
    }
  }

  override def reportStart(employeeName: String): LocalDateTime = {
    if (!employeeExists(employeeName)) throw EmployeeDoesNotExists(employeeName)

    if (employeeHasOpenSession(employeeName)) throw EmployeeAlreadyHasAnOpenSession(employeeName)

    val currentDateTime = LocalDateTime.now()
    openSession(employeeName)
    employees(employeeName) += Session(currentDateTime, None)

    currentDateTime
  }

  override def reportEnd(employeeName: String): LocalDateTime = {
    if (!employeeExists(employeeName)) throw EmployeeDoesNotExists(employeeName)

    if(!employeeHasOpenSession(employeeName)) throw EmployeeDoesNotHaveAnOpenSession(employeeName)

    val currentDateTime = LocalDateTime.now()
    endEmployeeLatestSession(employeeName, currentDateTime)
    closeSession(employeeName)

    currentDateTime
  }

  override def employeeExists(employeeName: String): Boolean = {
    employees.contains(employeeName)
  }

  override def employeeHasOpenSession(employeeName: String): Boolean = openSessions.exists(_ == employeeName)

  override def getEmployee(employeeName: String): Employee = {
    Try(employees(employeeName)) match {
      case Success(sessions) => Employee(employeeName, sessions.toList)
      case Failure(_) => throw EmployeeDoesNotExists(employeeName)
    }
  }

  override def openSession(employeeName: String): Unit = {
    openSessions += employeeName
  }

  override def closeSession(employeeName: String): Unit = {
    openSessions -= employeeName
  }

  def setEmployees(employees: Map[String, ListBuffer[Session]]): Unit = {
    this.employees = employees
  }

  def setOpenSessions(openSessions: ListBuffer[String]): Unit = {
    this.openSessions = openSessions
  }

  def resetData(): Unit = {
    this.employees = scala.collection.mutable.Map[String, ListBuffer[Session]]()
    this.openSessions = ListBuffer[String]()
  }

  private def endEmployeeLatestSession(employeeName: String, dateTime: LocalDateTime): Unit = {
    val latestSessionIndex = employees(employeeName).indexWhere(_.end.isEmpty)
    val latestSession = employees(employeeName)(latestSessionIndex)

    employees(employeeName)(latestSessionIndex) = Session(latestSession.start, Some(dateTime))
  }
}
