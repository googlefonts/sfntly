sfntly is dependent on the following components:

1. Google C++ Testing Framework.

Please download from http://code.google.com/p/googletest/, extract it into
this folder, and rename/symbolic link to gtest.

Tested with gTest 1.6.0.

2. ICU

For Linux/Mac, default ICU headers in system will be used.  Linux users
please make sure you have dev packages for ICU.  For example, you can

sudo apt-get install libicu-dev

in Ubuntu and see if the required library is installed.

For Windows, download from http://site.icu-project.org/download. Tested with
ICU 4.6.1 binary release. You can also provide your own ICU package. However,
you need to alter the include path, library path, and provide icudt.dll.

We have included gtest and icu in ext/redist.

