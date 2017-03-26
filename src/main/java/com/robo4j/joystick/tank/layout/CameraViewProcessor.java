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

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.logging.SimpleLoggingUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraViewProcessor  implements Runnable {
    private static final String ATTRIBUTE_IMAGE = "image";
    private final RoboReference<?> cameraProcessor;
    private volatile ImageView imageView;

    public CameraViewProcessor(RoboReference<?> cameraProcessor, ImageView imageView) {
        this.cameraProcessor = cameraProcessor;
        this.imageView = imageView;
    }

    @Override
    public void run() {
        DefaultAttributeDescriptor<Image> descriptor = DefaultAttributeDescriptor.create(Image.class, ATTRIBUTE_IMAGE);
        try {
            Image image = cameraProcessor.getAttribute(descriptor).get();
            imageView.setImage(image);
        } catch (InterruptedException | ExecutionException e) {
            SimpleLoggingUtil.error(getClass(), "image failure", e);
        }
    }
}