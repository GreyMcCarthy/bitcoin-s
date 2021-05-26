package org.bitcoins.bundle.gui

import org.bitcoins.gui._
import org.bitcoins.server.BitcoinSAppConfig
import scalafx.geometry._
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.text._

import scala.concurrent.ExecutionContext.global

class LandingPane(glassPane: VBox) {

  val appConfig: BitcoinSAppConfig =
    BitcoinSAppConfig.fromDefaultDatadir()(global)

  val model = new LandingPaneModel()

  private val label: Label = new Label("Welcome to Bitcoin-S") {
    alignmentInParent = Pos.BottomCenter
    textAlignment = TextAlignment.Center
    font = new Font(30)
  }

  val bitcoindConfigPane = new BitcoindConfigPane(appConfig, model)

  val bitcoindTab: Tab = new Tab() {
    text = "Bitcoin RPC Config"
    content = bitcoindConfigPane.view
  }

  val neutrinoConfigPane = new NeutrinoConfigPane(appConfig, model)

  val neutrinoTab: Tab = new Tab() {
    text = "Neutrino Config"
    content = neutrinoConfigPane.view
  }

  val tabPane: TabPane = new TabPane() {
    tabs = Vector(bitcoindTab, neutrinoTab)
    tabClosingPolicy = TabClosingPolicy.Unavailable
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = tabPane
  }

  view.autosize()

  val taskRunner: TaskRunner = {
    val runner = new TaskRunner(view, glassPane)
    model.taskRunner = runner
    runner
  }
}
