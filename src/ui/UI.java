package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class UI extends JFrame {
    private static final JLabel viewLabel = new JLabel();

    private static final Font defaultFont = new Font("Consolas", Font.BOLD, 28);

    private static final Image backgroundImage;
    private static final Image foregroundImage;

    static {
        try {
            backgroundImage = ImageIO.read(new File(".\\data\\resources\\background.png"));
            foregroundImage = ImageIO.read(new File(".\\data\\resources\\foreground.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UI () throws IOException {
        super("Test");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setUndecorated(true);

        add(viewLabel);

        setVisible(true);

        final double[] delta_time = {0};
        final double[] begin_time = {System.nanoTime()/1e9};
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Update(delta_time[0]);

                delta_time[0] = System.nanoTime()/1e9 - begin_time[0];
                begin_time[0] = System.nanoTime()/1e9;
            }
        }, 0, 1);
    }


    private void Update(double deltaTime) {
        BufferedImage frame = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);



        //TODO: draw games



        g.drawImage(foregroundImage, 0, 0, getWidth(), getHeight(), null);


        String time_str = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        String date_str = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d/M"));

        g.setColor(new Color(0x000000));
        g.setFont(defaultFont.deriveFont(getHeight() / 10f));
        g.drawString(time_str, (getWidth() + g.getFontMetrics().stringWidth("00:00") - 2 * g.getFontMetrics().stringWidth(time_str)) * 0.5f, getHeight() * 0.8f);

        g.setFont(defaultFont.deriveFont(getHeight() / 15f));
        g.drawString(date_str, (getWidth() - g.getFontMetrics().stringWidth(date_str)) * 0.5f, getHeight() * 0.9f);



        viewLabel.setIcon(new ImageIcon(frame));
        setVisible(true);
    }
}
