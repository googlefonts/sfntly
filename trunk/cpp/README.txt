Documentations
==============
Please refer to Java version of this library for APIs and design documents.


Build Environment Requirements
==============================
* cmake 2.6 or above
* C++ compiler requirement
  ** Windows: Visual C++ 2008, Visual C++ 2010
  ** Linux: g++ 4.3 or above.  g++ must support built-in atomic ops and has
     companion libstd++.
  ** Mac: Apple XCode 3.2.5 or above


Before You Build
================
sfntly is dependent on several external packages.  Please follow the
instructions in ext/README-sfntly.txt to download and setup dependent packages.


Building on Windows
===================
1. Open sfntly.vc10.sln (if using Visual C++ 2010) or sfntly.vc9.sln (if using
   Visual C++ 2008).  Since sfntly use STL extensively, please patch your
   Visual Studio for any STL-related hotfixes/service packs.
2. Build the solution.


Building on Linux/Mac
=====================
1. Run "cmake ." at the sfntly folder
2. make


Porting Guidelines
==================
1. Follow Google C++ Style Guide (
   http://google-styleguide.googlecode.com/svn/trunk/cppguide.xml) with
   following exceptions:
   a. Exception handling is used. This is unavoidable when we port Java
      programs.
   b. Nested class are allowed.  We try to keep the same class structure for
      better maintainability between C++ and Java branches.
   c. Functions/members are not grouped in the order of
      public/protected/private.  Instead, they follow the order appearing in
      Java code (except when the order affects compilation).

2. File structure mapping:
   a. src/sfntly maps to java/com/google/typography/fonts/sfntly and all
      include files stay with the source.  Keep its folder structures intact.
   b. All C++ port specific files are placed under src/port.
   c. All external dependencies are placed under ext/.
   d. All data files are placed under data/.
   e. Tests and samples does not map to their Java counterpart since we have
      different set of tests and samples.  Tests are placed under src/test/
      and samples are placed in src/sample/.
   f. Output files are in bin/ and lib/ folder.
   g. We have a vc/ folder to store all vcprojects, and two solution files in
      root. The goal is to perform everything through cmake and sunset these
      files.

3. Keep existing class structures, except:
   a. Namespaces are flatten to sfntly to simplify usage.
   b. Public enums are promoted to sfntly namespace within a struct.
   c. When we need to get around compiler bugs.

4. Keep existing naming. Variable names are converted to conform C++ style guide
   but should be easily associated with their Java counterpart.

5. For container data types, they are not returned as object references.
   Instead, use an additional function parameter to pass it in.  For example:

   byte[] getBuffer();  // Java version, callee new the byte array.
   void getBuffer(ByteVector* buffer);  // C++ version, caller allocate buffer.

6. C++ port uses reference-counted object. Please refer to src/port/refcount.h
   for usage.

7. Existing comments in Java code will be copied and maintained in a
   best-effort manner.


Useful Tips for Debugging Ref-Counting Issue
============================================

1. Define ENABLE_OBJECT_COUNTER and REF_COUNT_DEBUGGING.  All ref-count related
   activity will be ouput to stderr.

2. The logs will look like (under VC 2010)

   A class RefCounted<class sfntly::Table::Header> const *:oc=2,oid=2,rc=3

   Use your favorite editor to transform them into SQL statements, e.g.

   regex pattern:
   ^([ACDR]) class RefCounted<class sfntly::([A-Za-z0-9:]+)>[ *const]+:oc=([-0-9]+),oid=([0-9]+),rc=([-0-9]+)

   replace to:

   insert into log values('\1', '\2', \3, \4, \5);

3. Add one line to the beginning of log:

   create table log(action char(1), class_name char(30), oc integer, oid integer, rc integer);

4. Run sqlite shell, use .read to input the SQL file.
5. Run following commands to get the leaking object class and object id:

   create table todrop(class_name char(30), oid integer);
   insert into todrop select class_name, oid from log where action='D';
   create table tocreate (class_name char(30), oid integer);
   insert into tocreate select class_name, oid from log where action='C';
   select * from tocreate except select * from todrop;

6. Once you know which object is leaking, it's much easier to setup conditional
   breakpoints to identify the real culprit.

