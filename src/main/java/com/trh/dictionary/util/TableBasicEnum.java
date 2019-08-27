package com.trh.dictionary.util;

/**
 * 表的基础字段
 *
 * @author
 * @create 2019-08-07 10:38
 */
public enum TableBasicEnum {
    /**
     * 表的引擎
     */
    ENGINE("ENGINE"),
    /**
     * 表的字符集
     */
    CHARSET("CHARSET"),
    /**
     * 表的注释
     */
    COMMENT("COMMENT"),
    /**
     * 主键索引
     */
    PRIMARY_KEY("PRIMARY KEY"),
    /**
     * 唯一索引
     */
    UNIQUE_KEY("UNIQUE KEY"),
    /**
     * 普通索引
     */
    KEY("KEY "),
    /**
     * 字段PRIMARY
     */
    WORD_PRIMARY("PRIMARY"),
    /**
     * 字段key
     */
    WORD_key("key");

    private String desc;

    TableBasicEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
