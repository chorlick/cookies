/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cookies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author chorlick
 */
public class Cookies extends Application {

    private Scene scene;
    private Browser b;
    private boolean clicking;
    private Timer timer;
    private ScheduledExecutorService click_scheduler;
    private ScheduledExecutorService store_buy_scheduler;
    private ScheduledExecutorService product_buy_scheduler;
    private Button save_game;
    private Button load_game;
    private Button gold_cookie;
    private ToggleButton auto_click;
    private ToggleButton auto_store_buy;
    private ToggleButton auto_product_buy;
    private Vector<String> store_upgrades;
    
    public void init(Stage stage) {
        b = new Browser();
        store_upgrades = new Vector<String>();
        clicking = false;
        BorderPane pane = new BorderPane();
        HBox hbox = new HBox();
        save_game = new Button("Save Game");
        load_game = new Button("Load Game");
        gold_cookie = new Button("Gold Cookie");
        auto_store_buy = new ToggleButton("Auto Store Buy");
        auto_click = new ToggleButton("Auto Click");
        auto_product_buy = new ToggleButton("Auto Product Buy");

        auto_product_buy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (auto_product_buy.isSelected()) {
                    try {
                        auto_product_buy_start();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    auto_product_buy_stop();
                }
                System.out.printf("Auto product Buy Pressed(%b)\n", auto_click.isSelected());
            }
        });

        auto_store_buy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (auto_store_buy.isSelected()) {
                    try {
                        auto_store_buy_start();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    auto_store_buy_stop();
                }
                System.out.printf("Auto Store Buy Pressed(%b)\n", auto_click.isSelected());
            }
        });

        auto_click.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (auto_click.isSelected()) {
                    try {
                        auto_clicker_start();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    auto_clicker_stop();
                }
                System.out.printf("AutoClick Pressed(%b)\n", auto_click.isSelected());
            }
        });

        gold_cookie.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                gold_cookie_handler();
                System.out.printf("Gold Cookie Pressed\n");
            }
        });

        save_game.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                save_game_handler();
                System.out.printf("Save Pressed\n");
            }
        });

        load_game.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                load_game_handler();
                System.out.printf("Load Game Pressed\n");
            }
        });

        scene = new Scene(pane, 1024, 500, Color.web("#666970"));
        pane.setCenter(b);
        pane.setBottom(hbox);
        hbox.getChildren().add(auto_click);
        hbox.getChildren().add(auto_store_buy);
        hbox.getChildren().add(auto_product_buy);
        hbox.getChildren().add(save_game);
        hbox.getChildren().add(load_game);
        hbox.getChildren().add(gold_cookie);
        stage.setScene(scene);
    }

    public void gold_cookie_handler() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                b.webEngine.executeScript("Game.goldenCookie.spawn();");
            }
        });
    }

    public void load_game_handler() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String saveinfo;
                    File pfile = new File("C:\\Users\\chorlick\\cookies.txt");
                    try (BufferedReader reader = new BufferedReader(new FileReader(pfile))) {
                        saveinfo = reader.readLine();
                        System.out.printf("Save line %s\n", saveinfo);
                        b.webEngine.executeScript("Game.LoadSave( \"" + saveinfo + "\");");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void save_game_handler() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    File pfile = new File("C:\\Users\\chorlick\\cookies.txt");
                    pfile.delete();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(pfile))) {
                        writer.write((String) b.webEngine.executeScript("Game.WriteSave(1);"));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    public void auto_clicker_stop() {
        click_scheduler.shutdown();
    }

    public void auto_product_buy_stop() {
        product_buy_scheduler.shutdown();
    }

    private void auto_store_buy_stop() {
        store_buy_scheduler.shutdown();
    }

    public void auto_store_buy_start() throws InterruptedException {
        store_buy_scheduler = Executors.newScheduledThreadPool(1);
        store_buy_scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.printf("Starting store scan\n");
                        Document doc = b.webEngine.getDocument();
                        Element upgrades = doc.getElementById("upgrades");
                        NodeList store_item_list = upgrades.getChildNodes();
                        for (int i = 0; i < store_item_list.getLength(); i++) {
                            //System.out.printf("Store item class %s \n", store_item_list.item(i).getClass());
                            Element nodes = (Element) store_item_list.item(i);
                            if (nodes.getAttribute("class").contains("enabled")) {
                                store_upgrades.add(nodes.getAttribute("onclick"));
                                System.out.printf("Buying(store) %s\n", nodes.getAttribute("onclick"));
                                b.webEngine.executeScript(nodes.getAttribute("onclick"));
                            }
                        }
                    }
                });
            }
        }, 1, 2500, TimeUnit.MILLISECONDS);
    }

    public void auto_product_buy_start() throws InterruptedException {
        product_buy_scheduler = Executors.newScheduledThreadPool(1);
        product_buy_scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.printf("Starting product scan\n");
                        Document doc = b.webEngine.getDocument();
                        Element upgrades = doc.getElementById("products");
                        NodeList store_item_list = upgrades.getChildNodes();
                        for (int i = 0; i < store_item_list.getLength(); i++) {
                            //System.out.printf("Store item class %s \n", store_item_list.item(i).getClass());
                            Element nodes = (Element) store_item_list.item(i);
                            if (nodes.getAttribute("class").contains("enabled")) {
                                store_upgrades.add(nodes.getAttribute("onclick"));
                                System.out.printf("Buying(product) %s\n", nodes.getAttribute("onclick"));
                                b.webEngine.executeScript(nodes.getAttribute("onclick"));

                            }
                        }
                    }
                });
            }
        }, 1, 2500, TimeUnit.MILLISECONDS);
    }

    public void auto_clicker_start() throws InterruptedException {
        click_scheduler = Executors.newScheduledThreadPool(1);
        click_scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        b.webEngine.executeScript("Game.ClickCookie();");
                    }
                });
            }
        }, 1, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start(Stage stage) throws Exception {
        init(stage);
        stage.setTitle("Web View");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
