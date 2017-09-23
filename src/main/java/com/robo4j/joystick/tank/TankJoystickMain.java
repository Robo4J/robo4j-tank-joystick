/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.joystick.tank;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.joystick.tank.layout.Joystick;
import com.robo4j.joystick.tank.layout.JoystickException;
import com.robo4j.joystick.tank.layout.enums.JoystickCommandEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickEventEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickLevelEnum;
import com.robo4j.joystick.tank.layout.enums.QuadrantEnum;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.util.StringConstants;
import com.robo4j.util.SystemUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TankJoystickMain extends Application {

    private static final String ROBO4J_CONFIGURATION = "robo4j.xml";
    private static final String JOYSTICK_LOGO_1 = "robo4j128text.png";

    private static final String PANEL_CSS = "-fx-border-color:darkblue; \n" + "-fx-background-color: #336699;\n"
            + "-fx-border-insets:3;\n" + "-fx-border-radius:7;\n" + "-fx-border-width:2.0";

    private static final int VALUE_0 = 0;
    private static final int VALUE_1 = 1;
    private static final int ROTATION_ANGEL = 90;
    private static String joystickConfigurationFileName;

    // Speed Properties
    private ExecutorService executor;
    private String quadrant;
    private RoboContext roboSystem;
    private RoboReference<JoystickCommandEnum> platformController;

    public static void main(String... args) {
        switch (args.length){
            case 1:
                joystickConfigurationFileName = args[0];
                break;
            default:
                break;
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        SimpleLoggingUtil.print(getClass(), "Demo Starts");

        InputStream isConfig = joystickConfigurationFileName == null ?
                Thread.currentThread().getContextClassLoader().getResourceAsStream(ROBO4J_CONFIGURATION) :
                Files.newInputStream(Paths.get(joystickConfigurationFileName));
        RoboBuilder builder = new RoboBuilder()
                .add(isConfig);

        roboSystem = builder.build();
        platformController = roboSystem.getReference("legoController");
        roboSystem.start();
        System.out.println("RoboSystem after start:");
        System.out.println(SystemUtil.printStateReport(roboSystem));

        this.quadrant = StringConstants.EMPTY;
        this.executor = Executors.newFixedThreadPool(3);

        GridPane borderPane = new GridPane();

        borderPane.add(getLogos1(), VALUE_0, 0);
        borderPane.add(getLeftPanel(), VALUE_0, 1);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setTitle("Robo4j-Joystick");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executor.shutdownNow();
        roboSystem.stop();
        roboSystem.shutdown();
        System.out.println(SystemUtil.printStateReport(roboSystem));
    }

    // Private Methdos
    private BorderPane getJoystickPane() {
        BorderPane result = new BorderPane();
        result.setPadding(new Insets(15, 12, 15, 12));
        Joystick joystickPane = new Joystick(ROTATION_ANGEL, JoystickLevelEnum.values().length);
        joystickPane.addEventHandler(JoystickEventEnum.QUADRANT_CHANGED.getEventType(), e -> {
            quadrant = getQuadrantToCommand(e.getQuadrant());
            roboSystem.getScheduler().execute(() -> sendPostRequest(quadrant));
        });

        joystickPane.addEventHandler(JoystickEventEnum.LEVEL_CHANGED.getEventType(), e -> {
            roboSystem.getScheduler().execute(() -> sendPostRequest(quadrant));

        });
        result.setCenter(joystickPane);
        return result;
    }

    private void sendPostRequest(String command) {
        platformController.sendMessage(JoystickCommandEnum.getByName(command));
    }

    private String getQuadrantToCommand(final QuadrantEnum quadrant) {
        switch (quadrant) {
            case NONE:
                return "stop";
            case QUADRANT_I:
                return "right";
            case QUADRANT_II:
                return "move";
            case QUADRANT_III:
                return "left";
            case QUADRANT_IV:
                return "back";
            default:
                throw new JoystickException("no such command");
        }
    }

    private HBox getLogos1() {
        HBox result = new HBox();
        Image javaOne4Kids = new Image(ClassLoader.getSystemResourceAsStream(JOYSTICK_LOGO_1));
        ImageView imageView = new ImageView(javaOne4Kids);

        result.getChildren().addAll(imageView);
        return result;
    }

    private GridPane getLeftPanel() {

        GridPane result = new GridPane();

        HBox ipConnect = new HBox();
        ipConnect.setPadding(new Insets(15, 12, 2, 12));
        ipConnect.setSpacing(10); // Gap between nodes

        result.add(ipConnect, VALUE_0, VALUE_0);
        result.add(getJoystickPane(), VALUE_0, VALUE_1);

        result.setStyle(PANEL_CSS);
        return result;
    }

}
