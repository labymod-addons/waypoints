package net.labymod.addons.waypoints.core.activity.widgets;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;

import java.util.function.Consumer;

@AutoWidget
public class NumberTextFieldWidget extends TextFieldWidget {

  private int value = 0;
  private IntConsumer onUpdate;

  public NumberTextFieldWidget() {
    super();

    this.qualifiedName = "TextField";
    this.name = "TextField";

    this.setValue(this.value);
    this.updateListener = text -> {
      int newValue = 0;
      if (!text.isEmpty() && !text.equals("-")) {
        try {
          newValue = Integer.parseInt(text);
        } catch (NumberFormatException e) {
          newValue = this.value;
        }
      }

      if (newValue != this.value) {
        this.value = newValue;
        if (this.onUpdate != null) {
          this.onUpdate.accept(this.value);
        }
      }
    };

    this.validator(text -> {
      if (text.isEmpty() || text.equals("-")) {
        return true;
      }

      try {
        Integer.parseInt(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    });
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.isFocused() && (super.text.isEmpty() || super.text.equals("-"))) {
      this.setValue(0);
    }
  }

  @Override
  public boolean mouseScrolled(MutableMouse mouse, double scrollDelta) {
    if (this.isFocused()) {
      if (scrollDelta > 0) {
        this.setValue(this.getValue() + 1);
      } else {
        this.setValue(this.getValue() - 1);
      }

      return true;
    }

    return super.mouseScrolled(mouse, scrollDelta);
  }

  public int getValue() {
    return this.value;
  }

  public void setValue(int value) {
    super.setText(String.valueOf(value));
    super.setCursorAtEnd();
    super.viewIndex = 0;
  }

  /**
   * @deprecated Use {@link #setValue(int)} instead
   */
  @Override
  @Deprecated
  public void setText(String text) {
    super.setText(text);
  }

  /**
   * @deprecated Use {@link #onUpdate(IntConsumer)} instead
   */
  @Override
  @Deprecated
  public TextFieldWidget updateListener(Consumer<String> updateListener) {
    return super.updateListener(updateListener);
  }

  public NumberTextFieldWidget onUpdate(IntConsumer onUpdate) {
    this.onUpdate = onUpdate;
    return this;
  }
}
