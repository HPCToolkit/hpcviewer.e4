// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui;

public class BuildInfo {
    /**
     * Version of the application.
     */
    public static String VERSION = "${project.version}";

    /**
     * HTML fragment describing the license this project is under (proper noun).
     */
    public static String LICENSE_HTML = "the <a href=\"${license.url}\">${license.name}</a>";

    /**
     * HTML fragment describing the organization this project is part of (proper noun).
     */
    public static String ORGANIZATION_HTML = "the <a href=\"${project.organization.url}\">${project.organization.name}</a>, a project of the <a href=\"https://hpsf.io\">High Performance Software Foundation</a>";
}
