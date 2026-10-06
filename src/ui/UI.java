package ui;

import Util.Reference;
import database.Database;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class UI extends JFrame {
    private static final JLabel viewLabel = new JLabel();

    private static final Font defaultFont = new Font("Consolas", Font.BOLD, 28);

    private static final Image backgroundImage;
    private static final Image foregroundImage;
    private static final Image gameTemplateImage;
    private static final Image gamePlaceholderImage;


    private static List<Reference.Game> games = new ArrayList<>();

    static {
        try {
            backgroundImage = ImageIO.read(new File(".\\data\\resources\\background.png"));
            foregroundImage = ImageIO.read(new File(".\\data\\resources\\foreground.png"));
            gameTemplateImage = ImageIO.read(new File(".\\data\\resources\\game template.png"));
            gamePlaceholderImage = ImageIO.read(new File(".\\data\\resources\\game placeholder.png"));
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
                try {
                    Update(delta_time[0]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                delta_time[0] = System.nanoTime()/1e9 - begin_time[0];
                begin_time[0] = System.nanoTime()/1e9;
            }
        }, 0, 40);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                games = Database.GetAllGames();
            }
        }, 0, 5000);
    }


    private void Update(double deltaTime) throws IOException {
        BufferedImage frame = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g.drawImage(backgroundImage.getScaledInstance(getWidth(), getHeight(), 0), 0, 0, null);



        BufferedImage noiseImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random();
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                int v = 128 + random.nextInt(64);

                int rgb = (255 << 24) | (v << 16) | (v << 8) | v;

                noiseImage.setRGB(x, y, rgb);
            }
        }


        //TODO: draw games
        for (int page = -1; page < 2; page++) {
            if (page < 0 || (page /* + current page */) * 12 > games.size()) continue;

            for (int col = 0; col < 4; col++) {
                for (int row = 0; row < 3; row++) {
                    int index = (page /* + current page */) * 12 + row * 4 + col;

                    Reference.Game game = index < games.size() ? games.get(index) : null;

                    float x = 0.1f + 0.2f * col + 0.8f * page;
                    float y = 0.1f + 0.2f * row;

                    Image game_img;

                    if (game != null) {
                        if (!game.images.isEmpty()) {
                            game_img = ImageIO.read(game.images.getFirst());
                        } else {
                            game_img = gamePlaceholderImage;
                        }
                    } else {
                        game_img = noiseImage;
                    }

                    game_img = MultiplyImages(gameTemplateImage, game_img);

                    g.drawImage(game_img.getScaledInstance((int) (getWidth()/5f), (int) (getHeight()/5f), 0), (int) (x * getWidth()), (int) (y * getHeight()), null);
                }
            }
        }



        g.drawImage(foregroundImage.getScaledInstance(getWidth(), getHeight(), 0), 0, 0, null);


        String time_str = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        String date_str = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d/M"));

        g.setColor(new Color(0x222222));
        g.setFont(defaultFont.deriveFont(getHeight() / 10f));
        g.drawString(time_str, (getWidth() + g.getFontMetrics().stringWidth("00:00") - 2 * g.getFontMetrics().stringWidth(time_str)) * 0.5f, getHeight() * 0.8f);

        g.setFont(defaultFont.deriveFont(getHeight() / 15f));
        g.drawString(date_str, (getWidth() - g.getFontMetrics().stringWidth(date_str)) * 0.5f, getHeight() * 0.9f);



        g.dispose();

        viewLabel.setIcon(new ImageIcon(frame));
        setVisible(true);
    }


    private static Image MultiplyImages(Image image1, Image image2) {
        int width = image1.getWidth(null);
        int height = image1.getHeight(null);

        BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img1.createGraphics().drawImage(image1, 0, 0, null);

        BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img2.createGraphics().drawImage(image2.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p1 = img1.getRGB(x, y);
                int p2 = img2.getRGB(x, y);

                int a = ((p1 >> 24) & 0xFF) * ((p2 >> 24) & 0xFF) / 255;
                int r = ((p1 >> 16) & 0xFF) * ((p2 >> 16) & 0xFF) / 255;
                int g = ((p1 >> 8) & 0xFF) * ((p2 >> 8) & 0xFF) / 255;
                int b = (p1 & 0xFF) * (p2 & 0xFF) / 255;

                result.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }
}
