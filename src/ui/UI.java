package ui;

import Util.Reference;
import database.Database;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Future;

public class UI extends JFrame {
    public static class UILayout {
        public final Point TimePosition;
        public final float TimeSize;
        public final Point DatePosition;
        public final float DateSize;
        public final Rectangle PageSize;
        public final int PageRows;
        public final int PageColumns;

        public UILayout(Point timePosition, float timeSize, Point datePosition, float dateSize, Rectangle pageSize, int pageRows, int pageColumns) {
            TimePosition = timePosition;
            TimeSize = timeSize;
            DatePosition = datePosition;
            DateSize = dateSize;
            PageSize = pageSize;
            PageRows = pageRows;
            PageColumns = pageColumns;
        }
    }

    private static final JLabel viewLabel = new JLabel();

    private static final Font defaultFont = new Font("Consolas", Font.BOLD, 28);

    private static final Image backgroundImage;
    private static final Image foregroundImage;
    private static final Image gameTemplateImage;
    private static final Image gamePlaceholderImage;


    private static UILayout layout;


    private static List<Future<?>> futures = new ArrayList<>();


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

        layout = Reference.gson.fromJson(new FileReader(".\\data\\resources\\layout.json"), UILayout.class);

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
        for (int i = 0; i < 4; i++) {
            int start = 16 * i;
            int end = start + 16;

            futures.add(Reference.executor.submit(() -> {
                for (int x = start; x < end; x++) {
                    for (int y = 0; y < 64; y++) {
                        int v = 128 + random.nextInt(64);

                        int rgb = (255 << 24) | (v << 16) | (v << 8) | v;

                        noiseImage.setRGB(x, y, rgb);
                    }
                }
            }));
        }

        for (Future<?> future : futures){
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        futures.clear();


        //TODO: draw games
        for (int col = 0; col < layout.PageColumns; col++) {
            for (int row = 0; row < layout.PageRows; row++) {
                int index = (0 /* current page */) * 12 + row * 4 + col;
                int _row = row;
                int _col = col;
                int _page = 0;

                futures.add(Reference.executor.submit(() -> {
                    Reference.Game game = index < games.size() ? games.get(index) : null;

                    float x = (layout.PageSize.x + (float) layout.PageSize.width / layout.PageColumns * _col + layout.PageSize.width * _page) / 100f;
                    float y = (layout.PageSize.y + (float) layout.PageSize.height / layout.PageRows * _row) / 100f;

                    Image game_img;

                    if (game != null) {
                        if (!game.images.isEmpty()) {
                            try {
                                game_img = ImageIO.read(game.images.getFirst());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            game_img = gamePlaceholderImage;
                        }
                    } else {
                        game_img = noiseImage;
                    }

                    game_img = MultiplyImages(gameTemplateImage, game_img);

                    g.drawImage(game_img.getScaledInstance((int) ((float) (getWidth() * layout.PageSize.width)/(100 * layout.PageColumns)), (int) ((float) (getHeight() * layout.PageSize.height)/(100 * layout.PageRows)), 0), (int) (x * getWidth()), (int) (y * getHeight()), null);
                }));
            }
        }

        for (Future<?> future : futures){
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        futures.clear();



        g.drawImage(foregroundImage.getScaledInstance(getWidth(), getHeight(), 0), 0, 0, null);


        String time_str = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        String date_str = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d/M"));

        g.setColor(new Color(0x222222));
        g.setFont(defaultFont.deriveFont(getHeight() / layout.TimeSize));
        g.drawString(time_str, (getWidth() + g.getFontMetrics().stringWidth("00:00") - 2 * g.getFontMetrics().stringWidth(time_str)) * layout.TimePosition.x / 100f, getHeight() * layout.TimePosition.y / 100f);

        g.setFont(defaultFont.deriveFont(getHeight() / layout.DateSize));
        g.drawString(date_str, (getWidth() - g.getFontMetrics().stringWidth(date_str)) * layout.DatePosition.x / 100f, getHeight() * layout.DatePosition.y / 100f);



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
