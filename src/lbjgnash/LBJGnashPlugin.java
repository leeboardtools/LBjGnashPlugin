/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lbjgnash;

import com.leeboardtools.util.ResourceSource;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.plugin.FxPlugin;
import jgnash.uifx.views.main.MainView;
import lbjgnash.ui.ReportDefinition;
import lbjgnash.ui.ReportDesignerWindow;
import lbjgnash.ui.ReportSetupView;

/**
 *
 * @author albert
 */
public class LBJGnashPlugin implements FxPlugin {

    @Override
    public String getName() {
        return "LBJGnashPlugin";
    }

    @Override
    public void start(PluginPlatform pluginPlatform) {
        if (pluginPlatform == PluginPlatform.Fx) {
            installFxMenu();
        }
    }
    
    List<MenuItem> engineMenuItems = new ArrayList<>();

    private void installFxMenu() {
        final MenuBar menuBar = MainView.getInstance().getMenuBar();

        Menu lbMenu = new Menu(ResourceSource.getString("LBMenu.Title"));
        menuBar.getMenus().add(lbMenu);
        lbMenu.setOnShowing((event) -> {
            updateMenu();
        });
        
        MenuItem summaryMenuItem = new MenuItem(ResourceSource.getString("SummaryMenuItem.Title"));
        summaryMenuItem.setOnAction((event) -> { 
            onSummary();
        });
        engineMenuItems.add(summaryMenuItem);
        lbMenu.getItems().add(summaryMenuItem);
        
        MenuItem reportMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.Title"));
        reportMenuItem.setOnAction((event) -> {
            onReportDesigner();
        });
        engineMenuItems.add(reportMenuItem);
        lbMenu.getItems().add(reportMenuItem);
    }
    
    private void updateMenu() {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        boolean disableMenuItems = (engine == null);
        engineMenuItems.forEach((menuItem) -> {
            menuItem.setDisable(disableMenuItems);
        });
    }
    
    private void onSummary() {
    }
    
    
    private ReportDefinition reportDefinition = ReportDefinition.standardNetWorthDefintion();

    private void onReportDesigner() {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        if (engine != null) {
            //ReportDesignerWindow.showReportDesignerWindow(engine, null);
            //ReportDefinition definition = ReportDefinition.standardNetWorthDefintion();
            ReportSetupView.showAndWait(reportDefinition, engine, MainView.getPrimaryStage());
        }
    }
}
