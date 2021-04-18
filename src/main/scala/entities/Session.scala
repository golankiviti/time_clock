package entities
import java.time.LocalDateTime

case class Session(start: LocalDateTime, end: Option[LocalDateTime])
