package me.shadorc.discordbot.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import me.shadorc.discordbot.exceptions.RelicActivationException;
import me.shadorc.discordbot.utils.LogUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class PremiumManager {

	private static final File PREMIUM_DATA_FILE = new File("premium_data.json");

	public enum RelicType {
		USER, GUILD;
	}

	@SuppressWarnings("ucd")
	private static JSONObject dataObj;

	static {
		if(!PREMIUM_DATA_FILE.exists()) {
			try (FileWriter writer = new FileWriter(PREMIUM_DATA_FILE)) {
				writer.write(new JSONObject().toString(Config.INDENT_FACTOR));
				writer.flush();

			} catch (IOException err) {
				LogUtils.LOGGER.error("An error occurred during premium data file creation. Exiting.", err);
				System.exit(1);
			}
		}

		try (InputStream stream = PREMIUM_DATA_FILE.toURI().toURL().openStream()) {
			dataObj = new JSONObject(new JSONTokener(stream));
		} catch (JSONException | IOException err) {
			LogUtils.LOGGER.error("An error occurred during premium data file initialisation. Exiting.", err);
			System.exit(1);
		}
	}

	public static void generateRelic(String userID, RelicType type) {
		JSONArray userArray = dataObj.optJSONArray(userID);
		if(userArray == null) {
			userArray = new JSONArray();
		}

		JSONObject relicObj = new JSONObject();
		relicObj.put(JSONKey.RELIC_ID.toString(), UUID.randomUUID().toString());
		relicObj.put(JSONKey.RELIC_DURATION.toString(), 180);
		relicObj.put(JSONKey.RELIC_TYPE.toString(), type.toString());
		userArray.put(relicObj);

		dataObj.put(userID, userArray);
	}

	public static void activateRelic(IGuild guild, IUser user, String relicID) throws RelicActivationException {
		JSONArray userArray = dataObj.optJSONArray(user.getStringID());
		if(userArray == null) {
			throw new RelicActivationException("You don't have any Relic.");
		}

		JSONObject relicObj = null;
		for(int i = 0; i < userArray.length(); i++) {
			JSONObject obj = userArray.getJSONObject(i);
			if(obj.getString(JSONKey.RELIC_ID.toString()).equals(relicID)) {
				relicObj = obj;
			}
		}

		if(relicObj == null) {
			throw new RelicActivationException("Invalid key.");
		}

		if(relicObj.optLong(JSONKey.RELIC_ACTIVATION_MILLIS.toString()) != 0) {
			throw new RelicActivationException("This key is already activated.");
		}

		if(relicObj.getString(JSONKey.RELIC_TYPE.toString()).equals(RelicType.GUILD.toString()) && guild == null) {
			throw new RelicActivationException("You must activate the legendary relic in the desired server.");
		}

		relicObj.put(JSONKey.RELIC_ACTIVATION_MILLIS.toString(), System.currentTimeMillis());
		relicObj.put(JSONKey.GUILD_ID.toString(), guild.getStringID());
	}

	public static void save() {
		LogUtils.info("Saving premium data...");
		try (FileWriter writer = new FileWriter(PREMIUM_DATA_FILE)) {
			writer.write(dataObj.toString(Config.INDENT_FACTOR));
			writer.flush();

		} catch (IOException err) {
			LogUtils.error("Error while saving premium data.", err);
		}
		LogUtils.info("Premium data saved.");
	}

}