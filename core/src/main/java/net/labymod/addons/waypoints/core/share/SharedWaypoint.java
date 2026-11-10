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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A waypoint as it is shared through the Laby Chat. The payload has the keys of a saved
 * {@code WaypointMeta} without its identifier, as the receiver always stores it under a new one.
 * It keeps the server or world it belongs to, so it only shows up there on the receiver's side,
 * too.
 *
 * <p>Only plain Java and Gson, so the rules for received payloads can be tested on their own.
 */
public final class SharedWaypoint {

  /**
   * The longest name, the limit of a share title.
   */
  public static final int MAX_TITLE_LENGTH = 64;

  /**
   * Farther out than the world border can be moved, see the vanilla teleport command.
   */
  static final double MAX_HORIZONTAL = 30_000_000;
  static final double MAX_VERTICAL = 20_000_000;

  static final int MAX_CONTEXT_LENGTH = 255;

  private static final String TITLE_KEY = "title";
  private static final String TEXT_KEY = "text";
  private static final String COLOR_KEY = "color";
  private static final String TYPE_KEY = "type";
  private static final String LOCATION_KEY = "location";
  private static final String CONTEXT_TYPE_KEY = "contextType";
  private static final String CONTEXT_KEY = "context";
  private static final String ICON_KEY = "icon";
  private static final String ICON_PATH_KEY = "path";
  private static final String DIMENSION_KEY = "dimension";
  private static final String HASHED_SEED_KEY = "hashedSeed";
  private static final String WORLD_BOUND_KEY = "worldBound";

  // Component keys whose content is not the plain text of the title
  private static final String[] NON_TEXT_COMPONENT_KEYS = {
      "extra", "translate", "with", "fallback", "keybind", "score", "selector", "nbt", "object"
  };

  private static final Pattern FORMATTING_CODE = Pattern.compile("§.?");
  private static final Pattern ICON_PATH = Pattern.compile("textures/icons/[a-z0-9_]{1,32}\\.png");
  private static final Pattern DIMENSION = Pattern.compile("[a-z0-9_.-]{1,64}:[a-z0-9_./-]{1,128}");
  private static final Pattern SERVER_ADDRESS = Pattern.compile("[\\p{L}\\p{N}._:\\[\\]-]+");

  private final String title;
  private final int color;
  private final WaypointType type;
  private final double x;
  private final double y;
  private final double z;
  private final WaypointContext contextType;
  private final String context;
  private final @Nullable String iconPath;
  private final @Nullable String dimension;
  private final @Nullable Long hashedSeed;
  private final boolean worldBound;

  private SharedWaypoint(
      String title,
      int color,
      WaypointType type,
      double x,
      double y,
      double z,
      WaypointContext contextType,
      String context,
      @Nullable String iconPath,
      @Nullable String dimension,
      @Nullable Long hashedSeed,
      boolean worldBound
  ) {
    this.title = title;
    this.color = color;
    this.type = type;
    this.x = x;
    this.y = y;
    this.z = z;
    this.contextType = contextType;
    this.context = context;
    this.iconPath = iconPath;
    this.dimension = dimension;
    this.hashedSeed = hashedSeed;
    this.worldBound = worldBound;
  }

  /**
   * A waypoint of the player to share. A longer name is cut to {@link #MAX_TITLE_LENGTH}, an empty
   * one becomes {@code fallbackTitle}.
   *
   * @return the waypoint, or {@code null} if it cannot be shared: it belongs to no server or world
   * (made by an old version, shown everywhere) or is outside the world
   */
  public static @Nullable SharedWaypoint of(
      @NotNull String title,
      @NotNull String fallbackTitle,
      int color,
      @NotNull WaypointType type,
      double x,
      double y,
      double z,
      @Nullable WaypointContext contextType,
      @Nullable String context,
      @Nullable String iconPath,
      @Nullable String dimension,
      @Nullable Long hashedSeed,
      boolean worldBound
  ) {
    String plainTitle = plainText(title);
    if (plainTitle.length() > MAX_TITLE_LENGTH) {
      plainTitle = cut(plainTitle);
    }

    if (plainTitle.isEmpty()) {
      plainTitle = fallbackTitle;
    }

    return create(plainTitle, color, type, x, y, z, contextType, context, iconPath, dimension,
        hashedSeed, worldBound && hashedSeed != null);
  }

  /**
   * Reads a received payload. Unknown keys are ignored, anything else that is missing, of the
   * wrong type or out of range rejects the whole waypoint.
   *
   * @return the waypoint, or {@code null} if the payload is not a valid one
   */
  public static @Nullable SharedWaypoint read(@NotNull JsonObject data) {
    String title = title(data.get(TITLE_KEY));
    if (title == null) {
      return null;
    }

    Integer color = integer(data.get(COLOR_KEY));
    WaypointType type = constant(WaypointType.class, data.get(TYPE_KEY));
    WaypointContext contextType = constant(WaypointContext.class, data.get(CONTEXT_TYPE_KEY));
    String context = string(data.get(CONTEXT_KEY));
    if (color == null || type == null || contextType == null || context == null) {
      return null;
    }

    if (!(data.get(LOCATION_KEY) instanceof JsonObject location)) {
      return null;
    }

    Double x = number(location.get("x"));
    Double y = number(location.get("y"));
    Double z = number(location.get("z"));
    if (x == null || y == null || z == null) {
      return null;
    }

    String iconPath = null;
    JsonElement icon = data.get(ICON_KEY);
    if (icon != null && !icon.isJsonNull()) {
      if (!(icon instanceof JsonObject iconObject)) {
        return null;
      }

      iconPath = string(iconObject.get(ICON_PATH_KEY));
      if (iconPath == null) {
        return null;
      }
    }

    String dimension = null;
    JsonElement dimensionElement = data.get(DIMENSION_KEY);
    if (dimensionElement != null && !dimensionElement.isJsonNull()) {
      dimension = string(dimensionElement);
      if (dimension == null) {
        return null;
      }
    }

    Long hashedSeed = null;
    JsonElement seedElement = data.get(HASHED_SEED_KEY);
    if (seedElement != null && !seedElement.isJsonNull()) {
      hashedSeed = longValue(seedElement);
      if (hashedSeed == null) {
        return null;
      }
    }

    boolean worldBound = false;
    JsonElement boundElement = data.get(WORLD_BOUND_KEY);
    if (boundElement != null && !boundElement.isJsonNull()) {
      if (!(boundElement instanceof JsonPrimitive primitive) || !primitive.isBoolean()) {
        return null;
      }

      worldBound = primitive.getAsBoolean();
    }

    // Bound to a world it does not name: nothing a client writes
    if (worldBound && hashedSeed == null) {
      return null;
    }

    return create(title, color, type, x, y, z, contextType, context, iconPath, dimension,
        hashedSeed, worldBound);
  }

  private static @Nullable SharedWaypoint create(
      String title,
      int color,
      WaypointType type,
      double x,
      double y,
      double z,
      @Nullable WaypointContext contextType,
      @Nullable String context,
      @Nullable String iconPath,
      @Nullable String dimension,
      @Nullable Long hashedSeed,
      boolean worldBound
  ) {
    if (title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
      return null;
    }

    if (!inRange(x, MAX_HORIZONTAL) || !inRange(y, MAX_VERTICAL) || !inRange(z, MAX_HORIZONTAL)) {
      return null;
    }

    if (contextType == null || !isValidContext(contextType, context)) {
      return null;
    }

    if (iconPath != null && !ICON_PATH.matcher(iconPath).matches()) {
      return null;
    }

    if (dimension != null && !DIMENSION.matcher(dimension).matches()) {
      return null;
    }

    // Always opaque, a see-through waypoint could not be found again
    return new SharedWaypoint(title, color | 0xFF000000, type, x, y, z, contextType, context,
        iconPath, dimension, hashedSeed, worldBound);
  }

  /**
   * The server address (as the waypoints store it) or the folder name of a single player world.
   */
  static boolean isValidContext(@NotNull WaypointContext type, @Nullable String context) {
    if (context == null || context.isEmpty() || context.length() > MAX_CONTEXT_LENGTH) {
      return false;
    }

    return switch (type) {
      case MULTI_PLAYER -> SERVER_ADDRESS.matcher(context).matches();
      case SINGLE_PLAYER -> context.equals(context.trim())
          && context.chars().noneMatch(c -> Character.isISOControl(c) || c == '/' || c == '\\');
    };
  }

  /**
   * The name as plain text: no formatting codes, no control characters, trimmed.
   */
  static @NotNull String plainText(@NotNull String text) {
    String stripped = FORMATTING_CODE.matcher(text).replaceAll("");
    StringBuilder builder = new StringBuilder(stripped.length());
    stripped.codePoints()
        .filter(codePoint -> !Character.isISOControl(codePoint))
        .forEach(builder::appendCodePoint);
    return builder.toString().trim();
  }

  private static String cut(String text) {
    int end = MAX_TITLE_LENGTH;
    // Do not split a surrogate pair
    if (Character.isHighSurrogate(text.charAt(end - 1))) {
      end--;
    }

    return text.substring(0, end).trim();
  }

  /**
   * The title is a text component, or the bare string of one. Style keys are dropped, any content
   * other than the text rejects it.
   */
  private static @Nullable String title(@Nullable JsonElement element) {
    String text;
    if (element instanceof JsonObject object) {
      for (String key : NON_TEXT_COMPONENT_KEYS) {
        if (object.has(key)) {
          return null;
        }
      }

      text = string(object.get(TEXT_KEY));
    } else {
      text = string(element);
    }

    if (text == null || text.length() > MAX_TITLE_LENGTH * 4) {
      return null;
    }

    String plain = plainText(text);
    return plain.isEmpty() || plain.length() > MAX_TITLE_LENGTH ? null : plain;
  }

  private static boolean inRange(double value, double max) {
    return Double.isFinite(value) && Math.abs(value) <= max;
  }

  private static <E extends Enum<E>> @Nullable E constant(Class<E> type, JsonElement element) {
    String name = string(element);
    if (name == null) {
      return null;
    }

    for (E constant : type.getEnumConstants()) {
      if (constant.name().equals(name)) {
        return constant;
      }
    }

    return null;
  }

  private static @Nullable String string(@Nullable JsonElement element) {
    return element instanceof JsonPrimitive primitive && primitive.isString()
        ? primitive.getAsString()
        : null;
  }

  private static @Nullable Double number(@Nullable JsonElement element) {
    if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
      return null;
    }

    double value = primitive.getAsDouble();
    return Double.isFinite(value) ? value : null;
  }

  private static @Nullable Long longValue(@Nullable JsonElement element) {
    if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
      return null;
    }

    String raw = primitive.getAsString();
    // A number that long is no seed; keeps BigDecimal from parsing huge exponents
    if (raw.length() > 32) {
      return null;
    }

    try {
      return new BigDecimal(raw).longValueExact();
    } catch (ArithmeticException | NumberFormatException exception) {
      return null;
    }
  }

  /**
   * A color as the config stores it, an ARGB int. Also takes the unsigned form, like
   * {@code 4294967295} for white.
   */
  private static @Nullable Integer integer(@Nullable JsonElement element) {
    Long value = longValue(element);
    if (value == null || value < Integer.MIN_VALUE || value > 0xFFFFFFFFL) {
      return null;
    }

    return (int) (long) value;
  }

  /**
   * The payload of the share.
   */
  public @NotNull JsonObject write() {
    JsonObject data = new JsonObject();
    JsonObject title = new JsonObject();
    title.addProperty(TEXT_KEY, this.title);
    data.add(TITLE_KEY, title);
    data.addProperty(COLOR_KEY, this.color);
    data.addProperty(TYPE_KEY, this.type.name());

    JsonObject location = new JsonObject();
    location.addProperty("x", this.x);
    location.addProperty("y", this.y);
    location.addProperty("z", this.z);
    data.add(LOCATION_KEY, location);

    data.addProperty(CONTEXT_TYPE_KEY, this.contextType.name());
    data.addProperty(CONTEXT_KEY, this.context);
    if (this.iconPath != null) {
      JsonObject icon = new JsonObject();
      icon.addProperty(ICON_PATH_KEY, this.iconPath);
      data.add(ICON_KEY, icon);
    }

    if (this.dimension != null) {
      data.addProperty(DIMENSION_KEY, this.dimension);
    }

    if (this.hashedSeed != null) {
      data.addProperty(HASHED_SEED_KEY, this.hashedSeed);
    }

    data.addProperty(WORLD_BOUND_KEY, this.worldBound);
    return data;
  }

  /**
   * The name, plain text of 1 to {@link #MAX_TITLE_LENGTH} characters.
   */
  public @NotNull String title() {
    return this.title;
  }

  /**
   * The color, ARGB and always opaque.
   */
  public int color() {
    return this.color;
  }

  /**
   * The type it had on the sender's side. A received waypoint is always stored permanently.
   */
  public @NotNull WaypointType type() {
    return this.type;
  }

  public double x() {
    return this.x;
  }

  public double y() {
    return this.y;
  }

  public double z() {
    return this.z;
  }

  public @NotNull WaypointContext contextType() {
    return this.contextType;
  }

  public @NotNull String context() {
    return this.context;
  }

  /**
   * The path of the icon texture, or {@code null} for the default marker.
   */
  public @Nullable String iconPath() {
    return this.iconPath;
  }

  /**
   * The dimension, or {@code null} if the waypoint is shown in every dimension.
   */
  public @Nullable String dimension() {
    return this.dimension;
  }

  public @Nullable Long hashedSeed() {
    return this.hashedSeed;
  }

  public boolean isWorldBound() {
    return this.worldBound;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof SharedWaypoint that)) {
      return false;
    }

    return this.color == that.color
        && Double.compare(this.x, that.x) == 0
        && Double.compare(this.y, that.y) == 0
        && Double.compare(this.z, that.z) == 0
        && this.worldBound == that.worldBound
        && this.title.equals(that.title)
        && this.type == that.type
        && this.contextType == that.contextType
        && this.context.equals(that.context)
        && Objects.equals(this.iconPath, that.iconPath)
        && Objects.equals(this.dimension, that.dimension)
        && Objects.equals(this.hashedSeed, that.hashedSeed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.title, this.color, this.type, this.x, this.y, this.z,
        this.contextType, this.context, this.iconPath, this.dimension, this.hashedSeed,
        this.worldBound);
  }
}
