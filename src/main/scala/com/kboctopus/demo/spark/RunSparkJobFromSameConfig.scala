package com.kboctopus.demo.spark

import java.io.File

import com.kboctopus.spark.base.SparkBase
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._


/**
  * Created by 17427LF on 17/12/18.
  */
object RunSparkJobFromSameConfig extends SparkBase {
//object RunSparkJobFromSameConfig {

  def main(args: Array[String]) {

    System.setProperty("db2.jcc.charsetDecoderEncoder", "3")

    // 获取外部配置文件
    val jobConfiFileName = args(0)
    val dbConfiFileName = args(1)
    val jobConf = ConfigFactory.parseFile(new File(jobConfiFileName))
    val input = jobConf.getConfigList("inputs")
    val sqls = jobConf.getConfigList("sqls")

    // 初始化统一的连接数据
    val dbConf = ConfigFactory.parseFile(new File(dbConfiFileName))
    initDBConfig(dbConf.getConfig("db2"), dbConf.getConfig("elasticsearch"), dbConf.getConfig("phoenix"))

    // 初始化spark
    val sc = createSparkContext("RunSparkJobFromSameConfig", true)
    val hiveContext = getHiveSqlContext(sc)

    // 解析外部配置文件----处理输入
    handleInputFromOneConfig(sc, hiveContext, input.toList, args)

//    hiveContext.clearCache()
    // 处理sql执行
    handleSql(hiveContext, hiveContext, sqls.toList, args)
    hiveContext.clearCache()
  }
}
