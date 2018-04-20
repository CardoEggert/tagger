package tagger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Comparator<Picture> {

    private static final String PIC_PATH = "D:\\baka\\uuedAndmed\\2_days_data - Copy (3)\\"; // <-- location of pictures
    private static final String PATH = "D:\\baka\\taggerResults\\"; // <-- location of results
    private static final int PIC_PER_ROW = 7; // <-- number of columns
    private static final int HOW_MANY_ROWS = 2; // <-- number of rows
    // Attribute radiobuttons
    @FXML
    RadioButton stairsRB;
    @FXML
    RadioButton nullRB;
    @FXML
    RadioButton walkWithLoadRB;
    @FXML
    RadioButton walkRB;
    @FXML
    RadioButton bendingRB;
    @FXML
    RadioButton kneelingRB;
    @FXML
    RadioButton lyingRB;
    @FXML
    RadioButton sittingRB;
    @FXML
    RadioButton standingRB;


    @FXML
    Button submit;
    @FXML
    Button nopeButton;
    @FXML
    Button selectAllButton;
    @FXML
    Button deSelectAllButton;
    @FXML
    private VBox biggestParent;

    private ToggleGroup attrToggleGroup;
    private Stack<File> files;

    @FXML
    public void initialize() {
        files = new Stack<>();
        files.addAll(Arrays.asList(new File(PIC_PATH).listFiles()));
        for (int i = 0; i < HOW_MANY_ROWS; i++) {
            biggestParent.getChildren().add(i, create_HBox_with_x_pics(PIC_PER_ROW));
        }
        // Init togglegroup for attributes
        attrToggleGroup = new ToggleGroup();
        stairsRB.setToggleGroup(attrToggleGroup);
        stairsRB.setUserData("stairs");
        walkWithLoadRB.setToggleGroup(attrToggleGroup);
        walkWithLoadRB.setUserData("walk_with_load");
        walkRB.setToggleGroup(attrToggleGroup);
        walkRB.setUserData("walk");
        bendingRB.setToggleGroup(attrToggleGroup);
        bendingRB.setUserData("bending");
        kneelingRB.setToggleGroup(attrToggleGroup);
        kneelingRB.setUserData("kneeling");
        lyingRB.setToggleGroup(attrToggleGroup);
        lyingRB.setUserData("lying");
        sittingRB.setToggleGroup(attrToggleGroup);
        sittingRB.setUserData("sitting");
        standingRB.setToggleGroup(attrToggleGroup);
        standingRB.setUserData("standing");
        nullRB.setToggleGroup(attrToggleGroup);
        nullRB.setUserData("nothing");
        attrToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (attrToggleGroup.getSelectedToggle() != null) {
                    // Do something here with the userData of newly selected radioButton
                    for (int i = 0; i < HOW_MANY_ROWS; i++) {
                        markAllSelected(attrToggleGroup.getSelectedToggle().getUserData().toString(), ((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()));
                    }
                    attrToggleGroup.selectToggle(null);
                }
            }
        });
        refreshAllInstanceCounts();
        sortPics();
        submit.setOnAction((event) -> {
            // Button was clicked, do something...
            List<VBox> pics = new ArrayList<>(HOW_MANY_ROWS * PIC_PER_ROW);
            for (int i = 0; i < HOW_MANY_ROWS; i++) {
                pics.addAll(((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()));
            }
            moveFile(pics);
            markAllSelectedFalse();
            refreshAllInstanceCounts();
            sortPics();
        });
        nopeButton.setOnAction((event) -> {
            // Button was clicked, do something...
            for (int i = 0; i < HOW_MANY_ROWS; i++) {
                changeAllPics(((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()), PIC_PER_ROW);
                markAllSelectedFalse(i);
            }
            refreshAllInstanceCounts();
            sortPics();
        });
        selectAllButton.setOnAction((event) -> {
            // Button was clicked, do something...
            for (int i = 0; i < HOW_MANY_ROWS; i++) {
                markAllSelectedTrue(i);
            }
        });
        deSelectAllButton.setOnAction((event) -> {
            // Button was clicked, do something...
            for (int i = 0; i < HOW_MANY_ROWS; i++) {
                markAllSelectedFalse(i);
            }
        });
    }

    private void markAllSelectedFalse() {
        for (int i = 0; i < HOW_MANY_ROWS; i++) {
            markAllSelectedFalse(i);
        }
    }

    private void markAllSelectedFalse(int i) {
        markAllSelected(((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()), false);
    }

    private void markAllSelectedTrue(int i) {
        markAllSelected(((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()), true);
    }

    private void markAllSelected(List<VBox> pics, boolean selectVal) {
        for (VBox pic : pics ) {
            CheckBox cb = ((CheckBox) pic.getChildren().get(2));
            cb.setSelected(selectVal);
        }
    }

    private void markAllSelected(String markVal, List<VBox> pics){
        for (VBox pic : pics ) {
            CheckBox cb = ((CheckBox) pic.getChildren().get(2));
            if (cb.isSelected()) {
                cb.setText(markVal);
            }
        }
    }

    private void changeAllPics(List<VBox> pics, int nrOfPics) {
        List<File> temp = new ArrayList<>(nrOfPics);
        for (int i = 0; i < nrOfPics; i++) {
            temp.add(files.pop());
        }
        Collections.reverse(temp);
        int i = 0;
        for (VBox pic : pics) {
            Text picText = (Text) pic.getChildren().get(0);
            ImageView picImg = (ImageView) pic.getChildren().get(1);
            CheckBox picCB = (CheckBox) pic.getChildren().get(2);
            picImg.setImage(new Image(temp.get(i).toURI().toString()));
            picText.setText(temp.get(i).getName());
            picCB.setText("Not yet tagged");
            i++;
        }
    }

    private HBox create_HBox_with_x_pics(int id) {
        HBox newHbox = new HBox();
        newHbox.setId("picHBox" + id + System.currentTimeMillis());
        List<VBox> pictures = new ArrayList<>(PIC_PER_ROW);
        for (int i = 0; i< PIC_PER_ROW; i++) {
            pictures.add(create_pic(i));
        }
        for (int i = PIC_PER_ROW -1; i >= 0; i--) {
            newHbox.getChildren().add(pictures.get(i));
        }
        return newHbox;
    }

    private VBox create_pic(int id) {
        VBox picBox = new VBox();
        picBox.setId("picBox" + id + System.currentTimeMillis());
        File temp = files.pop();
        picBox.getChildren().add(createText(id, temp));
        picBox.getChildren().add(createImageview(id, temp));
        picBox.getChildren().add(createCheckbox(id));
        return picBox;
    }

    private Text createText(int id, File temp) {
        Text txt = new Text(temp.getName());
        txt.setId("pic" + id + "txt" + System.currentTimeMillis());
        return txt;
    }

    private CheckBox createCheckbox(int id) {
        CheckBox cb = new CheckBox("Not tagged yet");
        cb.setId("pic"+id+"cb" + System.currentTimeMillis());
        return cb;
    }

    private ImageView createImageview(int id, File temp) {
        ImageView img = new ImageView();
        img.setImage(new Image(temp.toURI().toString()));
        img.setFitWidth(200.0);
        img.setFitHeight(150.0);
        img.setId("pic"+id + System.currentTimeMillis());
        return img;
    }

    private void moveFile(List<VBox> pics) {
        List<VBox> movedPics = pics.stream().filter(x -> ((CheckBox) (x.getChildren().get(2))).isSelected()).collect(Collectors.toList());
        List<Text> picText = movedPics.stream().map(x -> ((Text) x.getChildren().get(0))).collect(Collectors.toList());
        List<ImageView> arrOfImageView = movedPics.stream().map(x -> ((ImageView) x.getChildren().get(1))).collect(Collectors.toList());
        List<CheckBox> checkBoxes = movedPics.stream().map(x -> ((CheckBox) x.getChildren().get(2))).collect(Collectors.toList());
        // Refrence : https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java
        if (!checkBoxes.isEmpty()) {
            File theDir = new File(PATH + checkBoxes.get(0).getText());

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                System.out.println("creating directory: " + theDir.getName());
                boolean result = false;

                try{
                    theDir.mkdir();
                    result = true;
                }
                catch(SecurityException se){
                    //handle it
                }
                if(result) {
                    System.out.println("DIR created");
                }
            }
            // give the series of pictures a name
            theDir = new File(PATH + checkBoxes.get(0).getText() + "\\" +  picText.get(0).getText().substring(0, 41));
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                System.out.println("creating directory: " + theDir.getName());
                boolean result = false;

                try{
                    theDir.mkdir();
                    result = true;
                }
                catch(SecurityException se){
                    //handle it
                }
                if(result) {
                    System.out.println("DIR created");
                }
            }
            for (Text picTxt : picText) {
                try {
                    Files.move(Paths.get(PIC_PATH + picTxt.getText()), Paths.get(PATH + checkBoxes.get(0).getText() + "\\" + picText.get(0).getText().substring(0, 41) + "\\" + picTxt.getText()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("Moving failed from " + picTxt.getText() + " to " + theDir.getAbsolutePath() + ". ");
                }
            }
            int indx = 0;
            for (ImageView pic  : arrOfImageView) {
                File temp = files.pop();
                Image replace = new Image(temp.toURI().toString());
                pic.setImage(replace);
                picText.get(indx).setText(temp.getName());
                checkBoxes.get(indx).setText("Not tagged yet");
                indx++;
            }
        }
    }

    private int countInstances(String instance) {
        File file = new File(PATH + instance);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return directories != null ? directories.length : 0;
    }

    private void refreshInstanceCount(RadioButton btn, String instance) {
        String txt = btn.getUserData().toString().concat("(" + countInstances(instance) + ")");
        btn.setText(txt);
    }

    private void refreshAllInstanceCounts() {
        refreshInstanceCount(stairsRB, "stairs");
        refreshInstanceCount(walkWithLoadRB, "walk_with_load");
        refreshInstanceCount(walkRB, "walk");
        refreshInstanceCount(bendingRB, "bending");
        refreshInstanceCount(kneelingRB, "kneeling");
        refreshInstanceCount(lyingRB, "lying");
        refreshInstanceCount(sittingRB, "sitting");
        refreshInstanceCount(standingRB, "standing");
        refreshInstanceCount(nullRB, "nothing");
    }

    private void sortPics() {
        List<VBox> pics = new ArrayList<>(PIC_PER_ROW * HOW_MANY_ROWS);
        for (int i = 0; i < HOW_MANY_ROWS; i++) {
            pics.addAll(((HBox) biggestParent.getChildren().get(i)).getChildren().stream().map(x -> ((VBox) x)).collect(Collectors.toList()));
        }
        List<Picture> sortablePics = new ArrayList<>(PIC_PER_ROW * HOW_MANY_ROWS);
        for (VBox pic : pics) {
            sortablePics.add(new Picture(((Text) pic.getChildren().get(0)).getText(), ((ImageView) pic.getChildren().get(1)).getImage(), ((CheckBox) pic.getChildren().get(2)).getText()));
        }
        sortablePics.sort(new Controller());
        for (int i = 0; i <HOW_MANY_ROWS; i++) {
            for (int j = 0; j < PIC_PER_ROW; j++) {
                Picture replaceWith = sortablePics.get(PIC_PER_ROW * i + j);
                ((Text) ((VBox) ((HBox) biggestParent.getChildren().get(i)).getChildren().get(j)).getChildren().get(0)).setText(replaceWith.getName());
                ((ImageView) ((VBox) ((HBox) biggestParent.getChildren().get(i)).getChildren().get(j)).getChildren().get(1)).setImage(replaceWith.getImg());
                ((CheckBox) ((VBox) ((HBox) biggestParent.getChildren().get(i)).getChildren().get(j)).getChildren().get(2)).setText(replaceWith.getTag());
            }
        }
    }

    @Override
    public int compare(Picture o1, Picture o2) {
        LocalDateTime time1 = extractTime(o1.getName());
        LocalDateTime time2 = extractTime(o2.getName());
        if (time1.isAfter(time2)) {
            return 1;
        } else if (time1.isBefore(time2)) {
            return -1;
        } else {
            return 0;
        }
    }

    private LocalDateTime extractTime(String name) {
        // I had generated the files with timestamp, so I could sort them according to time
        // silhouette_2017-05-01T18_53_54.179Zmarked
        // 0123456789012345678901234567890123456789
        // 11-35
        String date = name.substring(11, 35);
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSSz"));
    }


}
