<!--
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

= Unit and integration tests for hpcviewer

Most of the tests are performed automatically when executing `test` goal.
Due to Tycho issue, all automatic-test plugins have to have `pom.xml` with
`eclipse-test-plugin` as the packaging:

```xml
<packaging>eclipse-test-plugin</packaging>
```

The plugins for automatic tests:

- edu.rice.cs.hpcfilter.test: unit test for hpcfilter module
- edu.rice.cs.hpclocal.test: unit tests for local database
- edu.rice.cs.hpcmetric.test: unit tests for hpcmetric module
- edu.rice.cs.hpcremote.test: unit tests for hpcremote database
- edu.rice.cs.hpctraceviewer.ui.test: unit tests for hpctraceviewer.ui module
- edu.rice.cs.hpctree.test: unit tests for hpctree module
- edu.rice.cs.hpctest.report: jacoco report (not a test)
- edu.rice.cs.hpctest.util: utility plugin used by other unit tests projects
- resources: databases used for the tests

Tests that are required to be performed manually:

- edu.rice.cs.hpctest: independent tests, mainly to display windows. You need to run the test manually.
