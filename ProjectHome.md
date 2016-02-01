![http://taskblocks.googlecode.com/svn/wiki/taskblocks1.png](http://taskblocks.googlecode.com/svn/wiki/taskblocks1.png)

Task Blocks is an application that can help you to schedule projects consisted of many tasks in case every task is solved just by one person and people doesn't work on more tasks at the same time.

If you are interested, see [screenshots](screenshots.md) or go to the [download](http://code.google.com/p/taskblocks/downloads/list) section and try it. Simple [readme](readme.md) containing installation instructions is also available.

## About ##

For purposes of our project I needed simple utility to schedule tasks. There are available some scheduling applications (Gantt Project, MS Project, Gnome Planner). Actually they are so much general, that some simple things are almost impossible with them. For example in Gantt Project wasn’t possible to specify that one man can work only on one task at the same time. I consider desirable when an application checks such a constraints and adjusts tasks automatically.

So I decided to write such an application. I wanted to make an application, in which the manipulation with tasks is as easy as manipulating pieces of puzzle on the table desk. Finally, after successfully using it, I decided to share it with other people and to provide it with open source code. I hope you will enjoy using it.

## Main Features ##
The features I like:

  * Tasks of one man never coincide. They are automatically re-planned. Of course, the dependency is taken into account too.
  * Free task manipulation. You can move tasks within the tasks-area and “resize” them with Drag&Drop. Holding Shift-key you define new dependency constraint.
  * The "shrink" button adjusts the starting times of tasks so there remain no empty spaces between them (if possible). The tasks order is preserved.
  * Simple task creation - you can choose “Don’t close dialog” when adding new task, which allows you to add more tasks very quickly.
  * Export To Bugzilla. Can submit tasks as bugs in bugzilla, filling in "Status Whiteboard" and "Estimated time".

## Changes ##
  * **Version 0.8**
    * Undo/Redo support

  * **Version 0.7**
    * Support for editting a worker details
    * Workload can be specified for a worker. _Then a real duration of tasks is modified according to it. For example having workload 80%, a task with 4 day effort will take 5 days in reality._

  * **Version 0.6**
    * Bugzilla export dialog supports also UPDATE (if the task has Bug ID already assigned).
    * Minor bug-fixes: While shrinking, tasks are never moved before "now". Workers without tasks were not saved.

  * **Version 0.5**
    * Minor usability changes.

  * **Version 0.4**
    * Initial public release.

[Jakub Neubauer](mailto:jakub.neubauer@gmail.com), author