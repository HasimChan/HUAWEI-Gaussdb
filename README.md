# Gaussdb

## 1. 问题

### Q1：数据是否需要持久化？

一开始由于对数据库的主观印象，认为需要进行持久化，并以此为依据进行设计实现，后面出现结果超时，故改为非持久化版，赛题也是按case.sql为单位进行判别，后续有机会再参加此类比赛应注意审题

### Q2：通过率一直为0？

* 原因1：没有按照要求进行输出，输出了多余的东西
* 原因2：对于 Java 命令行的操作不够熟悉 `java xxx <xxx.xxx> xxx.out`为从标准输入读取`xxx.xxx`文件，并将输出存储到`xxx.out`中

### Q3：对于关键字的分割

* 对于关键字的分割没有考虑到其他字符串也有可能存在与关键字相同的子串的情况，如`password`与`or`，故在关键字的分割基础前后加上空格

## 2. 架构

* Gaussdb：程序入口，负责标准输入的读取以及整体软件功能
* SqlType：枚举类，定义sql语句的类型
* Table：数据表类，每个对象记录了表的字段信息和内容信息，并提供相关表操作
  * 索引
  * 字段
    * 新建表格：
    * 建立索引
    * 
  * 内容
* SqlParser： sql解析器，负责sql语句的解析

## 3. 解析思路

```sql
 -- 建表
 CREATE TABLE table_name (
     column1 data_type,
     column2 data_type,
     ...
     columnN data_type  
 );
 -- 插入
 INSERT INTO table_name VALUES (v1,v2,...,vn);
 -- 索引
 create index index_name on table_name(column_name);
 -- 单表查询
 select col1, col2, ..., coln from table_name where col1=const and col2=col3 and const=col4;
 -- 两表关联查询
 select t1.col1, t2.col2  from t1, t2 where t1.col1=t2.col1 and t1.col3=const and const=t2.col4;
```



