DROP TRIGGER T_LEAGUE ;
DROP TRIGGER T_PLAYER ;
DROP TRIGGER T_TEAM ;

DROP TABLE TEAMPLAYER ;
DROP TABLE PLAYER ;
DROP TABLE TEAM ;
DROP TABLE LEAGUE ;

CREATE TABLE PLAYER
(
    PLAYER_ID VARCHAR(255) PRIMARY KEY,
    NAME VARCHAR(255),
    POSITION VARCHAR(255),
    SALARY DOUBLE PRECISION NOT NULL,
    VERSION   NUMBER(19)  NOT NULL
);

CREATE TABLE LEAGUE
(
    LEAGUE_ID VARCHAR(255) PRIMARY KEY,
    NAME VARCHAR(255),
    SPORT VARCHAR(255),
    VERSION   NUMBER(19)  NOT NULL
);

CREATE TABLE TEAM
(
    TEAM_ID VARCHAR(255) PRIMARY KEY,
    CITY VARCHAR(255),
    NAME VARCHAR(255),
    LEAGUE_ID VARCHAR(255),
    VERSION   NUMBER(19)  NOT NULL,
    FOREIGN KEY (LEAGUE_ID)   REFERENCES LEAGUE (LEAGUE_ID)
);

CREATE TABLE TEAMPLAYER
(
    PLAYER_ID VARCHAR(255),
        TEAM_ID VARCHAR(255),
        CONSTRAINT PK_TEAMPLAYER PRIMARY KEY (PLAYER_ID, TEAM_ID),
    FOREIGN KEY (TEAM_ID)   REFERENCES TEAM (TEAM_ID),
    FOREIGN KEY (PLAYER_ID)   REFERENCES PLAYER (PLAYER_ID)
);

commit;

CREATE TRIGGER T_LEAGUE
   BEFORE UPDATE ON LEAGUE
   FOR EACH ROW
       WHEN (NEW.VERSION = OLD.VERSION)
   BEGIN
       :NEW.VERSION := :OLD.VERSION + 1;
   END;
/

CREATE TRIGGER T_PLAYER
   BEFORE UPDATE ON PLAYER
   FOR EACH ROW
       WHEN (NEW.VERSION = OLD.VERSION)
   BEGIN
       :NEW.VERSION := :OLD.VERSION + 1;
   END;
/

CREATE TRIGGER T_TEAM
   BEFORE UPDATE ON TEAM
   FOR EACH ROW
       WHEN (NEW.VERSION = OLD.VERSION)
   BEGIN
       :NEW.VERSION := :OLD.VERSION + 1;
   END;
/

commit;

quit;
