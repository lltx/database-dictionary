package com.trh.dictionary.dao.sqlserver;

import com.trh.dictionary.bean.sqlserver.SqlserverColumnInfo;
import com.trh.dictionary.bean.sqlserver.SqlserverIndexInfo;
import com.trh.dictionary.bean.sqlserver.SqlserverTabelInfo;
import com.trh.dictionary.service.sqlserver.GenerateDataBaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

/**
 * @author zhou
 * @create 2019-08-29 16:35
 * @description:
 */
public class TestConnection {

    static Logger logger = LoggerFactory.getLogger(SqlserverConnectionFactory.class);

    @org.junit.Test
    public  void  testConnection(){

        String dbURL="jdbc:sqlserver://192.168.161.3:1433;DatabaseName=zhou";
        String userName="SA";
        String userPwd="zhoufan123AAA";
        Connection connection = SqlserverConnectionFactory.getConnection(dbURL,userName,userPwd);
        String sqltabel="SELECT Name as name FROM SysObjects Where XType='U' ORDER BY Name;";
        List<SqlserverTabelInfo> list_table=null;
        try {
           list_table = GenerateDataBaseInfo.getTableInfo(connection,sqltabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(SqlserverTabelInfo Ta:list_table){
            logger.info(Ta.getTableName());
            String sqlcolumn="SELECT (CASE WHEN a.colorder=1 THEN d.name ELSE NULL END) table_name,a.colorder column_num,a.name column_name,(CASE WHEN COLUMNPROPERTY(a.id,a.name,'IsIdentity')=1 THEN 'YES' ELSE '' END) is_identity,(CASE WHEN (\n" +
                    "SELECT COUNT (*) FROM sysobjects WHERE (name IN (\n" +
                    "SELECT name FROM sysindexes WHERE (id=a.id) AND (indid IN (\n" +
                    "SELECT indid FROM sysindexkeys WHERE (id=a.id) AND (colid IN (\n" +
                    "SELECT colid FROM syscolumns WHERE (id=a.id) AND (name=a.name))))))) AND (xtype='PK'))> 0 THEN 'YES' ELSE '' END) p_k,b.name type,a.length occupied_num,COLUMNPROPERTY(a.id,a.name,'PRECISION') AS length,isnull(COLUMNPROPERTY(a.id,a.name,'Scale'),0) AS scale,(CASE WHEN a.isnullable=1 THEN 'YES' ELSE '' END) is_null,isnull(e.text,'') default_value,isnull(g.[value],' ') AS decs,isnull(g.[class_desc],' ') AS class_desc FROM syscolumns a LEFT JOIN systypes b ON a.xtype=b.xusertype INNER JOIN sysobjects d ON a.id=d.id AND d.xtype='U' AND d.name<> 'dtproperties' LEFT JOIN syscomments e ON a.cdefault=e.id LEFT JOIN sys.extended_properties g ON a.id=g.major_id AND a.colid=g.minor_id LEFT JOIN sys.extended_properties f ON d.id=f.class AND f.minor_id=0 WHERE d.name ='"+Ta.getTableName()+"'";
            try {
                List<SqlserverColumnInfo> list_column=GenerateDataBaseInfo.getColumnInfo(connection,sqlcolumn);
                for(SqlserverColumnInfo s:list_column){
                    logger.info(s.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String sqlindex="SELECT index_name,index_desc,(LEFT (ind_col,LEN(ind_col)-1)+CASE WHEN include_col IS NOT NULL THEN ' INCLUDE ('+LEFT (include_col,LEN(include_col)-1)+')' ELSE '' END) AS index_keys FROM (\n" +
                    "SELECT i.name AS index_name,(\n" +
                    "SELECT CONVERT (VARCHAR (MAX),CASE WHEN i.index_id =1 THEN 'clustered' ELSE 'nonclustered' END+CASE WHEN i.ignore_dup_key <> 0 THEN ', ignore duplicate keys' ELSE '' END+CASE WHEN i.is_unique <> 0 THEN ', unique' ELSE '' END+CASE WHEN i.is_hypothetical <> 0 THEN ', hypothetical' ELSE '' END+CASE WHEN i.is_primary_key <> 0 THEN ', primary key' ELSE '' END+CASE WHEN i.is_unique_constraint <> 0 THEN ', unique key' ELSE '' END+CASE WHEN s.auto_created <> 0 THEN ', auto create' ELSE '' END+CASE WHEN s.no_recompute <> 0 THEN ', stats no recompute' ELSE '' END+' located on '+ISNULL(name,'')+CASE WHEN i.has_filter =1 THEN ', filter={'+i.filter_definition +'}' ELSE '' END) FROM sys.data_spaces WHERE data_space_id=i.data_space_id) AS 'index_desc',(\n" +
                    "SELECT INDEX_COL(OBJECT_NAME(i.object_id),i.index_id,key_ordinal),CASE WHEN is_descending_key=1 THEN N'(-)' ELSE N'' END+',' FROM sys.index_columns WHERE object_id=i.object_id AND index_id=i.index_id AND key_ordinal<> 0 ORDER BY key_ordinal FOR XML PATH ('')) AS ind_col,(\n" +
                    "SELECT col.name +',' FROM sys.index_columns inxc JOIN sys.columns col ON col.object_id=inxc.object_id AND col.column_id =inxc.column_id WHERE inxc.object_id=i.object_id AND inxc.index_id =i.index_id AND inxc.is_included_column =1 FOR XML PATH ('')) AS include_col FROM sys.indexes i JOIN sys.stats s ON i.object_id=s.object_id AND i.index_id =s.stats_id WHERE i.object_id=object_id('"+Ta.getTableName()+"')) Ind ORDER BY index_name";

            try {
                List<SqlserverIndexInfo> list_index=GenerateDataBaseInfo.getIndexInfo(connection,sqlindex);
                for(SqlserverIndexInfo s:list_index){
                    logger.info(s.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}