// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * A Notification Message similar to a popup window, but without disrupting the
 * user's workflow.
 *
 * Non-modal info panel that vanishes after a certain time.
 *
 * This class only holds the data for a notification, {@link NotificationManager}
 * is responsible for building the message panel and displaying it on screen.
 *
 * example:
 * <pre>
 *      Notification note = new Notification("Hi there!");
 *      note.setDuration(4000); // optional
 *      note.setIcon(JOptionPane.INFORMATION_MESSAGE); // optional
 *      note.show();
 * </pre>
 */
public class Notification {

    public final static int DEFAULT_CONTENT_WIDTH = 350;

    private Icon icon;
    private int duration;
    private Component content;

    public Notification() {
        duration = NotificationManager.defaultNotificationTime;
    }

    public Notification(String msg) {
        this();
        setContent(msg);
    }

    /**
     * Set the content of the message.
     *
     * @param content any Component to be shown
     *
     * @see #setContent(java.lang.String)
     * @return the current Object, for convenience
     */
    public Notification setContent(Component content) {
        this.content = content;
        return this;
    }

    /**
     * Set the notification text. (Convenience method)
     *
     * @param msg the message String
     *
     * @see #Notification(java.lang.String)
     * @return the current Object, for convenience
     */
    public Notification setContent(String msg) {
        JMultilineLabel lbl = new JMultilineLabel(msg);
        lbl.setMaxWidth(DEFAULT_CONTENT_WIDTH);
        content = lbl;
        return this;
    }

    /**
     * Set the time after which the message is hidden.
     *
     * @param duration the time (in milliseconds)
     * @return the current Object, for convenience
     */
    public Notification setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Set an icon to display on the left part of the message window.
     *
     * @param icon the icon (null means no icon is displayed)
     * @return the current Object, for convenience
     */
    public Notification setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Set an icon to display on the left part of the message window by
     * choosing from the default JOptionPane icons.
     *
     * @param messageType one of the following: JOptionPane.ERROR_MESSAGE,
     * JOptionPane.INFORMATION_MESSAGE, JOptionPane.WARNING_MESSAGE,
     * JOptionPane.QUESTION_MESSAGE, JOptionPane.PLAIN_MESSAGE
     * @return the current Object, for convenience
     */
    public Notification setIcon(int messageType) {
        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE:
                return setIcon(UIManager.getIcon("OptionPane.errorIcon"));
            case JOptionPane.INFORMATION_MESSAGE:
                return setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            case JOptionPane.WARNING_MESSAGE:
                return setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            case JOptionPane.QUESTION_MESSAGE:
                return setIcon(UIManager.getIcon("OptionPane.questionIcon"));
            case JOptionPane.PLAIN_MESSAGE:
                return setIcon(null);
            default:
                throw new IllegalArgumentException("Unknown message type!");
        }
    }

    public Component getContent() {
        return content;
    }

    public int getDuration() {
        return duration;
    }

    public Icon getIcon() {
        return icon;
    }

    /**
     * Display the notification.
     */
    public void show() {
        NotificationManager.getInstance().showNotification(this);
    }
}
