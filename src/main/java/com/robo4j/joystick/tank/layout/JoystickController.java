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

package com.robo4j.joystick.tank.layout;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.robo4j.joystick.tank.layout.enums.JoystickEventEnum;
import com.robo4j.joystick.tank.layout.handler.JoystickEventHandler;
import com.robo4j.joystick.tank.layout.handler.PovEventHandler;
import com.robo4j.joystick.tank.layout.util.JoystickEventProducer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JoystickController {
	private Joystick joystick;

	private Map<Integer, IntegerProperty> levels;
	private DoubleProperty radiusProperty;
	private DoubleProperty povCenterXProperty;
	private DoubleProperty povCenterYProperty;

	private JoystickEventProducer eventProducer;
	private JoystickEventHandler joystickEventHandler;
	private PovEventHandler povEventHandler;

	public JoystickController(final Joystick joystick) {
		this.joystick = joystick;
		initProperties(joystick.getRadius(), joystick.getLevels());
		bindingProperties();
		addCanvasListener();
		initHandlers();
		addPovListener();
		povEventHandling();

		radiusProperty.set(joystick.getRadius());
	}

	public Joystick getJoystick() {
		return joystick;
	}

	public Map<Integer, IntegerProperty> getLevels() {
		return levels;
	}

	private void povEventHandling() {
		eventProducer = new JoystickEventProducer(joystick, joystick.getPov().centerXProperty(),
				joystick.getPov().centerYProperty(), povCenterXProperty, povCenterYProperty, levels);
		joystick.addEventHandler(MouseEvent.ANY, event -> eventProducer.handle(event));
	}

	private void initProperties(final double radius, final int levelNumber) {
		radiusProperty = new SimpleDoubleProperty();
		// @formatter:off
		levels = IntStream.range(0, levelNumber).boxed()
				.collect(Collectors.toMap(i -> i, i -> createProperty(radius, levelNumber, i + 1)));
		// @formatter:on
		povCenterXProperty = new SimpleDoubleProperty();
		povCenterYProperty = new SimpleDoubleProperty();
	}

	private IntegerProperty createProperty(final double radius, int levels, Integer i) {
		SimpleIntegerProperty simpleIntegerProperty = new SimpleIntegerProperty((int) (radius / i));
		simpleIntegerProperty.bind(radiusProperty.divide(levels).multiply(i));
		return simpleIntegerProperty;
	}

	private void bindingProperties() {

		joystick.getPov().radiusProperty().bind(radiusProperty.divide(levels.size()).divide(2));
		povCenterXProperty.bind(radiusProperty.add(joystick.getCanvas().layoutXProperty()));
		povCenterYProperty.bind(radiusProperty.add(joystick.getCanvas().layoutYProperty()));

		radiusProperty.addListener(e -> {
			joystickEventHandler.drawCircles(joystick.getCanvas(), radiusProperty, levels, levels.size());
			povEventHandler.bindPovToCanvas(joystick.getPov(), povCenterXProperty, povCenterYProperty);
			joystick.getCanvas().setHeight(radiusProperty.get() * 2);
			joystick.getCanvas().setWidth(radiusProperty.get() * 2);
		});
	}

	private void initHandlers() {
		joystickEventHandler = new JoystickEventHandler();
		povEventHandler = new PovEventHandler();
	}

	private void addPovListener() {
		joystick.getPov().addEventHandler(MouseEvent.MOUSE_DRAGGED, (e) -> povEventHandler.draggedPov(e,
				joystick.getPov(), radiusProperty, povCenterXProperty, povCenterYProperty));
		joystick.getPov().setOnMouseReleased(
				e -> povEventHandler.bindPovToCanvas(joystick.getPov(), povCenterXProperty, povCenterYProperty));
	}

	private void addCanvasListener() {
		joystick.getCanvas().addEventHandler(MouseEvent.MOUSE_DRAGGED,
				(e) -> joystickEventHandler.onMouseDragged(joystick.getPov(), povCenterXProperty, povCenterYProperty));
		joystick.addEventHandler(JoystickEventEnum.QUADRANT_CHANGED.getEventType(),
				e -> joystickEventHandler.drawTargetLevel(e, joystick.getCanvas(), radiusProperty, levels));
		joystick.addEventHandler(JoystickEventEnum.LEVEL_CHANGED.getEventType(),
				e -> joystickEventHandler.drawTargetLevel(e, joystick.getCanvas(), radiusProperty, levels));
	}
}
