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
import javafx.scene.control.SeparatorMenuItem;
import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.plugin.FxPlugin;
import jgnash.uifx.views.main.MainView;
import lbjgnash.ui.ReportDefinition;
import lbjgnash.ui.ReportManagerView;
import lbjgnash.ui.ReportView;

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
    Menu openReportsMenu;

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
        
        
        Menu reportMenu = new Menu(ResourceSource.getString("ReportMenuItem.Title"));
        
        MenuItem netWorthMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.NetWorth"));
        netWorthMenuItem.setOnAction((event) -> {
            onShowReportStyle(ReportDefinition.Style.NET_WORTH);
        });
        reportMenu.getItems().add(netWorthMenuItem);
        
        MenuItem incomeExpenseMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.IncomeExpense"));
        incomeExpenseMenuItem.setOnAction((event) -> {
            onShowReportStyle(ReportDefinition.Style.INCOME_EXPENSE);
        });
        reportMenu.getItems().add(incomeExpenseMenuItem);
        
        MenuItem portfolioMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.Portfolio"));
        portfolioMenuItem.setOnAction((event) -> {
            onShowReportStyle(ReportDefinition.Style.PORTFOLIO);
        });
        reportMenu.getItems().add(portfolioMenuItem);
        
        MenuItem customMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.Custom"));
        customMenuItem.setOnAction((event) -> {
            onShowReportStyle(ReportDefinition.Style.CUSTOM);
        });
        reportMenu.getItems().add(customMenuItem);

        reportMenu.getItems().add(new SeparatorMenuItem());
        
        MenuItem reportManagerMenuItem = new MenuItem(ResourceSource.getString("ReportMenuItem.ReportManager"));
        reportManagerMenuItem.setOnAction((event) -> {
            onReportManager();
        });
        reportMenu.getItems().add(reportManagerMenuItem);
        
        reportMenu.getItems().add(new SeparatorMenuItem());
        openReportsMenu = new Menu(ResourceSource.getString("ReportMenuItem.OpenReports"));
        reportMenu.getItems().add(openReportsMenu);
        
        engineMenuItems.add(reportMenu);
        lbMenu.getItems().add(reportMenu);
    }
    
    private void updateMenu() {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        boolean disableMenuItems = (engine == null);
        engineMenuItems.forEach((menuItem) -> {
            menuItem.setDisable(disableMenuItems);
        });
        
        if (engine != null) {
            openReportsMenu.getItems().clear();
            List<ReportView.ReportViewEntry> entries = ReportView.getOpenReports();
            if (entries.isEmpty()) {
                openReportsMenu.setDisable(true);
            }
            else {
                openReportsMenu.setDisable(false);
                entries.forEach((entry) -> {
                    MenuItem menuItem = new MenuItem(entry.getReportLabel());
                    menuItem.setOnAction((event) -> {
                        entry.getReportView().requestFocus();
                    });
                    openReportsMenu.getItems().add(menuItem);
                });
            }
        }
    }
    
    private void onSummary() {
    }
    
    
    private void onShowReportStyle(ReportDefinition.Style style) {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        if (engine != null) {
            ReportView.openReportView(style, engine, MainView.getPrimaryStage());
        }
    }
    
    private void onReportManager() {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        if (engine != null) {
            ReportManagerView.showAndWait(engine, MainView.getPrimaryStage());
        }
    }
}
