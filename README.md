# spark-statistics-demo
sparksql统计的多输入源和多输出源整合demo

我们经常有从其他数据库中导数出来进行分析的需求, 通常的做法是先导出成文本文件, 然后put到hdfs中, 最后使用hive或者spark使用。<br>
在网络打通的情况下其实没必要这么做, 毕竟apache提供了很多接口供spark去读取不同的数据库源。<br>
所以笔者将用过的整理成一个简单的程序, 用于spark处理不同数据源的输入和输出。<br>

## 执行demo程序(本地hdfs读取和输出)
1.主程序为RunSparkJobFromSameConfig<br>
2.以'job/conf/hdfs_demo.conf job/conf/db_config_dev.conf'作为程序的输入参数<br>

说明:<br>
db_config_dev.conf--------数据库配置文件<br>
hdfs_demo.conf------------sql任务配置文件<br>

## 配置文件说明
### 数据库配置文件
```java
{
  db2: {
    url: "jdbc:db2://127.0.0.1:60000/db22:currentSchema=db22;user=db22;password=db22;"
  },

  elasticsearch: {
    nodes: "127.0.0.1"
    port: "9200"
    pushdown: "true"
  },

  phoenix: {
    zk-url: "192.168.0.1,192.168.0.2,192.168.0.3"
  }
}
```
目前只支持db2/es/phoenix/hdfs的数据源输入(其他的笔者还没用到没添加)<br>

### sql任务配置文件
```java
{
  # 输入源处理, 可以同时有多个输入源
  inputs: [
    # hdfs的输入
    # table-name为sql项中定义的表名
    # 目前支持的类型有string/integer/double/long, 每行文件要和headers的顺序一一对应
    # table-name不在info内为代码失误, 各位同学配置时候请注意
    {
      type: "hdfs"
      info: {
        path: "files/input/person.txt"
        headrs: ["name:string", "age:integer", "gender:string", "marry:string", "income:double"]
        spliter: ","
      }
      table-name: "Person"
    },

    # es的输入
    # table-name为sql项中定义的表名
    {
      type: "elasticsearch"
      info: {
        path: "hello_es/doc"
        table-name: "hello_es"
      }
    },

    # phoenix的输入
    # table-name为sql项中定义的表名
    # columns为sql中需要用到的字段, 必写
    {
      type: "phoenix"
      info: {
        table: "HELLO_PHOENIX"
        columns: ["C_ORG_CODE", "C_SUB_ORG_CODE"]
        table-name: "HELLO_PHOENIX"
      }
    },

    # db2的输入
    # table-name为sql项中定义的表名
    {
      type: "db2"
      info: {
        table: "T_ATY_STEP"
        table-name: "T_ATY_STEP"
      }
    }
  ]

  # 处理sql计算输出
  sqls: [
    # 输出至hdfs, 需要指定分隔符
    {
      sql: "select name, age from Person"
      output: {
        type : "hdfs"
        path : "files/output/Person"
        spliter: ","
      }
    },

    # 保存至缓存, 保存至缓存的数据可以供其他sql使用
    {
      sql: """select t1.C_Aty_NO,
                     t1.C_Cst_NO,
                     t1.C_Step_ID,
                     t2.C_PHONE AS C_Cst_Tel
                from AAAA t1
           left join BBBB t2
                  on t1.C_Cst_NO = t2.C_CST_NO"""
      output: {
        type : "cache"
        table: "CACHE_TABLE"
      }
    },

    # 输出至es
    # values为输出到es中的字段名, 与sql中的select数据需要一一对应
    # keys为用作es索引的唯一组合字符串, 即keys中的字段会连接起来作为es索引的_id
    {
      sql: """select h1,
      h2, sum(h3),
      sum(h4) from
      demo_table group by h1, h2"""
      output: {
        type : "elasticsearch"
        path = "hello_es/doc"
        values = ["id", "name", "age"]
        keys = ["id", "name"]
      }
    },

    # 输出至phoenix
    # select中的字段名需要与phoenix表的字段名一摸一样, 可以使用as
    {
      sql: """select CLO1,CLO2,AAA as CLO3 from HELLO_PHOENIX where length(CLO1) > 2 """
      output: {
        type : "phoenix"
        table: "output_phoenix_table"
      }
    }
  ]
}
```

### 外部传参
有时候我们需要将外部参数传入到sql配置文件中, 我们需要做的也很简单<br>
1.直接在参数项追加需要的参数(在两个配置文件之后)
2.在sql配置文件中使用变量
```java
{
      sql: """select CLO1,CLO2,AAA as CLO3 from HELLO_PHOENIX where length(CLO1) > ${1} """
      output: {
        type : "phoenix"
        table: "output_phoenix_table_${2}"
      }
    }
```
上面的${1}和${2}就为传入的额外外部参数

## 笔者微信号:kboctopus, 欢迎交流
