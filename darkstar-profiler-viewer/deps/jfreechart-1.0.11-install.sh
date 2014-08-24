#!/bin/bash

mvn install:install-file \
  -Dfile=jfreechart-1.0.11/lib/jfreechart-1.0.11.jar \
  -DgroupId=jfree \
  -DartifactId=jfreechart \
  -Dversion=1.0.11 \
  -Dpackaging=jar \
  -DgeneratePom=true

 mvn install:install-file \
  -Dfile=jfreechart-1.0.11/lib/jcommon-1.0.14.jar \
  -DgroupId=jfree \
  -DartifactId=jcommon \
  -Dversion=1.0.14 \
  -Dpackaging=jar \
  -DgeneratePom=true
  