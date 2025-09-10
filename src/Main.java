import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Login - Sistema Diabete");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /*@Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader1 = new FXMLLoader(getClass().getResource("view/LoginView.fxml"));
        Stage stage1 = new Stage();
        stage1.setScene(new Scene(loader1.load()));
        stage1.setTitle("Utente 1");
        stage1.show();

        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("view/LoginView.fxml"));
        Stage stage2 = new Stage();
        stage2.setScene(new Scene(loader2.load()));
        stage2.setTitle("Utente 2");
        stage2.show();
    }*/


    public static void main(String[] args) {
        launch(args);
    }
}
