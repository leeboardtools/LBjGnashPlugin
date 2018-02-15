/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lbjgnash.ui;

import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jgnash.engine.Engine;

/**
 *
 * @author Albert Santos
 */
public class ReportDesignerWindow {
    final Engine engine;
    Stage stage;
    
    // TEST!!!
    AccountFilterViewController controller;
    static ReportDesignerWindow singleton;
    
    ReportDesignerWindow(Engine engine, Stage stage) {
        this.engine = engine;
        this.stage = stage;
    }
    
    
    public static void showReportDesignerWindow(Engine engine, Stage stage) {
        if (singleton == null) {
            singleton = new ReportDesignerWindow(engine, stage);
        }
        
        singleton.showWindow();
    }
    
    protected void showWindow() {
        if (this.controller == null) {
            setupWindow();
        }
        
        if (this.stage != null) {
            this.stage.show();
            this.stage.toFront();
            this.stage.requestFocus();
        }
    }
    
    protected void setupWindow() {
        try {
            if (this.stage == null) {
                this.stage = new Stage();
            }
            
            URL location = AccountFilterViewController.class.getResource("AccountFilterView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            Parent root = fxmlLoader.load();
            
            Scene scene = new Scene(root);
            //scene.getStylesheets().add("leeboardslog/Styles.css");
            
            this.stage.setScene(scene);
            
            this.controller = (AccountFilterViewController)fxmlLoader.getController();
            if (this.controller != null) {
                AccountFilter accountFilter = new AccountFilter();
                this.controller.setupController(this.engine, this.stage, accountFilter);
            }
            
            this.stage.setOnCloseRequest((event)-> {
/*                if (this.logBookEditor != null) {
                    if (!this.logBookEditor.safeCloseLogBookWindow(this)) {
                        // Cancel the close...
                        event.consume();
                    }
                    else {
                        if (this.controller != null) {
                            this.controller.setupController(null, null);
                            this.controller = null;
                        }
                    }
                }
*/
            });
            
        } catch (IOException ex) {
            Logger.getLogger(ReportDesignerWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void closeWindow() {
        
    }
}
