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
package net.labymod.addons.waypoints.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.externaldevice.ExternalDeviceCommandException;
import net.labymod.api.externaldevice.ExternalDeviceControl;
import net.labymod.api.externaldevice.ExternalDeviceRegistration;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.Nullable;

/**
 * What a paired controller (a Stream Deck, the Laby app) can ask this addon to do, on top of the
 * client's own operations. The operation is registered on the {@link ExternalDeviceControl} side
 * of LabyMod's external device service and invoked from a network thread.
 *
 * <ul>
 *   <li>{@value #OP_ADD} (+ {@code name}, {@code color} as an RGB number, {@code type}:
 *       {@code permanent} or {@code session}): puts a waypoint where the player stands, exactly
 *       where the create key would put it, and answers with its name and coordinates.</li>
 * </ul>
 *
 * <p>The registration is kept so the operation can be dropped again while the addon is switched
 * off in the mods menu: a controller pulls it, so unlike an event listener it would keep being
 * answered.
 */
public final class ExternalDeviceCommands {

  public static final String OP_ADD = "waypoints.add";

  /** The waypoint is created on the render thread; a controller waits for the answer. */
  private static final long TIMEOUT_SECONDS = 3L;

  private static ExternalDeviceRegistration registration;

  private ExternalDeviceCommands() {
  }

  public static void register() {
    unregister();
    registration = Laby.references().externalDeviceService().control()
        .registerCommand(OP_ADD, ExternalDeviceCommands::add);
  }

  public static void unregister() {
    if (registration != null) {
      registration.close();
      registration = null;
    }
  }

  private static JsonObject add(JsonObject request) throws Exception {
    ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
    if (player == null) {
      throw new ExternalDeviceCommandException("no_world");
    }

    Position position = player.position();
    int blockX = (int) Math.floor(position.getX());
    int blockY = (int) Math.floor(position.getY());
    int blockZ = (int) Math.floor(position.getZ());
    // The same spot the create key picks: the middle of the block, at eye height.
    DoubleVector3 location = new DoubleVector3(
        blockX + 0.5,
        (int) (position.getY() + player.getEyeHeight()),
        blockZ + 0.5
    );

    String name = string(request, "name");
    if (name == null || name.isBlank()) {
      // A nameless waypoint in the list tells nobody anything; its coordinates do.
      name = String.format(Locale.ROOT, "%d, %d, %d", blockX, blockY, blockZ);
    }

    String title = name;
    Integer color = integer(request, "color");
    boolean session = "session".equals(string(request, "type"));

    CompletableFuture<Void> created = new CompletableFuture<>();
    Laby.labyAPI().minecraft().executeOnRenderThread(() -> {
      try {
        WaypointBuilder builder = WaypointBuilder.create()
            .identifierPrefix(title)
            .title(Component.text(title))
            .type(session ? WaypointType.SERVER_SESSION : WaypointType.PERMANENT)
            .location(location)
            .visible(true)
            .applyCurrentContext()
            .currentDimension();
        if (color != null) {
          builder.color(Color.of(color, 255));
        }

        WaypointService waypointService = Waypoints.references().waypointService();
        if (waypointService.add(builder.build()) == null) {
          // Someone cancelled the add event, e.g. a server that manages the waypoints itself.
          throw new ExternalDeviceCommandException("rejected");
        }

        // add() only stores and saves; the overlays rebuild their lists on the refresh event.
        waypointService.refresh();
        created.complete(null);
      } catch (Throwable throwable) {
        created.completeExceptionally(throwable);
      }
    });

    try {
      created.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (ExecutionException exception) {
      Throwable cause = exception.getCause();
      throw cause instanceof Exception ? (Exception) cause : exception;
    }

    JsonObject answer = new JsonObject();
    answer.addProperty("name", title);
    answer.addProperty("x", blockX);
    answer.addProperty("y", blockY);
    answer.addProperty("z", blockZ);
    return answer;
  }

  private static @Nullable String string(JsonObject request, String key) {
    JsonElement element = request.get(key);
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }

  private static @Nullable Integer integer(JsonObject request, String key) {
    JsonElement element = request.get(key);
    return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
        ? element.getAsInt() : null;
  }
}
