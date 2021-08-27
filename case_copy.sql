create table   student (
                           id  int,
                           name  text,
                           age int,
                           password text
)    ;

create  index   t1idx on  student  ( id  );

insert  into student values( 1, 'hasim', 22, '9766554' )    ;
insert  into student values( 2, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 3, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 5, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 6, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 7, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 8, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 9, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 10, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 11, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 12, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 13, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 14, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 15, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 16, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 17, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 18, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 19, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 20, 'zero', 23, 'aacc55gg' )    ;
insert  into student values( 21, 'hs' , 23, 'kkvvhhjj' )    ;
insert  into student values( 22, 'zero', 23, 'aacc55gg' )    ;

select *
from student;

select  id, age  from  student  where   student.id=2  or  name='zero'  and  age=23;

select  id, age  from  student  where   student.id=2  or  id=22  or  id=18;

insert  into student values( 1, 'h1', 22, '9766 554' )    ;
insert  into student values( 2, 'h2' , 23, 'kkv vhhjj' )    ;
insert  into student values( 3, 'h3', 23, 'aacc55gg' )    ;
insert  into student values( 4, 'h4', 23, 'aacc55gg' )    ;
insert  into student values( 5, 'h5' , 23, 'kkvvhhjj' )    ;
insert  into student values( 6, 'h6', 23, 'aacc55gg' )    ;
insert  into student values( 7, 'h7' , 23, 'kkvvhhjj' )    ;
insert  into student values( 8, 'h8', 23, 'aacc55gg' )    ;
insert  into student values( 9, 'h9' , 23, 'kkvvhhjj' )    ;
insert  into student values( 10, 'h10', 23, 'aacc55gg' )    ;
insert  into student values( 11, 'h11' , 23, 'kkvvhhjj' )    ;
insert  into student values( 12, 'h12', 23, 'aacc55gg' )    ;
insert  into student values( 13, 'h13' , 23, 'kkvvhhjj' )    ;
insert  into student values( 14, 'h14', 23, 'aacc55gg' )    ;
insert  into student values( 15, 'h15' , 23, 'kkvvhhjj' )    ;
insert  into student values( 16, 'h16', 23, 'aacc55gg' )    ;
insert  into student values( 17, 'h17' , 23, 'kkvvhhjj' )    ;
insert  into student values( 18, 'h18', 23, 'aacc55gg' )    ;
insert  into student values( 19, 'h19' , 23, 'kkvvhhjj' )    ;
insert  into student values( 20, 'h20', 23, 'aacc55gg' )    ;

select *
from student;

select  id, name  from  student  where   student.id = 2  or  id != 2  and  id != 18;

select  id, age, name, password from  student  where   2 = student.id  or  'h3' = name   or  id = 18 or student.name  = 'h6' or student.password = 'kkvvhhjj';


select *
from student;

select id, age from student where student.id = 2 or name = 'zero' and age = 23;
