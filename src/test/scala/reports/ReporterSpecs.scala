package reports

import java.time.LocalDateTime

import DAL.InMemoryDB
import entities.{Employee, Session}
import exceptions.EmployeeDoesNotExists
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import Reporter.localDateTimeToDay

import scala.collection.mutable.{ListBuffer, Map}

class ReporterSpecs extends AnyFunSpec with Matchers with BeforeAndAfterEach {
  private val db = InMemoryDB

  override def beforeEach() {
    InMemoryDB.resetData()
  }

  describe(
    """
      | Reporter - creates a report for each employee.
      |            Returns a Report which contains the following:
      |            1) List of working hours per day.
      |            2) List of working hours per month.
    """.stripMargin) {
    describe("When employee does not exists") {
      it("throws an EmployeeDoesNotExists exception") {
        an [EmployeeDoesNotExists] must be thrownBy Reporter.report(db, "nonexist")
      }
    }

    describe("When employee has no working sessions") {
      it("returns an empty list for the working hours per day.") {
        InMemoryDB.createEmployee("golan")
        val report = Reporter.report(db, "golan")

        report.workingHoursPerDay must be(empty)
      }

      it("returns an empty list for the working hours per month.") {
        InMemoryDB.createEmployee("golan")
        val report = Reporter.report(db, "golan")

        report.workingHoursPerMonth must be(empty)
      }
    }

    describe("workingHoursPerDay in Report") {
      it("contains an element of (Day, Double), which represents the day and the working hours for each day.") {
        val date1 = LocalDateTime.now()
        val date2 = date1.plusDays(1)
        val sessions = ListBuffer(
          Session(date1, Some(date1.plusHours(8))),
          Session(date2, Some(date2.plusHours(8).plusMinutes(30)))
        )
        val employee = Employee("golan", sessions.toList)
        val employees = Map(employee.name -> sessions)
        InMemoryDB.setEmployees(employees)

        val expected = List(
          (localDateTimeToDay(date1), 8),
          (localDateTimeToDay(date2), 8.5)
        )
        val report = Reporter.report(db, "golan")

        report.workingHoursPerDay must contain theSameElementsAs expected
      }

      describe("When there are multiple sessions in the same day") {
        it("sums all the hours of all the sessions.") {
          val currentDate = LocalDateTime.now()
          val sessions = ListBuffer(
            Session(currentDate, Some(currentDate.plusHours(3))),
            Session(currentDate.plusHours(5), Some(currentDate.plusHours(9)))
          )
          val employee = Employee("golan", sessions.toList)
          val employees = Map(employee.name -> sessions)
          InMemoryDB.setEmployees(employees)

          val expected = List(
            (localDateTimeToDay(currentDate), 7)
          )
          val report = Reporter.report(db, "golan")

          report.workingHoursPerDay must contain theSameElementsAs expected
        }
      }

      describe("When there employee has an open session") {
        it("returns the result for all the finished sessions.") {
          val date1 = LocalDateTime.now()
          val date2 = date1.plusDays(1)
          val sessions = ListBuffer(
            Session(date1, Some(date1.plusHours(8))),
            Session(date2, Some(date2.plusHours(8).plusMinutes(30))),
            Session(date1.plusDays(3), None)
          )
          val employee = Employee("golan", sessions.toList)
          val employees = Map(employee.name -> sessions)
          InMemoryDB.setEmployees(employees)

          val expected = List(
            (localDateTimeToDay(date1), 8),
            (localDateTimeToDay(date2), 8.5)
          )
          val report = Reporter.report(db, "golan")

          report.workingHoursPerDay must contain theSameElementsAs expected
        }
      }
    }

    describe("workingHoursPerMonth in Report") {
      it("contains an element of (Month, Double), which represents the month and the working hours for each month.") {
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
        val employees = Map(employee.name -> sessions)
        InMemoryDB.setEmployees(employees)

        val expected = List(
          (Month(4, 2021), 16.5),
          (Month(5, 2021), 11.5)
        )
        val report = Reporter.report(db, "golan")

        report.workingHoursPerMonth must contain theSameElementsAs expected
      }

      describe("When there employee has an open session") {
        it("returns the result for all the finished sessions.") {
          val date1 = LocalDateTime.of(2021, 4, 4, 8, 0)
          val date2 = LocalDateTime.of(2021, 4, 5, 8, 0)
          val date3 = LocalDateTime.of(2021, 5, 4, 8, 0)
          val date4 = LocalDateTime.of(2021, 5, 5, 8, 0)

          val sessions = ListBuffer(
            Session(date1, Some(date1.plusHours(8))),
            Session(date2, Some(date2.plusHours(8).plusMinutes(30))),
            Session(date3, Some(date3.plusHours(8))),
            Session(date4, Some(date4.plusHours(3).plusMinutes(30))),
            Session(date4, None)
          )
          val employee = Employee("golan", sessions.toList)
          val employees = Map(employee.name -> sessions)
          InMemoryDB.setEmployees(employees)

          val expected = List(
            (Month(4, 2021), 16.5),
            (Month(5, 2021), 11.5)
          )
          val report = Reporter.report(db, "golan")

          report.workingHoursPerMonth must contain theSameElementsAs expected
        }
      }
    }
  }
}
