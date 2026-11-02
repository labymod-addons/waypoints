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
package net.labymod.addons.waypoints.core.share;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.activity.popup.ManageWaypointSimplePopup;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.labyconnect.share.ChatSharePreview;
import net.labymod.api.labyconnect.share.ChatShareable;
import net.labymod.api.util.Color;
import net.labymod.api.util.I18n;
import net.labymod.api.util.logging.Logging;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single waypoint as something to share in the Laby Chat. There is no current state to share,
 * so it is only sent from the waypoint list. A received waypoint is always added as a new one and
 * keeps its server or world, so it only shows up there.
 */
public final class WaypointShareable implements ChatShareable<SharedWaypoint> {

  public static final String KIND = Waypoints.NAMESPACE + ":waypoint";

  private static final String LABEL = "Waypoint";
  private static final Logging LOGGER = Logging.create(WaypointShareable.class);

  private static @Nullable WaypointShareable instance;

  /**
   * Registers the kind with the Laby Chat. Only called through {@link WaypointChatShare}, which
   * makes sure the share API exists.
   */
  static void register() {
    if (instance != null) {
      return;
    }

    WaypointShareable created = new WaypointShareable();
    Laby.references().chatShareRegistry().register(created);
    instance = created;
  }

  static boolean canShare(@NotNull WaypointMeta meta) {
    return instance != null && toShared(meta) != null;
  }

  /**
   * Lets the player pick a friend and sends them the waypoint.
   */
  static void share(@NotNull WaypointMeta meta) {
    WaypointShareable kind = instance;
    SharedWaypoint value = toShared(meta);
    if (kind != null && value != null) {
      Laby.references().chatShareRegistry().share(kind, value);
    }
  }

  @Override
  public @NotNull String kind() {
    return KIND;
  }

  @Override
  public @NotNull String label() {
    return LABEL;
  }

  @Override
  public @NotNull Component displayName() {
    return Component.translatable("labyswaypoints.share.name");
  }

  @Override
  public @NotNull Icon icon() {
    return WaypointIcon.DEFAULT;
  }

  /**
   * @return {@code null}, every waypoint is shared on its own from the waypoint list
   */
  @Override
  public @Nullable SharedWaypoint export() {
    return null;
  }

  @Override
  public @NotNull JsonObject write(@NotNull SharedWaypoint value) {
    return value.write();
  }

  @Override
  public @Nullable SharedWaypoint read(@NotNull JsonObject data) {
    return SharedWaypoint.read(data);
  }

  @Override
  public @NotNull String title(@NotNull SharedWaypoint value) {
    return value.title();
  }

  /**
   * The icon of the waypoint at its native size, the default marker in the waypoint's color like
   * in the world.
   */
  @Override
  public @Nullable ChatSharePreview preview(@NotNull SharedWaypoint value) {
    String path = value.iconPath() == null ? defaultIconPath() : value.iconPath();
    if (path == null) {
      return null;
    }

    BufferedImage image;
    try (InputStream stream = WaypointShareable.class.getResourceAsStream(
        "/assets/" + Waypoints.NAMESPACE + "/" + path)) {
      // An icon this addon version does not have; the receiver shows the default one
      if (stream == null) {
        return null;
      }

      image = ImageIO.read(stream);
    } catch (IOException exception) {
      LOGGER.warn("Could not read the waypoint icon " + path, exception);
      return null;
    }

    if (image == null
        || image.getWidth() > ChatSharePreview.MAX_SIZE
        || image.getHeight() > ChatSharePreview.MAX_SIZE) {
      return null;
    }

    boolean tinted = path.equals(defaultIconPath());
    int width = image.getWidth();
    int height = image.getHeight();
    int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = image.getRGB(x, y);
        pixels[y * width + x] = tinted ? tint(argb, value.color()) : argb;
      }
    }

    return ChatSharePreview.pixelArt(width, height, pixels);
  }

  /**
   * Adds the received waypoint as a new one, it never replaces one the player has.
   */
  @Override
  public void apply(@NotNull SharedWaypoint value) {
    WaypointService service = Waypoints.references().waypointService();
    service.add(toMeta(value));
    service.refresh();
  }

  @Override
  public boolean canView() {
    return true;
  }

  /**
   * Opens the received waypoint in the editor, unsaved. Saving there adds it as a new one.
   */
  @Override
  public void view(@NotNull SharedWaypoint value) {
    ManageWaypointSimplePopup.received(toMeta(value)).displayInOverlay();
  }

  /**
   * A waypoint of the player as it is shared.
   *
   * @return the waypoint, or {@code null} if it cannot be shared
   * @see SharedWaypoint#of
   */
  public static @Nullable SharedWaypoint toShared(@NotNull WaypointMeta meta) {
    ResourceLocation icon = meta.icon().getResourceLocation();
    DoubleVector3 location = meta.location();
    return SharedWaypoint.of(
        PlainTextComponentSerializer.plainText().serialize(meta.title()),
        defaultName(),
        meta.color().get(),
        meta.type(),
        location.getX(),
        location.getY(),
        location.getZ(),
        meta.contextType(),
        meta.getContext(),
        icon == null ? null : icon.getPath(),
        meta.getDimension(),
        meta.hashedSeed(),
        meta.isWorldBound()
    );
  }

  /**
   * A received waypoint under a new identifier, stored permanently and visible.
   */
  private static WaypointMeta toMeta(SharedWaypoint value) {
    WaypointService service = Waypoints.references().waypointService();
    WaypointMeta meta = new WaypointMeta(
        service.generateUniqueIdentifier(),
        Component.text(value.title()),
        Color.of(value.color()),
        WaypointType.PERMANENT,
        new DoubleVector3(value.x(), value.y(), value.z()),
        value.contextType(),
        value.context(),
        value.iconPath() == null ? WaypointIcon.DEFAULT : WaypointIcon.getByPath(value.iconPath()),
        value.dimension(),
        true,
        value.hashedSeed()
    );
    meta.setWorldBound(value.isWorldBound());
    return meta;
  }

  private static @Nullable String defaultIconPath() {
    ResourceLocation location = WaypointIcon.DEFAULT.getResourceLocation();
    return location == null ? null : location.getPath();
  }

  private static String defaultName() {
    String name = I18n.getTranslation("labyswaypoints.defaultName");
    return name == null || name.isBlank() ? LABEL : name;
  }

  /**
   * Multiplies the color channels, the way the marker is colored in the world.
   */
  private static int tint(int argb, int color) {
    int alpha = argb >>> 24;
    int red = ((argb >> 16) & 0xFF) * ((color >> 16) & 0xFF) / 255;
    int green = ((argb >> 8) & 0xFF) * ((color >> 8) & 0xFF) / 255;
    int blue = (argb & 0xFF) * (color & 0xFF) / 255;
    return alpha << 24 | red << 16 | green << 8 | blue;
  }
}
