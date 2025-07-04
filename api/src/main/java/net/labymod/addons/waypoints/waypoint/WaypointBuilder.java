package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.component.Component;
import net.labymod.api.reference.annotation.Referenceable;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;

@Referenceable
public interface WaypointBuilder {

  static WaypointBuilder newBuilder() {
    return Waypoints.getReferences().waypointBuilder();
  }

  WaypointBuilder title(Component title);

  WaypointBuilder color(Color color);

  WaypointBuilder type(WaypointType type);

  WaypointBuilder location(DoubleVector3 location);

  WaypointBuilder visible(boolean visible);

  WaypointBuilder world(String world);

  WaypointBuilder server(String server);

  WaypointBuilder dimension(String dimension);

  WaypointMeta build();


}
