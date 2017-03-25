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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.joystick.tank.layout.Joystick;
import com.robo4j.joystick.tank.layout.JoystickException;
import com.robo4j.joystick.tank.layout.enums.JoystickCommandEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickEventEnum;
import com.robo4j.joystick.tank.layout.enums.JoystickLevelEnum;
import com.robo4j.joystick.tank.layout.enums.QuadrantEnum;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
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

	private static final int INTERVAL = 150;
	private static final int VALUE_0 = 0;
	private static final int VALUE_1 = 1;
	private static final int PREF_COLUMN_COUNT = 5;
	private static final int PORT = 8025;
	private static final String ROBO_TARGET = "platform";
	private static final String BRICK_NOT_CONNETED = "Connect BRICK!";
	private static final String CONNECT_TEXT_FIELD = "Set IP Address";
	private static final String OPT_COMMANDS_TEXT_FIELD = "not available";
	private static final int ROTATION_ANGEL = 90;
	private volatile AtomicReference<String> cameraCommandsOpt;
	private volatile boolean isMouseTextField;
	private volatile ImageView controllImageView;

	private Button buttonConnect;
	private Button buttonFrontUnitRight;
	private Button buttonFrontUnitLeft;
	private TextField connectionTextField;
	private TextField optCommandsTextField;
	private GridPane gridOptions;

	// Speed Properties
	private Map<Integer, TextField> speedFieldMap;
	private Map<Integer, String> speedBasicValues;
	private Map<Integer, StringProperty> speedPropertyMap;
	private ExecutorService executor;
	private ScheduledExecutorService scheduledExecutor;
	private ScheduledExecutorService scheduledExecutor2;

	private boolean brickConnected;
	private int speed;
	private String quadrant;
	private RoboContext roboSystem;
	private RoboReference<JoystickCommandEnum> platformController;

	public static void main(String... args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		SimpleLoggingUtil.print(getClass(), "Demo Starts");
		RoboBuilder builder = new RoboBuilder().add(RoboClassLoader.getInstance().getResource("robo4j.xml"));
		roboSystem = builder.build();
		platformController = roboSystem.getReference("legoController");
		roboSystem.start();
		System.out.println("RoboSystem after start:");
		System.out.println(SystemUtil.printStateReport(roboSystem));

		/* default speed */
		this.speed = 100;
		this.quadrant = ConstantUtil.EMPTY_STRING;
		this.executor = Executors.newFixedThreadPool(3);
		this.scheduledExecutor = Executors.newScheduledThreadPool(1);
		this.scheduledExecutor2 = Executors.newScheduledThreadPool(1);
		this.connectionTextField = new TextField(CONNECT_TEXT_FIELD);
		this.connectionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() == 0 && !isMouseTextField) {
				connectionTextField.setText(CONNECT_TEXT_FIELD);
			} else {
				connectionTextField.setText(newValue);
			}
		});

		cameraCommandsOpt = new AtomicReference<>(OPT_COMMANDS_TEXT_FIELD);
		this.optCommandsTextField = new TextField(OPT_COMMANDS_TEXT_FIELD);
		this.optCommandsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!oldValue.equals(newValue) && !newValue.isEmpty()) {
				cameraCommandsOpt.set(newValue);
			}
		});

		this.connectionTextField.setOnMouseEntered((value) -> {
			isMouseTextField = true;
			if (connectionTextField.getText().length() == CONNECT_TEXT_FIELD.length() && !brickConnected) {
				connectionTextField.setText(ConstantUtil.EMPTY_STRING);
			}
		});

		this.connectionTextField.setOnMouseExited((value) -> {
			isMouseTextField = false;
		});

		speedBasicValues = getSpeedBasic();

		GridPane borderPane = new GridPane();
		gridOptions = getOptions();

		borderPane.add(getLogos1(), VALUE_0, 0);
		borderPane.add(getLogos2(), VALUE_1, 0);
		borderPane.add(getLeftPanel(), VALUE_0, 1);
		borderPane.add(getCameraWindow(), VALUE_1, 1);
		primaryStage.setScene(new Scene(borderPane));
		primaryStage.setTitle("Robo4j :: Control Center");
		SimpleLoggingUtil.print(getClass(), "SHOW SCENE");
		primaryStage.show();
		brickConnected = true;

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

	private void displayAlertDialog(String text) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Robo4j ERROR");
		alert.setContentText(text);
		alert.showAndWait();
	}

	private BorderPane getJoystickPane() {
		BorderPane result = new BorderPane();
		result.setPadding(new Insets(15, 12, 15, 12));
		Joystick joystickPane = new Joystick(ROTATION_ANGEL, JoystickLevelEnum.values().length);
		joystickPane.addEventHandler(JoystickEventEnum.QUADRANT_CHANGED.getEventType(), e -> {

			if (brickConnected) {
				quadrant = getQuadrantToCommand(e.getQuadrant());
				executor.execute(() -> sendPostRequest(quadrant, ROBO_TARGET, String.valueOf(speed)));
			} else {
				SimpleLoggingUtil.error(getClass(), BRICK_NOT_CONNETED);
				// displayAlertDialog(BRICK_NOT_CONNETED);
			}
		});

		joystickPane.addEventHandler(JoystickEventEnum.LEVEL_CHANGED.getEventType(), e -> {
			if (brickConnected) {
				speed = Integer.parseInt(speedPropertyMap.get(e.getJoystickLevel().getLevel()).getValue());
				executor.execute(() -> sendPostRequest(quadrant, ROBO_TARGET, String.valueOf(speed)));
			} else {
				SimpleLoggingUtil.error(getClass(), BRICK_NOT_CONNETED);
				// displayAlertDialog(BRICK_NOT_CONNETED);
			}

		});
		result.setCenter(joystickPane);
		return result;
	}

	private void sendPostCamRequest(String command) {
		SimpleLoggingUtil.print(getClass(), "send post command: " + command);
	}

	private void sendPostRequest(String command, String target, String speed) {
		SimpleLoggingUtil.print(getClass(),
				"send post command: " + command + " target: " + target + " speed: " + speed);
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
		Image javaOne4Kids = new Image(ClassLoader.getSystemResourceAsStream(JOYSTICK_LOGO_1));
		ImageView imageView = new ImageView(javaOne4Kids);

		result.getChildren().addAll(imageView);
		return result;
	}

	private HBox getLogos2() {
		HBox result = new HBox();
		Image image = new Image(ClassLoader.getSystemResourceAsStream(JOYSTICK_LOGO_2));
		ImageView imageView = new ImageView(image);

		result.getChildren().setAll(imageView);
		return result;
	}

	private GridPane getLeftPanel() {

		GridPane result = new GridPane();

		HBox ipConnect = new HBox();
		ipConnect.setPadding(new Insets(15, 12, 2, 12));
		ipConnect.setSpacing(10); // Gap between nodes

		buttonConnect = new Button("Connect");
		buttonConnect.setPrefSize(80, 20);
		buttonConnect.setOnAction((event) -> handleConnectButtonOnAction());
		ipConnect.getChildren().addAll(buttonConnect, connectionTextField);

		result.add(ipConnect, VALUE_0, VALUE_0);
		result.add(getJoystickPane(), VALUE_0, VALUE_1);
		result.add(getFrontUnitPane(), VALUE_0, 2);
		result.add(gridOptions, VALUE_0, 4);

		result.setStyle(PANEL_CSS);
		return result;
	}

	private HBox getFrontUnitPane() {
		HBox result = new HBox();
		result.setPadding(new Insets(15, 12, 2, 12));
		result.setSpacing(10); // Gap between nodes

		buttonFrontUnitLeft = new Button("CamLeft");
		buttonFrontUnitLeft.setPrefSize(120, 20);
		buttonFrontUnitLeft.setOnAction((event) -> handleFrontCamLeftButtonOnAction());

		buttonFrontUnitRight = new Button("CamRight");
		buttonFrontUnitRight.setPrefSize(120, 20);
		buttonFrontUnitRight.setOnAction((event) -> handleFrontCamRightButtonOnAction());

		result.getChildren().addAll(buttonFrontUnitLeft, buttonFrontUnitRight);

		return result;
	}

	private HBox getCameraWindow() {
		HBox result = new HBox();

		Image image = new Image(ClassLoader.getSystemResourceAsStream(NO_SIGNAL_IMAGE));

		controllImageView = new ImageView();
		controllImageView.setImage(image);
		controllImageView.setFitWidth(640);
		controllImageView.setFitHeight(480);
		controllImageView.setSmooth(true);
		controllImageView.setCache(true);
		result.setStyle(PANEL_CSS);
		result.getChildren().add(controllImageView);
		return result;
	}

	private Map<Integer, String> getSpeedBasic() {
		final Map<Integer, String> result = new HashMap<>();
		result.put(1, "200");
		result.put(2, "500");
		result.put(3, "700");
		return result;
	}

	private GridPane getOptions() {
		GridPane result = new GridPane();
		result.setPadding(new Insets(2, 12, 10, 12));
		result.setStyle("-fx-background-color: #336699;");
		speedPropertyMap = new HashMap<>();
		speedPropertyMap.put(0, new SimpleStringProperty("100"));

		speedFieldMap = new HashMap<>();
		result.add(new Text("Speed by Levels(Degrees/sec):"), VALUE_0, VALUE_0);
		int sizeOfOptions = JoystickLevelEnum.values().length;
		for (int i = VALUE_1; i < sizeOfOptions; i++) {
			result.add(new Text("Level" + i + ":"), VALUE_0, i);
			TextField speedField = new TextField();
			SimpleStringProperty simpleProperty = new SimpleStringProperty(speedBasicValues.get(i));
			speedField.setText(speedBasicValues.get(i));
			speedField.setPrefColumnCount(PREF_COLUMN_COUNT);

			/* assign handlers speed options */
			final String number = speedBasicValues.get(i);
			speedField.setOnMouseEntered((value) -> {
				isMouseTextField = true;
				// if(speedField.getText().length() == number.length()){
				// speedField.setText(EMPTY_STRING);
				// }
			});
			speedField.setOnMouseExited((value) -> {
				isMouseTextField = false;
				if (!isNumeric(speedField.getText()) || !numberInInterval(speedField.getText(), number)) {
					speedField.setText(number);
				}
			});

			// this.connectionTextField.setOnMouseEntered((value) -> {
			// isMouseTextField = true;
			// if(connectionTextField.getText().length() ==
			// CONNECT_TEXT_FIELD.length()){
			// connectionTextField.setText(EMPTY_STRING);
			// }
			// });
			//
			// this.connectionTextField.setOnMouseExited((value) -> {
			// isMouseTextField = false;
			// });

			speedFieldMap.put(i, speedField);
			speedPropertyMap.put(i, simpleProperty);
			result.add(speedFieldMap.get(i), VALUE_1, i);
		}

		return result;
	}

	private boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	private boolean numberInInterval(String str, String initValue) {

		int initial = Integer.valueOf(initValue);
		int value = Integer.valueOf(str);

		return (value > (initial - INTERVAL)) && (value < (initial + INTERVAL));
	}

	private void handleConnectButtonOnAction() {
		if (brickConnected) {
			displayAlertDialog("Robot has been deactivated!");
			brickConnected = false;
			buttonConnect.setText("Connect");
			connectionTextField.setEditable(true);
			connectionTextField.setText(CONNECT_TEXT_FIELD);
		} else {
			try {
				if (validateHost(connectionTextField.getText())) {
					buttonConnect.setText("Deactivate");
					brickConnected = true;
					connectionTextField.setEditable(false);

					// scheduledExecutor.scheduleAtFixedRate(
					// new ImageProducer(LINK_CAMERA_UNIT, controllImageView,
					// cameraCommandsOpt),
					// CEMARA_DELAY_INIT, CEMARA_DELAY, TimeUnit.SECONDS);
					// scheduledExecutor2.scheduleAtFixedRate(new
					// DeepNettProducer(deepNettsProperty), 1, 3,
					// TimeUnit.SECONDS);
				} else {
					displayAlertDialog("invalid host!");
					connectionTextField.setText(CONNECT_TEXT_FIELD);
				}
			} catch (IOException e) {
				displayAlertDialog(e.getMessage());
			}
		}
	}

	private void handleFrontCamLeftButtonOnAction() {
		if (brickConnected) {
			executor.execute(() -> sendPostCamRequest("front_left"));
		} else {
			displayAlertDialog(BRICK_NOT_CONNETED);
		}
	}

	private void handleFrontCamRightButtonOnAction() {
		if (brickConnected) {
			executor.execute(() -> sendPostCamRequest("front_right"));
		} else {
			displayAlertDialog(BRICK_NOT_CONNETED);
		}
	}

	private String getRoboIpAddress(String address) {
		return "http://" + address + ":" + PORT;
	}

	private boolean validateHost(final String address) throws IOException {
		SimpleLoggingUtil.print(getClass(), "validate host: " + address);
		return false;
	}

}
