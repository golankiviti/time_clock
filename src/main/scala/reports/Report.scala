package reports

case class Month(month: Int, year: Int)

case class Day(day: Int, month: Int, year: Int)

case class Report(workingHoursPerDay: List[(Day, Double)],
                  workingHoursPerMonth: List[(Month, Double)])
