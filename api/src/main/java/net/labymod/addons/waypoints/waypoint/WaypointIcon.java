/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.waypoints.waypoint;

import it.unimi.dsi.fastutil.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.labymod.api.Textures.SpriteCommon;
import net.labymod.api.client.gfx.pipeline.texture.data.Sprite;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.resources.texture.ThemeTextureLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaypointIcon {

  public static final ThemeTextureLocation SPRITE = ThemeTextureLocation.of("labyswaypoints",
      "markers", 128, 128);
  private static final Map<String, WaypointIcon> SELECTABLE_ICONS = new HashMap<>();
  public static final WaypointIcon DEFAULT = createDefault("default", 0, 0, 9, 14);
  public static final WaypointIcon TEST = getOrCompute(
      "test",
      key -> create(key, SpriteCommon.EXCLAMATION_MARK_DARK)
  );
  public static final WaypointIcon BIG = getOrCompute(
      "big",
      key -> create(key, SpriteCommon.ROBOT)
  );

  private final String identifier;
  private final Icon icon;
  private final Object configData;
  private final boolean builtIn;

  private WaypointIcon(
      @NotNull String identifier,
      @NotNull Icon icon,
      @Nullable Object configData,
      boolean builtIn
  ) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(icon, "icon");
    this.identifier = identifier;
    this.icon = icon;
    this.configData = configData;
    this.builtIn = builtIn;
  }

  protected WaypointIcon(
      @NotNull String identifier,
      @NotNull Icon icon,
      @Nullable Object configData
  ) {
    this(identifier, icon, configData, false);
  }

  private static WaypointIcon createDefault(
      @NotNull String identifier,
      int slotX,
      int slotY,
      int width,
      int height
  ) {
    WaypointIcon waypointIcon = new WaypointIcon(
        identifier,
        Icon.sprite(SPRITE, slotX, slotY, width, height),
        null,
        true
    );

    SELECTABLE_ICONS.put(identifier, waypointIcon);
    return waypointIcon;
  }

  public static @NotNull WaypointIcon get(@Nullable String identifier) {
    if (identifier == null) {
      return DEFAULT;
    }

    WaypointIcon waypointIcon = SELECTABLE_ICONS.get(identifier);
    if (waypointIcon != null) {
      return waypointIcon;
    }

    return new WaypointIcon(identifier, DEFAULT.icon(), null);
  }

  public static @NotNull WaypointIcon getOrCompute(
      @Nullable String identifier,
      @NotNull Function<@NotNull String, @Nullable WaypointIcon> function
  ) {
    if (identifier == null) {
      return DEFAULT;
    }

    Objects.requireNonNull(function, "function");
    return SELECTABLE_ICONS.computeIfAbsent(identifier, key -> {
      WaypointIcon icon = function.apply(key);
      return icon != null ? icon : new WaypointIcon(key, DEFAULT.icon(), null);
    });
  }

  public static @NotNull WaypointIcon createCustom(
      @NotNull String identifier,
      @NotNull String url
  ) {
    return new WaypointIcon(identifier, Icon.url(url), Pair.of("url", url));
  }

  public static @NotNull WaypointIcon createCustom(
      @NotNull String identifier,
      @NotNull ResourceLocation location
  ) {
    return new WaypointIcon(identifier, Icon.texture(location), location);
  }

  public static @NotNull WaypointIcon createUnknown(
      @NotNull String identifier,
      @Nullable Object configData
  ) {
    return new WaypointIcon(identifier, DEFAULT.icon(), configData);
  }

  public static @NotNull WaypointIcon create(
      @NotNull String identifier,
      @NotNull Icon icon
  ) {
    return new WaypointIcon(identifier, icon, null);
  }

  public @NotNull Icon icon() {
    return this.icon;
  }

  public @NotNull String getIdentifier() {
    return this.identifier;
  }

  public @Nullable Object getConfigData() {
    return this.configData;
  }

  public boolean isBuiltIn() {
    return this.builtIn;
  }

  public float getScaledWidth(float height) {
    return this.getWidth() / (this.getHeight() / height);
  }

  public float getScaledHeight(float width) {
    return this.getHeight() / (this.getWidth() / width);
  }

  public float getWidth() {
    Sprite sprite = this.icon.sprite();
    if (sprite.getWidth() == 0.0F) {
      return this.icon.getResolutionWidth();
    }

    return sprite.getWidth();
  }

  public float getHeight() {
    Sprite sprite = this.icon.sprite();
    if (sprite.getHeight() == 0.0F) {
      return this.icon.getResolutionHeight();
    }

    return sprite.getHeight();
  }
}
