package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

/** Frameset of frames nested two levels deep: frame-top (left, middle, right) and frame-bottom. */
public class NestedFramesPage extends TheInternetPage {

    private static final By BODY = By.tagName("body");
    private static final By FRAMES = By.tagName("frame");

    public NestedFramesPage(WebDriver driver) {
        super(driver);
    }

    public NestedFramesPage open() {
        openPath("/nested_frames");
        return this;
    }

    /** Text of the frame reached through the given frame names, outermost first. */
    public String textOfFrame(List<String> framePath) {
        By[] path = framePath.stream().map(By::name).toArray(By[]::new);
        return inFrame(() -> textOf(BODY), path);
    }

    /** Number of frames of the top-level document: proves the driver came back to it. */
    public int topLevelFrameCount() {
        return driver.findElements(FRAMES).size();
    }
}
