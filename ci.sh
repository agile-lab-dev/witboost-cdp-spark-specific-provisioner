#!/bin/bash
set -e

sbt clean \
  scalafmtSbtCheck scalafmtCheckAll \
  compile \
  coverage test coverageAggregate \
  -Dmockserver.logLevel=OFF -Droot.logLevel=OFF

