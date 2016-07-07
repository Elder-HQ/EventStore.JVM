package eventstore

sealed trait ExpectedVersion {
  def asInt(): Int
}

object ExpectedVersion {
  val First: Exact = Exact(0)

  def apply(expectedVersion: Int): ExpectedVersion = expectedVersion match {
    case -1 => ExpectedVersion.NoStream
    case -2 => ExpectedVersion.Any
    case _  => ExpectedVersion.Exact(expectedVersion)
  }

  //The stream being written to should not yet exist. If it does exist treat that as a concurrency problem.
  case object NoStream extends ExpectedVersion {
    override def toString = "Expected.NoStream"

    override def asInt(): Int = -1
  }

  sealed trait Existing extends ExpectedVersion

  // This write should not conflict with anything and should always succeed.
  case object Any extends Existing {
    override def toString = "Expected.AnyVersion"
    override def asInt(): Int = -2
  }

  // States that the last event written to the stream should have a sequence number matching your expected value.
  case class Exact(value: Int) extends Existing {
    require(value >= 0, s"expected version must be >= 0, but is $value")

    override def toString = s"Expected.Version($value)"
    override def asInt(): Int = value
  }

  object Exact {
    def apply(eventNumber: EventNumber.Exact): Exact = Exact(eventNumber.value)
  }
}