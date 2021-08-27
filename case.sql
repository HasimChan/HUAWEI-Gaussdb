create table student
(
    id       int,
    name     text,
    age      int,
    password text
);



insert into student values (10, 'h10', 23, 'aa c(55gg');
insert into student values (10, 'h5', 23, 'kkv v)hhjj');
insert into student values (9, 'h9', 23,',k,k,v,hhjj,,,');

create index t1idx on student (id);

select *
from student;


