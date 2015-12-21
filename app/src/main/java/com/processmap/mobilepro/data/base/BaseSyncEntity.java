package com.processmap.mobilepro.data.base;


import java.util.Date;

public class BaseSyncEntity extends BaseEntity {

    public Date createdAt = null;
    public long createdBy = 0;
    public String createdByName = null;
    public Date updatedAt = null;
    public long updatedBy = 0;
    public String updatedByName = null;

    //column names
    public static final String COLUMN_CREATED_AT            = "createdAt";
    public static final String COLUMN_CREATED_BY            = "createdBy";
    public static final String COLUMN_CREATED_BY_NAME       = "createdByName";
    public static final String COLUMN_UPDATED_AT            = "updatedAt";
    public static final String COLUMN_UPDATED_BY            = "updatedBy";
    public static final String COLUMN_UPDATED_BY_NAME       = "updatedByName";


}
