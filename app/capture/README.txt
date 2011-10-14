1) RXTX

This program uses RXTX: a free (LGPL), compatible implementation of the JavaComm
APIs.  A binary copy is included with this source code for your convenience.
Maven has been configured to find the included jar.  The application should
build successfully with no modifications.  However, it will not run without user
intervention.  The proper native libraries will need to be installed first.

To run the application, please find the proper native libraries for your
platform in the rxtx directory.  These will end in .dll, .so, or .jnilib.
Copy these files to your current working directory before running the
application.  For Eclipse users, copying these to the project root directory
(the directory with pom.xml) is sufficient.

Alternatively, follow the instructions from the INSTALL file in the rxtx
directory.
