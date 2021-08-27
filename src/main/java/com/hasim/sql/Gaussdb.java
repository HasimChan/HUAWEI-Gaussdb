package com.hasim.sql;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 现存问题：
 * 1. 数据库持久化采用什么格式的文件？json、Excel、txt？
 * 初步采用2种文件，一种定义数据库表规格，另一种负责存储数据
 * 2. 加载数据采用什么数据结构以提高查找效率？B+树
 * 3. 查找方式如何实现
 */

/**
 * 数据库表：
 * 1. .type存储表类型
 * 2. .data存储表数据
 */

/**
 * 考虑不持久化
 * String - 字段：field-索引
 * 内容用数组承载
 * 若有索引将内容转为Hash表
 * tableName - tableData
 *
 */
public class Gaussdb {

    public static void main(String[] args) {

        String[] sqls = FileUtils.readFile("G:\\Project\\Java\\Study\\huaweiDB\\case.sql", false).split(";");
        for (String sql : sqls) {
            SqlParser sqlParser = new SqlParser();
            String subSql = sqlParser.sqlParse(sql);
            switch (sqlParser.getSqlType()) {
                case CREATE_TABLE:
                    TableFields tableFields = sqlParser.createTableParse(subSql);
                    createTable(sqlParser.getTableName()[0], tableFields);
                    break;
                case CREATE_INDEX:
                    sqlParser.createIndexParse(subSql);
                    createIndex();
                    break;
                case INSERT:
                    String insertContent = sqlParser.insertParse(subSql);
                    insert(sqlParser.getTableName()[0], insertContent);
                    break;
                case SELECT:
                    int[] targetIndex = sqlParser.selectParse(subSql);
                    switch (sqlParser.getSqlType()) {
                        case SINGLE_TABLE_SELECT_ALL:
                            selectAll(sqlParser.getTableName()[0]);
                            break;
                        case SINGLE_TABLE_SELECT:
                            singleTableSelect(sqlParser.getTableName()[0], targetIndex,
                                    sqlParser.getOrEqualsKey(), sqlParser.getOrEqualsVal(),
                                    sqlParser.getAndEqualsKey(), sqlParser.getAndEqualsVal(),
                                    sqlParser.getOrNotEqualsKey(), sqlParser.getOrNotEqualsVal(),
                                    sqlParser.getAndNotEqualsKey(), sqlParser.getAndNotEqualsVal());
                            break;
                        case SINGLE_TABLE_SELECT_INDEX:
                            singleTableSelectIndex(sqlParser.getTableName()[0], targetIndex,
                                    sqlParser.getOrEqualsKey()[0], sqlParser.getOrEqualsVal());
                            break;
                    }
                    break;
            }
        }
    }

    private static void createTable(String tableName, TableFields tableFields) {
        File file = new File(tableName + ".type");
        if (file.exists())
            throw new RuntimeException("table is existed");

        FileUtils.writeFile(tableName + ".type", tableFields.getTableFieldsStr(), false);
        System.out.println("CREATE TABLE");
    }

    private static void createIndex() {
        System.out.println("CREATE INDEX");
    }

    /**
     * 插入数据库表
     *
     * @param tableName
     * @param insertContent
     */
    private static void insert(String tableName, String insertContent) {
        FileUtils.writeFile(tableName, insertContent, true);
        System.out.println("INSERT 1");
    }

    private static void selectAll(String tableName) {
        System.out.println(FileUtils.readFile(tableName, true));
    }

    private static void singleTableSelect(String tableName, int[] targetIndex,
                                          Integer[] orEqualsKey, String[] orEqualsVal,
                                          Integer[] andEqualsKey, String[] andEqualsVal,
                                          Integer[] orNotEqualsKey, String[] orNotEqualsVal,
                                          Integer[] andNotEqualsKey, String[] andNotEqualsVal) {
        TableData tableData = new TableData();
        String[][] table = tableData.getTableData(tableName);
        StringBuilder builder = new StringBuilder();

        for (String[] row : table) {
            boolean isTargetRow = false;
            if (orEqualsKey != null) {
                int len = orEqualsKey.length;
                for (int i = 0; i < len; i++) {
                    if (row[orEqualsKey[i]].equals(orEqualsVal[i])) {
                        builder.append(getResultStr(row, targetIndex));
                        isTargetRow = true;
                        break;
                    }
                }
            }
            if (isTargetRow)
                continue;

            if (orNotEqualsKey != null) {
                int len = orNotEqualsKey.length;
                for (int i = 0; i < len; i++) {
                    if (!row[orNotEqualsKey[i]].equals(orNotEqualsVal[i])) {
                        builder.append(getResultStr(row, targetIndex));
                        isTargetRow = true;
                        break;
                    }
                }
            }
            if (isTargetRow)
                continue;

            if (andEqualsKey != null) {
                int len = andEqualsKey.length;
                int count = 0;
                for (int i = 0; i < len; i++) {
                    if (row[andEqualsKey[i]].equals(andEqualsVal[i])) {
                        count++;
                    } else {
                        break;
                    }
                }
                if (count != len)
                    continue;
                if (andNotEqualsKey == null)
                    builder.append(getResultStr(row, targetIndex));
            }

            if (andNotEqualsKey != null) {
                int len = andNotEqualsKey.length;
                int count = 0;
                for (int i = 0; i < len; i++) {
                    if (!row[andNotEqualsKey[i]].equals(andNotEqualsVal[i])) {
                        count++;
                    }
                }
                if (count != len)
                    continue;
                builder.append(getResultStr(row, targetIndex));
            }
        }
        System.out.println(builder);
    }

    private static String getResultStr(String[] row, int[] targetIndex) {
        StringBuilder builder = new StringBuilder();
        int len = targetIndex.length;
        for (int i = 0; i < len; i++) {
            if (i == len - 1)
                builder.append(row[i] + "\n");
            else
                builder.append(row[i] + "|");
        }
        return builder.toString();
    }

    private static void singleTableSelectIndex(String tableName, int[] targetIndex,
                                               int index, String[] orEqualsVal) {
        TableData tableData = new TableData();
        Map<String, String[]> table = tableData.getTableDataIndex(tableName, index);
        StringBuilder builder = new StringBuilder();
        for (String val : orEqualsVal) {
            String[] target = table.get(val);
            if (target != null)
                builder.append(getResultStr(target, targetIndex));
        }
        System.out.println(builder);
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
    SINGLE_TABLE_SELECT_INDEX,
    SINGLE_TABLE_SELECT_ALL,
    TWO_TABLE_SELECT
}

/**
 * 数据表类型(字段集合)
 */
class TableFields {
    private ArrayList<Field> fieldList; // 字段集合
    private int tableIndex = -1;

    public TableFields() {
        this.fieldList = new ArrayList<>();
    }

    /**
     * 将字符串转为数据表字段对象
     *
     * @param tableFieldsStr
     */
    public TableFields(String tableFieldsStr) {
        this();
        String[] strings = tableFieldsStr.split("\n");
        if (strings.length == 1) {
            tableFieldsStr = strings[0].trim();
        } else {
            tableFieldsStr = strings[0].trim();
            tableIndex = Integer.valueOf(strings[1].trim());
        }
        for (String field : tableFieldsStr.split("\\|")) {
            String[] array = field.split(":");
            this.addField(array[0], Integer.valueOf(array[1]) == 1 ? true : false);
        }
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public String getTableFieldsStr() {
        StringBuilder builder = new StringBuilder();

        int len = getFieldsSize();
        for (int i = 0; i < len; i++) {
            Field field = fieldList.get(i);
            if (i == len - 1)
                builder.append(field.getFieldName() + ":" + (field.isInt() ? 1 : 0));
            else
                builder.append(field.getFieldName() + ":" + (field.isInt() ? 1 : 0) + "|");
        }

        return builder.toString();
    }

    /**
     * 添加字段
     *
     * @param fieldName
     * @param fieldType
     */
    public void addField(String fieldName, boolean fieldType) {
        fieldList.add(new Field(fieldName, fieldType));
    }

    public int getFieldsSize() {
        return fieldList.size();
    }

    public int indexOf(String fieldName) {
        int len = getFieldsSize();
        for (int i = 0; i < len; i++) {
            if (fieldList.get(i).getFieldName().equals(fieldName)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isIntIn(int index) {
        return fieldList.get(index).isInt();
    }

    static class Field {
        private String fieldName;
        private boolean fieldType;

        public Field(String fieldName, boolean fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public boolean isInt() {
            return fieldType;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "fieldName='" + fieldName + '\'' +
                    ", fieldType=" + fieldType +
                    '}';
        }
    }
}

class TableData {

    public String[][] getTableData(String tableName) {
        String tableStr = FileUtils.readFile(tableName, true);
        String[] rows = tableStr.split("\n");
        ArrayList<String[]> tableData = new ArrayList<>();
        for (String row : rows) {
            tableData.add(row.split("\\|"));
        }
        return tableData.toArray(new String[tableData.size()][]);
    }

    public Map<String, String[]> getTableDataIndex(String tableName, int index) {
        String tableStr = FileUtils.readFile(tableName, true);
        String[] rows = tableStr.split("\n");
        HashMap<String, String[]> tableData = new HashMap<>();
        for (String row : rows) {
            String[] rowArr = row.split("\\|");
            tableData.put(rowArr[index], rowArr);
        }
        return tableData;
    }
}

/**
 * sql 解析器
 */
class SqlParser {
    private SqlType sqlType;
    private String[] tableName;
    private Integer[] orEqualsKey;
    private String[] orEqualsVal;
    private Integer[] andEqualsKey;
    private String[] andEqualsVal;
    private Integer[] orNotEqualsKey;
    private String[] orNotEqualsVal;
    private Integer[] andNotEqualsKey;
    private String[] andNotEqualsVal;

    public SqlType getSqlType() {
        return sqlType;
    }

    public String[] getTableName() {
        return tableName;
    }

    public Integer[] getOrEqualsKey() {
        return orEqualsKey.length == 0 ? null : orEqualsKey;
    }

    public String[] getOrEqualsVal() {
        return orEqualsVal.length == 0 ? null : orEqualsVal;
    }

    public Integer[] getAndEqualsKey() {
        return andEqualsKey.length == 0 ? null : andEqualsKey;
    }

    public String[] getAndEqualsVal() {
        return andEqualsVal.length == 0 ? null : andEqualsVal;
    }

    public Integer[] getOrNotEqualsKey() {
        return orNotEqualsKey.length == 0 ? null : orNotEqualsKey;
    }

    public String[] getOrNotEqualsVal() {
        return orNotEqualsVal.length == 0 ? null : orNotEqualsVal;
    }

    public Integer[] getAndNotEqualsKey() {
        return andNotEqualsKey.length == 0 ? null : andNotEqualsKey;
    }

    public String[] getAndNotEqualsVal() {
        return andNotEqualsVal.length == 0 ? null : andNotEqualsVal;
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

    public TableFields createTableParse(String subSql) {
        subSql = subSql.trim();
        // 获取内容
        String fields = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")"));

        TableFields tableFields = new TableFields();
        for (String field : fields.split(",")) { // 分割字段
            field = field.trim();
            String[] kv = field.split("\\s+"); // 分割键值对
            if (kv.length != 2) {
                throw new RuntimeException("sql maybe error");
            }

            if (kv[1].equalsIgnoreCase("int")) {
                tableFields.addField(kv[0], true);
            } else if (kv[1].equalsIgnoreCase("text")) {
                tableFields.addField(kv[0], false);
            } else {
                throw new RuntimeException("sql maybe error");
            }
        }

        tableName = new String[]{subSql.substring(0, subSql.indexOf(" "))};
        return tableFields;
    }

    public void createIndexParse(String subSql) {
        subSql = subSql.trim();
        // 获取内容
        String fieldName = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")).trim();
        String tableName = subSql.substring(0, subSql.indexOf("(")).split("\\s+")[2]; // 分割键值对

        TableFields tableFields = new TableFields(FileUtils.readFile(tableName + ".type", false));
        int index = tableFields.indexOf(fieldName);
        if (index != -1)
            FileUtils.writeFile(tableName + ".type", "\n" + index, true);
    }

    public String insertParse(String subSql) {
        subSql = subSql.trim();
        String[] temp = subSql.split("\\s+");
        tableName = new String[]{temp[1]};

        TableFields tableFields = new TableFields(FileUtils.readFile(tableName[0] + ".type", true));

        String[] contents = subSql.substring(subSql.indexOf("(") + 1, subSql.indexOf(")")).split(","); // 插入内容
        int len = contents.length;

        if (len != tableFields.getFieldsSize())
            throw new RuntimeException("The number of parameters does not match!");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) { // 遍历内容
            String content = contents[i].trim();
            if (tableFields.isIntIn(i)) { // 字段为int类型
                Pattern pattern = Pattern.compile("[0-9]*");
                Matcher isNum = pattern.matcher(content);

                if (!isNum.matches()) {
                    throw new RuntimeException("Parameter type mismatch");
                }
            } else {
                if (content.charAt(0) == '\'')
                    content = content.substring(1, content.length() - 1);
                else
                    throw new RuntimeException("Parameter type mismatch");
            }

            if (i == len - 1)
                builder.append(content);
            else {
                builder.append(content + "|");
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    public int[] selectParse(String subSql) {
        subSql = subSql.trim();

        if (subSql.charAt(0) == '*') { // 查询所有
            sqlType = SqlType.SINGLE_TABLE_SELECT_ALL;
            String[] sql = subSql.split("\\s+");
            tableName = new String[]{sql[sql.length - 1]};
            return null;
        } else {
            String[] contents = splitIgnoreCase(subSql, "from");
            if (contents.length == 0)
                throw new RuntimeException("SQL syntax error");

            String targetFieldsStr = contents[0].trim();

            contents = splitIgnoreCase(contents[1], "where");
            if (contents.length == 0)
                throw new RuntimeException("SQL syntax error");

            String targetTablesStr = contents[0].trim();
            String selectConditionStr = contents[1].trim();
            tableName = targetTablesParse(targetTablesStr);

            if (tableName.length == 1) { // 单表查询
                sqlType = SqlType.SINGLE_TABLE_SELECT;
                TableFields tableFields = new TableFields(FileUtils.readFile(tableName[0] + ".type", true));
                // 目标字段索引数组
                int[] targetFieldsIndex = singleTargetFieldsParse(targetFieldsStr, tableFields);
                singleSelectConditionParse(selectConditionStr, tableFields);

                int tableIndex = tableFields.getTableIndex();
                if (tableFields.getTableIndex() != -1) {

                    if (getOrEqualsKey() != null && getOrNotEqualsKey() == null && getAndEqualsKey() == null && getAndNotEqualsKey() == null) {
                        int count = 0;
                        for (int key : getOrEqualsKey()) {
                            if (key == tableIndex)
                                count++;
                            else
                                break;
                        }
                        if (getOrEqualsKey().length == count) {
                            sqlType = SqlType.SINGLE_TABLE_SELECT_INDEX;
                        }
                    }
                }
                return targetFieldsIndex;
            } else {  // 两表查询

            }
        }
        return null;
    }

    private int[] singleTargetFieldsParse(String targetFieldsStr, TableFields tableFields) {
        // tableName - Fields - HashMap
        String[] targetFields = targetFieldsStr.split(",");
        int len = targetFields.length;
        int[] indexs = new int[len];
        for (int i = 0; i < len; i++) {
            String targetField = targetFields[i].trim();
            int index = tableFields.indexOf(targetField);
            if (index == -1)
                throw new RuntimeException("SQL syntax error");
            else
                indexs[i] = index;
        }
        return indexs;
    }

    private String[] targetTablesParse(String targetTablesStr) {
        String[] tables = targetTablesStr.split(",");
        return arrayTrim(tables);
    }

    private void singleSelectConditionParse(String selectConditionStr, TableFields tableFields) {
        String[] contents = splitIgnoreCase(selectConditionStr, "or");
        ArrayList<String> orConditions = new ArrayList<>();
        ArrayList<String> andConditions = new ArrayList<>();
        for (String content : contents) {
            String[] andContents = splitIgnoreCase(content, "and");
            if (andContents.length == 1) {
                orConditions.add(content.trim());
            } else {
                for (String andContent : andContents) {
                    andConditions.add(andContent.trim());
                }
            }
        }
        equalsConditionParse(orConditions.toArray(new String[orConditions.size()]), tableFields, true);
        equalsConditionParse(andConditions.toArray(new String[andConditions.size()]), tableFields, false);
    }

    private void equalsConditionParse(String[] conditionStrs, TableFields tableFields, boolean isOr) {
        ArrayList<Integer> equalsKey = new ArrayList<>();
        ArrayList<String> equalsVal = new ArrayList<>();
        ArrayList<Integer> notEqualsKey = new ArrayList<>();
        ArrayList<String> notEqualsVal = new ArrayList<>();

        for (String conditionStr : conditionStrs) {
            String[] conditions = conditionStr.split("!=");
            if (conditions.length == 1) {
                conditions = conditions[0].split("=");

                String fieldName = conditions[0].trim();
                if (fieldName.indexOf(".") != -1)
                    fieldName = fieldName.split("\\.")[1];
                int index = tableFields.indexOf(fieldName); // 字段有点
                equalsKey.add(index);
                if (tableFields.isIntIn(index))
                    equalsVal.add(conditions[1].trim());
                else {
                    String val = conditions[1].trim();
                    equalsVal.add(val.substring(1, val.length() - 1));
                }
            } else {
                String fieldName = conditions[0].trim();
                if (fieldName.indexOf(".") != -1)
                    fieldName = fieldName.split("\\.")[1];
                int index = tableFields.indexOf(fieldName); // 字段有点
                notEqualsKey.add(index);
                if (tableFields.isIntIn(index))
                    notEqualsVal.add(conditions[1].trim());
                else {
                    String val = conditions[1].trim();
                    notEqualsVal.add(val.substring(1, val.length() - 1));
                }
            }
        }

        if (isOr) {
            orEqualsKey = equalsKey.toArray(new Integer[equalsKey.size()]);
            orEqualsVal = equalsVal.toArray(new String[equalsVal.size()]);
            orNotEqualsKey = notEqualsKey.toArray(new Integer[notEqualsKey.size()]);
            orNotEqualsVal = notEqualsVal.toArray(new String[notEqualsVal.size()]);
        } else {
            andEqualsKey = equalsKey.toArray(new Integer[equalsKey.size()]);
            andEqualsVal = equalsVal.toArray(new String[equalsVal.size()]);
            andNotEqualsKey = notEqualsKey.toArray(new Integer[notEqualsKey.size()]);
            andNotEqualsVal = notEqualsVal.toArray(new String[notEqualsVal.size()]);
        }
    }

    private String[] splitIgnoreCase(String str, String target) {
        target = target.toLowerCase();
        int index = str.indexOf(target);
        if (index == -1) {
            target = target.toUpperCase();
            index = str.indexOf(target);
            if (index == -1)
                return new String[]{str};
            else
                return str.split(target);
        } else {
            return str.split(target);
        }
    }

    private String[] arrayTrim(String[] array) {
        int len = array.length;
        for (int i = 0; i < len; i++) {
            array[i] = array[i].trim();
        }
        return array;
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
