/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import sbt.testing._

final class JUnitRunner(
    val args: Array[String],
    val remoteArgs: Array[String],
    runSettings: RunSettings,
    customRunners: CustomRunners
) extends Runner {

  def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(new JUnitTask(_, runSettings))

  def done(): String = ""

  def serializeTask(task: Task, serializer: TaskDef => String): String =
    serializer(task.taskDef)

  def deserializeTask(task: String, deserializer: String => TaskDef): Task =
    new JUnitTask(deserializer(task), runSettings)

  def receiveMessage(msg: String): Option[String] = None
}
