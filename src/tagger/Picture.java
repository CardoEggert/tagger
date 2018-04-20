package tagger;

import javafx.scene.image.Image;

public class Picture {
    private String name;
    private Image img;
    private String tag;

    public Picture(String name, Image img, String tag) {
        this.name = name;
        this.img = img;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
