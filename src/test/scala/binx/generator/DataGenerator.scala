package binx.generator

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.compat.Platform
import scala.util.Random

object Text {
  
}

object DataGenerator {
  def now(): Long = Platform.currentTime

  def sequentialLong(env: StreamExecutionEnvironment, delay: Long = 500, numberOfElementsToEmit: Long = Long.MaxValue): DataStream[Long] = {
    env.addSource(new RichParallelSourceFunction[Long]() {
      private var running = true
      private var number = 0L
      override def run(ctx: SourceFunction.SourceContext[Long]): Unit = {
        while(running) {
          Thread.sleep(delay)
          ctx.collectWithTimestamp(number, now())
          continueOrCancel()
        }
      }

      def continueOrCancel(): Unit = {
        if (number == numberOfElementsToEmit) {
          cancel()
        } else {
          number += 1
        }
      }

      override def cancel(): Unit = {
        println("Canceling sequentialLong Generator")
        running = false
      }
    })
  }

  def randomIntegers(env: StreamExecutionEnvironment): DataStream[Integer] = {
    env.addSource(new RichParallelSourceFunction[Integer]() {
      private var running = true
      override def run(ctx: SourceFunction.SourceContext[Integer]): Unit = {
        while(running) {
          Thread.sleep(500)
          ctx.collectWithTimestamp(Random.nextInt(1000), now())
        }
      }
      override def cancel(): Unit = {
        running = false
      }
    })
  }

  def randomSensorData(env: StreamExecutionEnvironment): DataStream[String] = {
    env.addSource(new RichParallelSourceFunction[String]() {
      private var running = true
      override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
        while(running) {
          Thread.sleep(500)
          val data = SensorGenerator.iter.next()
          ctx.collectWithTimestamp(data.productIterator.map(_.toString).mkString(","), now())
        }
      }
      override def cancel(): Unit = {
        running = false
      }
    })
  }

  def sinusData(env: StreamExecutionEnvironment): DataStream[Double] = {
    env.addSource(new RichParallelSourceFunction[Double]() {
      private var running = true
      override def run(ctx: SourceFunction.SourceContext[Double]): Unit = {
        var x = 0.0
        while(running) {
          ctx.collectWithTimestamp(scala.math.sin(scala.math.toRadians(x)), now())
          x = if (x == 360.0) 0.0 else x + 1.0
        }
      }
      override def cancel(): Unit = {
        running = false
      }
    })
  }

  def squareData(env: StreamExecutionEnvironment): DataStream[Double] = {
    env.addSource(new RichParallelSourceFunction[Double]() {
      private var running = true
      override def run(ctx: SourceFunction.SourceContext[Double]): Unit = {
        var x = 0.0
        def squareWave(deg: Double): Double = {
          scala.math.signum(scala.math.sin(scala.math.toRadians(deg)))
        }
        while(running) {
          ctx.collectWithTimestamp(squareWave(x), now())
          x = if (x == 360.0) 0.0 else x + 1.0
        }
      }
      override def cancel(): Unit = {
        running = false
      }
    })
  }

//  def sawToothData(env: StreamExecutionEnvironment): DataStream[Double] = {
//    env.addSource(new RichParallelSourceFunction[Double]() {
//      private var running = true
//      override def run(ctx: SourceFunction.SourceContext[Double]): Unit = {
//        var x = 0.0
//        def squareWave(deg: Double): Double = {
//          scala.math.signum(scala.math.sin(scala.math.toRadians(deg)))
//        }
//        while(running) {
//          ctx.collectWithTimestamp(squareWave(x), now())
//          x = if (x == 360.0) 0.0 else x + 1.0
//        }
//      }
//      override def cancel(): Unit = {
//        running = false
//      }
//    })
//  }


//  def delayedTextGenerator(env: StreamExecutionEnvironment, text: String, delay: Long=1000): DataStream[String] = {
//    env.addSource(new RichParallelSourceFunction[String]() {
//      private var running = true
//      override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
//        while(running) {
//          Thread.sleep(delay)
//          ctx.collect(Random.nextInt(1000))
//        }
//      }
//      override def cancel(): Unit = {
//        running = false
//      }
//    })
//  }
}
