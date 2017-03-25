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

package com.robo4j.joystick.tank.layout.handler;

import java.util.Map;

import com.robo4j.joystick.tank.layout.enums.JoystickLevelEnum;
import com.robo4j.joystick.tank.layout.event.JoystickEvent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @author Choustoulakis Nikolaos (@eppnikos)
 */
public class JoystickEventHandler {

	private static final int ANGLE_360 = 360;
	private static final int MULTIPLIER = 2;

	public void onMouseDragged(Circle pov, DoubleProperty povCenteXProperty, DoubleProperty povCenteYProperty) {
		pov.setCenterX(povCenteXProperty.get());
		pov.setCenterY(povCenteYProperty.get());
	}

	public void setOnZoom(ZoomEvent e, DoubleProperty radiusProperty) {
		radiusProperty.set(radiusProperty.get() * e.getZoomFactor());
	}

	public void drawTargetLevel(JoystickEvent e, Canvas canvas, DoubleProperty radiusProperty,
			Map<Integer, IntegerProperty> levels) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		drawCircles(canvas, radiusProperty, levels, levels.size());
		// @formatter:off
        gc.setFill(Color.YELLOW);
        gc.fillArc(radiusProperty.get() - levels.get(getJoystickLevel(e.getJoystickLevel())).get(),
                radiusProperty.get() - levels.get(getJoystickLevel(e.getJoystickLevel())).get(),
                levels.get(getJoystickLevel(e.getJoystickLevel())).get() * MULTIPLIER,
                levels.get(getJoystickLevel(e.getJoystickLevel())).get() * MULTIPLIER, e.getQuadrant().getStartAngle(),
                e.getQuadrant().getAngleExtend(), ArcType.ROUND);
        // @formatter:on
		draw(canvas, radiusProperty, levels, e.getJoystickLevel().getLevel());
	}

	public void drawCircles(Canvas canvas, DoubleProperty radiusProperty, Map<Integer, IntegerProperty> levels,
			int levelNumber) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		draw(canvas, radiusProperty, levels, levelNumber);
	}

	public void draw(Canvas canvas, DoubleProperty radiusProperty, Map<Integer, IntegerProperty> levels,
			int levelNumber) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		levels.entrySet().stream().filter(e -> e.getKey() < levelNumber)
				.sorted((l, r) -> Integer.compare(r.getValue().get(), l.getValue().get())).forEach(i -> {
					switch (i.getKey()) {
					case 0:
						gc.setFill(Color.YELLOWGREEN);
						break;
					case 1:
						gc.setFill(Color.ROYALBLUE);
						break;
					case 2:
						gc.setFill(Color.STEELBLUE);
						break;
					case 3:
						gc.setFill(Color.SKYBLUE);
						break;

					}

					gc.setStroke(Color.DARKBLUE);
					double position = radiusProperty.get() - i.getValue().get();
					int size = i.getValue().get() * MULTIPLIER;
					gc.fillOval(position, position, size, size);
					gc.strokeArc(position, position, size, size, 0, ANGLE_360, ArcType.CHORD);
				});
	}

	// Private Methods
	private int getJoystickLevel(JoystickLevelEnum levelEnum) {
		return levelEnum.getLevel();
	}
}