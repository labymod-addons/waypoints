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

package net.labymod.addons.waypoints.event;

import java.util.Objects;
import net.labymod.api.event.DefaultCancellable;
import net.labymod.api.event.Event;
import org.jetbrains.annotations.NotNull;

class CancellableWaypointEvent<T> extends DefaultCancellable implements Event {

  private final T waypoint;

  CancellableWaypointEvent(@NotNull T waypoint) {
    Objects.requireNonNull(waypoint, "Waypoint cannot be null");
    this.waypoint = waypoint;
  }

  public @NotNull T waypoint() {
    return this.waypoint;
  }
}
