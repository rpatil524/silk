package controllers.workspace.workspaceRequests

import config.WorkbenchLinks
import controllers.workspaceApi.search.ItemType
import org.silkframework.config.TaskSpec
import org.silkframework.workspace.ProjectTask
import play.api.libs.json.{Json, OFormat}

/**
  * Response for copy task(s) endpoints.
  *
  * @param copiedTasks Tasks that are copied to the target project
  * @param overwrittenTasks Tasks that would overwrite existing tasks in the target project
  */
case class CopyTasksResponse(copiedTasks: Set[TaskToBeCopied], overwrittenTasks: Set[TaskToBeCopied])

object CopyTasksResponse {
  implicit val jsonFormat: OFormat[CopyTasksResponse] = Json.format[CopyTasksResponse]
}

/**
  * A task that is to be copied to another project.
  *
  * @param taskType The task type label
  * @param id The task id
  * @param label Task label
  * @param originalTaskLink Browser link to the original task
  * @param overwrittenTaskLink Browser link to the overwritten task, if any
  */
case class TaskToBeCopied(taskType: String, id: String, label: String, originalTaskLink: String, overwrittenTaskLink: Option[String])

object TaskToBeCopied {

  implicit val jsonFormat: OFormat[TaskToBeCopied] = Json.format[TaskToBeCopied]

  def fromTask(task: ProjectTask[_ <: TaskSpec], overwrittenTask: Option[ProjectTask[_ <: TaskSpec]]): TaskToBeCopied = {
    TaskToBeCopied(
      taskType = ItemType.itemType(task).label,
      id = task.id,
      label = task.taskLabel(),
      originalTaskLink = WorkbenchLinks.editorLink(task),
      overwrittenTaskLink = overwrittenTask.map(WorkbenchLinks.editorLink)
    )
  }

}

