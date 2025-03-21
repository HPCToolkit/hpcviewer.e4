<!--
SPDX-FileCopyrightText: Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

<a name="sec:trace" />

## Trace view

Trace view [N. R. Tallent et al. 2011](http://doi.acm.org/10.1145/1995896.1995908) is a time-centric user interface for interactive examination of a sample-based time series (hereafter referred to as a trace) view of a program execution.
Trace view can interactively present a large-scale execution trace without concern for the scale of parallelism it represents.

To collect a trace for a program execution, one must instruct HPCToolkit's measurement system to collect a trace.
When launching a dynamically-linked executable with `hpcrun`, add the `-t ` flag to enable tracing.
When launching a statically-linked executable, set the environment variable `HPCRUN_TRACE=1` to enable tracing.
When collecting a trace, one must also specify a metric to measure. The best way to collect a useful trace is to asynchronously sample the execution with a time-based metric such as `REALTIME`, `CYCLES`, or `CPUTIME`.

<a name="fig:hpctraceviewer-callpath" />

| Trace dimensions |
| :--------------: |
|<img alt="Trace View" src="images/hpctraceviewer-callpath.png" width="60%" />|
| The logical view of traces of call-path samples on three dimensions: **time**, **execution context** (rank/thread/GPU), and **call stack** (also known as call-path).|

As shown in the [Trace dimensions](#fig:hpctraceviewer-callpath) figure above, call-path traces consist of data in three dimensions: *execution context* (also called *profile*) representing a process or a thread rank or a GPU stream, *time*, and *call stack*.
A *crosshair* in Trace view is defined by a triplet `(p,t,d)` where `p` is the selected process/thread rank, `t` is the selected time, and `d` is the selected call stack depth.

Trace view renders a view of processes and threads over time. The [*Depth View*](#sec:depthview) shows the call stack depth over time for the thread selected by the cursor.
Trace view's [*Call-stack View*](#sec:callview) shows the call stack associated with the thread and time pair specified by the cursor.
Each of these views plays a role in understanding an application's performance.

In the Trace view, each procedure is assigned a specific color. The [Trace dimensions](#fig:hpctraceviewer-callpath) figure above shows that at depth 1, each call stack has the same color: blue. This node represents the main program that serves as the root of the call chain in all processes at all times. At depth 2, all processes have a green node, which indicates another procedure.
At depth 3, in the first time step, all processes have a yellow node; in subsequent time steps, they have purple nodes.
This might indicate that the processes are first observed in an initialization procedure (represented by yellow) and later observed in a solve procedure (represented by purple). The pattern of colors that appears in a particular depth slice of the Main View enables a user to visually identify inefficiencies such as load imbalance and serialization.

<a name="fig:hpctraceviewer-legend" />

| Trace view with legends |
| :---------------------: |
|<img alt="A screenshot of Trace view" src="images/traceview-legend.png" width="80%"/>|
| A snapshot of traces of an MPI+OpenMP program. The main view shows the *Rank* or *Execution context* as the Y-axis and the program execution *time* as the X-axis.|

The above [Trace view](#fig:hpctraceviewer-legend) figure highlights Trace view's four principal window panes: [Main View](#sec:mainview), [Depth View](#sec:depthview), [Call Stack View](#sec:callview), and [Mini Map View](#sec:miniview),
while the [Trace view](#fig:hpctraceviewer-stat) figure below shows two additional window panes: [Summary View](#sec:summaryview) and Statistics View:

<a name="fig:hpctraceviewer-stat" />

| Trace view with the Summary View and Statistics View |
| :--------------------------------------------------: |
|<img alt="Summary View and Statistics View" src="images/traceview-stat.png" width="80%" />
| A screenshot of `hpcviewer`'s Trace view showing the Summary View (tab in bottom, left pane) and Statistics View (tab in top, right pane) |


- **Main View** (top tab, left pane):
  This is the Trace view's primary view.
  This view shows time on the horizontal axis and the execution context (rank, thread, GPU stream) on the vertical axis; time moves from left to right.
  Compared to typical process/time views, there is one key difference.
  The view is a user-controllable slice of the execution-context/time/call-stack space to show the call stack hierarchy (see the [Trace dimensions](#fig:hpctraceviewer-callpath) figure).
  Given a call-stack depth, the view shows the color of the currently active procedure at a given time and process rank.
  (If the requested depth is deeper than a particular call stack, then Trace view simply displays the deepest procedure frame and, space permitting, overlays an annotation indicating the fact that this frame represents a shallower depth.)
  <br/>
  Trace View assigns colors to procedures based on (static) source code procedures.
  Thus, the same color within the Main and Depth views refers to the same procedure.
  <br/>
  The Main view has a white crosshair representing a selected time and process space.
  For this selected point, the [Call Stack View](#sec:callview) shows the corresponding call stack, while the Depth View shows the selected process.

<a name="sec:depthview" />

- **Depth View** (bottom tab, left pane):
  The view presents a \<call-path, time> dimension for the current execution context selected by the Main view's crosshair.
  It shows for each virtual time along the horizontal axis a stylized call stack along the vertical axis, where 'main' is at the top and leaves (samples) are at the bottom.
  In other words, this view shows for the whole time range, in a qualitative fashion, what the Call Path View shows for a selected point.
  The horizontal time axis aligns exactly with the Trace View's time axis; and the colors are consistent across both views.
  This view has a crosshair corresponding to the currently selected time and call stack depth.
  One can specify a new crosshair time and a new time range:
    - Selecting a new crosshair time `t` can be done by clicking a pixel within Depth View. This will update the crosshair in Main View and the call path in Call Stack View.
    - Selecting a new time range \[`t_m`,`t_n`\] = {`t` | `t_m` \<= `t` \<= `t_n`} is performed by first clicking the position of `t_m` and dragging the cursor to the position of `t_n`. A new content in Depth View and Main View is then updated. Note that this action will not update the call path in Call Stack View since it does not change the position of the crosshair.

<a name="sec:summaryview" />

- **Summary View** (bottom tab, left pane):
  The view shows the proportion of each subroutine within the current time range.
  Similar to the Depth view, the Summary view's time range reflects the Trace view's time range..

<a name="sec:callview" />

- **Call Stack View** (top tab, right pane):
  This view shows two things: 
    - the current call stack depth that defines the hierarchical slice shown in the Trace view, and
    - the actual call stack for the point selected by the Trace view's crosshair.
  To easily coordinate the call stack depth value with the call path, the Call Stack View currently suppresses details such as loop structure and call sites; we may use indentation or other techniques to display this in the future.
  In this view, the user can select the depth dimension of the Main view by either typing the depth in the depth editor or selecting a procedure in the Call stack view.

- **Statistics View** (top tab, right pane, not shown):
  This view shows the list of procedures active in the space-time region shown in the Main view at the current call stack depth. Each procedure's percentage in the Statistics view indicates the percentage of pixels in the Main view pane filled with this procedure's color at the current Call stack depth. When the Main view is navigated to show a new time-space interval or the call-stack's depth is changed, the statistics view will update its list of procedures and the percentage of execution time to reflect the new space-time interval or depth selection.

- **GPU Idleness Blame View** (top tab, right pane, not shown):
  The view is only available if the database contains information on GPU traces. It shows the list of procedures that cause GPU idleness displayed in the trace view.
  If the trace view displays one CPU thread and multiple GPU streams, then the CPU thread will be blamed for the idleness of those GPU streams.
  If the view contains more than one CPU thread and multiple GPU streams, then the cost of idleness is shared among the CPU threads.

<a name="sec:miniview" />

- **Mini Map View** (bottom tab, right pane):
  The Mini Map shows, relative to the process/time dimensions, the portion of the execution shown by the Trace View.
  The Mini Map enables one to zoom and to move from one close-up to another quickly.
  The user can also move the current selected region to another region by clicking the white rectangle and dragging it to the new place.

<a name="sec:mainview" />

### Action and Information Pane

Main View is divided into two parts: the top part, which contains *action* and *information* panes, and the main canvas, which displays the traces.

The buttons in the action pane are the following:

- **Home** ![image](images/hpctraceviewer-button-home-screen.png) : Resetting the view configuration into the original view, i.e., viewing traces for all times and processes.

- **Horizontal zoom in ![image](images/hpctraceviewer-button-zoom-in-time.png) / out** ![image](images/hpctraceviewer-button-zoom-out-time.png) : Zooming in/out the time dimension of the traces.

- **Vertical zoom in ![image](images/hpctraceviewer-button-zoom-in-process.png) / out ![image](images/hpctraceviewer-button-zoom-out-process.png)** : Zooming in/out the process dimension of the traces.

- **Navigation buttons** ![image](images/hpctraceviewer-button-go-east.png), ![image](images/hpctraceviewer-button-go-west.png), ![image](images/hpctraceviewer-button-go-north.png), ![image](images/hpctraceviewer-button-go-south.png) : Navigating the trace view to the left, right, up and bottom, respectively. It is also possible to navigate with the arrow keys in the keyboard. Since Main View does not support scroll bars, the only way to navigate is through navigation buttons (or arrow keys).

- **Undo** ![image](images/hpctraceviewer-button-undo.png) : Canceling the action of zoom or navigation and returning back to the previous view configuration.

- **Redo** ![image](images/hpctraceviewer-button-redo.png) : Redoing of previously undo change of view configuration.

- **Save** ![image](images/hpctraceviewer-button-save.png) / **Open ![image](images/hpctraceviewer-button-open.png) a view configuration** : Saving/loading a saved view configuration.
  A view configuration file contains information about the process/thread and time ranges shown, the selected depth, and the position of the crosshair.
  It is recommended that the view configuration file be stored in the same directory as the database to ensure that it matches the database since a configuration does not store its associated database. Although it is possible to open a view configuration file associated with a different database, it is not recommended since each database has different time/process dimensions and depth.

At the top of an execution's Main View pane is information about the data shown in the pane.

- **Time Range**. It shows the time interval of the Main view along the horizontal dimension.
- **Cross Hair**. It indicates the current cursor position in the time and execution-context dimensions.


<a name="sec:color-map" />

### Customizing the Color Map

Trace view allows users to customize the color of a specific procedure or a group of procedures.  
To do that, one can select the `View - Color map` menu, and `Color Map` window will appear as shown below:

<a name="fig:colormap" />

| Color map window |
| :----------------------------: |
| ![Color map](images/hpctraceviewer-dialog-mapping.png) |
| This snapshot shows that any procedure names that match with "`MPI*`" pattern are assigned with red, while procedures that match with "`PMPI*`" pattern are assigned with black. |


To add a new procedure-color map, click the `Add` button and a [Color map](#fig:colormap) window will appear.
In this window, one can specify the procedure's name or a *glob* pattern of procedure, and then specify the color to be associated by clicking the color button.
Clicking `OK` will close the window and add the new color map to the global list. 
Note that this map is persistent across sessions, and will apply to other databases as well.


### Filtering Execution Contexts

One can select which execution contexts (ranks, threads or GPU streams) to be displayed in Trace View, by selecting the `Filter - execution contexts` menu.
This will display a filter window that allows to select which execution contexts to show/hide:

| Filter execution context window |
| :----------------------------: |
| ![](images/hpctraceviewer-dialog-filter.png) |
| An example of narrowing the list of execution contexts using both a regular expression and the minimum number of samples criteria |

Similar to [Profile View's thread selection](profile.html#sec:thread-level-table), one can narrow the list by specifying the name of the execution context on the filter part of the window. 
In addition, one can also narrow the list based on the minimum number of trace samples (the third column of the table), as shown by the above figure.

### Tips
- Sometimes, it is helpful to associate a group of procedures (such as `MPI_*`) to a specific color to approximate its statistic percentage.
- Trace view also provides a context menu by right-clicking on the view to save the current display. 
  This context menu is also available on the Depth view and the Summary view.

