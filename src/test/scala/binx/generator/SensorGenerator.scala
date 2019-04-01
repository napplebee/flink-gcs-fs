package binx.generator

import org.scalacheck.Gen

import scala.util.{Failure, Success, Try}

object GenUtil {
  def sampleIterator[T](gen: Gen[T]): Iterator[Option[T]] = Iterator continually gen.sample
  def toUnboundedIterator[T](gen: Gen[T]): Iterator[T] = sampleIterator(gen) collect { case Some(x) => x }
  def toIterator[T](gen: Gen[T], attempts: Int): Iterator[T] = {
    val toTryIterator = Iterator continually tryGetN(gen, attempts)
    toTryIterator.map(_.get)
  }
  def tryGetN[T](gen: Gen[T], attempts: Int): Try[T] = {
    var attempt = 0
    while (attempt < attempts) {
      gen.sample match {
        case Some(t) => return Success(t)
        case None => attempt += 1
      }
    }
    Failure(new RuntimeException("attempts exhausted, return failure"))
  }
  def tryGet[T](gen: Gen[T]): Try[T] = tryGetN(gen, 100)
  def toIteratorN[T](gen: Gen[T], attempts: Int): Iterator[T] = {
    val toTryIterator = Iterator continually tryGetN(gen, attempts)
    toTryIterator.map(_.get)
  }
  def toIterator[T](gen: Gen[T]): Iterator[T] = toIteratorN(gen, 100)

  def toIterableN[T](gen: Gen[T], attempts: Int): Iterable[T] = new Iterable[T] {
    override val iterator: Iterator[T] = GenUtil.toIterator(gen, attempts)
  }
  def toIterable[T](gen: Gen[T]): Iterable[T] = GenUtil.toIterableN(gen, 100)
}

object SensorGenerator {
  private val timestampGen: Gen[Long] = Gen.calendar.map(cal => cal.getTime).map(_.getTime)
  private val sensorType: Gen[String] = Gen.oneOf("door", "temp", "pressure")
  private val randomValue: Gen[Double] = Gen.chooseNum(-10.0, 10.0)

  private val sensorGen: Gen[(Long, String, Double)] = for {
    ts <- timestampGen
    tpe <- sensorType
    value <- randomValue
  } yield (ts, tpe, value)

  val iter: Iterator[(Long, String, Double)] = GenUtil.toIterator(sensorGen)
}