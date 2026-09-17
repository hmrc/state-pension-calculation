#!/bin/bash
sbt clean scalafmt Test/scalafmt IntegrationTest/scalafmt coverage test IntegrationTest/test coverageReport