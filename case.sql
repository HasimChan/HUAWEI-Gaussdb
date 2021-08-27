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

select  id, name  from  student  where   student.id = 2  or  id != 2  and  id != 18;

select  id, name  from  student  where   student.name != ' !=where I go to'  or  id != 2  and  id != 18;

select  id, name  from  student  where   student.name = ' from I go to'  or  id != 2  and  id != 18;

select  id, name  from  student  where   student.name = ' or I go to'  or  id != 2  and  id != 18;

select  id, name  from  student  where   student.name = ' and I go to'  or  id != 2  and  id != 18;
