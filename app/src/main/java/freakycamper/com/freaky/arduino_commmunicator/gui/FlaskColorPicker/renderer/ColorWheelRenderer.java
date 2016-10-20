package freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.renderer;

import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.ColorCircle;

import java.util.List;

public interface ColorWheelRenderer {
	float GAP_PERCENTAGE = 0.025f;

	void draw();

	ColorWheelRenderOption getRenderOption();

	void initWith(ColorWheelRenderOption colorWheelRenderOption);

	List<ColorCircle> getColorCircleList();
}
