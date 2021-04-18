package DAL

import java.time.LocalDateTime

import entities.Employee

trait DB {
  def createEmployee(employeeName: String)
  def reportStart(employeeName: String): LocalDateTime
  def reportEnd(employeeName: String): LocalDateTime
  def employeeExists(employeeName: String): Boolean
  def employeeHasOpenSession(employeeName: String): Boolean
  def getEmployee(employeeName: String): Employee
  def openSession(employeeName: String)
  def closeSession(employeeName: String)
}
