package freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.builder;

import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.ColorPickerView;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.renderer.ColorWheelRenderer;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.renderer.FlowerColorWheelRenderer;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
	public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
		switch (wheelType) {
			case CIRCLE:
				return new SimpleColorWheelRenderer();
			case FLOWER:
				return new FlowerColorWheelRenderer();
		}
		throw new IllegalArgumentException("wrong WHEEL_TYPE");
	}
}