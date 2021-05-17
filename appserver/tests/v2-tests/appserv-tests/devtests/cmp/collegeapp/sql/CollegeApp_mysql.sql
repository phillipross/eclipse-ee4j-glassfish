DROP TABLE STUD_COURSE;
DROP TABLE COURSE;
DROP TABLE STUDENT;
DROP TABLE DEPT;
DROP TABLE ACCOUNT;
DROP TABLE ADDRESS;


CREATE TABLE ADDRESS (
    ADDRESSID DECIMAL(38),
    STREET VARCHAR(255),
    CODE DECIMAL(38),
    CITY VARCHAR(255),
    STATE VARCHAR(255),
    CONSTRAINT ADDR_CT PRIMARY KEY (ADDRESSID)
) ENGINE=INNODB;

CREATE TABLE ACCOUNT (
    ACCOUNTID VARCHAR(255),
    FEESPAID DECIMAL(38,2),
    FEESDUE DECIMAL(38,2),
    DUEDATE DATE,
    CONSTRAINT ACC_CT PRIMARY KEY (ACCOUNTID)
) ENGINE=INNODB;

CREATE TABLE DEPT (
    DEPTID DECIMAL(38),
    DEPTNAME VARCHAR(255),
    CONSTRAINT DEPT_CT PRIMARY KEY (DEPTID)
) ENGINE=INNODB;

CREATE TABLE STUDENT (
    STUDENTID DECIMAL(38),
    STUDENTNAME VARCHAR(255),
    DEPTID DECIMAL(38),
    ADDRESSID DECIMAL(38),
    ACCOUNTID VARCHAR(255),
    CONSTRAINT st_CT PRIMARY KEY (STUDENTID),
    FOREIGN KEY (DEPTID) REFERENCES DEPT (DEPTID),
    FOREIGN KEY (ADDRESSID) REFERENCES ADDRESS (ADDRESSID),
    FOREIGN KEY (ACCOUNTID) REFERENCES ACCOUNT (ACCOUNTID)
) ENGINE=INNODB;

CREATE TABLE COURSE (
    COURSEID DECIMAL(38),
    DEPTID DECIMAL(38),
    COURSENAME VARCHAR(255),
    SYLABUS BLOB,
    CONSTRAINT COURSE_CT PRIMARY KEY (COURSEID),
    FOREIGN KEY (DEPTID) REFERENCES DEPT (DEPTID)
) ENGINE=INNODB;

CREATE TABLE STUD_COURSE (
    COURSEID DECIMAL(38),
    STUDENTID DECIMAL(38),
    CONSTRAINT STCO_CT PRIMARY KEY (COURSEID, STUDENTID),
    FOREIGN KEY (COURSEID) REFERENCES COURSE (COURSEID),
    FOREIGN KEY (STUDENTID) REFERENCES STUDENT (STUDENTID)
) ENGINE=INNODB;

exit;
