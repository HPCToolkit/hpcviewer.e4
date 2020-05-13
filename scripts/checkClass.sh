#!/bin/bash

# print the number of class version
# usage: checkClass <java_class>

echo -n "$1 : "
javap -verbose $1 | grep major

#od --format=d1 $1 -j 7 -N 1
