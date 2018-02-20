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
public class ReportSetupView {
    public ReportSetupView() {}
    
    public static void showAndWait(String reportName, ReportDefinition definition, Engine engine, Stage primaryStage) {
        try {
            Stage stage = new Stage();

            URL location = ReportSetupViewController.class.getResource("ReportSetupView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            //scene.getStylesheets().add("leeboardslog/Styles.css");
            
            ReportSetupViewController controller = (ReportSetupViewController)fxmlLoader.getController();
            controller.setupController(definition, engine, stage);

            stage.setScene(scene);
            if (primaryStage != null) {
                stage.initOwner(primaryStage);
            }
            
            stage.setTitle(ResourceSource.getString("ReportSetupView.Title", reportName));

            stage.showAndWait();
            
        } catch (IOException ex) {
            Logger.getLogger(ReportSetupView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
