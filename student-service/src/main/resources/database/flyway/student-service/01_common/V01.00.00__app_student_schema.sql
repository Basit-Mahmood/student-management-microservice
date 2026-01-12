create sequence if not exists student_seq
start with 1
increment by 1
no cache;

create table student (
    student_id bigint default (next VALUE for student_seq) primary key,
    name varchar(64) not null,
    email varchar(32) not null,
    grade varchar(16) not null,
    mobile_number varchar(36) unique, -- Mobile is a better unique constraint than name
    school_name varchar(64) not null
);
