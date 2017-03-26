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

package com.robo4j.joystick.tank.processor;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.httpunit.codec.CameraMessage;
import com.robo4j.core.logging.SimpleLoggingUtil;

import javafx.scene.image.Image;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ImageProcessor extends RoboUnit<CameraMessage> {
    private static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
    private static final AttributeDescriptor<Image> ATTRIBUTE_IMAGE = DefaultAttributeDescriptor.create(Image.class, "image");
    private static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections.singleton(ATTRIBUTE_IMAGE);
    private String output;
    private Image image;

    public ImageProcessor(RoboContext context, String id) {
        super(CameraMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        output = configuration.getString("output", null);
        if (output == null) {
            throw ConfigurationException.createMissingConfigNameException("output");
        }
        image = new Image(ClassLoader.getSystemResourceAsStream(NO_SIGNAL_IMAGE));

    }

    @Override
    public void onMessage(CameraMessage message) {
        SimpleLoggingUtil.print(getClass(), "JAVAFX output: " + output + " -> onMessage: " + message);
        image = new Image(new ByteArrayInputStream(message.getImage()));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
        if (descriptor.getAttributeName().equals("image") && descriptor.getAttributeType() == Image.class) {
            return (R) image;
        }
        return super.onGetAttribute(descriptor);
    }
}
