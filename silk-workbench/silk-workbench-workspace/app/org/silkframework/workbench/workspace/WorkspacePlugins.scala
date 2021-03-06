package org.silkframework.workbench.workspace

import org.silkframework.runtime.plugin.PluginModule
import org.silkframework.workspace.reports.InMemoryExecutionReportManager

class WorkspacePlugins extends PluginModule {
  override def pluginClasses: Seq[Class[_]] =
    Seq(
      classOf[WorkbenchPluginDataset],
      classOf[WorkbenchPluginCustomTask],
      classOf[FileExecutionReportManager])
}
