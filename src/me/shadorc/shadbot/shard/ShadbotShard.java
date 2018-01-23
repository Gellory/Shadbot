package me.shadorc.shadbot.shard;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.LogUtils;
import sx.blah.discord.api.IShard;
import sx.blah.discord.util.MessageBuilder;

public class ShadbotShard {

	private static final int MAX_QUEUE_SIZE = 20;

	private final IShard shard;
	private final int shardID;
	private final List<MessageBuilder> messagesQueue;
	private final AtomicLong lastEvent;
	private final AtomicLong lastMessage;

	private ThreadPoolExecutor threadPool;

	public ShadbotShard(IShard shard) {
		this.shard = shard;
		this.shardID = shard.getInfo()[0];
		this.messagesQueue = new LinkedList<>();
		this.threadPool = ShardManager.createThreadPool(this);
		this.lastEvent = new AtomicLong();
		this.lastMessage = new AtomicLong();
	}

	public IShard getShard() {
		return shard;
	}

	public int getID() {
		return shardID;
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	public long getLastEventTime() {
		return lastEvent.get();
	}

	public long getLastMessageTime() {
		return lastMessage.get();
	}

	public void queue(MessageBuilder message) {
		messagesQueue.add(0, message);
		if(messagesQueue.size() > MAX_QUEUE_SIZE) {
			messagesQueue.remove(messagesQueue.size() - 1);
			LogUtils.infof("{Shard %d} The limit size of the queue has been exceeded, last message removed.", this.getID());
		}
	}

	public void sendQueue() {
		messagesQueue.stream().forEach(BotUtils::sendMessage);
		messagesQueue.clear();
	}

	public void eventReceived() {
		lastEvent.set(System.currentTimeMillis());
	}

	public void messageReceived() {
		lastMessage.set(System.currentTimeMillis());
	}

	public void restart() {
		LogUtils.infof("{Shard %d} Logging out.", this.getID());
		if(shard.isLoggedIn()) {
			shard.logout();
		}

		threadPool.shutdown();
		try {
			if(!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
				LogUtils.infof("{Shard %d} Thread pool was abruptly shut down. %d tasks will not be executed.",
						this.getID(), threadPool.shutdownNow().size());
			}
		} catch (InterruptedException e) {
			LogUtils.infof("{Shard %d} Thread was interrupted, thread pool was abruptly shut down. %d tasks will not be executed.",
					this.getID(), threadPool.shutdownNow().size());
		}

		LogUtils.infof("{Shard %d} Logging in.", this.getID());
		shard.login();
		LogUtils.infof("{Shard %d} Shard restarted.", this.getID());
		threadPool = ShardManager.createThreadPool(this);
	}
}
