import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.lang.Class;
import java.util.Map;

import javax.swing.DefaultButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;

/** *********************************************************************
 *  Main alarm button with app state stylings.
 */
public class AlarmButton extends JButton {
    static final Integer ALARM_BUTTON_FONT_SIZE = 19;
    static final Integer ALARM_BUTTON_WIDTH = 240;
    static final Integer ALARM_BUTTON_HEIGHT = 44;

    static final Color ALARM_BUTTON_STYLE = Color.GRAY;
    static final Color ALARM_BUTTON_SET_STYLE = Color.YELLOW;
    static final Color ALARM_BUTTON_PRESSED_STYLE = Color.BLUE;
    static final Color ALARM_BUTTON_RINGING_STYLE = Color.RED;

    final int mRadius = 20;
    final int mStroke = 1;

    public AlarmButton() {
        setSize(ALARM_BUTTON_WIDTH, ALARM_BUTTON_HEIGHT);
        setPreferredSize(new Dimension(ALARM_BUTTON_WIDTH, ALARM_BUTTON_HEIGHT));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);

        setFont(new Font("Dialog", Font.PLAIN, ALARM_BUTTON_FONT_SIZE));
        getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                final DefaultButtonModel buttonModel =
                    (DefaultButtonModel) getModel();
                if (buttonModel.isPressed()) {
                    setBackground(ALARM_BUTTON_PRESSED_STYLE);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color styleColor = Color.BLACK;
        if (swingClock.getAppState() == Global.APPSTATE.ALARM_NOT_SET) {
            styleColor = ALARM_BUTTON_STYLE;
        } else if (swingClock.getAppState() == Global.APPSTATE.ALARM_SET) {
            styleColor = ALARM_BUTTON_SET_STYLE;
        } else if (swingClock.getAppState() == Global.APPSTATE.ALARM_RINGING) {
            styleColor = ALARM_BUTTON_RINGING_STYLE;
        }

        final Graphics2D gc = (Graphics2D) g.create();

        final Double startPoint = getHeight() * .2;
        gc.setPaint(new GradientPaint(new Point(0, startPoint.intValue()),
            Color.WHITE,
            new Point(0, getHeight() - mStroke),
            styleColor));
        gc.fillRoundRect(0, 0, getWidth(), getHeight(), mRadius, mRadius);

        gc.setColor(Color.BLACK);
        gc.setStroke(new BasicStroke(1));
        gc.drawRoundRect(0, 0, getWidth() - mStroke, getHeight() - mStroke, mRadius, mRadius);
        gc.dispose();

        super.paintComponent(g);
    }
}

