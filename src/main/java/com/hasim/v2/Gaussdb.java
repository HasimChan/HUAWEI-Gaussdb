package com.hasim.v2;

import java.io.*;
import java.util.*;

/**
 * 非持久化版
 * 提交时把除查询的输出去掉
 * 问题：
 * 1. 只有查询所有时的通过率比实现单表查询的通过率高：猜测是有些查询结果为空，而实现了单表查询后结果不为空？why
 * 2. 列=常量、常量=列, 列=列没考虑
 * 3. 先看看实现两表查询后通过率有何改变
 * 4. 插入的数据中文本类型数据可能带有(),
 * 5. 索引名字段值可重复
 */
public class Gaussdb {

    private static HashMap<String, Table> tables = new HashMap<>();

    public static void main(String[] args) {

        String[] sqls = FileUtils.readFile("G:\\Project\\Java\\Study\\huaweiDB\\case.sql", false).split(";");
//        Scanner scan = new Scanner(System.in);
//        StringBuilder builder = new StringBuilder();
//        while (scan.hasNext()) {
//            builder.append(scan.next() + " ");
//        }
//
//        String[] sqls = builder.toString().trim().split(";");

        for (String sql : sqls) {
            System.out.println("[INFO] Gaussdb# " + sql);
            SqlParser sqlParser = new SqlParser();
            String subSql = sqlParser.sqlParse(sql);

            switch (sqlParser.getSqlType()) {
                case CREATE_TABLE:
                    sqlParser.createTableParse(subSql);
                    createTable(sqlParser.getTableName()[0], sqlParser.getFieldsName(), sqlParser.getFieldsType());
                    break;
                case CREATE_INDEX:
                    String fieldsName = sqlParser.createIndexParse(subSql);
                    createIndex(sqlParser.getTableName()[0], fieldsName);
                    break;
                case INSERT:
                    String[] contents = sqlParser.insertParse(subSql);
                    insert(sqlParser.getTableName()[0], contents);
                    break;
                case SELECT:
                    Map<String, Map<String, String[]>> conditionMapList = sqlParser.selectParse(subSql);
                    switch (sqlParser.getSqlType()) {
                        case SINGLE_TABLE_SELECT_ALL:
                            selectAll(sqlParser.getTableName()[0]);
                            break;
                        case SINGLE_TABLE_SELECT:
                            String tableName = sqlParser.getTableName()[0];
                            singleTableSelect(tableName, conditionMapList.get(tableName));
                            break;
                        case TWO_TABLE_SELECT:
                            break;
                    }
                    break;
            }
        }
    }

    private static void createTable(String tableName, ArrayList<String> fieldsName, boolean[] fieldsType) {
        Table table = new Table(fieldsName, fieldsType);
        tables.put(tableName, table);
        System.out.println("CREATE TABLE");
    }

    private static void createIndex(String tableName, String fieldName) {
        Table table = tables.get(tableName);
        table.setTableIndex(fieldName);
        System.out.println("CREATE INDEX");
    }

    /**
     * 插入数据库表
     *
     * @param tableName
     * @param contents
     */
    private static void insert(String tableName, String[] contents) {
        Table table = tables.get(tableName);
        table.insert(contents);
        System.out.println("INSERT 1");
    }

    private static void selectAll(String tableName) {
        Table table = tables.get(tableName);
        System.out.println(table.selectAll());
    }

    private static void singleTableSelect(String tableName, Map<String, String[]> conditionMap) {
        Table table = tables.get(tableName);
//        System.out.println(table.singleTableSelect(conditionMap));
    }
}

/**
 * sql命令类型枚举
 */
enum SqlType {
    CREATE_TABLE,
    CREATE_INDEX,
    INSERT,
    SELECT,
    SINGLE_TABLE_SELECT,
    SINGLE_TABLE_SELECT_ALL,
    TWO_TABLE_SELECT
}

/**
 * 考虑建立索引后的插入
 */
class Table {
    private int tableIndex = -1;
    private ArrayList<String> tableFieldsName; // 0: type 1: index
    private boolean[] tableFieldsType;
    private ArrayList<String[]> tableDataArray;
    private HashMap<String, ArrayList<String[]>> tableDataMap; // 索引字段值 - 行

    /**
     * 建表，初始化字段名称及类型及存储结构
     *
     * @param tableFieldsName
     * @param tableFieldsType
     */
    public Table(ArrayList<String> tableFieldsName, boolean[] tableFieldsType) {
        this.tableFieldsName = tableFieldsName;
        this.tableFieldsType = tableFieldsType;
        tableDataArray = new ArrayList<>();
    }

    public boolean isIndex() {
        return tableIndex != -1;
    }

    public void setTableIndex(String fieldName) {
        int index = tableFieldsName.indexOf(fieldName);
        if (index != -1) {
            this.tableIndex = index;
            if (tableDataArray.size() != 0) {
                makeMap();
            } else {
                tableDataMap = new HashMap<>();
            }
        }
        System.out.println("[INFO] Index field: " + fieldName);
        System.out.println("[INFO] Index: " + index);
    }

    public void insert(String[] contents) {
        int len = contents.length;
        String[] row = new String[len];

        for (int i = 0; i < len; i++) {
            if (tableFieldsType[i]) { // 字段类型为int
                row[i] = contents[i];
            } else { // 字段类型为text
                row[i] = contents[i].substring(1, contents[i].length() - 1);
            }
        }
        System.out.println("[INFO] the inserted content: " + Arrays.toString(row));
        if (isIndex()) {
            ArrayList<String[]> list = tableDataMap.get(row[tableIndex]);
            if (list == null) {
                list = new ArrayList<>();
                list.add(row);
                tableDataMap.put(row[tableIndex], list);
            } else {
                list.add(row);
            }
        } else {
            tableDataArray.add(row);
        }
    }

    public String selectAll() {
        StringBuilder builder = new StringBuilder();
        ArrayList<String[]> rows;

        if (isIndex()) {
            rows = new ArrayList<>();
            for (ArrayList<String[]> val : tableDataMap.values())  // 合并Map数据
                rows.addAll(val);
        } else {
            rows = tableDataArray;
        }

        for (String[] row : rows)
            builder.append(format(row, null));
        return builder.toString();
    }

    //
//    public String singleTableSelect(Map<String, String[]> conditionMap) {
//
//        String[] targetFields = conditionMap.get("targetFields");
//        int len = targetFields.length;
//        int[] targetIndex = new int[len]; // 目标字段索引
//        for (int i = 0; i < len; i++) {
//            targetIndex[i] = tableFields.get(targetFields[i])[1];
//        }
//
//        if (isIndex() && conditionMap.get("andEqualsKey") == null && conditionMap.get("andNotEqualsKey") == null) {
//            String[] orEqualsKeys = conditionMap.get("orEqualsKey");
//            String[] orNotEqualsKeys = conditionMap.get("orNotEqualsKey");
//            if (orEqualsKeys != null && tableFields.get(orEqualsKeys[0])[1] == tableIndex && arrayAllEquals(orEqualsKeys)) {
//                if (orNotEqualsKeys == null || orNotEqualsKeys != null && tableFields.get(orNotEqualsKeys[0])[1] == tableIndex && arrayAllEquals(orNotEqualsKeys)) {
//                    // 有索引且索引有效，索引查询
//                    StringBuilder builder = new StringBuilder();
//                    if (orNotEqualsKeys != null) {
//                        HashMap<String, String[]> copyTableData = tableDataMap; // 数据副本
//                        String[] orNotEqualsVals = conditionMap.get("orNotEqualsVal");
//                        for (String val : orNotEqualsVals) { // 删除元素
//                            copyTableData.remove(val);
//                        }
//                        if (orEqualsKeys != null) { // 补充
//                            String[] orEqualsVals = conditionMap.get("orEqualsVal");
//                            for (String val : orEqualsVals) {
//                                String[] row = tableDataMap.get(val);
//                                if (row != null)
//                                    copyTableData.put(val, row);
//                            }
//                        }
//                        for (String[] row : copyTableData.values()) {
//                            builder.append(printDeal(row, targetIndex, false));
//                        }
//                        return builder.toString();
//                    }
//
//                    if (orEqualsKeys != null) {
//                        String[] orEqualsVals = conditionMap.get("orEqualsVal");
//                        for (String val : orEqualsVals) {
//                            String[] row = tableDataMap.get(val);
//                            if (row != null) {
//                                builder.append(printDeal(row, targetIndex, false));
//                            }
//                        }
//                        return builder.toString();
//                    } else {
//                        return "";
//                    }
//                }
//            }
//        }
//
//        String[] orEqualsKeys = conditionMap.get("orEqualsKey");
//        String[] orNotEqualsKeys = conditionMap.get("orNotEqualsKey");
//        String[] andEqualsKeys = conditionMap.get("andEqualsKey");
//        String[] andNotEqualsKeys = conditionMap.get("andNotEqualsKey");
//
//        int[] orEqualsKeysIndex = new int[0];
//        int[] orNotEqualsKeysIndex = new int[0];
//        int[] andEqualsKeysIndex = new int[0];
//        int[] andNotEqualsKeysIndex = new int[0];
//
//        // 查询字段索引
//        if (orEqualsKeys != null) {
//            orEqualsKeysIndex = getFieldIndex(orEqualsKeys);
//        }
//        if (orNotEqualsKeys != null) {
//            orNotEqualsKeysIndex = getFieldIndex(orNotEqualsKeys);
//        }
//        if (andEqualsKeys != null) {
//            andEqualsKeysIndex = getFieldIndex(andEqualsKeys);
//        }
//        if (andNotEqualsKeys != null) {
//            andNotEqualsKeysIndex = getFieldIndex(andNotEqualsKeys);
//        }
//        StringBuilder builder = new StringBuilder();
//
//        String[][] copyTableData;
//
//        if (isIndex()) {
//            copyTableData = tableDataMap.values().toArray(new String[tableDataMap.size()][]);
//        } else {
//            copyTableData = tableDataArray.toArray(new String[tableDataArray.size()][]);
//        }
//
//        // 全表查询
//        for (String[] row : copyTableData) {
//            if (orEqualsKeys != null) {
//                String[] orEqualsVals = conditionMap.get("orEqualsVal");
//                if (isOrRow(row, orEqualsKeysIndex, orEqualsVals, true)) {
//                    builder.append(printDeal(row, targetIndex, false));
//                    continue;
//                }
//            }
//            if (orNotEqualsKeys != null) {
//                String[] orNotEqualsVals = conditionMap.get("orNotEqualsVal");
//                if (isOrRow(row, orNotEqualsKeysIndex, orNotEqualsVals, false)) {
//                    builder.append(printDeal(row, targetIndex, false));
//                    continue;
//                }
//            }
//
//            if (andEqualsKeys == null && andNotEqualsKeys == null)
//                continue;
//
//            boolean andEquals = false;
//            boolean andNotEquals = false;
//
//            if (andEqualsKeys != null) {
//                String[] andEqualsVals = conditionMap.get("andEqualsVal");
//                andEquals = isAndRow(row, andEqualsKeysIndex, andEqualsVals, true);
//            } else {
//                andEquals = true;
//            }
//
//            if (andNotEqualsKeys != null) {
//                String[] andNotEqualsVals = conditionMap.get("andNotEqualsVal");
//                andNotEquals = isAndRow(row, andNotEqualsKeysIndex, andNotEqualsVals, false);
//            } else {
//                andNotEquals = true;
//            }
//
//            if (andEquals && andNotEquals)
//                builder.append(printDeal(row, targetIndex, false));
//        }
//        return builder.toString();
//    }
//
    private void makeMap() { // 考虑索引存在重复值的情况
        tableDataMap = new HashMap<>();
        int index = tableIndex;
        for (String[] row : tableDataArray) {
            ArrayList<String[]> list = tableDataMap.get(row[index]);
            if (list == null) {
                list = new ArrayList<>();
                list.add(row);
                tableDataMap.put(row[index], list);
            } else {
                list.add(row);
            }
        }
    }

    /**
     * 格式化查询输出
     * @param row
     * @param index
     * @return
     */
    private String format(String[] row, int[] index) {
        StringBuilder builder = new StringBuilder();

        if (index == null) {
            int len = row.length;
            for (int i = 0; i < len; i++) {
                if (i == len - 1)
                    builder.append(row[i] + "\n");
                else
                    builder.append(row[i] + "|");
            }
            return builder.toString();
        } else {
            int len = index.length;
            for (int i = 0; i < len; i++) {
                if (i == len - 1)
                    builder.append(row[index[i]] + "\n");
                else
                    builder.append(row[index[i]] + "|");
            }
            return builder.toString();
        }
    }

    private boolean arrayAllEquals(String[] arr) {
        String first = arr[0];
        int len = arr.length;
        int count = 1;
        for (int i = 1; i < len; i++) {
            if (arr[i].equals(first))
                count++;
            else
                break;
        }
        return count == len;
    }

//    private boolean isOrRow(String[] row, int[] keyIndex, String[] values, boolean isEquals) {
//        int len = keyIndex.length;
//
//        for (int i = 0; i < len; i++) {
//            if (isEquals) {
//                if (typeArr[keyIndex[i]]) {
//                    if (row[keyIndex[i]].equals(values[i]))
//                        return true;
//                    continue;
//                } else {
//                    if (row[keyIndex[i]].equals(values[i].substring(1, values[i].length() - 1)))
//                        return true;
//                    continue;
//                }
//            } else {
//                if (typeArr[keyIndex[i]]) {
//                    if (!row[keyIndex[i]].equals(values[i]))
//                        return true;
//                    continue;
//                } else {
//                    if (!row[keyIndex[i]].equals(values[i].substring(1, values[i].length() - 1)))
//                        return true;
//                    continue;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean isAndRow(String[] row, int[] keyIndex, String[] values, boolean isEquals) {
//        int len = keyIndex.length;
//        int count = 0;
//
//        for (int i = 0; i < len; i++) {
//            if (isEquals) {
//                if (typeArr[keyIndex[i]]) {
//                    if (row[keyIndex[i]].equals(values[i]))
//                        count++;
//                    continue;
//                } else {
//                    if (row[keyIndex[i]].equals(values[i].substring(1, values[i].length() - 1)))
//                        count++;
//                    continue;
//                }
//            } else {
//                if (typeArr[keyIndex[i]]) {
//                    if (!row[keyIndex[i]].equals(values[i]))
//                        count++;
//                    continue;
//                } else {
//                    if (!row[keyIndex[i]].equals(values[i].substring(1, values[i].length() - 1)))
//                        count++;
//                    continue;
//                }
//            }
//        }
//        return count == len;
//    }
//
//    private int[] getFieldIndex(String[] keys) {
//        int keyLen = keys.length;
//        int[] indexs = new int[keyLen];
//        for (int i = 0; i < keyLen; i++) {
//            int[] kv = tableFields.get(keys[i]);
//            if (kv != null)
//                indexs[i] = kv[1];
//        }
//        return indexs;
//    }
}

/**
 * sql 解析器
 */
class SqlParser {
    private SqlType sqlType;
    private String[] tableName;
    private ArrayList<String> fieldsName;
    private boolean[] fieldsType;

    public SqlType getSqlType() {
        return sqlType;
    }

    public String[] getTableName() {
        return tableName;
    }

    public ArrayList<String> getFieldsName() {
        return fieldsName;
    }

    public boolean[] getFieldsType() {
        return fieldsType;
    }

    public String sqlParse(String sql) {
        sql = sql.trim();
        String type = sql.substring(0, 6);
        if ("create".equalsIgnoreCase(type)) {
            String subSql = sql.substring(6).trim();
            type = subSql.substring(0, 5);
            if ("table".equalsIgnoreCase(type)) {
                sqlType = SqlType.CREATE_TABLE;
                return subSql.substring(5);
            } else if ("index".equalsIgnoreCase(type)) {
                sqlType = SqlType.CREATE_INDEX;
                return subSql.substring(5);
            } else {
                throw new RuntimeException("sql maybe error");
            }
        } else if ("insert".equalsIgnoreCase(type)) {
            sqlType = SqlType.INSERT;
            return sql.substring(6);
        } else if ("select".equalsIgnoreCase(type)) {
            sqlType = SqlType.SELECT;
            return sql.substring(6);
        } else {
            throw new RuntimeException("sql maybe error");
        }
    }

    public void createTableParse(String subSql) { // 解析信息：表名、字段名、字段属性
        subSql = subSql.trim();

        String fieldsStr = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")); // 获取字段定义部分内容
        String[] fields = fieldsStr.split(","); // 分割字段

        int len = fields.length;
        fieldsName = new ArrayList<>();
        fieldsType = new boolean[len];

        for (int i = 0; i < len; i++) {
            String field = fields[i].trim();
            String[] kv = field.split("\\s+"); // 分割键值对(字段名-字段类型)

            if (kv.length != 2) {
                throw new RuntimeException("sql maybe error"); // 键值对长度为1，抛异常
            }

            if (kv[1].equalsIgnoreCase("int")) {
                fieldsType[i] = true;
            } else if (kv[1].equalsIgnoreCase("text")) {
                fieldsType[i] = false;
            } else {
                throw new RuntimeException("sql maybe error");
            }
            fieldsName.add(kv[0]);
        }
        tableName = new String[]{subSql.substring(0, subSql.indexOf("(")).trim()}; // 此处以往按空格为右边界，出现student(的情况，故修改

        System.out.println("[INFO] new table name: " + tableName[0]);
        System.out.println("[INFO] fieldsName: " + fieldsName);
        System.out.println("[INFO] fieldsType: " + Arrays.toString(fieldsType));
    }

    public String createIndexParse(String subSql) { // 解析信息，表名，字段名
        subSql = subSql.trim();
        // 获取字段名称
        String fieldName = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")).trim();
        tableName = new String[]{subSql.substring(0, subSql.indexOf("(")).split("\\s+")[2]}; // 获取表格名称，第三个位置
        return fieldName;
    }

    public String[] insertParse(String subSql) {
        subSql = subSql.trim();
        String[] temp = subSql.split("\\s+");
        tableName = new String[]{temp[1]}; //获取表名

        // 此处截取为最后一个右括号，并且解决了逗号问题
        String[] contents = subSql.substring(subSql.indexOf("(") + 1, subSql.lastIndexOf(")")).split(",(?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1); // 获取插入内容
        System.out.println("[INFO] will be inserted content: " + Arrays.toString(arrayTrim(contents)));
        return arrayTrim(contents);
    }

    public Map<String, Map<String, String[]>> selectParse(String subSql) {
        subSql = subSql.trim();

        if (subSql.charAt(0) == '*') { // 查询所有
            sqlType = SqlType.SINGLE_TABLE_SELECT_ALL;
            String[] sql = subSql.split("\\s+");
            tableName = new String[]{sql[sql.length - 1]};
            System.out.println("[INFO] select all from: " + tableName[0]);
            return null;
        } else {
            // 考虑text存在等号与不等号的情况和关键字的情况，关键字分割单靠加空格不行
            // Map  集合名称-等值条件
            HashMap<String, Map<String, String[]>> conditionMapList = new HashMap<>();
            String[] contents = splitIgnoreCase(subSql, "from");
            if (contents.length == 0)
                throw new RuntimeException("SQL syntax error");
            String targetFieldsStr = contents[0].trim();

            contents = splitIgnoreCase(contents[1], "where");
            if (contents.length == 0)
                throw new RuntimeException("SQL syntax error");

            String targetTablesStr = contents[0];
            String selectConditionStr = contents[1].trim();
            tableName = targetTablesParse(targetTablesStr);

            if (tableName.length == 1) { // 单表查询
                sqlType = SqlType.SINGLE_TABLE_SELECT;
                Map<String, String[]> conditionMap = singleSelectConditionParse(selectConditionStr);
                conditionMap.put("targetFields", arrayTrim(targetFieldsStr.split(",")));
                conditionMapList.put(tableName[0], conditionMap);
                System.out.println("[INFO] single table select from Table: " + tableName[0]);
                System.out.println("[INFO] target fields are: " + Arrays.toString(arrayTrim(targetFieldsStr.split(","))));
                return conditionMapList;
            } else {  // 两表查询

            }
        }
        return null;
    }

    private String[] targetTablesParse(String targetTablesStr) {
        String[] tables = targetTablesStr.split(",");
        return arrayTrim(tables);
    }

    private Map<String, String[]> singleSelectConditionParse(String selectConditionStr) {
        String[] contents = splitIgnoreCase(selectConditionStr, "or");

        ArrayList<String> orEqualsKey = new ArrayList<>();
        ArrayList<String> orEqualsVal = new ArrayList<>();
        ArrayList<String> orNotEqualsKey = new ArrayList<>();
        ArrayList<String> orNotEqualsVal = new ArrayList<>();
        ArrayList<String> andEqualsKey = new ArrayList<>();
        ArrayList<String> andEqualsVal = new ArrayList<>();
        ArrayList<String> andNotEqualsKey = new ArrayList<>();
        ArrayList<String> andNotEqualsVal = new ArrayList<>();

        for (String content : contents) {
            String[] andContents = splitIgnoreCase(content, "and");
            if (andContents.length == 1) { // 没有and
                String[] split = andContents[0].split("!=(?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1);
                System.out.println(Arrays.toString(split));
                if (split.length == 1) { // 等于
                    String[] kv = arrayTrim(andContents[0].split("=(?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1));
                    orEqualsKey.add(fieldParse(kv[0]));
                    orEqualsVal.add(kv[1]);
                } else { // 不等于
                    String[] kv = arrayTrim(andContents[0].split("!="));
                    orNotEqualsKey.add(fieldParse(kv[0]));
                    orNotEqualsVal.add(kv[1]);
                }
            } else { // 有and
                for (String andContent : andContents) {
                    if (andContent.indexOf("!=") == -1) { // 等于
                        String[] kv = arrayTrim(andContent.split("="));
                        andEqualsKey.add(fieldParse(kv[0]));
                        andEqualsVal.add(kv[1]);
                    } else { // 不等于
                        String[] kv = arrayTrim(andContent.split("!="));
                        andNotEqualsKey.add(fieldParse(kv[0]));
                        andNotEqualsVal.add(kv[1]);
                    }
                }
            }
        }
        HashMap<String, String[]> conditionMap = new HashMap<>();
        if (orEqualsKey.size() != 0) {
            conditionMap.put("orEqualsKey", orEqualsKey.toArray(new String[orEqualsKey.size()]));
            conditionMap.put("orEqualsVal", orEqualsVal.toArray(new String[orEqualsVal.size()]));
        }
        if (andEqualsKey.size() != 0) {
            conditionMap.put("andEqualsKey", andEqualsKey.toArray(new String[andEqualsKey.size()]));
            conditionMap.put("andEqualsVal", andEqualsVal.toArray(new String[andEqualsVal.size()]));
        }
        if (orNotEqualsKey.size() != 0) {
            conditionMap.put("orNotEqualsKey", orNotEqualsKey.toArray(new String[orNotEqualsKey.size()]));
            conditionMap.put("orNotEqualsVal", orNotEqualsVal.toArray(new String[orNotEqualsVal.size()]));
        }
        if (andNotEqualsKey.size() != 0) {
            conditionMap.put("andNotEqualsKey", andNotEqualsKey.toArray(new String[andNotEqualsKey.size()]));
            conditionMap.put("andNotEqualsVal", andNotEqualsVal.toArray(new String[andNotEqualsVal.size()]));
        }
        return conditionMap;
    }

    private String[] splitIgnoreCase(String str, String target) {
        target = target.toLowerCase();
        String[] split = str.split(" " + target + " (?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1);
        if (split.length == 1) {
            target = target.toUpperCase();
            split = str.split(" " + target + " (?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1);
            if (split.length != -1)
                return split;
        } else {
            return split;
        }
        return null;
    }

    private String[] arrayTrim(String[] array) {
        int len = array.length;
        for (int i = 0; i < len; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    private String fieldParse(String fieldStr) {
        String[] content = fieldStr.split("\\.");
        return content.length == 1 ? content[0] : content[1];
    }
}

/**
 * 文件IO工具类
 */
class FileUtils {
    public static void writeFile(String fileName, String content, boolean isAppend) {
        FileWriter fileWriter = null;
        try {
            //使用true，即进行append file
            fileWriter = new FileWriter(fileName, isAppend);
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String fileName, boolean isNewLine) {
        File file = new File(fileName);
        if (!file.exists())
            throw new RuntimeException("File does not exists");
        BufferedReader reader = null;
        StringBuilder sb = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (isNewLine)
                    sb.append(line + "\n");
                else
                    sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
