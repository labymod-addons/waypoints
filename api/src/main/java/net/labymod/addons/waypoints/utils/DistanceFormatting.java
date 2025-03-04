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

package net.labymod.addons.waypoints.utils;

import net.labymod.api.client.component.Component;

public enum DistanceFormatting {

  BRACKETS("[", "]"),
  CURLY("{", "}"),
  PARENTHESIS("(", ")"),
  ANGLE("<", ">"),
  INVERTED_ANGLE(">", "<"),
  PIPE("|", "|"),
  DASH("-", null),
  WAVY("~", null),
  SINGLE_PIPE("|", null),
  ;

  private static final Component SPACE = Component.text(" ");
  private final Component prefix;
  private final Component suffix;

  DistanceFormatting(String prefix, String suffix) {
    this.prefix = Component.text(prefix);
    this.suffix = suffix == null ? null : Component.text(suffix);
  }

  public static Component space() {
    return SPACE;
  }

  public Component build(Component component, boolean back) {
    if (this.suffix == null) {
      if (!back) {
        return Component.empty()
            .append(component)
            .append(SPACE)
            .append(this.prefix);
      }

      return Component.empty()
          .append(this.prefix)
          .append(SPACE)
          .append(component);
    }

    return Component.empty()
        .append(this.prefix)
        .append(component)
        .append(this.suffix);
  }
}
