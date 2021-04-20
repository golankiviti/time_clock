import DAL.{DB, InMemoryDB}
import entities.{Employee, Session}
import java.time.LocalDateTime
import java.util.Scanner

import Action._
import reports.Reporter

import scala.collection.mutable.{ListBuffer, Map}
import scala.util.{Failure, Success, Try}
object Main {
  def main(args: Array[String]): Unit = {
    val db = InMemoryDB
    val employees = getInitEmployeesData()
    db.setEmployees(employees)
    val scanner = new java.util.Scanner(System.in)

    printMainMenu()

    var enum = processInput(scanner)

    while (enum != CLOSE) {
      enum match {
        case CREATE_EMPLOYEE => createEmployee(scanner, db)
        case REPORT_START => reportStart(scanner, db)
        case REPORT_END => reportEnd(scanner, db)
        case EMPLOYEE_REPORT => employeeReport(scanner, db)
      }

      printMainMenu()
      enum = processInput(scanner)
    }
  }

  private def printMainMenu(): Unit = {
    println("Action Menu:")
    Action.values.foreach(println)
    println
  }

  private def processInput(scanner: Scanner): Actions = {
    print("Please select your action: ")
    var line = scanner.nextLine()
    var action = Try(Action.withName(line.toUpperCase))
    while(action.isFailure) {
      print("You have entered an invalid option, please select your action again: ")
      line = scanner.nextLine()
      action = Try(Action.withName(line.toUpperCase))
    }

    action.get
  }

  private def createEmployee(scanner: Scanner, db: DB): Unit = {
    val employeeName = scanEmployeeName(scanner)

    val action = Try(db.createEmployee(employeeName))

    action match {
      case Failure(exception) => println(s"Failed to create employee because: ${exception.getMessage}")
      case Success(_) => println(s"Successfully created the employee named ${employeeName}")
    }

    println
  }

  private def reportStart(scanner: Scanner, db: DB): Unit = {
    val employeeName = scanEmployeeName(scanner)

    val action = Try(db.reportStart(employeeName))

    action match {
      case Failure(exception) => println(s"Failed to report start because: ${exception.getMessage}")
      case Success(_) => println(s"Successfully reported start for the employee named ${employeeName}")
    }

    println
  }

  private def reportEnd(scanner: Scanner, db: DB): Unit = {
    val employeeName = scanEmployeeName(scanner)

    val action = Try(db.reportEnd(employeeName))

    action match {
      case Failure(exception) => println(s"Failed to report end because: ${exception.getMessage}")
      case Success(_) => println(s"Successfully reported end for the employee named ${employeeName}")
    }

    println
  }

  private def employeeReport(scanner: Scanner, db: DB): Unit = {
    val employeeName = scanEmployeeName(scanner)

    val action = Try(Reporter.report(db, employeeName))

    action match {
      case Failure(exception) => println(s"Failed to create employee report because: ${exception.getMessage}")
      case Success(report) => {
        println
        println(s"Working hours per day of ${employeeName}")
        report.workingHoursPerDay.foreach{case (day, hours) => println(s"${day} -> ${hours}")}
        println
        println(s"Working hours per month of ${employeeName}")
        report.workingHoursPerMonth.foreach{case (month, hours) => println(s"${month} -> ${hours}")}
        println
      }
    }
  }

  private def scanEmployeeName(scanner: Scanner): String = {
    print("Please enter employee name: ")
    scanner.nextLine()
  }

  private def getInitEmployeesData(): Map[String, ListBuffer[Session]] = {
    val date1 = LocalDateTime.of(2021, 4, 4, 8, 0)
    val date2 = LocalDateTime.of(2021, 4, 5, 8, 0)
    val date3 = LocalDateTime.of(2021, 5, 4, 8, 0)
    val date4 = LocalDateTime.of(2021, 5, 5, 8, 0)

    val sessions = ListBuffer(
      Session(date1, Some(date1.plusHours(8))),
      Session(date2, Some(date2.plusHours(8).plusMinutes(30))),
      Session(date3, Some(date3.plusHours(8))),
      Session(date4, Some(date4.plusHours(3).plusMinutes(30)))
    )
    val employee = Employee("golan", sessions.toList)
    Map(employee.name -> sessions)
  }
}