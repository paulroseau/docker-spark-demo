package com.github.paulroseau.dockersparkdemo

import org.apache.spark.{ SparkConf, SparkContext }

object Job {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf())
    sys.addShutdownHook(sc.stop())
    val rdd = sc.parallelize(Array(1, 2, 3, 4, 5))
    println(s"Sum of rdd : ${rdd.reduce((a, b) ⇒ a + b)}")
  }
}
