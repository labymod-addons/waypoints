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
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.labyconnect.share.ChatSharePreview;
import net.labymod.api.labyconnect.share.ShareCodec;
import net.labymod.api.labyconnect.share.ShareKind;
import net.labymod.api.labyconnect.share.ShareTarget;
import net.labymod.api.labyconnect.share.ShareUndo;
import net.labymod.api.util.Color;
import net.labymod.api.util.I18n;
import net.labymod.api.util.logging.Logging;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single waypoint as something to share in the Laby Chat. The kind has no source, so it is only
 * sent from the waypoint list. A received waypoint is always added as a new one and keeps its
 * server or world, so it only shows up there.
 */
public final class WaypointShareKind {

  public static final String ID = Waypoints.NAMESPACE + ":waypoint";

  static final String LABEL = "Waypoint";

  /**
   * One waypoint as {@link SharedWaypoint#write()} writes it.
   */
  static final ShareCodec<SharedWaypoint> CODEC = new ShareCodec<>() {
    @Override
    public int version() {
      return 1;
    }

    @Override
    public @NotNull JsonObject write(@NotNull SharedWaypoint value) {
      return value.write();
    }

    @Override
    public @Nullable SharedWaypoint read(@NotNull JsonObject data, int version) {
      return SharedWaypoint.read(data);
    }
  };

  private static @Nullable ShareKind<SharedWaypoint> kind;

  private WaypointShareKind() {
  }

  /**
   * Registers the kind with the Laby Chat. Only called through {@link WaypointChatShare}, which
   * makes sure the share API exists.
   */
  static void register() {
    if (kind != null) {
      return;
    }

    ShareKind<SharedWaypoint> created = create();
    Laby.references().chatShareRegistry().register(created);
    kind = created;
  }

  static @NotNull ShareKind<SharedWaypoint> create() {
    return ShareKind.builder(ID, CODEC, ShareTarget.sync(WaypointShareKind::add))
        .label(LABEL)
        .displayName(Component.translatable("labyswaypoints.share.name"))
        .icon(WaypointIcon.DEFAULT)
        .title(SharedWaypoint::title)
        .preview(WaypointShareKind::preview)
        .viewer(WaypointShareKind::view)
        .build();
  }

  static boolean canShare(@NotNull WaypointMeta meta) {
    return kind != null && toShared(meta) != null;
  }

  /**
   * Lets the player pick a friend and sends them the waypoint.
   */
  static void share(@NotNull WaypointMeta meta) {
    ShareKind<SharedWaypoint> registered = kind;
    SharedWaypoint value = toShared(meta);
    if (registered != null && value != null) {
      Laby.references().chatShareRegistry().share(registered, value);
    }
  }

  /**
   * Adds the received waypoint as a new one. It never replaces one the player has, so there is
   * nothing to undo.
   */
  private static @Nullable ShareUndo add(SharedWaypoint value) {
    WaypointService service = Waypoints.references().waypointService();
    service.add(toMeta(value));
    service.refresh();
    return null;
  }

  /**
   * Opens the received waypoint in the editor, unsaved. Saving there adds it as a new one.
   */
  private static void view(SharedWaypoint value) {
    ManageWaypointSimplePopup.received(toMeta(value)).displayInOverlay();
  }

  /**
   * The icon of the waypoint at its native size, the default marker in the waypoint's color like
   * in the world.
   */
  private static @Nullable ChatSharePreview preview(SharedWaypoint value) {
    String path = value.iconPath() == null ? defaultIconPath() : value.iconPath();
    if (path == null) {
      return null;
    }

    BufferedImage image;
    try (InputStream stream = WaypointShareKind.class.getResourceAsStream(
        "/assets/" + Waypoints.NAMESPACE + "/" + path)) {
      // An icon this addon version does not have; the receiver shows the default one
      if (stream == null) {
        return null;
      }

      image = ImageIO.read(stream);
    } catch (IOException exception) {
      Logging.create(WaypointShareKind.class)
          .warn("Could not read the waypoint icon " + path, exception);
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
