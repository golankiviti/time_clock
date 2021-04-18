package DAL

import java.time.LocalDateTime

import entities.{Employee, Session}
import exceptions.{EmployeeAlreadyExists, EmployeeAlreadyHasAnOpenSession, EmployeeDoesNotExists, EmployeeDoesNotHaveAnOpenSession}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import scala.collection.mutable.{ListBuffer, Map}

class InMemoryDBSpecs extends AnyFunSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach() {
    InMemoryDB.resetData()
  }

  describe(
    """ InMemoryDB - a singleton object which implements the db using:
      | employees: Map[String, List[Session]] - mock a db table of employees.
      | openSessions: List[String] - mock a db table of employees with open working session.
    """.stripMargin) {
    describe("createEmployee - creates a new employee with empty sessions.") {
      describe("If employee with the name already exists") {
        it("throws an EmployeeAlreadyExists exception.") {
          InMemoryDB.createEmployee("golan")
          an [EmployeeAlreadyExists] mustBe thrownBy(InMemoryDB.createEmployee("golan"))
        }
      }

      describe("If employee with the name doesn't exists") {
        it("adds a new elements to the employees map with the employeeName as key, and an empty list as value.") {
          InMemoryDB.createEmployee("golan")

          InMemoryDB.employees must contain ("golan" -> List[Session]())
        }
      }
    }

    describe("reportStart - starting a session for an employee.") {
      describe("When the employee doesn't exists") {
        it("throws an EmployeeDoesNotExists exception.") {
          an [EmployeeDoesNotExists] must be thrownBy(InMemoryDB.reportStart("golan"))
        }
      }

      describe("When the employee already has an open working session") {
        it("throws an EmployeeAlreadyHasAnOpenSession exception.") {
          InMemoryDB.createEmployee("golan")
          InMemoryDB.setOpenSessions(ListBuffer("golan"))

          an [EmployeeAlreadyHasAnOpenSession] must be thrownBy(InMemoryDB.reportStart("golan"))
        }
      }

      describe("When the employee doesn't have an open working session") {
        it("adds the employee name to the openSessions list.") {
          InMemoryDB.createEmployee("golan")
          InMemoryDB.reportStart("golan")

          InMemoryDB.openSessions must contain("golan")
        }

        it("adds a Session with the start as the current time and the end as None.") {
          InMemoryDB.createEmployee("golan")
          val startDateTime = InMemoryDB.reportStart("golan")

          val employee = InMemoryDB.getEmployee("golan")
          employee.sessions.head mustEqual Session(startDateTime, None)
        }
      }
    }

    describe("reportEnd - ending a session for an employee.") {
      describe("When the employee doesn't exists") {
        it("throws an EmployeeDoesNotExists exception.") {
          an [EmployeeDoesNotExists] must be thrownBy(InMemoryDB.reportEnd("golan"))
        }
      }

      describe("When the employee doesn't have an open working session") {
        it("throws an EmployeeDoesNotHaveAnOpenSession exception.") {
          InMemoryDB.createEmployee("golan")

          an [EmployeeDoesNotHaveAnOpenSession] must be thrownBy(InMemoryDB.reportEnd("golan"))
        }
      }

      describe("When the employee has an open working session") {
        it("removes the employee name from the openSessions list.") {
          InMemoryDB.createEmployee("golan")
          InMemoryDB.reportStart("golan")
          InMemoryDB.reportEnd("golan")

          InMemoryDB.openSessions must not contain("golan")
        }

        it("sets the end of the latest session with the current time.") {
          InMemoryDB.createEmployee("golan")
          val startDateTime = InMemoryDB.reportStart("golan")
          val endDateTime = InMemoryDB.reportEnd("golan")

          val employee = InMemoryDB.getEmployee("golan")
          employee.sessions.head mustEqual Session(startDateTime, Some(endDateTime))
        }
      }
    }

    describe("employeeExists - returns whether or not an employee exists in the db.") {
      describe("When employee exists") {
        it("returns true.") {
          InMemoryDB.createEmployee("golan")

          InMemoryDB.employeeExists("golan") mustBe true
        }
      }

      describe("When employee does not exists") {
        it("returns false.") {
          InMemoryDB.employeeExists("golan") mustBe false
        }
      }
    }

    describe("employeeHasOpenSession - returns whether or not an employee has an open working session.") {
      describe("When employee has an open working session") {
        it("returns true.") {
          InMemoryDB.setOpenSessions(ListBuffer("golan"))

          InMemoryDB.employeeHasOpenSession("golan") mustBe true
        }
      }

      describe("When employee does not exists") {
        it("returns false.") {
          InMemoryDB.employeeHasOpenSession("golan") mustBe false
        }
      }
    }

    describe("getEmployee - returns an employee by its name.") {
      describe("When the employee doesn't exists") {
        it("throws an EmployeeDoesNotExists exception.") {
          an [EmployeeDoesNotExists] must be thrownBy(InMemoryDB.getEmployee("golan"))
        }
      }

      describe("When employee exists") {
        it("returns the employee.") {
          val currentDate = LocalDateTime.now()
          val sessions = ListBuffer(
            Session(currentDate, Some(currentDate.plusHours(8))),
            Session(currentDate, Some(currentDate.plusHours(8)))
          )

          val employee = Employee("golan", sessions.toList)
          val employees = Map(employee.name -> sessions)
          InMemoryDB.setEmployees(employees)

          InMemoryDB.getEmployee("golan") mustEqual employee
        }
      }
    }

  }
}
