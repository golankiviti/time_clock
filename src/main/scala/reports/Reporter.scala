package reports

import java.time.LocalDateTime

import DAL.DB
import java.time.temporal.ChronoUnit.SECONDS

import entities.{Day, Employee, Month, Report}


object Reporter {
  def report(db: DB, employeeName: String): Report = {
    val employee = db.getEmployee(employeeName)

    val workingHoursPerSession = calcWorkingHoursPerSession(employee)

    val workingHoursGroupedByDay = workingHoursPerSession.groupBy(_._1)
    val workingHoursGroupedByMonth = workingHoursPerSession.groupBy(x => Month(x._1.month, x._1.year))

    val workingHoursPerDay = workingHoursGroupedByDay.map{x => (x._1, sumHoursInDay(x._2))}.toList
    val workingHoursPerMonth = workingHoursGroupedByMonth.map{x => (x._1, sumHoursInMonth(x._2))}.toList

    entities.Report(workingHoursPerDay, workingHoursPerMonth)
  }

  def localDateTimeToDay(date: LocalDateTime): Day = {
    Day(date.getDayOfMonth, date.getMonthValue, date.getYear)
  }

  private def sumHoursInDay(workingDays: List[(Day, Double)]): Double = {
    workingDays.map(_._2).sum
  }

  private def sumHoursInMonth(workingDays: List[(Day, Double)]): Double = {
    workingDays.map(_._2).sum
  }

  private def calcWorkingHoursPerSession(employee: Employee): List[(Day, Double)] = {
    employee.sessions.filter(_.end.isDefined).map{ session =>
      val minutesInSession = SECONDS.between(session.start, session.end.get)

      val hoursInSession = (minutesInSession.toDouble / 60 / 60)
      (localDateTimeToDay(session.start), hoursInSession)
    }
  }
}
