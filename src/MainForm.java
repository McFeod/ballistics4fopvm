
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainForm extends Application implements Initializable {

	@FXML private GridPane root;
	private static MainView mainView;
	@FXML private VBox verticalScale;
	@FXML private HBox horizontalScale;
	@FXML private Label speedXLabel;
	@FXML private Label speedYLabel;
	@FXML private Label speedLabel;
	@FXML private Label xLabel;
	@FXML private Label yLabel;
	@FXML private Label timeLabel;
	@FXML private Label angleLabel;
	private boolean isStarted = false;
	private Double mSleepFactor = 0.01;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception{

		root = FXMLLoader.load(getClass().getResource("main_form.fxml"));

		primaryStage.setTitle("Кликни мышкой на поле");
		Scene scene = new Scene(root, 720, 535);
		primaryStage.setScene(scene);
		scene.getStylesheets().add("main_form.css");
		primaryStage.show();
	}

	/*Теперь в наличии 3 функции инициализации.
	* Проблема в том, что обращение к FXML элементам из start() даёт NullPointer,
	* т.к. они ещё не загрузились.
	* А обращение к mainView, определённому в start() из initialize() тоже даёт NullPointer,
	* т.к. initialize вызывается строчкой FXMLLoader.load(getClass().getResource("main_form.fxml"));
	* При этом в неё FXML элементы уже загружены))
	* */

	@FXML @Override
	public void initialize(URL location, ResourceBundle resources) {
		Canvas packetView = new Canvas(512, 512 + 2);
		mainView = new MainView(packetView, 512, 512 + 2, mSleepFactor);
		mainView.setRefreshableObjects(speedXLabel, speedYLabel, speedLabel, xLabel, yLabel, timeLabel,
				angleLabel);
		root.add(mainView, 1, 0);
		root.add(packetView, 1, 0);
		verticalScale.setAlignment(Pos.BASELINE_RIGHT);
		verticalScale.setSpacing(34);
		buildVerticalScale(verticalScale, mainView.getHeight());
		buildHorizontalScale(horizontalScale, mainView.getWidth());
		packetView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!isStarted) {
					mainView.setGoal(new Point2D(event.getX() * mainView.getScale(),
							(mainView.getHeight() - event.getY()) * mainView.getScale()));
					mainView.setAngleBisectionEnabled(true);
					VisualizationThread thread = new VisualizationThread();
					thread.start(mSleepFactor, mainView);
					isStarted = true;
				}
			}
		});
	}

	/*Из start() и initialize() нельзя получить width и height() элементов,
	* т.к. они ещё не созданы. Даже из слушателя на WindowEvent.OnShown нельзя.
	* */

	@FXML
	public void repaintBackground(){
		System.out.println(verticalScale.getHeight());
		System.out.println(horizontalScale.getWidth());
		System.out.println(mainView.getScale());
		mainView.fillBackground();
	}

	/*Между label-ами внутри vbox остаются зазоры, которые не удаётся отключить.
	* Зависимость высоты зазора от высоты шрифта:
	* Шрифт   10   13   15   20   25   30
	* Зазор    4    3    4    5    7    6
	* Т.к. алгебра здесь бессильна, подогнал шрифт и spacing таким образом,
	* чтобы расстояния между делениями были по 50.
	* */

	private void buildVerticalScale(Pane pane, double markLimit){
		int scaleMark = (int)Math.round(mainView.getScale()) * 50;
		int currentMark = scaleMark * (int)(markLimit / 50);
		while(currentMark >= 0){
			Label lbl = new Label(Integer.toString(currentMark));
			lbl.getStyleClass().add("scaleLabel");
			pane.getChildren().add(lbl);
			currentMark-=scaleMark;
		}
	}

	private void buildHorizontalScale(Pane pane, double markLimit){
		int scaleMark = (int)Math.round(mainView.getScale()) * 50;
		int maxMark = scaleMark * (int)(markLimit / 50);
		int currentMark = 0;
		while(currentMark < maxMark){
			Label line = new Label("|");
			line.getStyleClass().add("scaleMark");
			pane.getChildren().add(line);

			Label lbl = new Label(Integer.toString(currentMark));
			lbl.getStyleClass().add("scaleLabel");
			lbl.setMinWidth(45);// опять подгон
			pane.getChildren().add(lbl);

			currentMark+=scaleMark;
		}
	}
}
