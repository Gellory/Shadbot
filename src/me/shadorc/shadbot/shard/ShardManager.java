package me.shadorc.shadbot.shard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;

import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.TimeUtils;
import me.shadorc.shadbot.utils.executor.ShadbotCachedExecutor;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IGuild;

public class ShardManager {

	private static final int SHARD_TIMEOUT = 30;

	private static final Map<IShard, ShadbotShard> SHARDS_MAP = new HashMap<>();
	private static final ThreadPoolExecutor DEFAUT_THREAD_POOL = new ShadbotCachedExecutor("Shadbot-DefaultThreadPool-%d");

	public static void start() {
		Shadbot.getScheduler().scheduleAtFixedRate(() -> ShardManager.check(), 10, 10, TimeUnit.MINUTES);
	}

	public static void stop() {
		SHARDS_MAP.values().stream().forEach(shadbotShard -> shadbotShard.getThreadPool().shutdownNow());
		DEFAUT_THREAD_POOL.shutdownNow();
	}

	public static ThreadPoolExecutor createThreadPool(ShadbotShard shard) {
		return new ShadbotCachedExecutor("ShadbotShard-" + shard.getID() + "-%d");
	}

	public static ShadbotShard getShadbotShard(IShard shard) {
		return SHARDS_MAP.get(shard);
	}

	public static ThreadPoolExecutor getThreadPool(IGuild guild) {
		// Private message
		if(guild == null) {
			return DEFAUT_THREAD_POOL;
		}

		SHARDS_MAP.get(guild.getShard()).eventReceived();
		return SHARDS_MAP.get(guild.getShard()).getThreadPool();
	}

	public static void addShardIfAbsent(IShard shard) {
		SHARDS_MAP.putIfAbsent(shard, new ShadbotShard(shard));
	}

	private static void check() {
		LogUtils.infof("Checking dead shards...");
		for(ShadbotShard shardStatus : SHARDS_MAP.values()) {
			try {
				// Ignore shards with less than 100 guilds
				if(shardStatus.getShard().getGuilds().size() < 100) {
					continue;
				}
				long lastEventTime = TimeUtils.getMillisUntil(shardStatus.getLastEventTime());
				long lastMessageTime = TimeUtils.getMillisUntil(shardStatus.getLastMessageTime());
				if(lastEventTime > TimeUnit.SECONDS.toMillis(SHARD_TIMEOUT) || lastMessageTime > TimeUnit.SECONDS.toMillis(SHARD_TIMEOUT)) {
					LogUtils.infof(String.format("Restarting shard %d (Response time: %d ms | Last event: %s ago | Last message: %s ago)",
							shardStatus.getID(),
							shardStatus.getShard().getResponseTime(),
							DurationFormatUtils.formatDurationWords(lastEventTime, true, true),
							DurationFormatUtils.formatDurationWords(lastMessageTime, true, true)));
					shardStatus.restart();
				}
			} catch (Exception err) {
				LogUtils.errorf(err, "An error occurred while restarting a shard.");
			}
		}
		LogUtils.infof("Dead shards checked.");
	}
}