drop table LINEITEM;
drop table ORDERS;
drop table VENDOR_PART;
drop table VENDOR;
drop table PART_DETAIL;
drop table PART;

/* Self-ref to identify Bill of Material (BOM)
 * Compound PK
 */
create table PART (
    PART_NUMBER        VARCHAR(15)    NOT NULL ,
    REVISION        NUMERIC(2)    NOT NULL ,
    DESCRIPTION        VARCHAR(127)    ,
    REVISION_DATE        DATE        NOT NULL ,
    BOM_PART_NUMBER        VARCHAR(15)    ,
    BOM_REVISION        NUMERIC(2)    ,
    PRIMARY KEY (PART_NUMBER, REVISION),
    FOREIGN KEY (BOM_PART_NUMBER, BOM_REVISION) REFERENCES PART (PART_NUMBER, REVISION)
);

/* Bean will be mapped to 2 tables (PART and PART_DETAIL)
 * BLOB column type
 * CLOB column type */
create table PART_DETAIL (
    PART_NUMBER    VARCHAR(15)    NOT NULL ,
    REVISION    NUMERIC(2)    NOT NULL ,
    SPECIFICATION    CLOB        ,
    DRAWING        BLOB        ,
    PRIMARY KEY (PART_NUMBER, REVISION)
);

/* PK can be mapped to a primitive PK field type */
create table VENDOR (
    VENDOR_ID    INTEGER        PRIMARY KEY,
    NAME        VARCHAR(30)    NOT NULL ,
    ADDRESS        VARCHAR(127)    NOT NULL ,
    CONTACT        VARCHAR(127)    NOT NULL ,
    PHONE        VARCHAR(30)    NOT NULL
);

/* Can be used for unknown PK
 * 1-1 to PART
 * Compound FK
 */
create table VENDOR_PART (
    VENDOR_PART_NUMBER    NUMERIC(19)        PRIMARY KEY,
    DESCRIPTION        VARCHAR(127)        ,
    PRICE            DOUBLE PRECISION    NOT NULL ,
    VENDOR_ID        INTEGER            NOT NULL ,
    PART_NUMBER        VARCHAR(15)        NOT NULL ,
    PART_REVISION        NUMERIC(2)        NOT NULL ,
    FOREIGN KEY (VENDOR_ID) REFERENCES VENDOR (VENDOR_ID),
    FOREIGN KEY (PART_NUMBER, PART_REVISION) REFERENCES PART (PART_NUMBER, REVISION),
    UNIQUE (PART_NUMBER, PART_REVISION)
);

create table ORDERS (
    ORDER_ID    INTEGER        PRIMARY KEY,
    STATUS        CHAR(1)        NOT NULL ,
    LAST_UPDATE    DATE        NOT NULL ,
    DISCOUNT    NUMERIC(2)    NOT NULL ,
    SHIPMENT_INFO    VARCHAR(127)
);

/* Overlapping PK-FK
 * Uni-directional to VENDOR_PART */
create table LINEITEM (
    ORDER_ID        INTEGER        NOT NULL ,
    ITEM_ID            NUMERIC(3)    NOT NULL ,
    QUANTITY        NUMERIC(3)    NOT NULL ,
    VENDOR_PART_NUMBER    NUMERIC(19)    NOT NULL ,
    FOREIGN KEY (ORDER_ID) REFERENCES ORDERS (ORDER_ID),
    FOREIGN KEY (VENDOR_PART_NUMBER) REFERENCES VENDOR_PART (VENDOR_PART_NUMBER),
    PRIMARY KEY (ORDER_ID, ITEM_ID)
);
