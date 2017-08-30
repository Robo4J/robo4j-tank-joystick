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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.HelperUtil;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.joystick.tank.layout.CameraViewProcessor;
import com.robo4j.joystick.tank.layout.Joystick;
import com.robo4j.joystick.tank.layout.JoystickException;
import com.robo4j.joystick.tank.layout.enums.JoystickCommandEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickEventEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickLevelEnum;
import com.robo4j.joystick.tank.layout.enums.QuadrantEnum;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TankJoystickMain extends Application {

	private static final String JOYSTICK_LOGO_1 = "20161021_Robo4j_LogoJava.png";
	private static final String JOYSTICK_LOGO_2 = "20161021_Robo4j_Sponsors.png";
	private static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
	private static final String PANEL_CSS = "-fx-border-color:darkblue; \n" + "-fx-background-color: #336699;\n"
			+ "-fx-border-insets:3;\n" + "-fx-border-radius:7;\n" + "-fx-border-width:2.0";

	private static final int VALUE_0 = 0;
	private static final int VALUE_1 = 1;
	private static final String OPT_COMMANDS_TEXT_FIELD = "not available";
	private static final int ROTATION_ANGEL = 90;
	private static final String IMAGE_PROCESSOR = "imageProcessor";
	private static final int CAMERA_IMAGE_WIDTH = 640;
	private static final int CAMERA_IMAGE_HEIGHT = 480;
	private volatile AtomicReference<String> cameraCommandsOpt;
	private volatile ImageView controllImageView;
	private volatile MediaView controllMediaView;

	private Button buttonConnect;
	private TextField optCommandsTextField;

	// Speed Properties
	private ExecutorService executor;
	private ScheduledExecutorService scheduledExecutor;
	private ScheduledExecutorService scheduledExecutor2;

	private boolean roboSchedulerActive;
	private String quadrant;
	private RoboContext roboSystem;
	private RoboReference<JoystickCommandEnum> platformController;

	public static void main(String... args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		SimpleLoggingUtil.print(getClass(), "Demo Starts");
		RoboBuilder builder = new RoboBuilder();
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4j.xml"));
		roboSystem = builder.build();
		platformController = roboSystem.getReference("legoController");
		roboSystem.start();
		System.out.println("RoboSystem after start:");
		System.out.println(SystemUtil.printStateReport(roboSystem));

		this.quadrant = HelperUtil.EMPTY_STRING;
		this.executor = Executors.newFixedThreadPool(3);
		this.scheduledExecutor = Executors.newScheduledThreadPool(1);
		this.scheduledExecutor2 = Executors.newScheduledThreadPool(1);

		cameraCommandsOpt = new AtomicReference<>(OPT_COMMANDS_TEXT_FIELD);
		optCommandsTextField = new TextField(OPT_COMMANDS_TEXT_FIELD);
		optCommandsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!oldValue.equals(newValue) && !newValue.isEmpty()) {
				cameraCommandsOpt.set(newValue);
			}
		});

		roboSchedulerActive = false;
		GridPane borderPane = new GridPane();

		borderPane.add(getLogos1(), VALUE_0, 0);
		borderPane.add(getLogos2(), VALUE_1, 0);
		borderPane.add(getLeftPanel(), VALUE_0, 1);
		borderPane.add(getCameraWindow(), VALUE_1, 1);
		primaryStage.setScene(new Scene(borderPane));
		primaryStage.setTitle("Robo4j :: Control Center");
		SimpleLoggingUtil.print(getClass(), "SHOW SCENE");
		primaryStage.show();

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		executor.shutdownNow();
		scheduledExecutor.shutdownNow();
		scheduledExecutor2.shutdownNow();
		SimpleLoggingUtil.print(getClass(), "JOYSTICK STOPPED");
		roboSystem.stop();
		System.out.println("State after stop:");
		System.out.println(SystemUtil.printStateReport(roboSystem));
		roboSystem.shutdown();
	}

	// Private Methdos
	private BorderPane getJoystickPane() {
		final BorderPane result = new BorderPane();
		result.setPadding(new Insets(15, 12, 15, 12));
		Joystick joystickPane = new Joystick(ROTATION_ANGEL, JoystickLevelEnum.values().length);
		joystickPane.addEventHandler(JoystickEventEnum.QUADRANT_CHANGED.getEventType(), e -> {
			quadrant = getQuadrantToCommand(e.getQuadrant());
			executor.execute(() -> sendPostRequest(quadrant));
		});

		joystickPane.addEventHandler(JoystickEventEnum.LEVEL_CHANGED.getEventType(), e -> {
			executor.execute(() -> sendPostRequest(quadrant));

		});
		result.setCenter(joystickPane);
		return result;
	}

	private void sendPostRequest(String command) {
		SimpleLoggingUtil.print(getClass(), "send post command: " + command);
		SimpleLoggingUtil.print(getClass(), " sendPostRequest: " + JoystickCommandEnum.getByName(command));
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
		Image javaOne4Kids = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(JOYSTICK_LOGO_1));
		ImageView imageView = new ImageView(javaOne4Kids);

		result.getChildren().addAll(imageView);
		return result;
	}

	private HBox getLogos2() {
		HBox result = new HBox();
		Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(JOYSTICK_LOGO_2));
		ImageView imageView = new ImageView(image);

		result.getChildren().setAll(imageView);
		return result;
	}

	private GridPane getLeftPanel() {

		GridPane result = new GridPane();

		HBox ipConnect = new HBox();
		ipConnect.setPadding(new Insets(15, 12, 2, 12));
		ipConnect.setSpacing(10); // Gap between nodes

		buttonConnect = new Button("Camera");
		buttonConnect.setPrefSize(80, 20);
		buttonConnect.setOnAction((event) -> handleConnectButtonOnAction());
		ipConnect.getChildren().addAll(buttonConnect);

		result.add(ipConnect, VALUE_0, VALUE_0);
		result.add(getJoystickPane(), VALUE_0, VALUE_1);
		result.add(getVideoPanel(), VALUE_0, 2);

		result.setStyle(PANEL_CSS);
		return result;
	}

	private HBox getCameraWindow() {
		HBox result = new HBox();

		Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));

		controllImageView = new ImageView();
		controllImageView.setImage(image);
		controllImageView.setFitWidth(CAMERA_IMAGE_WIDTH);
		controllImageView.setFitHeight(CAMERA_IMAGE_HEIGHT);
		controllImageView.setSmooth(true);
		controllImageView.setCache(true);
		result.setStyle(PANEL_CSS);
		result.getChildren().add(controllImageView);
		return result;
	}

	private HBox getVideoPanel() {

		try {
			HBox result = new HBox();
			// URL url =
			// RoboClassLoader.getInstance().getClassLoader().getResource("video1.mp4");
			String testUrl = "http://192.168.178.28:8554/life.fvl";

			// Path path = Paths.get(url.toURI());
			// Media media = new Media(path.toFile().toURI().toString());
			Media media = new Media(testUrl);
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.play();

			controllMediaView = new MediaView(mediaPlayer);
			controllMediaView.setFitWidth(320);
			controllMediaView.setFitHeight(240);
			result.setStyle(PANEL_CSS);
			result.getChildren().add(controllMediaView);
			return result;

			// } catch (NullPointerException | URISyntaxException e){
		} catch (NullPointerException e) {
			throw new RuntimeException("wrong ", e);
		}

	}

	private void handleConnectButtonOnAction() {
		if (roboSchedulerActive) {
			SimpleLoggingUtil.print(getClass(), "scheduler active");
		} else {
			buttonConnect.setText("Activated");
			scheduledExecutor.scheduleAtFixedRate(
					new CameraViewProcessor(roboSystem.getReference(IMAGE_PROCESSOR), controllImageView), 1, 1200,
					TimeUnit.MILLISECONDS);
		}
	}

}
