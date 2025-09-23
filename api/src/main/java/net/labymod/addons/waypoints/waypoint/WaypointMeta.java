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

import java.util.Objects;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.FloatVector3;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaypointMeta {

  private final String id;
  private final @Nullable WaypointContext contextType; //Nullable for backwards compatibility
  private final @Nullable String context; //Nullable for backwards compatibility
  private final WaypointType type;
  private String dimension;
  private Component title; // todo why is this a component? replace with string
  private Color color;
  private DoubleVector3 location;
  private Icon icon;
  private boolean visible;

  /**
   * Use the {@link WaypointBuilder} to create a new instance of this class instead.
   */
  @Internal
  public WaypointMeta(
      @NotNull String id,
      @NotNull Component title,
      @NotNull Color color,
      @NotNull WaypointType type,
      @NotNull DoubleVector3 location,
      @Nullable WaypointContext contextType,
      @Nullable String context,
      @NotNull Icon icon,
      @Nullable String dimension,
      boolean visible
  ) {
    this.id = id;
    this.title = title;
    this.color = color;
    this.type = type;
    this.location = location;
    this.visible = visible;
    this.contextType = contextType;
    this.context = context;
    this.icon = icon;
    this.dimension = dimension;
  }

  /**
   * Use the {@link WaypointBuilder} to create a new instance of this class instead.
   *
   * @deprecated still uses {@link FloatVector3}
   */
  @Internal
  @Deprecated
  public WaypointMeta(
      @NotNull Component title,
      @NotNull Color color,
      @NotNull WaypointType type,
      @NotNull FloatVector3 location,
      boolean visible,
      @Nullable String world,
      @NotNull String server,
      @NotNull String dimension
  ) {
    this(
        Waypoints.references().waypointService().generateUniqueIdentifier(),
        title,
        color,
        type,
        new DoubleVector3(
            location.getX(),
            location.getY(),
            location.getZ()
        ),
        world == null ? WaypointContext.MULTI_PLAYER : WaypointContext.SINGLE_PLAYER,
        world == null ? server : world,
        WaypointIcon.DEFAULT,
        dimension,
        visible
    );
  }

  public @NotNull String getIdentifier() {
    return this.id;
  }

  public @NotNull Component title() {
    return this.title;
  }

  /**
   * @deprecated Use {@link #title()} instead
   */
  @Deprecated
  public Component getTitle() {
    return this.title;
  }

  public void setTitle(@NotNull Component title) {
    Objects.requireNonNull(title, "Title cannot be null");
    this.title = title;
  }

  public @NotNull Color color() {
    return this.color;
  }

  /**
   * @deprecated Use {@link #color()} instead
   */
  @Deprecated
  public Color getColor() {
    return this.color;
  }

  public void setColor(@NotNull Color color) {
    Objects.requireNonNull(color, "Color cannot be null");
    this.color = color;
  }

  public @NotNull WaypointType type() {
    return this.type;
  }

  /**
   * @deprecated Use {@link #type()} instead
   */
  @Deprecated
  public WaypointType getType() {
    return this.type;
  }

  /**
   * @deprecated not supported anymore
   */
  @Deprecated
  public void setType(@NotNull WaypointType type) {
    // not supported anymore
  }

  public @NotNull Icon icon() {
    if (this.icon == null) {
      this.icon = WaypointIcon.DEFAULT;
    }
    return this.icon;
  }

  public void setIcon(@NotNull Icon icon) {
    this.icon = icon;
  }

  public @NotNull DoubleVector3 location() {
    return this.location;
  }

  /**
   * @deprecated Use {@link #location()} instead
   */
  @Deprecated
  public FloatVector3 getLocation() {
    return new FloatVector3(
        (float) this.location.getX(),
        (float) this.location.getY(),
        (float) this.location.getZ()
    );
  }

  public void setLocation(@NotNull DoubleVector3 location) {
    this.location = location;
  }

  /**
   * @deprecated Use {@link #setLocation(DoubleVector3)} instead
   */
  @Deprecated
  public void setLocation(FloatVector3 location) {
    this.location = new DoubleVector3(
        location.getX(),
        location.getY(),
        location.getZ()
    );
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisible(boolean visible) {
    if (this.visible != visible) {
      this.visible = visible;
      Waypoints.refresh();
    }
  }

  /**
   * @return the context type this waypoint was created in
   */
  public @Nullable WaypointContext contextType() {
    return this.contextType;
  }

  /**
   * @return the context this waypoint was created in
   */
  public @Nullable String getContext() {
    return this.context;
  }

  /**
   * Determines if the provided {@code type} and {@code value} match the current waypoint's context type and context value.
   * If both {@code contextType} and {@code context} in the waypoint are null, this method returns {@code true}.
   *
   * @param type  the context type to match, must not be null
   * @param value the context value to match, must not be null
   * @return {@code true} if the provided type and value match the waypoint's context type and value, or if both context values are null;
   *         otherwise, {@code false}
   * @throws NullPointerException if {@code type} or {@code value} is null
   */
  public boolean matchesContext(@Nullable WaypointContext type, @Nullable String value) {
    /*if (this.contextType == null && this.context == null) {
      return true; //Old waypoint, deprecated
    }*/

    return Objects.equals(value, this.context) && type == this.contextType;
  }

  /**
   * @deprecated Use {@link #contextType()} and {@link #getContext()} instead
   */
  @Deprecated
  public @Nullable String getWorld() {
    return this.contextType == WaypointContext.MULTI_PLAYER ? null : this.context;
  }

  public @Nullable String getDimension() {
    return this.dimension;
  }

  public void setDimension(@NotNull String dimension) {
    this.dimension = dimension;
    Waypoints.refresh();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof WaypointMeta that)) {
      return false;
    }

    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }

  public WaypointMeta copy() {
    return new WaypointMeta(
        this.id,
        this.title.copy(),
        this.color,
        this.type,
        this.location.copy(),
        this.contextType,
        this.context,
        this.icon,
        this.dimension,
        this.visible
    );
  }
}
