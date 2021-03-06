package PlanerApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;



public class Main extends Application{
/*Реализация всплывающего окна-будильника. Из фонового асинхронного потока для остлеживания оповещений меняется статичная перменная-флаг
* isAlertNow. Animationtimer все время отслеживает значение этой переменной, и как только оно становится true, вызывается метод,
* который запускает звук и показывает диалоговое окно. Поскольку в animationtimer нельзя вызвать showAndWait
* (его можно вызывать только в обработчике событий), то animationtimer запускает метод showandwait через runLater
* (runlater запускает fx поток через некоторое неопределеное время), что позволяет сначала отключить таймер,
*  а потом вызвать диалоговое окно */
//    private static Alert alertAlarm = new Alert(Alert.AlertType.CONFIRMATION);
    public static Path PATH;
    private static Pane root;
    private static Stage stageLocal;
    public static TimeLineController timeLine1;
    private static Sound alarm;
    public static    SystemTray tray;
    public static java.awt.Image planerIcon;
    public static    TrayIcon trayIcon;
    private static MenuItem menuClose =new MenuItem("Exit");
    private static MenuItem menuOpen =new MenuItem("Open");
    private static PopupMenu popupMenu = new PopupMenu();

    public static void showAlarmDialog(String s, Alert alertAlarm){

        alertAlarm.setHeaderText(s);
        alertAlarm.setContentText("Alarm now");
        alertAlarm.setTitle("Alarm!");
        alarm.stop();
        alarm.play();
        Optional<ButtonType> result = alertAlarm.showAndWait();
        if (result.get() == ButtonType.OK){
            alarm.stop();
            alertAlarm.showAndWait();
        }else if(result.get()==ButtonType.CANCEL){
            alarm.stop();
            alertAlarm.close();
        }
    }

    private static Alert alertClose;
    private static void showCloseDialog(){
        alertClose.setHeaderText("Hide or exit?");
        alertClose.setContentText("Do you want to hide the application instead of exiting it?");
        alertClose.setTitle("Exit?");

        ButtonType hide = new ButtonType("Hide", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType exit = new ButtonType("Exit", ButtonBar.ButtonData.NO);
        System.out.println(alertClose.getButtonTypes());
        alertClose.getButtonTypes().clear();
        alertClose.getButtonTypes().addAll(hide,exit);
        System.out.println(alertClose.getButtonTypes());
        Optional<ButtonType> result = alertClose.showAndWait();


            if (result.get() == hide ) {
                stageLocal.hide();
                System.out.println("result hide");
            }else if (result.get() == exit) {
                exitApp();
                System.out.println("result hide");

    }}




    private static void exitApp(){
        SystemTray.getSystemTray().remove(trayIcon);
        Plan.save();
        Plan.closePool();
        Platform.exit();
        System.exit(0);
    }
    private static void showApp(){
        stageLocal.show();
    }

    public static void trayNote(){
        if (SystemTray.isSupported()){
            try {
                tray = SystemTray.getSystemTray();

                Path trayIconPath = Path.of(PATH + "\\src\\trayIcon.png");
                try {
                    planerIcon = ImageIO.read(trayIconPath.toFile());
                }catch (IOException e){System.out.println(e + " thisistray " + trayIconPath.toAbsolutePath().toString());}

                trayIcon = new TrayIcon(planerIcon, "your Daily Planer");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("A daily planer notification");
                tray.add(trayIcon);
                trayIcon.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showApp();
                            }
                        });
                    }
                });
            }
            catch (Exception err){System.err.println(err); }}
        menuClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        exitApp();
                    }
                });
            }
        });
        menuOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showApp();
                    }
                });
            }
        });
        popupMenu.add(menuClose);
        popupMenu.addSeparator();
        popupMenu.add(menuOpen);
        trayIcon.setPopupMenu(popupMenu);
    }

    @Override
    public void start(Stage stage) {
        System.out.println();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainUI.fxml"));
        timeLine1 = new TimeLineController();
        try{
            root = (Pane) loader.load();}
        catch (IOException e){System.out.println(e.getCause());}
        alertClose = new Alert(Alert.AlertType.CONFIRMATION);

        timeLine1.setSize((int)root.getPrefHeight());
        root.getChildren().add(timeLine1);
        Scene sceneTest = new Scene(root);
        stage.setScene(sceneTest);
        stage.setTitle("DailyPlanner");
        stage.setResizable(false);
        stage.show();
        Plan.load();
        Plan.launchPoolNotes();
        Plan.deleteOldData();
        trayNote();
        ControllerClass.updateFilteredKeysList();
        ControllerClass.updateFilteredNoteList();
        stageLocal = stage;
        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                showCloseDialog();
            }
        });

        alarm = new Sound(Path.of(PATH + "\\src\\Alarm.wav").toFile());

    }

    public static void main(String[] args) throws Exception{
        System.out.println("MainYes");
        App a = new App();
        PATH = a.getPath();

        System.out.println("TO  URI = " + a.getPath());
        Application.launch(args);



}

}

