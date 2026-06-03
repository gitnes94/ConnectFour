package com.example.botservice.consumer;

import com.example.botservice.bot.BotStrategy;
import com.example.botservice.bot.GameClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Consumes move-played events. This is the asynchronous half of the system:
 * Game Service publishes, the bot reacts here.
 * Idempotent consumer: RabbitMQ guarantees at-least-once delivery, so the same
 * event may arrive twice. We remember processed eventIds and skip duplicates,
 * exactly the "Inbox / processed-message store" idea from the async lecture.
 * (In-memory set for the lab; a real system would use a DB table.)
 */
@Component
public class MovePlayedConsumer {

    private final BotStrategy strategy;
    private final GameClient gameClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // processed-message store for deduplication
    private final Set<String> processedEventIds = Collections.synchronizedSet(new HashSet<>());

    public MovePlayedConsumer(BotStrategy strategy, GameClient gameClient) {
        this.strategy = strategy;
        this.gameClient = gameClient;
    }

    @RabbitListener(queues = "move-played")
    public void onMovePlayed(Message message) {
        String eventId = message.getMessageProperties().getMessageId();

        // dedupe: skip if we have already handled this event
        if (eventId != null && !processedEventIds.add(eventId)) {
            return;
        }

        try {
            String json = new String(message.getBody());
            JsonNode event = objectMapper.readTree(json);

            String status = event.path("status").asText("IN_PROGRESS");
            int nextPlayer = event.path("nextPlayer").asInt(0);

            // Only act when the game is ongoing and it is the bot's turn (player 2).
            if (!"IN_PROGRESS".equals(status) || nextPlayer != 2) {
                return;
            }

            String gameId = event.path("gameId").asText();
            String boardCsv = event.path("board").asText();

            int[][] board = BotStrategy.parse(boardCsv);
            int column = strategy.chooseColumn(board);
            if (column >= 0) {
                gameClient.postBotMove(gameId, column);
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to parse move-played event payload", ex);
        } catch (Exception ex) {
            // Let it bubble so RabbitMQ can retry / dead-letter a poison message.
            throw new RuntimeException("Failed to process move-played event", ex);
        }
    }
}