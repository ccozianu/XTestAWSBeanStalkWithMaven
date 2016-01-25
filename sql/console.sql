-- Very much work in prppgress yet, working towards a generic schema for the first stage

CREATE TABLE SYSTEMS (
  SYSTEM_ID IDENTITY NOT NULL PRIMARY KEY,
  SYSTEM_NAMEID VARCHAR(255) NOT NULL
);

CREATE TABLE OBJECT_TYPES (
  TYPE_ID IDENTITY NOT NULL PRIMARY KEY,
  TYPE_NAMEID VARCHAR(255) NOT NULL,
  SYSTEM_ID LONG NOT NULL,
  CONSTRAINT OBJECT_TYPES_FK1 FOREIGN KEY (SYSTEM_ID) REFERENCES SYSTEMS(SYSTEM_ID)
);

ALTER TABLE OBJECT_TYPES ADD CONSTRAINT OBJECT_TYPES_UQ2 UNIQUE (SYSTEM_ID,TYPE_NAMEID);

CREATE TABLE OBJECTS (
  OBJECT_ID IDENTITY NOT NULL PRIMARY KEY,
  SYSTEM_ID LONG NOT NULL,
  TYPE_ID LONG NOT NULL,
  OBJECT_NAMEID VARCHAR(4096) NOT NULL,
  CONSTRAINT OBJECTS_FK1 FOREIGN KEY  (TYPE_ID,SYSTEM_ID) REFERENCES OBJECT_TYPES(TYPE_ID, SYSTEM_ID)
);

DROP TABLE RELATION_TYPES;
CREATE TABLE RELATION_TYPES(
  SRC_SYSTEM_ID LONG NOT NULL,
  SRC_TYPE_ID LONG NOT NULL,
  RELATION_TYPE_ID IDENTITY NOT NULL PRIMARY KEY,
  TGT_SYSTEM_ID LONG NOT NULL,
  TGT_TYPE_ID LONG NOT NULL,
  RELATION_NAME VARCHAR(255) NOT NULL,
  CONSTRAINT RELATION_TYPES_UQ2 UNIQUE (SRC_TYPE_ID, TGT_TYPE_ID , RELATION_NAME),
  CONSTRAINT RELATION_TYPES_FK1 FOREIGN KEY (SRC_SYSTEM_ID,SRC_TYPE_ID) REFERENCES OBJECT_TYPES(SYSTEM_ID,TYPE_ID),
  CONSTRAINT RELATION_TYPES_FK2 FOREIGN KEY (TGT_SYSTEM_ID,TGT_TYPE_ID) REFERENCES OBJECT_TYPES(SYSTEM_ID,TYPE_ID)
);

DROP TABLE RELATION_INSTANCES;
CREATE TABLE RELATION_INSTANCES (
  SRC_SYSTEM_ID LONG NOT NULL,
  SRC_TYPE_ID LONG NOT NULL,
  SRC_OBJECT_ID LONG NOT NULL,
  RELATION_TYPE_ID LONG NOT NULL,
  TGT_SYSTEM_ID LONG NOT NULL,
  TGT_TYPE_ID LONG NOT NULL,
  TGT_OBJECT_ID LONG NOT NULL,
  RELATION_INSTANCE_ID IDENTITY NOT NULL PRIMARY KEY ,
  CONSTRAINT  RELATION_INSTANCES_FK1 FOREIGN KEY  (SRC_SYSTEM_ID, SRC_TYPE_ID, SRC_OBJECT_ID)
    REFERENCES OBJECTS (SYSTEM_ID, TYPE_ID, OBJECT_ID),
  CONSTRAINT RELATION_INSTANCES_FK2 FOREIGN KEY (TGT_SYSTEM_ID,TGT_TYPE_ID,TGT_OBJECT_ID)
    REFERENCES OBJECTS (SYSTEM_ID,TYPE_ID, OBJECT_ID),
  CONSTRAINT RELATION_INSTANCES_FK3 FOREIGN KEY  (SRC_SYSTEM_ID,SRC_TYPE_ID,RELATION_TYPE_ID,TGT_SYSTEM_ID,TGT_TYPE_ID)
    REFERENCES RELATION_TYPES (SRC_SYSTEM_ID, SRC_TYPE_ID, RELATION_TYPE_ID, TGT_SYSTEM_ID, TGT_TYPE_ID)
);

CREATE TABLE WCP0.WIKI_TEAMS
(
    TEAM_NAME VARCHAR(100) PRIMARY KEY NOT NULL,
    TEAM_CREATOR VARCHAR(100) NOT NULL,
    CONSTRAINT CONSTRAINT_77F FOREIGN KEY (TEAM_CREATOR) REFERENCES WIKI_USERS (USERNAME)
);

create SCHEMA CONST;
set ALLOW_LITERALS ALL;

CREATE CONSTANT Const.SelfSystem VALUE 'self'
;

INSERT INTO SYSTEMS(SYSTEM_ID, SYSTEM_NAMEID) values (-1, 'self');
INSERT INTO OBJECT_TYPES(TYPE_ID, TYPE_NAMEID, SYSTEM_ID) values (-2,'system',-1);
INSERT INTO OBJECT_TYPES(TYPE_ID, TYPE_NAMEID, SYSTEM_ID) values (-3,'type',-1);
INSERT INTO OBJECTs(SYSTEM_ID, TYPE_ID, OBJECT_NAMEID)  values (-1,-2,'self');
INSERT INTO OBJECTs(SYSTEM_ID, TYPE_ID, OBJECT_NAMEID)  values (-1,-3,'type');
iNSERT INTO RELATION_TYPES(SRC_SYSTEM_ID, SRC_TYPE_ID, TGT_SYSTEM_ID, TGT_TYPE_ID, RELATION_NAME)
      values (-1, -3, -1, -3 , 'is type of');

INSERT INTO RELATION_TYPES(SRC_SYSTEM_ID, SRC_TYPE_ID, TGT_SYSTEM_ID, TGT_TYPE_ID, RELATION_NAME)
    values (-1, -3, -1, -2 , 'is system of');
