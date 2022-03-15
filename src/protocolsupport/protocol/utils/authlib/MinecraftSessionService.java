package protocolsupport.protocol.utils.authlib;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.utils.JsonUtils;

public class MinecraftSessionService {

	private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

	public static GameProfile hasJoinedServer(String name, String hash) throws AuthenticationUnavailableException {
		try {
			final URL url = new URL(BASE_URL + "?username=" + name + "&serverId=" + hash);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(new InputStreamReader(url.openStream()));
			if(!element.isJsonObject()) return null;
			JsonObject root = element.getAsJsonObject();
			String rname = JsonUtils.getString(root, "name");
			UUID ruuid = UUIDTypeAdapter.fromString(JsonUtils.getString(root, "id"));
			GameProfile profile = new GameProfile(ruuid, rname);
			JsonArray properties = JsonUtils.getJsonArray(root, "properties");
			for (JsonElement property : properties) {
				JsonObject propertyobj = property.getAsJsonObject();
				profile.addProperty(new ProfileProperty(
					JsonUtils.getString(propertyobj, "name"),
					JsonUtils.getString(propertyobj, "value"),
					JsonUtils.getString(propertyobj, "signature")
				));
			}
			return profile;
		} catch(MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException | IllegalStateException | JsonParseException e) {
			throw new AuthenticationUnavailableException("Failed to make request to " + BASE_URL, e);
		}
	}

	public static class AuthenticationUnavailableException extends Exception {
		private static final long serialVersionUID = 1L;

		public AuthenticationUnavailableException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
