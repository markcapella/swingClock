import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

// *********************************************************************
// *** swingClock JavaFX Application                                 ***
// *********************************************************************

public class swingClock {
    // All app static finals.
    static final String WINDOW_TITLE = "swingClock";

    static final String ALARM_SOUND_FOR_APP = "alarmBeep.wav";
    static final String OK_BUTTON_PNG = "okButton.png";
    static final String CANCEL_BUTTON_PNG = "cancelButton.png";

    static final int WINDOW_ICON_PNG_HEIGHT = 96;
    static final int WINDOW_ICON_PNG_WIDTH = 96;

    static final Integer GNOME_ICON_HEIGHT = 24;
    static final Integer GNOME_ICON_WIDTH = 24;
    static final Integer GNOME_IMAGE_RIGHT_MARGIN = 10;

    static final Integer CANCEL_ICON_HEIGHT = 12;
    static final Integer CANCEL_ICON_WIDTH = 12;

    static final Integer OK_ICON_HEIGHT = 12;
    static final Integer OK_ICON_WIDTH = 12;

    static final String ALARM_VALUE_PREFNAME = "Alarm_Value";
    static final String WINDOW_ONTOP_PREFNAME   = "Window_OnTop";
    static final String WINDOW_POS_X_PREFNAME   = "Window_Position_X";
    static final String WINDOW_POS_Y_PREFNAME   = "Window_Position_Y";
    static final String WINDOW_WIDTH_PREFNAME   = "Window_Width";
    static final String WINDOW_HEIGHT_PREFNAME  = "Window_Height";
    static final String APP_STATE_PREFNAME      = "App_State";

    static final Boolean WINDOW_ONTOP_DEFAULT = false;
    static final Double WINDOW_DEFAULT_X = 200.0;
    static final Double WINDOW_DEFAULT_Y = 200.0;
    static final Double WINDOW_DEFAULT_WIDTH = 380.0;
    static final Double WINDOW_DEFAULT_HEIGHT = 400.0;
    static final Global.APPSTATE APP_STATE_DEFAULT = Global.APPSTATE.ALARM_NOT_SET;

    static final Integer TIME_LABEL_FONT_SIZE = 32;
    static final Integer DATE_LABEL_FONT_SIZE = 22;

    static final String[] MONTH_NAMES = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    /** *********************************************************************
     * Class Global stubs.
     */
    static final Timer mSecondTimer = new Timer();
    static final Preferences mPref = Preferences.userRoot().node(WINDOW_TITLE);

    // Application for this UI framework.
    static JFrame mApplication;
    static final BufferedImage mApplicationIcon = new BufferedImage(
        WINDOW_ICON_PNG_WIDTH, WINDOW_ICON_PNG_HEIGHT,
        BufferedImage.TYPE_INT_ARGB);

    // Application root view for this UI framework.
    static JPanel mTimeDatePanel;
    static JLabel mGnomeImageView;
    static JLabel mTimeLabel;
    static JLabel mDateLabel;

    static int foo = 0;

    static JPanel mAlarmButtonPanel;
    static AlarmButton mAlarmButton;

    static JPanel mAlarmEditPanel;
    static Box mAlarmEdit;
    static JSpinner mAlarmSpinner;

    static JPanel mActionsPanel;
    static JButton mOkButton;
    static JButton mCancelButton;

    static AudioInputStream CLIP_SOUND_FOR_APP;
    static Clip mClip;

    /** *********************************************************************
     * Main application entery. Start framework lifecycle loop.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                start();
            }
        });
    }

    /** *********************************************************************
     * Main application start(). Set title, icon, etc.
     */
    private static void start() {
        // Load audio clip for alarm.
        try {
            CLIP_SOUND_FOR_APP = AudioSystem.getAudioInputStream(
                new File(ALARM_SOUND_FOR_APP));
            mClip = AudioSystem.getClip();
            mClip.open(CLIP_SOUND_FOR_APP);
        } catch (IOException | LineUnavailableException |
                 UnsupportedAudioFileException e) {
            System.out.println(
                "swingClock: start() Alarm beep audio sound is unavailable.");
        }

        // Set window titlebar title & icon.
        mApplication = new JFrame(/* WINDOW_TITLE */);
        mApplication.setLayout(new BoxLayout(mApplication.getContentPane(), BoxLayout.Y_AXIS));
        mApplication.setVisible(true);
        mApplication.setSize(getWindowWidth().intValue(), getWindowHeight().intValue());

        createWindowIcon();
        setWindowIcon();

        // Restore window location, size, onTop user prefs.
        mApplication.setBounds(getWindowPosX().intValue(), getWindowPosY().intValue(),
                         getWindowWidth().intValue(), getWindowHeight().intValue());
        mApplication.setAlwaysOnTop(getWindowOnTopValue());

        // Connect our custom onClose() method ---> stop().
        mApplication.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mApplication.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
                mApplication.setVisible(false);
                mApplication.dispose();
                System.exit(0);
            }
        });

        // Set window and initial size.
        initMainWindow();
        //updateWindowState();

        // Create main timer.
        mSecondTimer.scheduleAtFixedRate(new TimerTask() {
            Integer mTimerPrevMin;
            @Override
            public void run() {
                // Check if alarm has gone off (per-second).
                if (getAppState() == Global.APPSTATE.ALARM_SET) {
                    final LocalDateTime a = getDateFromAlarmString(getAlarmValue()).toInstant().
                        atZone(ZoneId.systemDefault()).toLocalDateTime();
                    if (a.isBefore(LocalDateTime.now())) {
                        setAppState(Global.APPSTATE.ALARM_RINGING);
                        mAlarmButtonPanel.setVisible(true);
                        mAlarmButton.setText(getStyledAlarmString(getAlarmValue()));
                        mAlarmButton.repaint();
                        mAlarmEditPanel.setVisible(false);
                        mClip.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                }

                // Per-minute, update app with on initial entry, then on minute change.
                final Integer nowMinute = LocalDateTime.ofInstant(
                    Instant.now(), ZoneId.systemDefault()).getMinute();
                if (mTimerPrevMin == null || mTimerPrevMin != nowMinute) {
                    mTimerPrevMin = nowMinute;
                    // Create and set app icon.
                    createWindowIcon();
                    setWindowIcon();
                    updateTimeDatePanel();
                }

            }
        }, 0, 1000 /* per-second */);
    }

    /** *********************************************************************
     * Main applicartion stop(). Save state thru prefs.
     */
    static public void stop() {
        // Cancel main timer.
        mSecondTimer.cancel();

        // Save window location, size, onTop user prefs.
        setWindowOnTopValue(mApplication.isAlwaysOnTop());

        final Rectangle frameRect = mApplication.getBounds();
        setWindowPosX(frameRect.getX());
        setWindowPosY(frameRect.getY());
        setWindowWidth(frameRect.getWidth());
        setWindowHeight(frameRect.getHeight());
    }

    /** *********************************************************************
     * Main window (form) has three vertical panels.
     */
    static public void initMainWindow() {
        initTimeDatePanel();
        initAlarmButtonPanel();
        initAlarmEditPanel();

        if (getAppState() == Global.APPSTATE.ALARM_NOT_SET) {
            mAlarmButtonPanel.setVisible(true);
            mAlarmButton.setText("Alarm");
            mAlarmEditPanel.setVisible(false);

        } else if (getAppState() == Global.APPSTATE.SETTING_ALARM) {
            mAlarmButtonPanel.setVisible(false);
            mAlarmEditPanel.setVisible(true);
            mAlarmSpinner.setValue(getDateFromAlarmString(getAlarmValue()));

        } else if (getAppState() == Global.APPSTATE.ALARM_SET) {
            mAlarmButtonPanel.setVisible(true);
            mAlarmButton.setText(getStyledAlarmString(getAlarmValue()));
            mAlarmEditPanel.setVisible(false);

        } else if (getAppState() == Global.APPSTATE.ALARM_RINGING) {
            mAlarmButtonPanel.setVisible(true);
            mAlarmButton.setText(getStyledAlarmString(getAlarmValue()));
            mClip.loop(Clip.LOOP_CONTINUOUSLY);
            mAlarmEditPanel.setVisible(false);
        }
    }

    /** *********************************************************************
     * Init the Time / date display panel.
     */
    static public void initTimeDatePanel() {
        final LocalDateTime ldt = LocalDateTime.ofInstant(
            Instant.now(), ZoneId.systemDefault());

        // Construct top level.
        mTimeDatePanel = new JPanel();

        // Add Icon to Window contents as GNOME doesn't use it in the titlebar.
        mGnomeImageView = new JLabel();
        if (System.getenv("XDG_SESSION_DESKTOP").contains("GNOME")) {
            final BufferedImage tempBI = new BufferedImage(
                GNOME_ICON_WIDTH, GNOME_ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D gc = tempBI.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gc.drawImage(mApplicationIcon, 0, 0,
                GNOME_ICON_WIDTH, GNOME_ICON_HEIGHT, null);
            gc.dispose();
            mGnomeImageView.setIcon(new ImageIcon(tempBI));
            mTimeDatePanel.add(mGnomeImageView);
        }

        mTimeLabel = new JLabel(getNNWithLeadZero(ldt.getHour()) + ":" +
            getNNWithLeadZero(ldt.getMinute()) + " ");
        mTimeLabel.setFont(new Font("Times", Font.PLAIN, TIME_LABEL_FONT_SIZE));
        mTimeDatePanel.add(mTimeLabel);

        mDateLabel = new JLabel(MONTH_NAMES[ldt.getMonthValue() - 1] + " " +
            getNNWithLeadZero(ldt.getDayOfMonth()));
        mDateLabel.setFont(new Font("Times", Font.PLAIN, DATE_LABEL_FONT_SIZE));
        mTimeDatePanel.add(mDateLabel);

        mApplication.getContentPane().add(mTimeDatePanel);
    }

    /** *********************************************************************
     * Update main window ... time / date and alarm button.
     */
    static public void updateTimeDatePanel() {
        // Add Icon to Window contents as GNOME doesn't use it in the titlebar.
        if (System.getenv("XDG_SESSION_DESKTOP").contains("GNOME")) {
            final BufferedImage tempBI = new BufferedImage(
                GNOME_ICON_WIDTH, GNOME_ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D gc = tempBI.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gc.drawImage(mApplicationIcon, 0, 0,
                GNOME_ICON_WIDTH, GNOME_ICON_HEIGHT, null);
            gc.dispose();
            mGnomeImageView.setIcon(new ImageIcon(tempBI));
        }

        // Display of Current time and Current date.
        final LocalDateTime ldt = LocalDateTime.ofInstant(
            Instant.now(), ZoneId.systemDefault());
          mTimeLabel.setText(getNNWithLeadZero(ldt.getHour()) + ":" +
            getNNWithLeadZero(ldt.getMinute()) + " ");
        mDateLabel.setText(MONTH_NAMES[ldt.getMonthValue() - 1] + " " +
            getNNWithLeadZero(ldt.getDayOfMonth()));
    }

    /** *********************************************************************
     * Init the Alarm Button panel.
     */
    static public void initAlarmButtonPanel() {
        mAlarmButtonPanel = new JPanel();

        mAlarmButton = new AlarmButton();
        mAlarmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getAppState() == Global.APPSTATE.ALARM_NOT_SET) {
                    setAppState(Global.APPSTATE.SETTING_ALARM);
                    mAlarmButtonPanel.setVisible(false);
                    mAlarmEditPanel.setVisible(true);
                    mAlarmSpinner.setValue(getDateFromAlarmString(getAlarmValue()));
                    removeAlarmValue();
                    return;
                }

                if (getAppState() == Global.APPSTATE.ALARM_SET) {
                    setAppState(Global.APPSTATE.ALARM_NOT_SET);
                    mAlarmButton.setText("Alarm");
                    mAlarmButtonPanel.setVisible(true);
                    mAlarmEditPanel.setVisible(false);
                    removeAlarmValue();
                    return;
                }

                if (getAppState() == Global.APPSTATE.ALARM_RINGING) {
                    setAppState(Global.APPSTATE.ALARM_NOT_SET);
                    mAlarmButton.setText("Alarm");
                    mAlarmButtonPanel.setVisible(true);
                    mAlarmEditPanel.setVisible(false);
                    mClip.stop();
                    removeAlarmValue();
                    return;
                }
            }
        });
        mAlarmButtonPanel.add(mAlarmButton);

        mApplication.getContentPane().add(mAlarmButtonPanel);
    }

    /** *********************************************************************
     * Init the date entry / edit panel.
     */
    static public void initAlarmEditPanel() {
        mAlarmEditPanel = new JPanel();

        mAlarmEdit = Box.createHorizontalBox();

        mAlarmSpinner = new JSpinner(new SpinnerDateModel());
        final Double newWidth = mAlarmSpinner.getPreferredSize().getWidth() + 20;
        final Double newHeight = mAlarmSpinner.getPreferredSize().getHeight() + 15;
        mAlarmSpinner.setPreferredSize(new Dimension(newWidth.intValue(), newHeight.intValue()));

        mAlarmSpinner.setEditor(new DateEditor(mAlarmSpinner, "HH:mm  MMM dd yyyy"));
        mAlarmSpinner.setValue(new GregorianCalendar().getTime());

        mAlarmEdit.add(mAlarmSpinner);
        mAlarmEditPanel.add(mAlarmEdit);

        // Action Buttons node (Cancel / Ok)
        mActionsPanel = new JPanel();

        // Cancel button.
        mCancelButton = new JButton();
        try {
            final Image image = ImageIO.read(new File(CANCEL_BUTTON_PNG));
            mCancelButton.setIcon(new ImageIcon(image.getScaledInstance(
                CANCEL_ICON_WIDTH, CANCEL_ICON_HEIGHT, Image.SCALE_DEFAULT)));
        } catch (Exception e) {
            System.out.println(
                "swingClock: initAlarmEditPanel( cancelButton image load fails.");
        }

        // Cancel button actions.
        mCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAppState(Global.APPSTATE.ALARM_NOT_SET);
                mAlarmButton.setText("Alarm");
                mAlarmButtonPanel.setVisible(true);
                mAlarmEditPanel.setVisible(false);
                removeAlarmValue();
            }
        });
        mActionsPanel.add(mCancelButton);

        // Ok button.
        mOkButton = new JButton();
        try {
            final Image image = ImageIO.read(new File(OK_BUTTON_PNG));
            mOkButton.setIcon(new ImageIcon(image.getScaledInstance(
                OK_ICON_WIDTH, OK_ICON_HEIGHT, Image.SCALE_DEFAULT)));
        } catch (Exception e) {
            System.out.println(
                "swingClock: initAlarmEditPanel( okButton image load fails.");
        }

        // Ok button actions.
        mOkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAppState(Global.APPSTATE.ALARM_SET);
                mAlarmButtonPanel.setVisible(true);

                final Date alarmDateTime = (Date) mAlarmSpinner.getValue();
                setAlarmValue(alarmDateTime.toString());

                mAlarmButton.setText(getStyledAlarmString(getAlarmValue())); // no !
                mAlarmEditPanel.setVisible(false);
            }
        });
        mActionsPanel.add(mOkButton);
        mAlarmEditPanel.add(mActionsPanel);

        mApplication.getContentPane().add(mAlarmEditPanel);
    }

    /** *********************************************************************
     * Helper methods ... all Preferences getter / setters.
     */
    static public Boolean getWindowOnTopValue() {
        final Boolean r = mPref.getBoolean(WINDOW_ONTOP_PREFNAME, WINDOW_ONTOP_DEFAULT);
        // System.out.println("swingClock: getWindowOnTopValue() DEBUG - reports : " + r);
        return r;
    }

    static public void setWindowOnTopValue(Boolean onTopValue) {
        // System.out.println("swingClock: setWindowOnTopValue() DEBUG - reports : " + onTopValue);
        mPref.putBoolean(WINDOW_ONTOP_PREFNAME, onTopValue);
    }

    static public String getAlarmValue() {
        final String dateString = mPref.get(ALARM_VALUE_PREFNAME, "");
        //System.out.println("swingClock: getAlarmValue() alarmString :" + dateString + ":");
        return dateString;
    }

    static public void setAlarmValue(String alarmValue) {
        //System.out.println("swingClock: setAlarmValue() alarmString :" + alarmValue + ":");
        mPref.put(ALARM_VALUE_PREFNAME, alarmValue);
    }

    static public void removeAlarmValue() {
        mPref.remove(ALARM_VALUE_PREFNAME);
    }

    static public Global.APPSTATE getAppState() {
        return Global.APPSTATE.appStateValueOf(mPref.get(APP_STATE_PREFNAME,
            APP_STATE_DEFAULT.getStringValue()));
    }

    static public void setAppState(Global.APPSTATE state) {
        mPref.put(APP_STATE_PREFNAME, state.getStringValue());
    }


    static public Double getWindowPosX() {
        return mPref.getDouble(WINDOW_POS_X_PREFNAME, WINDOW_DEFAULT_X);
    }

    static public void setWindowPosX(Double x) {
        mPref.putDouble(WINDOW_POS_X_PREFNAME, x);
    }


    static public Double getWindowPosY() {
        return mPref.getDouble(WINDOW_POS_Y_PREFNAME, WINDOW_DEFAULT_Y);
    }

    static public void setWindowPosY(Double y) {
        mPref.putDouble(WINDOW_POS_Y_PREFNAME, y);
    }


    static public Double getWindowWidth() {
        return mPref.getDouble(WINDOW_WIDTH_PREFNAME, WINDOW_DEFAULT_WIDTH);
    }

    static public void setWindowWidth(Double w) {
        mPref.putDouble(WINDOW_WIDTH_PREFNAME, w);
    }


    static public Double getWindowHeight() {
        return mPref.getDouble(WINDOW_HEIGHT_PREFNAME, WINDOW_DEFAULT_HEIGHT);
    }

    static public void setWindowHeight(Double h) {
        mPref.putDouble(WINDOW_HEIGHT_PREFNAME, h);
    }

    /** *********************************************************************
     * Helper method, format number ( < 60 ) as two-digit with leading zero.
     */
    static public String getNNWithLeadZero(Integer number) {
        // System.out.println("NN: " + number);
        return (number < 10) ?
            "0" + number.toString() :
            number.toString();
    }

    /** *********************************************************************
     * Helper methods to Format LocalDateTime to String
     * and String to LocalDateTime.
     */
    static public String getStyledAlarmString(String alarm) {
        return alarm.substring(11, 13) + ":" + alarm.substring(14, 16) + " " +
            alarm.substring(4, 7) + " " + alarm.substring(8, 10) + " " +
            alarm.substring(24, 28);
    }

    /** *********************************************************************
    * Helper method determine month number from name (DEC = 11).
    */
    static int getMonthFromString(String mmm) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equals(mmm)) {
                return i;
            }
        }
        return -1;
    }

    /** *********************************************************************
    * Helper method converts alarm string to Date object.
    */
    static public Date getDateFromAlarmString(String alarm) {
        if (alarm.isEmpty()) {
            return new Date();
        }
        
        final int yy = Integer.parseInt(alarm.substring(24, 28));
        final String mmm = alarm.substring(4, 7);
        final int dd = Integer.parseInt(alarm.substring(8, 10));
        final int hh = Integer.parseInt(alarm.substring(11, 13));
        final int mn = Integer.parseInt(alarm.substring(14, 16));

        Date d = new Date(yy - 1900, getMonthFromString(mmm), dd);
        d.setHours(hh);
        d.setMinutes(mn);
        d.setSeconds(0);

        return d;
    }

    /** *********************************************************************
     * Creates resource of clock face with current time.
     */
    static public void createWindowIcon() {
        // canvas = new Canvas().getGraphics();
        final Graphics2D gc = mApplicationIcon.createGraphics();
        final Color scGreen = new Color(30, 167, 31);

        // Set background transparent.
        gc.setBackground(new Color(0, true));
        gc.clearRect(0, 0, WINDOW_ICON_PNG_WIDTH, WINDOW_ICON_PNG_HEIGHT);

        // <!-- Clock "ear" bells -->
        gc.setColor(Color.BLACK);
        gc.fillOval(4, 4, 32, 32);
        gc.fillOval(60, 4, 32, 32);

        // <!-- Outer clock body circle -->
        gc.setColor(scGreen);
        gc.fillOval(8, 8, 80, 80);

        // <!-- Inner clock body circle -->
        gc.setColor(Color.WHITE);
        gc.fillOval(16, 16, 64, 64);

        // <!-- Clock top alarm button -->
        gc.setColor(Color.BLACK);
        gc.fillRect(44, 2, 8, 6);

        // <!-- Two clock feet -->
        gc.setColor(Color.BLACK);
        gc.setStroke(new BasicStroke(6));
        gc.drawLine(14, 86, 20, 80);
        gc.drawLine(82, 86, 76, 80);
        gc.setStroke(new BasicStroke(1));

        // <!-- Two clock hands -->
        final Instant instant = Instant.now();
        final LocalDateTime ldt = LocalDateTime.ofInstant(
            instant, ZoneId.systemDefault());
        final Integer nowHour = ldt.getHour();
        final Integer nowMin = ldt.getMinute();

        final Integer hourMod = nowHour % 12;
        final Double hourSec = (hourMod * 3600.0);
        final Double minSec = (nowMin * 60.0);
        final Double totSec = (hourSec + minSec);

        final Double totSecondsInHour = 12.0 * 60.0 * 60.0;
        final Double hourRot = totSec / totSecondsInHour * 360.0 - 90;
        final Integer minRot = nowMin * 360 / 60 - 90;

        gc.setColor(Color.BLACK);
        gc.setStroke(new BasicStroke(4));

        int f = 48 - (int) (4 * 32.0);
        gc.drawLine(48 - (int) ( 4 * Math.cos(Math.toRadians(hourRot))),
            48 - (int) ( 4 * Math.sin(Math.toRadians(hourRot))),
            48 + (int) (18 * Math.cos(Math.toRadians(hourRot))),
            48 + (int) (18 * Math.sin(Math.toRadians(hourRot))));
        gc.drawLine(48 - (int) ( 4 * Math.cos(Math.toRadians(minRot))),
            48 - (int) ( 4 * Math.sin(Math.toRadians(minRot))),
            48 + (int) (26 * Math.cos(Math.toRadians(minRot))),
            48 + (int) (26 * Math.sin(Math.toRadians(minRot))));
        gc.setStroke(new BasicStroke(1));

        // <!-- Clock center, small circle -->
        gc.setColor(scGreen);
        gc.fillOval(46, 46, 4, 4);

        gc.dispose();
    }

    /** *********************************************************************
     * Helper method, loads Window Icon from where we've
     * Created resource of clock face with current time.
     */
    static public void setWindowIcon() {
        try {
            mApplication.setIconImage(mApplicationIcon);
        } catch (Exception e) {
            System.out.println(
                "swingClock: setWindowIcon() Setting window icon fails: \n" + e);
        }
    }
}
