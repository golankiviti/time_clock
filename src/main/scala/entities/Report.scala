package entities

case class Month(month: Int, year: Int) {
  override def toString: String = s"${month}/${year}"
}

case class Day(day: Int, month: Int, year: Int) {
  override def toString: String = s"${day}/${month}/${year}"
}

case class Report(workingHoursPerDay: List[(Day, Double)],
                  workingHoursPerMonth: List[(Month, Double)])
